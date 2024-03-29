package aws.lambda.handlers.users;

import aws.model.RDSManagedCredentials;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import helper.CORSConfiguration;
import helper.MySQLConnectionManager;
import resources.model.UserClaimedIdentity;
import resources.users.DeleteUser;
import resources.users.model.DeleteUserRequest;
import resources.users.model.DeleteUserResponse;

import java.util.HashMap;
import java.util.Map;

import static aws.lambda.HTTPStatusCodes.*;

public class UserProxyDELETE implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final SecretCache dbCredentialsSecretCache = new SecretCache();
    private static final MySQLConnectionManager jdbcManager = MySQLConnectionManager.getInstance();
    private static final Gson gson = new Gson();

    // On cold boot set up and create a db connection
    static {
        RDSManagedCredentials dbCredentials = gson.fromJson(
                dbCredentialsSecretCache.getSecretString(System.getenv("DB_CREDENTIALS_SECRET_ID")),
                RDSManagedCredentials.class);

        jdbcManager.setUpAndConnect(
                System.getenv("READ_ENDPOINT"),
                System.getenv("WRITE_ENDPOINT"),
                Integer.parseInt(System.getenv("PORT")),
                dbCredentials.getUsername(),
                dbCredentials.getPassword(),
                System.getenv("DB_SCHEMA"));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        APIGatewayProxyResponseEvent response = handleRequestDelegate(event, context);

        HashMap<String, String> headers = new HashMap<>();

        if (response.getHeaders() != null) headers.putAll(response.getHeaders());
        headers.putAll(CORSConfiguration.getCORSHeadersMap(System.getenv("CORS_ALLOWED_ORIGINS")));

        return response.withHeaders(headers);
    }

    private APIGatewayProxyResponseEvent handleRequestDelegate(APIGatewayProxyRequestEvent event, Context context) {
        String requestedUsername = event.getPathParameters().getOrDefault("proxy", "").trim();

        if (requestedUsername.isEmpty())
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(gson.toJson("Nombre de usuario inválido"));

        String claimedCognitoUsername = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .getOrDefault("cognito:username", "");

        if (claimedCognitoUsername.isEmpty())
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(UNAUTHORIZED)
                    .withBody("No se ha podido obtener la identidad del usuario");

        DeleteUserResponse response = DeleteUser.run(
                new DeleteUserRequest(
                        requestedUsername,
                        new UserClaimedIdentity(claimedCognitoUsername)));

        if (response.flagged(DeleteUserResponse.Flags.ERROR_USER_NOT_FOUND))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_FOUND)
                    .withBody(response.getFlagMessage(DeleteUserResponse.Flags.ERROR_USER_NOT_FOUND));

        if (response.flagged(DeleteUserResponse.Flags.ERROR_UNAUTHORIZED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(UNAUTHORIZED)
                    .withBody(response.getFlagMessage(DeleteUserResponse.Flags.ERROR_UNAUTHORIZED));

        if (response.flagged(DeleteUserResponse.Flags.ERROR_USER_DELETION_UNSUCCESSFUL))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(INTERNAL_SERVER_ERROR)
                    .withBody(response.getFlagMessage(DeleteUserResponse.Flags.ERROR_USER_DELETION_UNSUCCESSFUL));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(NO_CONTENT);
    }
}

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
import model.User;
import resources.users.RegisterUser;
import resources.users.model.RegisterUserRequest;
import resources.users.model.RegisterUserResponse;

import java.util.HashMap;

import static aws.lambda.HTTPStatusCodes.*;

public class UsersPOST implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        User newUser;

        if (!event.getBody().isEmpty()) newUser = gson.fromJson(event.getBody(), User.class);
        else return new APIGatewayProxyResponseEvent().withStatusCode(BAD_REQUEST);

        RegisterUserResponse response = RegisterUser.run(new RegisterUserRequest(newUser));

        if (response.flagged(RegisterUserResponse.Flags.ERROR_INVALID_USER))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(RegisterUserResponse.Flags.ERROR_INVALID_USER));

        if (response.flagged(RegisterUserResponse.Flags.ERROR_USER_ALREADY_EXISTS))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(FORBIDDEN)
                    .withBody(response.getFlagMessage(RegisterUserResponse.Flags.ERROR_USER_ALREADY_EXISTS));

        if (response.flagged(RegisterUserResponse.Flags.ERROR_UNABLE_TO_REGISTER))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(INTERNAL_SERVER_ERROR)
                    .withBody(response.getFlagMessage(RegisterUserResponse.Flags.ERROR_UNABLE_TO_REGISTER));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(CREATED);
    }
}

package aws.lambda.handlers.routes;

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
import resources.routes.DeleteRoute;
import resources.routes.model.DeleteRouteRequest;
import resources.routes.model.DeleteRouteResponse;

import java.util.HashMap;
import java.util.Map;

import static aws.lambda.HTTPStatusCodes.*;

public class RouteProxyDELETE implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        long routeId = Long.parseLong(event.getPathParameters().get("proxy"));
        String cognitoUsername = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        DeleteRouteResponse response = DeleteRoute.run(new DeleteRouteRequest(routeId, new UserClaimedIdentity(cognitoUsername)));

        if (response.flagged(DeleteRouteResponse.Flags.ERROR_INVALID_ROUTE_ID))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(DeleteRouteResponse.Flags.ERROR_INVALID_ROUTE_ID));

        if (response.flagged(DeleteRouteResponse.Flags.ERROR_ROUTE_NOT_FOUND))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_FOUND)
                    .withBody(response.getFlagMessage(DeleteRouteResponse.Flags.ERROR_ROUTE_NOT_FOUND));


        if (response.flagged(DeleteRouteResponse.Flags.ERROR_UNAUTHORIZED))
            return new APIGatewayProxyResponseEvent().withStatusCode(UNAUTHORIZED);

        if (response.flagged(DeleteRouteResponse.Flags.ERROR_UNEXPECTED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(INTERNAL_SERVER_ERROR)
                    .withBody(response.getFlagMessage(DeleteRouteResponse.Flags.ERROR_UNEXPECTED));

        return new APIGatewayProxyResponseEvent().withStatusCode(NO_CONTENT);
    }
}

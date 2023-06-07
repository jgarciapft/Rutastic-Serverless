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
import model.Route;
import resources.model.UserClaimedIdentity;
import resources.routes.CreateRoute;
import resources.routes.model.CreateRouteRequest;
import resources.routes.model.CreateRouteResponse;

import java.util.HashMap;
import java.util.Map;

import static aws.lambda.HTTPStatusCodes.*;

public class RoutesPOST implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String THIS_RESOURCE = "/rutas";
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
        Route newRoute = gson.fromJson(event.getBody(), Route.class);
        String cognitoUsername = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        CreateRouteResponse response =
                CreateRoute.run(new CreateRouteRequest(newRoute, new UserClaimedIdentity(cognitoUsername)));

        if (response.flagged(CreateRouteResponse.Flags.ERROR_INVALID_ROUTE))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(CreateRouteResponse.Flags.ERROR_INVALID_ROUTE));

        if (response.flagged(CreateRouteResponse.Flags.ERROR_SAVING_ROUTE))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(INTERNAL_SERVER_ERROR)
                    .withBody(response.getFlagMessage(CreateRouteResponse.Flags.ERROR_SAVING_ROUTE));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(CREATED)
                .withHeaders(Map.of("Location", String.format("%s/%d", THIS_RESOURCE, response.getNewRouteId())));
    }
}

package aws.lambda.handlers.routeCategories;

import aws.model.RDSManagedCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.google.gson.Gson;
import helper.CORSConfiguration;
import helper.MySQLConnectionManager;
import resources.routeCategories.GetAllRouteCategories;
import resources.routeCategories.model.GetAllRouteCategoriesResponse;

import static aws.lambda.HTTPStatusCodes.NO_CONTENT;
import static aws.lambda.HTTPStatusCodes.OK;

public class RouteCategoriesGET implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        return handleRequestDelegate(event, context)
                .withHeaders(CORSConfiguration.getCORSHeadersMap(System.getenv("CORS_ALLOWED_ORIGINS")));
    }

    private APIGatewayProxyResponseEvent handleRequestDelegate(APIGatewayProxyRequestEvent event, Context context) {
        GetAllRouteCategoriesResponse response = GetAllRouteCategories.run();

        if (response.getRouteCategoriesList() == null || response.getRouteCategoriesList().isEmpty())
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NO_CONTENT);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getRouteCategoriesList()));
    }
}

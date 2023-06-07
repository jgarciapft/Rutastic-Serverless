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
import resources.routes.GetRankedRoutes;
import resources.routes.model.GetRankedRoutesRequest;
import resources.routes.model.GetRankedRoutesResponse;

import java.util.HashMap;

import static aws.lambda.HTTPStatusCodes.BAD_REQUEST;
import static aws.lambda.HTTPStatusCodes.OK;

public class RouteStatisticsGET implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        String requestedRankCriterion = event.getQueryStringParameters().get("e");

        // Validate there's a route rank criterion being requested

        if (requestedRankCriterion == null || requestedRankCriterion.trim().isEmpty())
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody("No se ha solicitado ninguna estadística de ruta");

        GetRankedRoutesResponse response = GetRankedRoutes.run(new GetRankedRoutesRequest(requestedRankCriterion));

        if (response.flagged(GetRankedRoutesResponse.Flags.ERROR_UNKNOWN_RANK_CRITERION))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(GetRankedRoutesResponse.Flags.ERROR_UNKNOWN_RANK_CRITERION));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getRankedRoutesList()));
    }
}

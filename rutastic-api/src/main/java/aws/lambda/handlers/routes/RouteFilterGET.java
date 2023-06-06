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
import helper.TypeUtils;
import resources.routes.ExecuteRouteFilter;
import resources.routes.model.ExecuteRouteFilterRequest;
import resources.routes.model.ExecuteRouteFilterResponse;
import resources.model.RequestParameter;

import java.util.HashMap;
import java.util.Map;

import static aws.lambda.HTTPStatusCodes.BAD_REQUEST;
import static aws.lambda.HTTPStatusCodes.OK;

public class RouteFilterGET implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        Map<String, String> queryStringParameters =
                event.getQueryStringParameters() != null ? event.getQueryStringParameters() : new HashMap<>();

        String searchText = queryStringParameters.get("buscarTexto");
        String routeKudosOrdering = queryStringParameters.getOrDefault("ordenarPorKudos", "no-ordenar");
        String minimumKudosSource = queryStringParameters.get("kudosMinimos");
        String hideBlockedRoutesSource = queryStringParameters.getOrDefault("ocultarRutasBloq", "false");
        String showMyRoutes = queryStringParameters.get("mostrarMisrutas");
        String skillLevelSource = queryStringParameters.get("filtroDificultad");
        String filterByUsername = queryStringParameters.get("filtrarUsuario");
        String minDistanceSource = queryStringParameters.get("distanciaMinima");
        String maxDistanceSource = queryStringParameters.get("distanciaMaxima");

        ExecuteRouteFilterRequest request = new ExecuteRouteFilterRequest();

        // INPUT VALIDATION

        // Validate minimum kudos format

        if (minimumKudosSource != null) {
            if (TypeUtils.isANumber(minimumKudosSource))
                request.setMinimumKudos(
                        new RequestParameter<>(Integer.parseInt(minimumKudosSource)));
            else
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(BAD_REQUEST)
                        .withBody("Parámetro (kudosMinimos) no es numérico");
        }

        // Validate skill level format

        if (skillLevelSource != null) {
            if (TypeUtils.isANumber(skillLevelSource))
                request.setSkillLevel(
                        new RequestParameter<>(Integer.parseInt(skillLevelSource)));
            else
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(BAD_REQUEST)
                        .withBody("Parámetro (filtroDificultad) no es numérico");
        }

        // Validate blocked routes format

        if (hideBlockedRoutesSource != null) {
            if (TypeUtils.isABoolean(hideBlockedRoutesSource))
                request.setHideBlockedRoutes(
                        new RequestParameter<>(Boolean.parseBoolean(hideBlockedRoutesSource)));
            else
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(BAD_REQUEST)
                        .withBody("Parámetro (ocultarRutasBloq) tiene un valor inválido");
        }

        // Validate min route distance format

        if (minDistanceSource != null) {
            if (TypeUtils.isANumber(minDistanceSource))
                request.setMinDistance(
                        new RequestParameter<>(Integer.parseInt(minDistanceSource)));
            else
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(BAD_REQUEST)
                        .withBody("Parámetro (distanciaMinima) no es numérico");
        }

        // Validate max route distance format

        if (maxDistanceSource != null) {
            if (TypeUtils.isANumber(maxDistanceSource))
                request.setMaxDistance(
                        new RequestParameter<>(Integer.parseInt(maxDistanceSource)));
            else
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(BAD_REQUEST)
                        .withBody("Parámetro (distanciaMaxima) no es numérico");
        }

        if (searchText != null)
            request.setSearchText(new RequestParameter<>(searchText));
        if (routeKudosOrdering != null)
            request.setRouteKudosOrdering(new RequestParameter<>(routeKudosOrdering));
        if (showMyRoutes != null)
            request.setShowMyRoutes(new RequestParameter<>(showMyRoutes));
        if (filterByUsername != null)
            request.setFilterByUsername(new RequestParameter<>(filterByUsername));

        ExecuteRouteFilterResponse response = ExecuteRouteFilter.run(request);

        if (response.flagged(ExecuteRouteFilterResponse.Flags.ERROR_INVALID_REQUEST_PARAM))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(ExecuteRouteFilterResponse.Flags.ERROR_INVALID_REQUEST_PARAM));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getFilteredRoutesList()));
    }
}

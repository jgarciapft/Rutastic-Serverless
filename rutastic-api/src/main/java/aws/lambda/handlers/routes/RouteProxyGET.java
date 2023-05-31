package aws.lambda.handlers.routes;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import helper.MySQLConnectionManager;
import helper.TypeUtils;
import model.validators.RouteValidation;
import resources.model.APIErrorBody;
import resources.model.APIGatewayProxyResponse;
import resources.model.RequestParameter;
import resources.routes.GetRelatedRoutes;
import resources.routes.GetRoute;
import resources.routes.model.GetRelatedRoutesRequest;
import resources.routes.model.GetRelatedRoutesResponse;
import resources.routes.model.GetRouteRequest;
import resources.routes.model.GetRouteResponse;

import java.util.HashMap;
import java.util.Map;

import static aws.lambda.HTTPStatusCodes.*;

public class RouteProxyGET implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final MySQLConnectionManager jdbcManager = MySQLConnectionManager.getInstance();
    private static final Gson gson = new Gson();

    static {
        // On cold boot set up and create a db connection
        jdbcManager.setUpAndConnect(
                System.getenv("READ_ENDPOINT"),
                System.getenv("WRITE_ENDPOINT"),
                Integer.parseInt(System.getenv("PORT")),
                System.getenv("DB_USER"),
                System.getenv("DB_USER_PWD"),
                System.getenv("DB_SCHEMA"));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String resourceProxyValue = event.getPathParameters().get("proxy");

        // Requested GET /rutas/{idRuta}
        if (resourceProxyValue.matches(RouteValidation.ROUTE_ID_REGEX))
            return handleGetRouteRequest(event, context);

        // Requested GET /rutas/{idRuta}/similares
        if (resourceProxyValue.matches(RouteValidation.ROUTE_ID_REGEX + "/similares"))
            return handleRelatedRoutesRequest(event, context);

        // Unknown request
        return new APIGatewayProxyResponseEvent().withStatusCode(NOT_FOUND);
    }

    private APIGatewayProxyResponseEvent handleGetRouteRequest(APIGatewayProxyRequestEvent event, Context context) {
        String routeIdString = event.getPathParameters().get("proxy");

        // Validate the route ID

        long routeId;
        if (RouteValidation.routeIdIsValid(routeIdString))
            routeId = Long.parseLong(routeIdString);
        else
            return new APIGatewayProxyResponseEvent().withStatusCode(BAD_REQUEST).withBody("ID de ruta inválido");

        GetRouteResponse response = GetRoute.run(new GetRouteRequest(routeId));

        if (response.getRoute() != null)
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(OK)
                    .withBody(gson.toJson(response.getRoute()));
        else
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_FOUND)
                    .withBody("No se encuentra la ruta solicitada");
    }

    private APIGatewayProxyResponseEvent handleRelatedRoutesRequest(APIGatewayProxyRequestEvent event, Context context) {
        Map<String, String> queryStringParameters =
                event.getQueryStringParameters() != null ? event.getQueryStringParameters() : new HashMap<>();

        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        String similarity = queryStringParameters.get("por");
        String limitSource = queryStringParameters.get("limite");
        String distanceDeltaSource = queryStringParameters.get("deltaDistancia");

        GetRelatedRoutesRequest request = new GetRelatedRoutesRequest();
        request.setRouteId(routeId);

        // INPUT VALIDATION

        // Validate similarity query param

        if (similarity != null && !similarity.trim().isEmpty())
            request.setSimilarity(new RequestParameter<>(similarity.trim()));
        else
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody("La característica de similitud no puede estar vacía");

        // Validate limit query param

        if (limitSource != null) {
            if (TypeUtils.isANumber(limitSource))
                request.setLimit(new RequestParameter<>(Integer.parseInt(limitSource)));
            else
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(BAD_REQUEST)
                        .withBody("El parámetro (limite) no es un número");
        }

        // Validate distanceDelta query param

        if (distanceDeltaSource != null) {
            if (TypeUtils.isANumber(distanceDeltaSource))
                request.setDistanceDelta(new RequestParameter<>(Integer.parseInt(distanceDeltaSource)));
             else
                 return new APIGatewayProxyResponseEvent()
                         .withStatusCode(BAD_REQUEST)
                         .withBody("El parámetro (deltaDistancia) no es un número");
        }

        GetRelatedRoutesResponse response = GetRelatedRoutes.run(request);

        if (response.flagged(GetRelatedRoutesResponse.Flags.ERROR_INVALID_REQUEST_PARAM))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(GetRelatedRoutesResponse.Flags.ERROR_INVALID_REQUEST_PARAM));

        if (response.flagged(GetRelatedRoutesResponse.Flags.ERROR_NO_ROUTE_FOUND))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_FOUND);

        if (response.flagged(GetRelatedRoutesResponse.Flags.ERROR_UNKNOWN_SIMILARITY))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(GetRelatedRoutesResponse.Flags.ERROR_UNKNOWN_SIMILARITY));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getRelatedRoutesList()));
    }
}

package aws.lambda.handlers.routes;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import helper.MySQLConnectionManager;
import model.Route;
import model.validators.RouteValidation;
import resources.model.UserClaimedIdentity;
import resources.routes.ChangeBlockedState;
import resources.routes.ChangeKudos;
import resources.routes.EditRoute;
import resources.routes.model.*;

import java.util.Map;

import static aws.lambda.HTTPStatusCodes.*;

public class RouteProxyPUT implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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

        // Requested PUT /rutas/{idRuta}/estado?accion={bloquear|desbloquear}
        if (resourceProxyValue.matches(String.format("%s/estado", RouteValidation.ROUTE_ID_REGEX)))
            return handleChangeRouteBlockedStateRequest(event, context);

        // Requested PUT /rutas/{idRuta}/kudos?accion={dar|quitar}
        if (resourceProxyValue.matches(String.format("%s/kudos", RouteValidation.ROUTE_ID_REGEX)))
            return handleChangeRouteKudosRequest(event, context);

        // Requested PUT /rutas/{idRuta} { Body: Route JSON }
        if (resourceProxyValue.matches(RouteValidation.ROUTE_ID_REGEX))
            return handleEditRouteRequest(event, context);

        // Unknown request
        return new APIGatewayProxyResponseEvent().withStatusCode(NOT_FOUND);
    }

    private APIGatewayProxyResponseEvent handleChangeRouteBlockedStateRequest(APIGatewayProxyRequestEvent event, Context context) {
        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        String action = event.getQueryStringParameters().get("accion");
        String cognitoUsername = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        ChangeBlockedStateRequest request = new ChangeBlockedStateRequest();

        request.setRequestedRouteId(routeId);
        request.setUserClaimedIdentity(new UserClaimedIdentity(cognitoUsername));

        // INPUT VALIDATION

        if (action != null && !action.trim().isEmpty())
            request.setAction(action.trim());
        else
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody("Es obligatorio especificar la acción a realizar");

        ChangeBlockedStateResponse response = ChangeBlockedState.run(request);

        if (response.flagged(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ROUTE_ID))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ROUTE_ID));

        if (response.flagged(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ACTION_PARAM))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ACTION_PARAM));

        if (response.flagged(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ACTION))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_MODIFIED)
                    .withBody(response.getFlagMessage(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ACTION));

        if (response.flagged(ChangeBlockedStateResponse.Flags.ERROR_ROUTE_NOT_FOUND))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_FOUND)
                    .withBody(response.getFlagMessage(ChangeBlockedStateResponse.Flags.ERROR_ROUTE_NOT_FOUND));

        if (response.flagged(ChangeBlockedStateResponse.Flags.ERROR_UNAUTHORIZED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(UNAUTHORIZED);

        if (response.flagged(ChangeBlockedStateResponse.Flags.ERROR_UNEXPECTED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(INTERNAL_SERVER_ERROR)
                    .withBody(response.getFlagMessage(ChangeBlockedStateResponse.Flags.ERROR_UNEXPECTED));

        return new APIGatewayProxyResponseEvent().withStatusCode(NO_CONTENT);
    }

    private APIGatewayProxyResponseEvent handleChangeRouteKudosRequest(APIGatewayProxyRequestEvent event, Context context) {
        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        String action = event.getQueryStringParameters().get("accion");
        String cognitoUsername = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        ChangeKudosRequest request = new ChangeKudosRequest();

        request.setRequestedRouteId(routeId);
        request.setUserClaimedIdentity(new UserClaimedIdentity(cognitoUsername));

        // INPUT VALIDATION

        if (action != null && !action.trim().isEmpty())
            request.setAction(action.trim());
        else return new APIGatewayProxyResponseEvent()
                .withStatusCode(BAD_REQUEST)
                .withBody("Es obligatorio especificar la acción a realizar");

        ChangeKudosResponse response = ChangeKudos.run(request);

        if (response.flagged(ChangeKudosResponse.Flags.ERROR_INVALID_ROUTE_ID))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(ChangeKudosResponse.Flags.ERROR_INVALID_ROUTE_ID));

        if (response.flagged(ChangeKudosResponse.Flags.ERROR_INVALID_ACTION_PARAM))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(ChangeKudosResponse.Flags.ERROR_INVALID_ACTION_PARAM));

        if (response.flagged(ChangeKudosResponse.Flags.ERROR_ROUTE_NOT_FOUND))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_FOUND)
                    .withBody(response.getFlagMessage(ChangeKudosResponse.Flags.ERROR_ROUTE_NOT_FOUND));

        if (response.flagged(ChangeKudosResponse.Flags.ERROR_UNEXPECTED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(INTERNAL_SERVER_ERROR)
                    .withBody(response.getFlagMessage(ChangeKudosResponse.Flags.ERROR_UNEXPECTED));

        return new APIGatewayProxyResponseEvent().withStatusCode(NO_CONTENT);
    }

    private APIGatewayProxyResponseEvent handleEditRouteRequest(APIGatewayProxyRequestEvent event, Context context) {
        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        Route modifiedRoute = gson.fromJson(event.getBody(), Route.class);
        String cognitoUsername = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        EditRouteRequest request = new EditRouteRequest();

        request.setRouteId(routeId);
        request.setUserClaimedIdentity(new UserClaimedIdentity(cognitoUsername));

        // INPUT VALIDATION

        if (modifiedRoute != null)
            request.setModifiedRoute(modifiedRoute);
        else return new APIGatewayProxyResponseEvent()
                .withStatusCode(BAD_REQUEST)
                .withBody("Es obligatorio incluir los campos editados de la ruta");

        EditRouteResponse response = EditRoute.run(request);

        if (response.flagged(EditRouteResponse.Flags.ERROR_INVALID_ROUTE_ID))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(EditRouteResponse.Flags.ERROR_INVALID_ROUTE_ID));

        if (response.flagged(EditRouteResponse.Flags.ERROR_INVALID_EDIT))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody(response.getFlagMessage(EditRouteResponse.Flags.ERROR_INVALID_EDIT));

        if (response.flagged(EditRouteResponse.Flags.ERROR_NON_MATCHING_ROUTE))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(FORBIDDEN)
                    .withBody(response.getFlagMessage(EditRouteResponse.Flags.ERROR_NON_MATCHING_ROUTE));

        if (response.flagged(EditRouteResponse.Flags.ERROR_ROUTE_NOT_FOUND))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NOT_FOUND)
                    .withBody(response.getFlagMessage(EditRouteResponse.Flags.ERROR_ROUTE_NOT_FOUND));

        if (response.flagged(EditRouteResponse.Flags.ERROR_UNAUTHORIZED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(UNAUTHORIZED);

        if (response.flagged(EditRouteResponse.Flags.ERROR_UNAUTHORIZED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(INTERNAL_SERVER_ERROR)
                    .withBody(response.getFlagMessage(EditRouteResponse.Flags.ERROR_UNEXPECTED));

        return new APIGatewayProxyResponseEvent().withStatusCode(NO_CONTENT);
    }
}

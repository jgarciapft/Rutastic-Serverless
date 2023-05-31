package aws.lambda.handlers.kudoEntries;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import helper.MySQLConnectionManager;
import resources.kudoEntries.GetUserKudoEntries;
import resources.kudoEntries.GetUserKudoEntriesForRoute;
import resources.kudoEntries.model.GetUserKudoEntriesForRouteRequest;
import resources.kudoEntries.model.GetUserKudoEntriesForRouteResponse;
import resources.kudoEntries.model.GetUserKudoEntriesRequest;
import resources.kudoEntries.model.GetUserKudoEntriesResponse;
import resources.model.UserClaimedIdentity;
import model.validators.UserValidation;

import java.util.Map;

import static aws.lambda.HTTPStatusCodes.*;

public class KudoEntriesUserProxyGET implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        String resourceProxyValue = event.getPathParameters().getOrDefault("proxy", "");

        // Decide how to handle the API Gateway event to return the adequate data

        // Requested GET /kudos/{usuario}
        if (resourceProxyValue.matches(UserValidation.USERNAME_REGEX))
            return handleGetUserKudoEntriesRequest(event, context);

        // Requested GET /kudos/{usuario}/{idRuta}
        if (resourceProxyValue.matches(String.format("%s/[0-9]+", UserValidation.USERNAME_REGEX)))
            return handleGetUserKudoEntriesForRouteRequest(event, context);

        // Unknown requested resource
        return new APIGatewayProxyResponseEvent().withStatusCode(NOT_FOUND);
    }

    private APIGatewayProxyResponseEvent handleGetUserKudoEntriesRequest(APIGatewayProxyRequestEvent event, Context context) {
        String username = event.getPathParameters().get("proxy");
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        GetUserKudoEntriesResponse response =
                GetUserKudoEntries.run(new GetUserKudoEntriesRequest(username, new UserClaimedIdentity(cognitoUser)));

        if (response.flagged(GetUserKudoEntriesResponse.Flags.ERROR_UNAUTHORIZED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(UNAUTHORIZED)
                    .withBody(response.getFlagMessage(GetUserKudoEntriesResponse.Flags.ERROR_UNAUTHORIZED));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getKudoEntriesList()));
    }

    private APIGatewayProxyResponseEvent handleGetUserKudoEntriesForRouteRequest(APIGatewayProxyRequestEvent event, Context context) {
        String username = event.getPathParameters().get("proxy").split("/")[0];
        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[1]);
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        GetUserKudoEntriesForRouteResponse response =
                GetUserKudoEntriesForRoute.run(new GetUserKudoEntriesForRouteRequest(username, routeId, new UserClaimedIdentity(cognitoUser)));

        if (response.flagged(GetUserKudoEntriesForRouteResponse.Flags.ERROR_UNAUTHORIZED))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(UNAUTHORIZED)
                    .withBody(response.getFlagMessage(GetUserKudoEntriesForRouteResponse.Flags.ERROR_UNAUTHORIZED));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getKudoEntry()));
    }
}

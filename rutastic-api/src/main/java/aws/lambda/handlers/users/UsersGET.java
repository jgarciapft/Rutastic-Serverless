package aws.lambda.handlers.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import helper.MySQLConnectionManager;
import resources.users.GetAllUsers;
import resources.users.GetUserStatistics;
import resources.users.model.GetAllUsersResponse;
import resources.users.model.GetUserStatisticsRequest;
import resources.users.model.GetUserStatisticsResponse;

import java.util.Map;

import static aws.lambda.HTTPStatusCodes.*;

public class UsersGET implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

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
        Map<String, String> httpQuery = event.getQueryStringParameters();

        // Requested GET /usuarios
        if (httpQuery == null) return handleGetAllUsersRequest(event, context);

        // Requested GET /usuarios?estadistica={top5UsuariosPorTopRutas|top5UsuariosPorMediaKudos}
        if (httpQuery.containsKey("estadistica")) return handleGetUserStatisticsRequest(event, context);

        // Unknown request
        return new APIGatewayProxyResponseEvent().withStatusCode(NOT_FOUND);
    }

    private APIGatewayProxyResponseEvent handleGetAllUsersRequest(APIGatewayProxyRequestEvent event, Context context) {
        GetAllUsersResponse response = GetAllUsers.run();

        if (response.getAllUsers() == null || response.getAllUsers().isEmpty())
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NO_CONTENT);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getAllUsers()));
    }

    private APIGatewayProxyResponseEvent handleGetUserStatisticsRequest(APIGatewayProxyRequestEvent event, Context context) {
        String requestedStat = event.getQueryStringParameters().getOrDefault("estadistica", "").trim();

        if (requestedStat.isEmpty())
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody("No se ha solicitado ninguna estadística de usuario");

        GetUserStatisticsResponse response = GetUserStatistics.run(new GetUserStatisticsRequest(requestedStat));

        if (response.flagged(GetUserStatisticsResponse.Flags.ERROR_UNKNOWN_STATISTIC))
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(BAD_REQUEST)
                    .withBody("Estadística desconocida");

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getUserStatistics()));
    }
}
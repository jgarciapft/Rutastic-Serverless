package aws.lambda.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import helper.MySQLConnectionManager;
import resources.routeCategories.GetAllRouteCategories;
import resources.routeCategories.model.GetAllRouteCategoriesResponse;

import static aws.lambda.HTTPStatusCodes.NO_CONTENT;
import static aws.lambda.HTTPStatusCodes.OK;

public class CategoriasRutaGET implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final MySQLConnectionManager jdbcManager = MySQLConnectionManager.getInstance();
    private static final Gson gson = new Gson();

    // On cold boot set up and create a db connection
    static {
        jdbcManager.setUpAndConnect(System.getenv("READ_ENDPOINT"),
                Integer.parseInt(System.getenv("PORT")),
                System.getenv("DB_USER"),
                System.getenv("DB_USER_PWD"),
                System.getenv("DB_SCHEMA"));
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        GetAllRouteCategoriesResponse response = GetAllRouteCategories.run();

        if (response.getRouteCategoriesList() == null || response.getRouteCategoriesList().isEmpty())
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(NO_CONTENT);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(OK)
                .withBody(gson.toJson(response.getRouteCategoriesList()));
    }
}

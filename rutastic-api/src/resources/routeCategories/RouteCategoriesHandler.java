package resources.routeCategories;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.RouteCategoryDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.RouteCategory;
import resources.APIErrorBody;
import resources.APIGatewayProxyResponse;
import resources.MySQLConnectionManager;

import java.util.List;

import static resources.HTTPStatus.*;

public class RouteCategoriesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponse<?>> {

    private static final MySQLConnectionManager jdbcManager = MySQLConnectionManager.getInstance();
    private static final DAOAbstractFactory daoAbstractFactory = DAOAbstractFactory.get();
    private static final RouteCategoryDAO routeCategoryDAO;

    private static final String THIS_RESOURCE = "/categoriasruta"; // This resource on the REST API

    static {
        // On cold boot set up and create a db connection
        jdbcManager.setUpAndConnect(System.getenv("PROXY_ENDPOINT"),
                Integer.parseInt(System.getenv("PORT")),
                System.getenv("DB_USER"),
                System.getenv("DB_USER_PWD"),
                System.getenv("DB_SCHEMA"));

        // Initialize DAOs
        routeCategoryDAO = daoAbstractFactory.impl(DAOImplJDBC.class).forModel(RouteCategory.class);
    }

    @Override
    public APIGatewayProxyResponse<?> handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        String resource = event.getResource();

        // Requested GET /categoriasruta
        if (resource.equals(THIS_RESOURCE) && event.getHttpMethod().equals("GET")) {
            return getAllRouteCategories(event, context).addCORS();
        }
        // Unkonw requested resource
        else {
            return new APIGatewayProxyResponse<>(NOT_FOUND).addCORS();
        }
    }

    // GET /categoriasruta
    private APIGatewayProxyResponse<Object> getAllRouteCategories(APIGatewayProxyRequestEvent event, Context context) {

        List<RouteCategory> allCategories = routeCategoryDAO.getAll(); // Try retrieving all route categories

        // Check that the DAO could complete the operation

        if (allCategories == null)
            return new APIGatewayProxyResponse<>(SERVICE_UNAVAILABLE,
                    new APIErrorBody("Ocurrió un error al pedir todas las categorías de ruta"));

        // At least one category should be always retrieved

        if (allCategories.isEmpty())
            return new APIGatewayProxyResponse<>(NOT_FOUND,
                    new APIErrorBody("No se ha encontrado ninguna categoría de ruta"));

        return new APIGatewayProxyResponse<>(OK, allCategories);
    }

}

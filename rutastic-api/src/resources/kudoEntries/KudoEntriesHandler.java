package resources.kudoEntries;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import dao.KudoEntryDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.KudoEntry;
import resources.APIErrorBody;
import resources.APIGatewayProxyResponse;
import resources.MySQLConnectionManager;

import java.util.Map;

import static resources.HTTPStatus.*;

public class KudoEntriesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponse<?>> {

    private static final MySQLConnectionManager jdbcManager = MySQLConnectionManager.getInstance();
    private static final DAOAbstractFactory daoAbstractFactory = DAOAbstractFactory.get();
    private static final KudoEntryDAO kudoEntryDAO;

    private static final String THIS_RESOURCE = "/kudos"; // This resource on the REST API

    static {
        // On cold boot set up and create a db connection
        jdbcManager.setUpAndConnect(System.getenv("PROXY_ENDPOINT"),
                Integer.parseInt(System.getenv("PORT")),
                System.getenv("DB_USER"),
                System.getenv("DB_USER_PWD"),
                System.getenv("DB_SCHEMA"));

        // Initialize DAOs
        kudoEntryDAO = daoAbstractFactory.impl(DAOImplJDBC.class).forModel(KudoEntry.class);
    }

    @Override
    public APIGatewayProxyResponse<?> handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        String resource = event.getResource();
        String resourceProxyValue = event.getPathParameters() != null ? event.getPathParameters().get("proxy") : "";

        // Decide how to handle the API Gateway event to return the adequate data

        // Requested GET /kudos/{usuario}
        if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("\\w+") &&
                event.getHttpMethod().equals("GET")) {
            return getUserKudoEntries(event, context).addCORS();
        }
        // Requested GET /kudos/{usuario}/{idRuta}
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("\\w+/[0-9]+")
                && event.getHttpMethod().equals("GET")) {
            return getUserKudoEntriesForRoute(event, context).addCORS();
        }
        // Unkonw requested resource
        else {
            return new APIGatewayProxyResponse<>(NOT_FOUND).addCORS();
        }
    }

    // GET /kudos/{usuario}
    private APIGatewayProxyResponse<?> getUserKudoEntries(APIGatewayProxyRequestEvent event, Context context) {

        String username = event.getPathParameters().get("proxy");
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        // AUTHORISATION FILTER. The logged user can only retrieve his kudo entries

        if (cognitoUser.equals(username)) {
            // Return the collection of kudo entries for the requested user
            return new APIGatewayProxyResponse<>(OK, kudoEntryDAO.getAllByUser(username));
        } else { // Insufficient privileges
            return new APIGatewayProxyResponse<>(UNAUTHORIZED,
                    new APIErrorBody("Su usario no tiene permisos para recuperar las entradas kudo del usuario solicitado"));
        }
    }

    // GET /kudos/{usuario}/{idRuta}
    private APIGatewayProxyResponse<?> getUserKudoEntriesForRoute(APIGatewayProxyRequestEvent event, Context context) {

        String username = event.getPathParameters().get("proxy").split("/")[0];
        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[1]);
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        // AUTHORISATION FILTER. The logged user can only retrieve his kudo entries

        if (cognitoUser.equals(username)) {
            // Return the kudo entry by the requested user to the requested route
            return new APIGatewayProxyResponse<>(OK, kudoEntryDAO.getByPKey(username, routeId));
        } else { // Insufficient privileges
            return new APIGatewayProxyResponse<>(UNAUTHORIZED,
                    new APIErrorBody("Su usario no tiene permisos para recuperar las entradas kudo del usuario solicitado"));
        }
    }

}

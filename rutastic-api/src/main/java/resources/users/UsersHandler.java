package resources.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import dao.UserDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.User;
import model.statistic.UserStatistic;
import resources.APIErrorBody;
import resources.APIGatewayProxyResponse;
import helper.MySQLConnectionManager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static aws.lambda.HTTPStatusCodes.*;

public class UsersHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponse<?>> {

    private static final MySQLConnectionManager jdbcManager = MySQLConnectionManager.getInstance();
    private static final DAOAbstractFactory daoAbstractFactory = DAOAbstractFactory.get();
    private static final UserDAO userDAO;
    private static final Gson gson = new Gson();

    private static final String THIS_RESOURCE = "/usuarios"; // This resource on the REST API

    static {
        // On cold boot set up and create a db connection
        jdbcManager.setUpAndConnect(System.getenv("PROXY_ENDPOINT"),
                Integer.parseInt(System.getenv("PORT")),
                System.getenv("DB_USER"),
                System.getenv("DB_USER_PWD"),
                System.getenv("DB_SCHEMA"));

        // Initialize user DAO
        userDAO = daoAbstractFactory.impl(DAOImplJDBC.class).forModel(User.class);
    }

    @Override
    public APIGatewayProxyResponse<?> handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        String resource = event.getResource();
        Map<String, String> httpQuery = event.getQueryStringParameters();

        // Decide how to handle the API Gateway event to return the adequate data

        // Requested GET /usuarios
        if (resource.equals(THIS_RESOURCE) && event.getHttpMethod().equals("GET") && httpQuery == null) {
            return getAllUsers().addCORS();
        }
        // Requested GET /usuarios?estadistica={top5UsuariosPorTopRutas|top5UsuariosPorMediaKudos}
        else if (resource.equals(THIS_RESOURCE) && event.getHttpMethod().equals("GET")
                && httpQuery != null && httpQuery.containsKey("estadistica")) {
            return getUserStatistics(event, context).addCORS();
        }
        // Requested POST /usuarios { Body: User JSON }
        else if (resource.equals(THIS_RESOURCE) && event.getHttpMethod().equals("POST") && !event.getBody().isEmpty()) {
            return registerNewUser(event, context).addCORS();
        }
        // Requested DELETE /usuarios/{usuario}
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && event.getHttpMethod().equals("DELETE")) {
            return deleteUser(event, context).addCORS();
        }
        // Unkonw requested resource
        else {
            return new APIGatewayProxyResponse<>(NOT_FOUND).addCORS();
        }

    }

    // GET /usuarios
    private APIGatewayProxyResponse<?> getAllUsers() {

        List<User> allUsers = userDAO.getAll(); // Get all users

        return new APIGatewayProxyResponse<>(OK, allUsers);
    }

    // GET /usuarios?estadistica={top5UsuariosPorTopRutas|top5UsuariosPorMediaKudos}
    private APIGatewayProxyResponse<?> getUserStatistics(APIGatewayProxyRequestEvent event, Context context) {

        String requestedStat = event.getQueryStringParameters().get("estadistica");

        // Validate there's an user stat being requested

        if (requestedStat == null || requestedStat.trim().isEmpty())
            return new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("No se ha solicitado ninguna estadística de usuario"));

        // Get the user stat being requested

        if (requestedStat.equals("top5UsuariosPorTopRutas")) { // Serve top 5 users by top monthly routes

            List<UserStatistic> top5UsersByTopMonthlyRoutes = userDAO.getTopUsersByTopMonthlyRoutes();

            // Get first 5 results

            top5UsersByTopMonthlyRoutes = top5UsersByTopMonthlyRoutes
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            return new APIGatewayProxyResponse<>(OK, top5UsersByTopMonthlyRoutes);

        } else if (requestedStat.equals("top5UsuariosPorMediaKudos")) { // Serve top 5 users by average kudo ratings of their routes

            List<UserStatistic> top5UsersByAvgKudos = userDAO.getTopUsersByAvgKudos();

            // Get first 5 results

            top5UsersByAvgKudos = top5UsersByAvgKudos
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            return new APIGatewayProxyResponse<>(OK, top5UsersByAvgKudos);
        } else { // Other unhandled user stats
            return new APIGatewayProxyResponse<>(NOT_FOUND, new APIErrorBody("Estadística desconocida"));
        }
    }

    // POST /usuarios
    // { User JSON }
    private APIGatewayProxyResponse<?> registerNewUser(APIGatewayProxyRequestEvent event, Context context) {

        User newUser = gson.fromJson(event.getBody(), User.class);

        // Validate uploaded new user

        if (newUser != null) {

            long newUserID = userDAO.add(newUser)[0]; // Try registering the new user at the backend

            // Check if the new user could be registered

            if (newUserID != -1) {
                return new APIGatewayProxyResponse<>(CREATED); // New user registered. Return code 201 (Created)
            } else { // Error registering the user at the backend
                return new APIGatewayProxyResponse<>(INTERNAR_SERVER_ERROR, new APIErrorBody("Ocurrió un error registrando al nuevo usuario"));
            }
        } else { // The uploaded user is invalid
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("El usuario proporcionado no es válido"));
        }
    }

    // DELETE /usuarios/{usuario}
    // Auth: {JWT ID Token}
    private APIGatewayProxyResponse<?> deleteUser(APIGatewayProxyRequestEvent event, Context context) {

        String requestedUser = event.getPathParameters().get("proxy");

        // Validate the username

        if (requestedUser == null || requestedUser.trim().isEmpty())
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("Nombre de usuario inválido"));

        // Check if the user could be found at the backend

        User registeredUser = userDAO.getByUsername(requestedUser);
        if (registeredUser != null) {

            // AUTHORISATION FILTER. The logged user is the only one who can delete his profile

            String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                    .get("cognito:username");

            if (cognitoUser.equals(requestedUser)) {

                // Try deleting the requested user

                boolean deletionSuccessful = userDAO.deleteByUsername(true, registeredUser.getUsername());

                if (deletionSuccessful) {
                    return new APIGatewayProxyResponse<>(NO_CONTENT); // Return code 204 - No content
                } else { // An error occurred while deleting the requested user
                    return new APIGatewayProxyResponse<>(INTERNAR_SERVER_ERROR, new APIErrorBody("Ocurrió un error al eliminar el usuario solicitado"));
                }
            } else { // Insufficient privileges
                return new APIGatewayProxyResponse<>(UNAUTHORIZED, new APIErrorBody("Este usuario no tiene permisos para eliminar el perfil solicitado"));
            }
        } else { // User not found
            return new APIGatewayProxyResponse<>(NOT_FOUND, new APIErrorBody("No se encuentra el usuario solicitado"));
        }
    }

}

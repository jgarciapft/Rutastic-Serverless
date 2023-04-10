package resources.routes;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import dao.JDBCRouteDAO;
import dao.KudoEntryDAO;
import dao.RouteDAO;
import dao.UserDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import dao.implementations.RouteDAOImplJDBC;
import model.KudoEntry;
import model.Route;
import model.User;
import resources.APIErrorBody;
import resources.APIGatewayProxyResponse;
import resources.MySQLConnectionManager;
import routefilter.RouteSkillLevel;
import routefilter.SQLRouteFilterBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static resources.HTTPStatus.*;

public class RoutesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponse<?>> {

    private static final MySQLConnectionManager jdbcManager = MySQLConnectionManager.getInstance();
    private static final DAOAbstractFactory daoAbstractFactory = DAOAbstractFactory.get();
    private static final RouteDAO routeDAO;
    private static final UserDAO userDAO;
    private static final KudoEntryDAO kudoEntryDAO;
    private static final Gson gson = new Gson();

    private static final String THIS_RESOURCE = "/rutas"; // This resource on the REST API

    static {
        // On cold boot set up and create a db connection
        jdbcManager.setUpAndConnect(System.getenv("PROXY_ENDPOINT"),
                Integer.parseInt(System.getenv("PORT")),
                System.getenv("DB_USER"),
                System.getenv("DB_USER_PWD"),
                System.getenv("DB_SCHEMA"));

        // Initialize DAOs
        routeDAO = daoAbstractFactory.impl(DAOImplJDBC.class).forModel(Route.class);
        userDAO = daoAbstractFactory.impl(DAOImplJDBC.class).forModel(User.class);
        kudoEntryDAO = daoAbstractFactory.impl(DAOImplJDBC.class).forModel(KudoEntry.class);
    }

    @Override
    public APIGatewayProxyResponse<?> handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        String resource = event.getResource();
        String resourceProxyValue = event.getPathParameters() != null ? event.getPathParameters().get("proxy") : "";

        // Decide how to handle the API Gateway event to return the adequate data

        // Requested GET /rutas/{idRuta}
        if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("[0-9]+") &&
                event.getHttpMethod().equals("GET")) {
            return getRouteById(event, context).addCORS();
        }
        // Requested GET /rutas/filtro?{query}
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.equals("filtro")
                && event.getHttpMethod().equals("GET")) {
            return executeRouteFilter(event, context).addCORS();
        }
        // Requested GET /rutas/estadisticas?e={topRutasSemanal|topRutasMensual}
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.equals("estadisticas")
                && event.getHttpMethod().equals("GET")) {
            return routeStatisticsQueryHandler(event, context).addCORS();
        }
        // Requested GET /rutas/{idRuta}/similares
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("[0-9]+/similares")
                && event.getHttpMethod().equals("GET")) {
            return relatedRoutesHandler(event, context).addCORS();
        }
        // Requested POST /rutas
        else if (resource.equals(THIS_RESOURCE) && event.getHttpMethod().equals("POST") && !event.getBody().isEmpty()) {
            return addRoute(event, context).addCORS();
        }
        // Requested PUT /rutas/{idRuta}/estado?accion={bloquear|desbloquear}
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("[0-9]+/estado")
                && event.getHttpMethod().equals("PUT")) {
            return routeBlockedStateHandler(event, context).addCORS();
        }
        // Requested PUT /rutas/{idRuta}/kudos?accion={dar|quitar}
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("[0-9]+/kudos")
                && event.getHttpMethod().equals("PUT")) {
            return routeKudosHandler(event, context).addCORS();
        }
        // Requested PUT /rutas/{idRuta} { Body: Route JSON }
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("[0-9]+")
                && event.getHttpMethod().equals("PUT") && !event.getBody().isEmpty()) {
            return saveRoute(event, context).addCORS();
        }
        // Requested DELETE /rutas/{idRuta}
        else if (resource.equals(THIS_RESOURCE + "/{proxy+}") && resourceProxyValue.matches("[0-9]+")
                && event.getHttpMethod().equals("DELETE")) {
            return saveRoute(event, context).addCORS();
        }
        // Unkonw requested resource
        else {
            return new APIGatewayProxyResponse<>(NOT_FOUND).addCORS();
        }

    }

    // GET /rutas/{idRuta}
    private APIGatewayProxyResponse<?> getRouteById(APIGatewayProxyRequestEvent event, Context context) {

        String routeIdString = event.getPathParameters().get("proxy");
        long routeId;

        // Validate the route ID

        if (routeIdString.matches("[0-9]+"))
            routeId = Long.parseLong(routeIdString);
        else
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("ID de ruta inválido"));

        Route requestedRoute = routeDAO.getById(routeId); // Try retrieving the requested route

        // Check that the route could be retrieved

        if (requestedRoute == null)
            return new APIGatewayProxyResponse<>(NOT_FOUND, new APIErrorBody("No se encuentra la ruta solicitada"));

        return new APIGatewayProxyResponse<>(OK, requestedRoute);
    }

    // GET /rutas/filtro?{query}
    private APIGatewayProxyResponse<?> executeRouteFilter(APIGatewayProxyRequestEvent event, Context context) {

        String searchText = event.getQueryStringParameters().get("buscarTexto");
        String routeKudosOrdering = event.getQueryStringParameters().getOrDefault("ordenarPorKudos", "no-ordenar");
        String minimumKudosSource = event.getQueryStringParameters().get("kudosMinimos");
        String hideBlockedRoutesSource = event.getQueryStringParameters().getOrDefault("ocultarRutasBloq", "false");
        String showOnlyMyRoutes = event.getQueryStringParameters().get("mostrarMisrutas");
        String skillLevelSource = event.getQueryStringParameters().get("filtroDificultad");
        String filterByUsername = event.getQueryStringParameters().get("filtrarUsuario");
        String minDistanceSource = event.getQueryStringParameters().getOrDefault("distanciaMinima", "-1");
        String maxDistanceSource = event.getQueryStringParameters().getOrDefault("distanciaMaxima", "-1");

        // Validate route order based on kudos

        if (!routeKudosOrdering.matches("(no-ordenar|ascendentes|descendentes)")) {
            return new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("Parámetro (ordenarPorKudos) tiene un valor inválido"));
        }

        // Validate minimum kudos

        int minimumKudos = 0;
        if (minimumKudosSource != null) {
            if (minimumKudosSource.matches("-?[0-9]+"))
                minimumKudos = Integer.parseInt(minimumKudosSource);
            else
                return new APIGatewayProxyResponse<>(BAD_REQUEST,
                        new APIErrorBody("Parámetro (kudosMinimos) tiene un valor inválido"));
        }

        // Validate skill level

        int skillLevel = 0;
        if (skillLevelSource != null) {
            if (skillLevelSource.matches("[0123]"))
                skillLevel = Integer.parseInt(skillLevelSource);
            else
                return new APIGatewayProxyResponse<>(BAD_REQUEST,
                        new APIErrorBody("Parámetro (filtroDificultad) tiene un valor inválido"));
        }

        // Validate blocked routes

        boolean hideBlockedRoutes = false;
        if (hideBlockedRoutesSource.matches("true|false"))
            hideBlockedRoutes = Boolean.parseBoolean(hideBlockedRoutesSource);
        else
            new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("Parámetro (ocultarRutasBloq) tiene un valor inválido"));


        // Validate min route distance

        int minDistance = -1;
        if (!minDistanceSource.equals("-1")) {
            if (minDistanceSource.matches("[0-9]+"))
                minDistance = Integer.parseInt(minDistanceSource);
            else
                return new APIGatewayProxyResponse<>(BAD_REQUEST,
                        new APIErrorBody("Parámetro (distanciaMinima) tiene un valor inválido"));
        }

        // Validate max route distance

        int maxDistance = -1;
        if (!maxDistanceSource.equals("-1")) {
            if (maxDistanceSource.matches("[0-9]+"))
                maxDistance = Integer.parseInt(maxDistanceSource);
            else
                return new APIGatewayProxyResponse<>(BAD_REQUEST,
                        new APIErrorBody("Parámetro (distanciaMaxima) tiene un valor inválido"));
        }

        // If execution reaches this point the query is valid --> Apply all the suitable filters

        SQLRouteFilterBuilder sqlRouteFilterBuilder = new SQLRouteFilterBuilder();

        if (searchText != null && !searchText.trim().isEmpty()) {

            // Check if we're dealing with a list of keywords or a literal sentence to match

            if (searchText.contains(";")) { // Is a list of keywords
                List<String> keywords = Arrays.stream(searchText.split(";"))
                        .map(keyword -> keyword = keyword.trim())
                        .collect(Collectors.toList());

                sqlRouteFilterBuilder.titleOrDescriptionContains(keywords);
            } else { // Is a literal sentence
                sqlRouteFilterBuilder.titleOrDescriptionLiterallyContains(searchText);
            }
        }
        if (!routeKudosOrdering.matches("no-ordenar"))
            sqlRouteFilterBuilder.orderByKudos(routeKudosOrdering.matches("descendentes"));
        if (minimumKudosSource != null)
            sqlRouteFilterBuilder.minimumKudos(minimumKudos);
        if (hideBlockedRoutes)
            sqlRouteFilterBuilder.hideBlockedRoutes();
        if (showOnlyMyRoutes != null && !showOnlyMyRoutes.trim().isEmpty())
            sqlRouteFilterBuilder.byUser(showOnlyMyRoutes);
        switch (skillLevel) {
            case 1:
                sqlRouteFilterBuilder.ofSkillLevel(RouteSkillLevel.EASY);
                break;
            case 2:
                sqlRouteFilterBuilder.ofSkillLevel(RouteSkillLevel.MEDIUM);
                break;
            case 3:
                sqlRouteFilterBuilder.ofSkillLevel(RouteSkillLevel.HARD);
                break;
        }
        if (filterByUsername != null && !filterByUsername.trim().isEmpty()) {
            User filteredUserModel = userDAO.getByUsername(filterByUsername.trim());
            if (filteredUserModel != null)
                sqlRouteFilterBuilder.byUser(filteredUserModel.getUsername());
        }
        sqlRouteFilterBuilder.ofDistanceRange(minDistance, maxDistance);

        // Execute the filter and return the filtered routes

        RouteDAOImplJDBC jdbcRouteDAO = (RouteDAOImplJDBC) routeDAO;

        return new APIGatewayProxyResponse<>(OK, jdbcRouteDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));
    }

    // GET /rutas/estadisticas?e={topRutasSemanal|topRutasMensual}
    private APIGatewayProxyResponse<?> routeStatisticsQueryHandler(APIGatewayProxyRequestEvent event, Context context) {

        String requestedStat = event.getQueryStringParameters().get("e");

        // Validate there's a route stat being requested

        if (requestedStat == null || requestedStat.trim().isEmpty())
            return new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("No se ha solicitado ninguna estadística de ruta"));

        // Get the route stat being requested

        if (requestedStat.equals("topRutasSemanal")) { // Serve the top 5 weekly routes

            List<Route> top5RoutesOfTheWeek = routeDAO.getTopRoutesOfTheWeek();

            // Get first 5 results

            top5RoutesOfTheWeek = top5RoutesOfTheWeek
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            // Set route creation date to the format of the top route card container

            top5RoutesOfTheWeek
                    .forEach(route -> route.changeDateFormat(new SimpleDateFormat("dd MMM - HH:mm")));

            return new APIGatewayProxyResponse<>(OK, top5RoutesOfTheWeek);

        } else if (requestedStat.equals("topRutasMensual")) { // Serve the top 5 monthly routes

            List<Route> top5RoutesOfTheMonth = routeDAO.getTopRoutesOfTheMonth();

            // Get first 5 results

            top5RoutesOfTheMonth = top5RoutesOfTheMonth
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            // Set route creation date to the format of the top route card container

            top5RoutesOfTheMonth
                    .forEach(route -> route.changeDateFormat(new SimpleDateFormat("dd MMM - HH:mm")));

            return new APIGatewayProxyResponse<>(OK, top5RoutesOfTheMonth);

        } else { // Other unhandled route stat
            return new APIGatewayProxyResponse<>(BAD_REQUEST, "No se reconoce el parámetro (e)");
        }
    }

    // GET /rutas/{idRuta}/similares
    private APIGatewayProxyResponse<?> relatedRoutesHandler(APIGatewayProxyRequestEvent event, Context context) {

        SQLRouteFilterBuilder sqlRouteFilterBuilder = new SQLRouteFilterBuilder();
        JDBCRouteDAO jdbcRouteDAO = (JDBCRouteDAO) routeDAO;

        Long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        String similarity = event.getQueryStringParameters().get("por");
        String limitSource = event.getQueryStringParameters().get("limite");
        String distanceDeltaSource = event.getQueryStringParameters().get("deltaDistancia");

        // Validate similarity query param

        if (similarity == null || similarity.trim().isEmpty())
            return new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("La característica de similitud no puede estar vacía"));

        // Validate limit query param

        int limit;
        if (limitSource != null && limitSource.matches("[0-9]+")) {
            limit = Integer.parseInt(limitSource);
        } else {
            return new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("El parámetro (limite) no es un número"));
        }

        // Get the requested route and check that it exists

        Route requestedRoute = routeDAO.getById(routeId);

        if (requestedRoute == null)
            return new APIGatewayProxyResponse<>(NOT_FOUND, new APIErrorBody("No se encuentra la ruta"));

        // Process the requested similarity

        similarity = similarity.trim();
        switch (similarity) {
            case "distancia":  // Similar routes by similar distance

                int distanceDelta;
                if (distanceDeltaSource != null && distanceDeltaSource.matches("[0-9]+"))
                    distanceDelta = Integer.parseInt(distanceDeltaSource);
                else
                    return new APIGatewayProxyResponse<>(BAD_REQUEST,
                            new APIErrorBody("Parámetro (distanciaDelta) no es un número"));

                // 3 Related routes by distance within a range given a distance delta (with more kudos)

                sqlRouteFilterBuilder
                        .ofDistanceDelta(requestedRoute.getDistance(), distanceDelta)
                        .orderByKudos(true)
                        .exclude(routeId); // Exclude self

                // Validate route limit query parameter, it should be a positive integer. 0 means it's not set

                if (limit > 0)
                    sqlRouteFilterBuilder.limit(limit);
                else if (limit < 0)
                    return new APIGatewayProxyResponse<>(BAD_REQUEST,
                            new APIErrorBody("El límite no puede ser un número negativo"));


                return new APIGatewayProxyResponse<>(OK, jdbcRouteDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));

            case "dificultad":  // Similar routes by same skill level

                // 3 Related routes with the same skill level (with more kudos)

                sqlRouteFilterBuilder
                        .ofSkillLevel(RouteSkillLevel.parseSkillLevelFromString(requestedRoute.getSkillLevel()))
                        .orderByKudos(true)
                        .exclude(routeId); // Exclude self

                // Validate route limit query parameter, it should be a positive integer. 0 means it's not set

                if (limit > 0)
                    sqlRouteFilterBuilder.limit(limit);
                else if (limit < 0)
                    return new APIGatewayProxyResponse<>(BAD_REQUEST,
                            new APIErrorBody("El límite no puede ser un número negativo"));

                return new APIGatewayProxyResponse<>(OK, jdbcRouteDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));

            case "categorias":  // Similar routes by same set of categories

                // 3 Related routes with shared route categories (with more kudos)

                sqlRouteFilterBuilder
                        .ofCategories(requestedRoute.getCategories().split(Route.CATEGORY_SEPARATOR))
                        .orderByKudos(true)
                        .exclude(routeId); // Exclude self

                // Validate route limit query parameter, it should be a positive integer. 0 means it's not set

                if (limit > 0)
                    sqlRouteFilterBuilder.limit(limit);
                else if (limit < 0)
                    return new APIGatewayProxyResponse<>(BAD_REQUEST,
                            new APIErrorBody("El límite no puede ser un número negativo"));

                return new APIGatewayProxyResponse<>(OK, jdbcRouteDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));

            default:  // Unhandled similarities
                return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("Característica de similitud inválida"));
        }
    }

    // POST /rutas
    private APIGatewayProxyResponse<?> addRoute(APIGatewayProxyRequestEvent event, Context context) {

        Route newRoute = gson.fromJson(event.getBody(), Route.class);
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        // Validate the route

        ArrayList<String> validationMessages = new ArrayList<>();
        if (newRoute == null || !newRoute.validateFormFields(validationMessages))
            return new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("La ruta proporcionada es inválida\n" + validationMessages.toString()));

        newRoute.setCreatedByUser(cognitoUser); // Set the author of this route

        // Try creating the new route

        long newRouteID = routeDAO.add(newRoute)[0];

        // New route created. Return code 201 (Created) and set Location header to /rutas/{idNuevaRuta}
        if (newRouteID != -1)
            return new APIGatewayProxyResponse<>(CREATED)
                    .addHeader("Access-Control-Expose-Headers", "Location")
                    .addHeader("Location", THIS_RESOURCE + "/" + newRouteID);
        else // An error occurred while creating the new route
            return new APIGatewayProxyResponse<>(INTERNAR_SERVER_ERROR,
                    new APIErrorBody("Ocurrió un error al crear la ruta proporcionada"));
    }

    // PUT /rutas/{idRuta}/estado?accion={bloquear|desbloquear}
    private APIGatewayProxyResponse<?> routeBlockedStateHandler(APIGatewayProxyRequestEvent event, Context context) {

        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        String action = event.getQueryStringParameters().get("accion");
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");
        boolean validAction = false;

        // Validate the route ID

        if (!Route.validateID(routeId))
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("ID de ruta inválido"));

        // Validate the requested action. It can either be a request to block or unblock a route

        if (action == null || action.trim().isEmpty() || !action.matches("(des)?bloquear"))
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("Acción no proporcionada o inválida"));

        // Check if a route can be retrieved with the requested ID

        Route requestedRoute = routeDAO.getById(routeId);
        if (requestedRoute != null) {

            // AUTHORISATION FILER. Only the author of the route can block or unblock it

            if (cognitoUser.equals(requestedRoute.getCreatedByUser())) {

                /*
                 * Check the given action against the current route status. If the route is blocked and the
                 * action tells to unblock it, or the route is unblocked and the action tells to block it, it
                 * is considered a valid action.
                 */

                if (action.equals("bloquear") && !requestedRoute.isBlocked()) {
                    requestedRoute.setBlocked(true);
                    validAction = true;
                } else if (action.equals("desbloquear") && requestedRoute.isBlocked()) {
                    requestedRoute.setBlocked(false);
                    validAction = true;
                }

                // Check action validity before committing to the execution of an action

                if (validAction) {

                    boolean success = routeDAO.save(requestedRoute); // Try executing the requested action

                    // Error executing the requested action
                    if (success)
                        return new APIGatewayProxyResponse<>(NO_CONTENT); // On valid action return code 204 - No content
                    else
                        return new APIGatewayProxyResponse<>(INTERNAR_SERVER_ERROR,
                                new APIErrorBody("Ocurrió un error al actualizar el estado de bloqueo de la ruta"));
                } else {
                    return new APIGatewayProxyResponse<>(NOT_MODIFIED); // On invalid action return code 304 - Not modified
                }
            } else { // Insufficient privileges
                return new APIGatewayProxyResponse<>(UNAUTHORIZED,
                        new APIErrorBody("Este usuario no tiene permiso para modificar el estado de bloqueo de esta ruta"));
            }
        } else { // Couldn't find the route at the backend
            return new APIGatewayProxyResponse<>(NOT_FOUND, new APIErrorBody("No se encuentra la ruta solicitada"));
        }
    }

    // PUT /rutas/{idRuta}/kudos?accion={dar|quitar}
    private APIGatewayProxyResponse<?> routeKudosHandler(APIGatewayProxyRequestEvent event, Context context) {
        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        String action = event.getQueryStringParameters().get("accion");
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");
        boolean kudoUpdateSuccessful = false;

        // Validate the route ID

        if (!Route.validateID(routeId))
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("ID de ruta inválido"));

        // Validate the requested action. It can either be a request to give or take the logged user's kudo given to the requested route

        if (action == null || action.trim().isEmpty() || !action.matches("(dar|quitar)"))
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("Acción no proporcionada o inválida"));

        int equivalentKudoModifier = action.equals("dar") ? 1 : -1; // Equivalent kudo modifier for the requested action

        // Check if a route can be retrieved with the requested ID

        Route requestedRoute = routeDAO.getById(routeId);
        if (requestedRoute != null) {

            KudoEntry matchingKudoEntry = kudoEntryDAO.getByPKey(cognitoUser, routeId);

            // Check if the user has already given a kudo or not to the route, if not make a new kudo entry

            if (matchingKudoEntry != null) {

                /*
                 * Query the current kudo rating the user has given to this route and decide the appropriate action.
                 *
                 *    1. +1 Kudo already given AND +1 modifier --> Remove the +1 kudo
                 *    2. +1 Kudo already given AND -1 modifier --> Change kudo rating from +1 to -1
                 *    3. -1 Kudo already given AND +1 modifier --> Change the kudo rating from -1 to +1
                 *    4. -1 Kudo already given AND -1 modifier --> Remove the -1 kudo
                 */

                if (matchingKudoEntry.getModifier() == equivalentKudoModifier) { // 1. and 4.
                    kudoUpdateSuccessful = kudoEntryDAO.deleteByPKey(true, cognitoUser, routeId);
                } else if (matchingKudoEntry.getModifier() == 1 && equivalentKudoModifier == -1) { // 2.
                    matchingKudoEntry.setModifier(-1);
                    kudoUpdateSuccessful = kudoEntryDAO.save(matchingKudoEntry);
                } else if (matchingKudoEntry.getModifier() == -1 && equivalentKudoModifier == 1) { // 3.
                    matchingKudoEntry.setModifier(1);
                    kudoUpdateSuccessful = kudoEntryDAO.save(matchingKudoEntry);
                }
            } else {

                // User never gave any kudo to this route, create a new entry

                KudoEntry newKudoEntry = new KudoEntry();
                newKudoEntry.setUser(cognitoUser);
                newKudoEntry.setRoute(routeId);
                newKudoEntry.setModifier(equivalentKudoModifier);
                kudoUpdateSuccessful = kudoEntryDAO.add2(newKudoEntry, true)[0] instanceof String;
            }

            // Error registering a new kudo entry / updating an existing kudo entry at the backend
            if (kudoUpdateSuccessful)
                return new APIGatewayProxyResponse<>(NO_CONTENT); // On valid kudo update return code 204 - No content
            else
                return new APIGatewayProxyResponse<>(INTERNAR_SERVER_ERROR,
                        new APIErrorBody("Ocurrió un error al crear o actualizar una entrada kudo"));
        } else { // Couldn't find the route at the backend
            return new APIGatewayProxyResponse<>(NOT_FOUND, new APIErrorBody("No se encuentra la ruta solicitada"));
        }

    }

    // PUT /rutas/{idRuta} { Body: Route JSON }
    private APIGatewayProxyResponse<?> saveRoute(APIGatewayProxyRequestEvent event, Context context) {

        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        Route uploadedRoute = gson.fromJson(event.getBody(), Route.class);
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        // Validate the route ID

        ArrayList<String> validationMessages = new ArrayList<>();
        if (!Route.validateID(routeId))
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("ID de ruta inválido"));

        // Validate the uploaded route

        if (uploadedRoute == null || !uploadedRoute.validateRouteEditionAttempt(validationMessages))
            return new APIGatewayProxyResponse<>(BAD_REQUEST,
                    new APIErrorBody("La ruta proporcionada es inválida" + validationMessages.toString()));

        // Check that the ID of the URI matches with the ID of the uploaded route

        if (routeId != uploadedRoute.getId()) {
            return new APIGatewayProxyResponse<>(FORBIDDEN,
                    new APIErrorBody("La URI solicitada y el ID de la ruta proporcionado no coinciden"));
        }

        Route storedRoute = routeDAO.getById(routeId);

        // Check if the route could be found at the backend

        if (storedRoute != null) {

            // AUTHORISATION FILTER. Only the author of the route can update it

            if (cognitoUser.equals(storedRoute.getCreatedByUser())) {

                // Try updating the requested route

                boolean updateSuccessful = routeDAO.save(uploadedRoute);

                // An error occurred while updating the requested route
                if (updateSuccessful)
                    return new APIGatewayProxyResponse<>(NO_CONTENT); // Return code 204 - No content
                else
                    return new APIGatewayProxyResponse<>(INTERNAR_SERVER_ERROR,
                            new APIErrorBody("Ocurrió un error al actualizar los datos de la ruta solicitada"));
            } else { // Insufficient privileges
                return new APIGatewayProxyResponse<>(UNAUTHORIZED,
                        new APIErrorBody("Este usuario no tiene permisos para editar la ruta solicitada"));
            }
        } else { // Route not found at the backend
            return new APIGatewayProxyResponse<>(NOT_FOUND, new APIErrorBody("No se encontró la ruta solicitada"));
        }
    }

    // DETELE /rutas/{idRuta}
    private APIGatewayProxyResponse<?> deleteRoute(APIGatewayProxyRequestEvent event, Context context) {
        long routeId = Long.parseLong(event.getPathParameters().get("proxy").split("/")[0]);
        String cognitoUser = ((Map<String, String>) event.getRequestContext().getAuthorizer().get("claims"))
                .get("cognito:username");

        // Validate the route ID

        if (!Route.validateID(routeId))
            return new APIGatewayProxyResponse<>(BAD_REQUEST, new APIErrorBody("ID de ruta inválido"));

        Route routeBeingDeleted = routeDAO.getById(routeId);

        // Check if the route could be found at the backend

        if (routeBeingDeleted != null) {

            // AUTHORISATION FILTER. Only the author of the route can delete it

            if (cognitoUser.equals(routeBeingDeleted.getCreatedByUser())) {

                // Try deleting the requested route

                boolean deletionSuccessful = routeDAO.deleteById(routeId);

                if (deletionSuccessful) {
                    return new APIGatewayProxyResponse<>(NO_CONTENT); // Return code 204 - No content
                } else { // An error occurred while deleting the requested route
                    return new APIGatewayProxyResponse<>(INTERNAR_SERVER_ERROR,
                            new APIErrorBody("Ocurrió un error al eliminar la ruta solicitada"));
                }
            } else { // Insufficient privileges
                return new APIGatewayProxyResponse<>(UNAUTHORIZED,
                        new APIErrorBody("Este usuario no tiene permisos para eliminar la ruta seleccionada"));
            }
        } else { // Route not found
            return new APIGatewayProxyResponse<>(NOT_FOUND, "No se encontró la ruta solicitada");
        }
    }

}

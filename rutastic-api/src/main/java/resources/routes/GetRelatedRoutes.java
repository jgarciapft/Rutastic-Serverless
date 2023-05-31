package resources.routes;

import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import dao.implementations.RouteDAOImplJDBC;
import model.Route;
import resources.model.RequestParameter;
import resources.routes.model.GetRelatedRoutesRequest;
import resources.routes.model.GetRelatedRoutesResponse;
import routefilter.RouteSkillLevel;
import routefilter.SQLRouteFilterBuilder;

public class GetRelatedRoutes {

    public static GetRelatedRoutesResponse run(GetRelatedRoutesRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        long routeId = request.getRouteId();
        String similarity = request.getSimilarity().get();
        RequestParameter<Integer> limitParam = request.getLimit();
        RequestParameter<Integer> distanceDeltaParam = request.getDistanceDelta();

        GetRelatedRoutesResponse response = new GetRelatedRoutesResponse();

        // REQUEST VALIDATION

        if (!similarity.matches("distancia|dificultad|categorias")) {
            response.flag(
                    GetRelatedRoutesResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                    "Parámetro (similitud) no es válido");
            return response;
        }

        if (similarity.matches("distancia") && distanceDeltaParam == null) {
            response.flag(
                    GetRelatedRoutesResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                    "Parámetro (distanciaDelta) no está presente");
            return response;
        }

        if (limitParam != null) {
            int limit = limitParam.get();
            if (!(limit >= 0)) {
                response.flag(
                        GetRelatedRoutesResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                        "Parámetro (límite) no es un entero positivo");
                return response;
            }
        }

        if (distanceDeltaParam != null) {
            int deltaDistance = distanceDeltaParam.get();
            if (!(deltaDistance > 0)) {
                response.flag(
                        GetRelatedRoutesResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                        "Parámetro (distanciaDelta) no es un entero positivo");
                return response;
            }
        }

        // Check that the requested route exists

        Route requestedRoute = routeDAO.getById(routeId);
        if (requestedRoute == null) {
            response.flag(GetRelatedRoutesResponse.Flags.ERROR_NO_ROUTE_FOUND);
            return response;
        }

        switch (similarity) {
            case "distancia":  // Similar routes by similar distance
                return getRelatedRoutesByDistance(request, requestedRoute);
            case "dificultad":  // Similar routes by same skill level
                return getRelatedRoutesByDifficulty(request, requestedRoute);
            case "categorias":  // Similar routes by same set of categories
                return getRelatedRoutesByCategory(request, requestedRoute);
            default:  // Unhandled similarities
                response.flag(GetRelatedRoutesResponse.Flags.ERROR_UNKNOWN_SIMILARITY, "Característica de similitud inválida");
                return response;
        }
    }

    private static GetRelatedRoutesResponse getRelatedRoutesByDistance(GetRelatedRoutesRequest request, Route requestedRoute) {
        RouteDAOImplJDBC routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);
        SQLRouteFilterBuilder sqlRouteFilterBuilder = new SQLRouteFilterBuilder();

        int distanceDelta = request.getDistanceDelta().get();
        RequestParameter<Integer> limitParam = request.getLimit();

        // 3 Related routes by distance within a range given a distance delta (with more kudos)

        sqlRouteFilterBuilder
                .ofDistanceDelta(requestedRoute.getDistance(), distanceDelta)
                .orderByKudos(true)
                .exclude(request.getRouteId()); // Exclude self

        // If limit is 0 then not set
        if (limitParam != null && limitParam.get() > 0)
            sqlRouteFilterBuilder.limit(limitParam.get());

        return new GetRelatedRoutesResponse(routeDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));
    }

    private static GetRelatedRoutesResponse getRelatedRoutesByDifficulty(GetRelatedRoutesRequest request, Route requestedRoute) {
        RouteDAOImplJDBC routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);
        SQLRouteFilterBuilder sqlRouteFilterBuilder = new SQLRouteFilterBuilder();

        RequestParameter<Integer> limitParam = request.getLimit();

        // 3 Related routes with the same skill level (with more kudos)

        sqlRouteFilterBuilder
                .ofSkillLevel(RouteSkillLevel.parseSkillLevelFromString(requestedRoute.getSkillLevel()))
                .orderByKudos(true)
                .exclude(request.getRouteId()); // Exclude self

        // If limit is 0 then not set
        if (limitParam != null && limitParam.get() > 0)
            sqlRouteFilterBuilder.limit(limitParam.get());

        return new GetRelatedRoutesResponse(routeDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));
    }

    private static GetRelatedRoutesResponse getRelatedRoutesByCategory(GetRelatedRoutesRequest request, Route requestedRoute) {
        RouteDAOImplJDBC routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);
        SQLRouteFilterBuilder sqlRouteFilterBuilder = new SQLRouteFilterBuilder();

        RequestParameter<Integer> limitParam = request.getLimit();

        // 3 Related routes with shared route categories (with more kudos)

        sqlRouteFilterBuilder
                .ofCategories(requestedRoute.getCategories().split(Route.CATEGORY_SEPARATOR))
                .orderByKudos(true)
                .exclude(request.getRouteId()); // Exclude self

        // If limit is 0 then not set
        if (limitParam != null && limitParam.get() > 0)
            sqlRouteFilterBuilder.limit(limitParam.get());

        return new GetRelatedRoutesResponse(routeDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));
    }
}

package resources.routes;

import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.Route;
import resources.routes.model.GetRouteRequest;
import resources.routes.model.GetRouteResponse;

public class GetRoute {

    public static GetRouteResponse run(GetRouteRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        long requestedRouteId = request.getRequestedRouteId();

        Route requestedRoute = routeDAO.getById(requestedRouteId);

        return new GetRouteResponse(requestedRoute);
    }

}

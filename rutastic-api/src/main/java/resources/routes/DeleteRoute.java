package resources.routes;

import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.Route;
import model.validators.RouteValidation;
import resources.routes.model.DeleteRouteRequest;
import resources.routes.model.DeleteRouteResponse;

public class DeleteRoute {
    public static DeleteRouteResponse run(DeleteRouteRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        long routeId = request.getRouteId();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        DeleteRouteResponse response = new DeleteRouteResponse();

        // Validate the route ID

        if (!RouteValidation.routeIdIsValid((routeId))) {
            response.flag(DeleteRouteResponse.Flags.ERROR_INVALID_ROUTE_ID);
            return response;
        }

        // Check if the route could be found at the backend

        Route routeBeingDeleted = routeDAO.getById(routeId);

        if (routeBeingDeleted == null) {
            response.flag(DeleteRouteResponse.Flags.ERROR_ROUTE_NOT_FOUND, "La ruta solicitada no existe");
            return response;
        }

        // AUTHORISATION FILTER. Only the author of the route can delete it

        if (!userClaimedUsername.equals(routeBeingDeleted.getCreatedByUser())) {
            response.flag(DeleteRouteResponse.Flags.ERROR_UNAUTHORIZED);
            return response;
        }

        // Try deleting the requested route

        boolean deletionSuccessful = routeDAO.deleteById(routeId);

        if (!deletionSuccessful)
            response.flag(DeleteRouteResponse.Flags.ERROR_UNEXPECTED,
                    "Ocurrió un error al eliminar la ruta solicitada");

        return response;
    }
}

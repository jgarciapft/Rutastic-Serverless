package resources.routes;

import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.Route;
import model.validators.RouteValidation;
import resources.routes.model.ChangeBlockedStateRequest;
import resources.routes.model.ChangeBlockedStateResponse;

public class ChangeBlockedState {

    public static ChangeBlockedStateResponse run(ChangeBlockedStateRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        long requestedRouteId = request.getRequestedRouteId();
        String action = request.getAction();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        ChangeBlockedStateResponse response = new ChangeBlockedStateResponse();

        // REQUEST VALIDATION

        if (!RouteValidation.routeIdIsValid((requestedRouteId))) {
            response.flag(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ROUTE_ID, "ID de ruta inválido");
            return response;
        }

        // Validate the requested action. It can either be a request to block or unblock a route

        if (!action.matches("bloquear|desbloquear")) {
            response.flag(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ACTION_PARAM, "Acción inválida");
            return response;
        }

        // Check if a route can be retrieved with the requested ID

        Route requestedRoute = routeDAO.getById(requestedRouteId);

        if (requestedRoute == null) {
            response.flag(ChangeBlockedStateResponse.Flags.ERROR_ROUTE_NOT_FOUND, "No se encuentra la ruta solicitada");
            return response;
        }

        // AUTHORISATION FILER. Only the author of the route can block or unblock it

        if (!userClaimedUsername.equals(requestedRoute.getCreatedByUser())) {
            response.flag(
                    ChangeBlockedStateResponse.Flags.ERROR_UNAUTHORIZED,
                    "Este usuario no tiene permiso para modificar el estado de bloqueo de esta ruta");
            return response;
        }

        /*
         * Check the given action against the current route status. If the route is blocked and the
         * action tells to unblock it, or the route is unblocked and the action tells to block it, it
         * is considered a valid action.
         */
        boolean validAction = false;

        if (action.equals("bloquear") && !requestedRoute.isBlocked()) {
            validAction = true;
            requestedRoute.setBlocked(true);
        }
        if (action.equals("desbloquear") && requestedRoute.isBlocked()) {
            validAction = true;
            requestedRoute.setBlocked(false);
        }

        // Check action validity before committing to the execution of an action

        if (validAction) {
            boolean success = routeDAO.save(requestedRoute); // Try executing the requested action

            if (!success)
                response.flag(
                        ChangeBlockedStateResponse.Flags.ERROR_UNEXPECTED,
                        "Ocurrió un error al actualizar el estado de bloqueo de la ruta");

        } else {
            response.flag(ChangeBlockedStateResponse.Flags.ERROR_INVALID_ACTION, "Acción inválida");
        }

        return response;
    }
}

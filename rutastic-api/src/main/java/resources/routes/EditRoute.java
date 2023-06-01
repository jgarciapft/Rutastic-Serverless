package resources.routes;

import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.Route;
import model.validators.RouteValidation;
import resources.routes.model.EditRouteRequest;
import resources.routes.model.EditRouteResponse;

import java.util.ArrayList;

public class EditRoute {

    public static EditRouteResponse run(EditRouteRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        long routeId = request.getRouteId();
        Route modifiedRoute = request.getModifiedRoute();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        EditRouteResponse response = new EditRouteResponse();

        // Validate the route ID

        if (!RouteValidation.routeIdIsValid((routeId))) {
            response.flag(EditRouteResponse.Flags.ERROR_INVALID_ROUTE_ID, "ID de ruta inválido");
            return response;
        }

        // Validate the uploaded route

        ArrayList<String> validationMessages = new ArrayList<>();
        if (!RouteValidation.validateRouteEditionAttempt(modifiedRoute, validationMessages)) {
            response.flag(
                    EditRouteResponse.Flags.ERROR_INVALID_EDIT,
                    String.format("La ruta proporcionada es inválida%s", validationMessages));
            return response;
        }

        // Check that the ID of the URI matches with the ID of the uploaded route

        if (routeId != modifiedRoute.getId()) {
            response.flag(EditRouteResponse.Flags.ERROR_NON_MATCHING_ROUTE,
                    "La URI solicitada y el ID de la ruta proporcionado no coinciden");
            return response;
        }

        Route storedRoute = routeDAO.getById(routeId);

        // Check if the route could be found at the backend

        if (storedRoute == null) {
            response.flag(EditRouteResponse.Flags.ERROR_ROUTE_NOT_FOUND, "No se encontró la ruta solicitada");
            return response;
        }

        // AUTHORISATION FILTER. Only the author of the route can update it

        if (!userClaimedUsername.equals(storedRoute.getCreatedByUser())) {
            response.flag(EditRouteResponse.Flags.ERROR_UNAUTHORIZED,
                    "Este usuario no tiene permisos para editar la ruta solicitada");
            return response;
        }

        // Try updating the requested route

        boolean updateSuccessful = routeDAO.save(modifiedRoute);

        // An error occurred while updating the requested route
        if (!updateSuccessful)
            response.flag(EditRouteResponse.Flags.ERROR_UNEXPECTED,
                    "Ocurrió un error al actualizar los datos de la ruta solicitada");

        return response;
    }
}

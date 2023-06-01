package resources.routes;

import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import helper.DateTimeUtils;
import model.Route;
import model.validators.RouteValidation;
import resources.routes.model.CreateRouteRequest;
import resources.routes.model.CreateRouteResponse;

import java.time.Instant;
import java.util.ArrayList;

public class CreateRoute {

    public static CreateRouteResponse run(CreateRouteRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        Route newRoute = request.getRoute();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        CreateRouteResponse response = new CreateRouteResponse();

        // Validate the route

        ArrayList<String> validationMessages = new ArrayList<>();
        if (!RouteValidation.validateFormFields(newRoute, validationMessages)) {
            response.flag(
                    CreateRouteResponse.Flags.ERROR_INVALID_ROUTE,
                    String.format("La ruta proporcionada es inválida\n%s", validationMessages));
            return response;
        }


        // Fill default values where needed

        if (newRoute.getSkillLevel() == null || newRoute.getSkillLevel().isEmpty())
            newRoute.setSkillLevel(Route.DEFAULT_SKILL_LEVEL);

        newRoute.setBlocked(Route.DEFAULT_BLOCKED_STATE);

        // Set the author of this route
        newRoute.setCreatedByUser(userClaimedUsername);

        // Set creation date
        newRoute.setCreationDate(DateTimeUtils.formatISO8601(Instant.now()));

        // Try creating the new route

        long newRouteID = routeDAO.add(newRoute)[0];

        if (newRouteID != -1) {
            // New route created successfully
            return new CreateRouteResponse(newRouteID);
        } else { // An error occurred while creating the new route
            response.flag(CreateRouteResponse.Flags.ERROR_SAVING_ROUTE, "Ocurrió un error al crear la ruta proporcionada");
            return response;
        }
    }
}

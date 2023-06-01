package resources.routes;

import dao.KudoEntryDAO;
import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import helper.DateTimeUtils;
import model.KudoEntry;
import model.Route;
import model.validators.RouteValidation;
import resources.routes.model.ChangeKudosRequest;
import resources.routes.model.ChangeKudosResponse;

import java.time.Instant;

public class ChangeKudos {

    public static ChangeKudosResponse run(ChangeKudosRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);
        KudoEntryDAO kudoEntryDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(KudoEntry.class);

        long routeId = request.getRequestedRouteId();
        String action = request.getAction();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        ChangeKudosResponse response = new ChangeKudosResponse();

        // REQUEST VALIDATION

        // Validate the route ID

        if (!RouteValidation.routeIdIsValid((routeId))) {
            response.flag(ChangeKudosResponse.Flags.ERROR_INVALID_ROUTE_ID, "ID de ruta inválido");
            return response;
        }

        // Validate the requested action. It can either be a request to give or take the logged user's kudo given to the requested route

        if (!action.matches("dar|quitar")) {
            response.flag(ChangeKudosResponse.Flags.ERROR_INVALID_ACTION_PARAM, "Acción inválida");
            return response;
        }

        // Check if a route can be retrieved with the requested ID

        Route requestedRoute = routeDAO.getById(routeId);

        if (requestedRoute == null) {
            response.flag(ChangeKudosResponse.Flags.ERROR_ROUTE_NOT_FOUND, "No se encuentra la ruta solicitada");
            return response;
        }

        // KUDO UPDATE

        KudoEntry matchingKudoEntry = kudoEntryDAO.getByPKey(userClaimedUsername, routeId);
        boolean kudoUpdateSuccessful = false;

        int equivalentKudoModifier = action.equals("dar") ? 1 : -1; // Equivalent kudo modifier for the requested action

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
                kudoUpdateSuccessful = kudoEntryDAO.deleteByPKey(true, userClaimedUsername, routeId);
            } else if (matchingKudoEntry.getModifier() == 1 && equivalentKudoModifier == -1) { // 2.
                matchingKudoEntry.setModifier(-1);
                kudoUpdateSuccessful = kudoEntryDAO.save(matchingKudoEntry);
            } else if (matchingKudoEntry.getModifier() == -1 && equivalentKudoModifier == 1) { // 3.
                matchingKudoEntry.setModifier(1);
                kudoUpdateSuccessful = kudoEntryDAO.save(matchingKudoEntry);
            }

        } else { // User never gave any kudo to this route, create a new entry
            KudoEntry newKudoEntry = new KudoEntry();

            newKudoEntry.setUser(userClaimedUsername);
            newKudoEntry.setRoute(routeId);
            newKudoEntry.setModifier(equivalentKudoModifier);
            newKudoEntry.setSubmissionDate(DateTimeUtils.formatISO8601(Instant.now()));

            kudoUpdateSuccessful = kudoEntryDAO.add2(newKudoEntry, true)[0] instanceof String;
        }

        // Error registering a new kudo entry / updating an existing kudo entry at the backend
        if (!kudoUpdateSuccessful)
            response.flag(
                    ChangeKudosResponse.Flags.ERROR_UNEXPECTED,
                    "Ocurrió un error al crear o actualizar una entrada kudo");

        return response;
    }
}

package resources.kudoEntries;

import dao.KudoEntryDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.KudoEntry;
import resources.kudoEntries.model.GetUserKudoEntriesForRouteRequest;
import resources.kudoEntries.model.GetUserKudoEntriesForRouteResponse;

public class GetUserKudoEntriesForRoute {

    public static GetUserKudoEntriesForRouteResponse run(GetUserKudoEntriesForRouteRequest request) {
        KudoEntryDAO kudoEntryDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(KudoEntry.class);

        String requestedUsername = request.getRequestedUsername();
        long requestedRouteId = request.getRequestedRouteId();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        // AUTHORISATION FILTER. The logged user can only retrieve his kudo entries

        if (userClaimedUsername.equals(requestedUsername)) {
            // Return the kudo entry by the requested user to the requested route
            return new GetUserKudoEntriesForRouteResponse(kudoEntryDAO.getByPKey(requestedUsername, requestedRouteId));
        } else { // Insufficient privileges
            GetUserKudoEntriesForRouteResponse response = new GetUserKudoEntriesForRouteResponse();
            response.flag(GetUserKudoEntriesForRouteResponse.Flags.ERROR_UNAUTHORIZED,
                    "Su usuario no tiene permisos para recuperar las entradas kudo del usuario solicitado");
            return response;
        }
    }
}

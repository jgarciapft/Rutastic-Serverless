package resources.kudoEntries;

import dao.KudoEntryDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.KudoEntry;
import resources.kudoEntries.model.GetUserKudoEntriesRequest;
import resources.kudoEntries.model.GetUserKudoEntriesResponse;

public class GetUserKudoEntries {

    public static GetUserKudoEntriesResponse run(GetUserKudoEntriesRequest request) {
        KudoEntryDAO kudoEntryDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(KudoEntry.class);

        String requestedUsername = request.getRequestedUsername();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        // AUTHORISATION FILTER. The logged user can only retrieve his kudo entries

        if (userClaimedUsername.equals(requestedUsername)) {
            // Return the collection of kudo entries for the requested user
            return new GetUserKudoEntriesResponse(kudoEntryDAO.getAllByUser(requestedUsername));
        } else { // Insufficient privileges
            GetUserKudoEntriesResponse response = new GetUserKudoEntriesResponse();
            response.flag(
                    GetUserKudoEntriesResponse.Flags.ERROR_UNAUTHORIZED,
                    "Su usario no tiene permisos para recuperar las entradas kudo del usuario solicitado");
            return response;
        }
    }
}

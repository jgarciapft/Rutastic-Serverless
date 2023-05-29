package resources.users;

import dao.UserDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.User;
import resources.users.model.DeleteUserRequest;
import resources.users.model.DeleteUserResponse;

public class DeleteUser {

    public static DeleteUserResponse run(DeleteUserRequest request) {
        UserDAO userDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(User.class);

        String requestedUsernameForDeletion = request.getRequestedUsernameForDeletion();
        String userClaimedUsername = request.getUserClaimedIdentity().getUsername();

        DeleteUserResponse response = new DeleteUserResponse();

        // Check if the user could be found at the backend

        User registeredUser = userDAO.getByUsername(requestedUsernameForDeletion);

        if (registeredUser != null) {

            // AUTHORISATION FILTER. The logged user is the only one who can delete his profile

            if (userClaimedUsername.equals(requestedUsernameForDeletion)) {
                boolean deletionSuccessful = userDAO.deleteByUsername(true, requestedUsernameForDeletion);

                if (deletionSuccessful)
                    response.flag(DeleteUserResponse.Flags.INFO_USER_DELETED);
                else
                    response.flag(DeleteUserResponse.Flags.ERROR_USER_DELETION_UNSUCCESSFUL, "Ocurrió un error al eliminar el usuario solicitado");
            } else { // Insufficient privileges
                response.flag(DeleteUserResponse.Flags.ERROR_UNAUTHORIZED, "Este usuario no tiene permisos para eliminar el perfil solicitado");
            }
        } else { // User not found
            response.flag(DeleteUserResponse.Flags.ERROR_USER_NOT_FOUND, "No se encuentra el usuario solicitado");
        }

        return response;
    }
}

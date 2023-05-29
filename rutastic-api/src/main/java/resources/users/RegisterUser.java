package resources.users;

import dao.UserDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.User;
import resources.users.model.RegisterUserRequest;
import resources.users.model.RegisterUserResponse;
import resources.users.validators.UsernameValidators;

public class RegisterUser {

    public static RegisterUserResponse run(RegisterUserRequest request) {
        UserDAO userDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(User.class);
        User newUser = request.getNewUser();
        RegisterUserResponse response = new RegisterUserResponse();

        if (!newUserIsValid(newUser)) {
            response.flag(RegisterUserResponse.Flags.ERROR_INVALID_USER, "El usuario proporcionado no es válido");
            return response;
        }

        if (userDAO.getByUsername(newUser.getUsername()) != null) {
            response.flag(RegisterUserResponse.Flags.ERROR_USER_ALREADY_EXISTS, "El usuario proporcionado ya existe");
            return response;
        }

        long newUserID = userDAO.add(newUser)[0];

        if (newUserID != -1)
            response.flag(RegisterUserResponse.Flags.INFO_USER_REGISTERED_SUCCESSFULLY);
        else
            response.flag(RegisterUserResponse.Flags.ERROR_UNABLE_TO_REGISTER, "Ocurrió un error registrando al nuevo usuario");

        return response;
    }

    private static boolean newUserIsValid(User newUser) {
        return newUser != null && UsernameValidators.usernameIsValid(newUser.getUsername());
    }
}

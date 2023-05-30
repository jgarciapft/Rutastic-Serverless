package resources.users;

import dao.DAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.User;
import resources.users.model.GetAllUsersResponse;

import java.util.List;

public class GetAllUsers {

    public static GetAllUsersResponse run() {
        DAO<User> userDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(User.class);

        List<User> allUsers = userDAO.getAll();

        return new GetAllUsersResponse(allUsers);
    }
}

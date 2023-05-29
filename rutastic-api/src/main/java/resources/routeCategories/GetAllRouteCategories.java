package resources.routeCategories;

import dao.RouteCategoryDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.RouteCategory;
import resources.routeCategories.model.GetAllRouteCategoriesResponse;

import java.util.List;

public class GetAllRouteCategories {

    public static GetAllRouteCategoriesResponse run() {
        RouteCategoryDAO routeCategoryDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(RouteCategory.class);

        List<RouteCategory> allCategories = routeCategoryDAO.getAll();

        return new GetAllRouteCategoriesResponse(allCategories);
    }
}

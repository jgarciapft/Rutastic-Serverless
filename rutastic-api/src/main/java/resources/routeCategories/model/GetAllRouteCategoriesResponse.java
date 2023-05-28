package resources.routeCategories.model;

import model.RouteCategory;

import java.util.List;

public class GetAllRouteCategoriesResponse {

    private List<RouteCategory> routeCategoriesList;

    public GetAllRouteCategoriesResponse() {
    }

    public List<RouteCategory> getRouteCategoriesList() {
        return routeCategoriesList;
    }

    public void setRouteCategoriesList(List<RouteCategory> routeCategoriesList) {
        this.routeCategoriesList = routeCategoriesList;
    }
}

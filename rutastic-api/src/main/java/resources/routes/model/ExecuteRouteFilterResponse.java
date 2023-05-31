package resources.routes.model;

import model.Route;
import resources.model.ResponseWithFlags;

import java.util.List;

public class ExecuteRouteFilterResponse extends ResponseWithFlags<Integer> {

    private List<Route> filteredRoutesList;

    public ExecuteRouteFilterResponse() {
    }

    public ExecuteRouteFilterResponse(List<Route> filteredRoutesList) {
        this.filteredRoutesList = filteredRoutesList;
    }

    public List<Route> getFilteredRoutesList() {
        return filteredRoutesList;
    }

    public void setFilteredRoutesList(List<Route> filteredRoutesList) {
        this.filteredRoutesList = filteredRoutesList;
    }

    public static class Flags {
        public static final int ERROR_INVALID_REQUEST_PARAM = 10;
    }
}

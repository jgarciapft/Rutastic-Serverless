package resources.routes.model;

import model.Route;
import resources.model.ResponseWithFlags;

import java.util.List;

public class GetRelatedRoutesResponse extends ResponseWithFlags<Integer> {

    private List<Route> relatedRoutesList;

    public GetRelatedRoutesResponse() {
    }

    public GetRelatedRoutesResponse(List<Route> relatedRoutesList) {
        this.relatedRoutesList = relatedRoutesList;
    }

    public List<Route> getRelatedRoutesList() {
        return relatedRoutesList;
    }

    public void setRelatedRoutesList(List<Route> relatedRoutesList) {
        this.relatedRoutesList = relatedRoutesList;
    }

    public static class Flags {
        public static final Integer ERROR_INVALID_REQUEST_PARAM = 10;
        public static final Integer ERROR_NO_ROUTE_FOUND = 20;
        public static final Integer ERROR_UNKNOWN_SIMILARITY = 30;
    }
}

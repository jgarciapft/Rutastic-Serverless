package resources.routes.model;

import model.Route;
import resources.model.ResponseWithFlags;

import java.util.List;

public class GetRankedRoutesResponse extends ResponseWithFlags<Integer> {

    private List<Route> rankedRoutesList;

    public GetRankedRoutesResponse() {
    }

    public GetRankedRoutesResponse(List<Route> rankedRoutesList) {
        this.rankedRoutesList = rankedRoutesList;
    }

    public List<Route> getRankedRoutesList() {
        return rankedRoutesList;
    }

    public void setRankedRoutesList(List<Route> rankedRoutesList) {
        this.rankedRoutesList = rankedRoutesList;
    }

    public static class Flags {
        public static final int ERROR_UNKNOWN_RANK_CRITERION = 10;
    }
}

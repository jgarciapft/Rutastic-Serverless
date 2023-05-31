package resources.routes.model;

import resources.model.RequestParameter;

public class GetRelatedRoutesRequest {

    private long routeId;
    private RequestParameter<String> similarity;
    private RequestParameter<Integer> limit;
    private RequestParameter<Integer> distanceDelta;

    public GetRelatedRoutesRequest() {
    }

    public GetRelatedRoutesRequest(
            long routeId,
            RequestParameter<String> similarity,
            RequestParameter<Integer> limit,
            RequestParameter<Integer> distanceDelta
    ) {
        this.routeId = routeId;
        this.similarity = similarity;
        this.limit = limit;
        this.distanceDelta = distanceDelta;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public RequestParameter<String> getSimilarity() {
        return similarity;
    }

    public void setSimilarity(RequestParameter<String> similarity) {
        this.similarity = similarity;
    }

    public RequestParameter<Integer> getLimit() {
        return limit;
    }

    public void setLimit(RequestParameter<Integer> limit) {
        this.limit = limit;
    }

    public RequestParameter<Integer> getDistanceDelta() {
        return distanceDelta;
    }

    public void setDistanceDelta(RequestParameter<Integer> distanceDelta) {
        this.distanceDelta = distanceDelta;
    }
}

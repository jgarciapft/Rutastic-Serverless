package resources.routes.model;

import resources.model.RequestParameter;

public class ExecuteRouteFilterRequest {

    private RequestParameter<String> searchText;
    private RequestParameter<String> routeKudosOrdering;
    private RequestParameter<Integer> minimumKudos;
    private RequestParameter<Boolean> hideBlockedRoutes;
    private RequestParameter<String> showMyRoutes;
    private RequestParameter<Integer> skillLevel;
    private RequestParameter<String> filterByUsername;
    private RequestParameter<Integer> minDistance;
    private RequestParameter<Integer> maxDistance;

    public ExecuteRouteFilterRequest() {
    }

    public ExecuteRouteFilterRequest(
            RequestParameter<String> searchText,
            RequestParameter<String> routeKudosOrdering,
            RequestParameter<Integer> minimumKudos,
            RequestParameter<Boolean> hideBlockedRoutes,
            RequestParameter<String> showMyRoutes,
            RequestParameter<Integer> skillLevel,
            RequestParameter<String> filterByUsername,
            RequestParameter<Integer> minDistance,
            RequestParameter<Integer> maxDistance
    ) {
        this.searchText = searchText;
        this.routeKudosOrdering = routeKudosOrdering;
        this.minimumKudos = minimumKudos;
        this.hideBlockedRoutes = hideBlockedRoutes;
        this.showMyRoutes = showMyRoutes;
        this.skillLevel = skillLevel;
        this.filterByUsername = filterByUsername;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
    }

    public RequestParameter<String> getSearchText() {
        return searchText;
    }

    public void setSearchText(RequestParameter<String> searchText) {
        this.searchText = searchText;
    }

    public RequestParameter<String> getRouteKudosOrdering() {
        return routeKudosOrdering;
    }

    public void setRouteKudosOrdering(RequestParameter<String> routeKudosOrdering) {
        this.routeKudosOrdering = routeKudosOrdering;
    }

    public RequestParameter<Integer> getMinimumKudos() {
        return minimumKudos;
    }

    public void setMinimumKudos(RequestParameter<Integer> minimumKudos) {
        this.minimumKudos = minimumKudos;
    }

    public RequestParameter<Boolean> getHideBlockedRoutes() {
        return hideBlockedRoutes;
    }

    public void setHideBlockedRoutes(RequestParameter<Boolean> hideBlockedRoutes) {
        this.hideBlockedRoutes = hideBlockedRoutes;
    }

    public RequestParameter<String> getShowMyRoutes() {
        return showMyRoutes;
    }

    public void setShowMyRoutes(RequestParameter<String> showMyRoutes) {
        this.showMyRoutes = showMyRoutes;
    }

    public RequestParameter<Integer> getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(RequestParameter<Integer> skillLevel) {
        this.skillLevel = skillLevel;
    }

    public RequestParameter<String> getFilterByUsername() {
        return filterByUsername;
    }

    public void setFilterByUsername(RequestParameter<String> filterByUsername) {
        this.filterByUsername = filterByUsername;
    }

    public RequestParameter<Integer> getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(RequestParameter<Integer> minDistance) {
        this.minDistance = minDistance;
    }

    public RequestParameter<Integer> getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(RequestParameter<Integer> maxDistance) {
        this.maxDistance = maxDistance;
    }
}

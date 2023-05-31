package model.validators;

public class RouteValidation {

    public static final String ROUTE_ID_REGEX = "[0-9]+";

    public static boolean routeIdIsValid(String routeIdString) {
        return routeIdString != null
                && !routeIdString.trim().isEmpty()
                && routeIdString.matches(ROUTE_ID_REGEX);
    }

}

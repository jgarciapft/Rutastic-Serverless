package routefilter.validators;

public class RouteFilterValidation {

    public static boolean routeKudosOrderingIsValid(String routeKudosOrdering) {
        return routeKudosOrdering.matches("(no-ordenar|ascendentes|descendentes)");
    }

    public static boolean minimumKudosIsValid(int minimumKudos) {
        return minimumKudos >= 0;
    }

    public static boolean skillLevelIsValid(int skillLevel) {
        return skillLevel >= 0 && skillLevel <= 3;
    }

    public static boolean minDistanceIsValid(int minDistance) {
        return minDistance >= 0;
    }

    public static boolean maxDistanceIsValid(int maxDistance) {
        return maxDistance >= 0;
    }
}

package model.validators;

import model.Route;

import java.util.List;

public class RouteValidation {

    public static final String ROUTE_ID_REGEX = "[0-9]+";
    public static final String CATEGORIES_REGEX = String.format("((senderismo|carrera|ciclismo)(%s)?)+", Route.CATEGORY_SEPARATOR);

    public static boolean routeIdStringIsValid(String routeIdString) {
        return routeIdString != null
                && !routeIdString.trim().isEmpty()
                && routeIdString.matches(ROUTE_ID_REGEX);
    }

    public static boolean routeIdIsValid(long routeId) {
        return routeId > 0;
    }

    /**
     * Validate this route bean parsed from a form. The user bean is valid if the attributes set from form fields
     * are valid.
     * Any validation message is added to the list {@code validationMessages}
     *
     * @param route
     * @param validationMessages Where to log any message for the user to see
     * @return If the route bean is valid
     */
    public static boolean validateFormFields(Route route, List<String> validationMessages) {
        boolean validFields = true;

        if (route == null)
            return false;

        // Validate all route attributes from form fields

        if (route.getTitle() == null || route.getTitle().trim().isEmpty()) {
            validationMessages.add("Introduzca un t�tulo para la ruta");
            validFields = false;
        }
        if (route.getDescription() == null || route.getDescription().trim().isEmpty()) {
            validationMessages.add("Introduzca una descripci�n para la ruta");
            validFields = false;
        }
        if (route.getDistance() <= 0) {
            validationMessages.add("La distancia de la ruta debe ser un n�mero positivo de metros");
            validFields = false;
        }
        if (route.getDuration() <= 0) {
            validationMessages.add("La duraci�n de la ruta debe ser un n�mero positivo de minutos");
            validFields = false;
        }
        if (route.getElevation() <= 0) {
            validationMessages.add("La elevaci�n de la ruta debe ser un n�mero positivo de metros");
            validFields = false;
        }
        if (route.getCategories() == null || route.getCategories().trim().isEmpty()) {
            validationMessages.add("No se ha especificado ninguna categor�a para la ruta");
            validFields = false;
        }
        if (route.getCategories() == null || !route.getCategories().trim().matches(CATEGORIES_REGEX)) {
            validationMessages.add("Nombre(s) de categor�a(s) desconocido(s)");
            validFields = false;
        }
        if (route.getCategories() == null || route.getSkillLevel().trim().isEmpty()) {
            validationMessages.add("No se ha especificado ninguna dificultad para la categor�a");
            validFields = false;
        }
        if (route.getSkillLevel() == null || !route.getSkillLevel().trim().matches("((facil|media|dificil),?)+")) {
            validationMessages.add("Grado de dificultad desconocido");
            validFields = false;
        }

        return validFields;
    }

    /**
     * Validate this route bean parsed from a route edition attempt. The user bean is valid if the attributes set from
     * form fields and the ID is valid (greater than 0).
     * Any validation message is added to the list {@code validationMessages}
     *
     * @param route
     * @param validationMessages Where to log any message for the user to see
     * @return If the route bean is valid for a route edition attempt
     */
    public static boolean validateRouteEditionAttempt(Route route, List<String> validationMessages) {
        boolean validID = true;

        if (route == null)
            return false;

        // Validate route ID

        if (!routeIdIsValid(route.getId())) {
            validationMessages.add("El ID de la ruta no es v�lida");
            validID = false;
        }

        // Validate form fields

        boolean validFields = validateFormFields(route, validationMessages);

        return validID && validFields;
    }
}

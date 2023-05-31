package resources.routes;

import dao.UserDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import dao.implementations.RouteDAOImplJDBC;
import model.Route;
import model.User;
import resources.routes.model.ExecuteRouteFilterRequest;
import resources.routes.model.ExecuteRouteFilterResponse;
import resources.model.RequestParameter;
import routefilter.RouteSkillLevel;
import routefilter.SQLRouteFilterBuilder;
import routefilter.validators.RouteFilterValidation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExecuteRouteFilter {

    public static ExecuteRouteFilterResponse run(ExecuteRouteFilterRequest request) {
        UserDAO userDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(User.class);
        RouteDAOImplJDBC routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        RequestParameter<String> searchTextParam = request.getSearchText();
        RequestParameter<String> routeKudosOrderingParam = request.getRouteKudosOrdering();
        RequestParameter<Integer> minimumKudosParam = request.getMinimumKudos();
        RequestParameter<Boolean> hideBlockedRoutesParam = request.getHideBlockedRoutes();
        RequestParameter<String> showMyRoutesParam = request.getShowMyRoutes();
        RequestParameter<Integer> skillLevelParam = request.getSkillLevel();
        RequestParameter<String> filterByUsernameParam = request.getFilterByUsername();
        RequestParameter<Integer> minDistanceParam = request.getMinDistance();
        RequestParameter<Integer> maxDistanceParam = request.getMaxDistance();

        ExecuteRouteFilterResponse response = new ExecuteRouteFilterResponse();

        // REQUEST VALIDATION

        // Validate route order based on kudos

        if (routeKudosOrderingParam != null) {
            if (!RouteFilterValidation.routeKudosOrderingIsValid(routeKudosOrderingParam.get())) {
                response.flag(
                        ExecuteRouteFilterResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                        "Parámetro (ordenarPorKudos) tiene un valor inválido");
                return response;
            }
        }

        // Validate minimum kudos

        if (minimumKudosParam != null) {
            if (!RouteFilterValidation.minimumKudosIsValid(minimumKudosParam.get())) {
                response.flag(
                        ExecuteRouteFilterResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                        "Parámetro (kudosMinimos) debe tener un valor entero");
                return response;
            }
        }

        // Validate skill level

        if (skillLevelParam != null) {
            if (!RouteFilterValidation.skillLevelIsValid(skillLevelParam.get())) {
                response.flag(
                        ExecuteRouteFilterResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                        "Parámetro (filtroDificultad) tiene un valor inválido");
                return response;
            }
        }

        // Validate min route distance

        if (minDistanceParam != null) {
            if (!RouteFilterValidation.minDistanceIsValid(minDistanceParam.get())) {
                response.flag(
                        ExecuteRouteFilterResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                        "Parámetro (distanciaMinima) debe tener un valor entero");
                return response;
            }
        }

        // Validate max route distance

        if (maxDistanceParam != null) {
            if (!RouteFilterValidation.maxDistanceIsValid(maxDistanceParam.get())) {
                response.flag(
                        ExecuteRouteFilterResponse.Flags.ERROR_INVALID_REQUEST_PARAM,
                        "Parámetro (distanciaMaxima) debe tener un valor entero");
                return response;
            }
        }

        // If execution reaches this point the query is valid --> Apply all the suitable filters

        SQLRouteFilterBuilder sqlRouteFilterBuilder = new SQLRouteFilterBuilder();

        if (searchTextParam != null) {
            String searchText = searchTextParam.get();
            if (!searchText.trim().isEmpty()) {

                // Check if we're dealing with a list of keywords or a literal sentence to match

                if (searchText.contains(";")) { // Is a list of keywords
                    List<String> keywords = Arrays.stream(searchText.split(";"))
                            .map(keyword -> keyword = keyword.trim())
                            .collect(Collectors.toList());

                    sqlRouteFilterBuilder.titleOrDescriptionContains(keywords);
                } else { // Is a literal sentence
                    sqlRouteFilterBuilder.titleOrDescriptionLiterallyContains(searchText);
                }
            }
        }
        if (routeKudosOrderingParam != null) {
            String routeKudosOrdering = routeKudosOrderingParam.get();
            if (!routeKudosOrdering.matches("no-ordenar"))
                sqlRouteFilterBuilder.orderByKudos(routeKudosOrdering.matches("descendentes"));
        }
        if (minimumKudosParam != null)
            sqlRouteFilterBuilder.minimumKudos(minimumKudosParam.get());
        if (hideBlockedRoutesParam != null) {
            boolean hideBlockedRoutes = hideBlockedRoutesParam.get();
            if (hideBlockedRoutes)
                sqlRouteFilterBuilder.hideBlockedRoutes();
        }
        if (showMyRoutesParam != null) {
            String showMyRoutes = showMyRoutesParam.get();
            if (!showMyRoutes.trim().isEmpty())
                sqlRouteFilterBuilder.byUser(showMyRoutes);
        }
        if (skillLevelParam != null) {
            switch (skillLevelParam.get()) {
                case 1:
                    sqlRouteFilterBuilder.ofSkillLevel(RouteSkillLevel.EASY);
                    break;
                case 2:
                    sqlRouteFilterBuilder.ofSkillLevel(RouteSkillLevel.MEDIUM);
                    break;
                case 3:
                    sqlRouteFilterBuilder.ofSkillLevel(RouteSkillLevel.HARD);
                    break;
            }
        }
        if (filterByUsernameParam != null) {
            String filterByUsername = filterByUsernameParam.get();
            if (!filterByUsername.trim().isEmpty()) {
                User filteredUserModel = userDAO.getByUsername(filterByUsername.trim());
                if (filteredUserModel != null)
                    sqlRouteFilterBuilder.byUser(filteredUserModel.getUsername());
            }
        }
        sqlRouteFilterBuilder.ofDistanceRange(
                minDistanceParam != null ? minDistanceParam.get() : -1,
                maxDistanceParam != null ? maxDistanceParam.get() : -1);

        // Execute the filter and return the filtered routes

        return new ExecuteRouteFilterResponse(routeDAO.executeFilter(sqlRouteFilterBuilder.buildFilter()));
    }
}

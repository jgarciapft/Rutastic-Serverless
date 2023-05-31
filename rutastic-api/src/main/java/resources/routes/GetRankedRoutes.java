package resources.routes;

import dao.RouteDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.Route;
import resources.routes.model.GetRankedRoutesRequest;
import resources.routes.model.GetRankedRoutesResponse;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class GetRankedRoutes {

    public static GetRankedRoutesResponse run(GetRankedRoutesRequest request) {
        RouteDAO routeDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(Route.class);

        String requestedRankCriterion = request.getRequestedRankCriterionName();

        if (requestedRankCriterion.equals("topRutasSemanal")) { // Serve the top 5 weekly routes

            List<Route> top5RoutesOfTheWeek = routeDAO.getTopRoutesOfTheWeek();

            // Get first 5 results

            top5RoutesOfTheWeek = top5RoutesOfTheWeek
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            // Set route creation date to the format of the top route card container

            top5RoutesOfTheWeek
                    .forEach(route -> route.changeDateFormat(new SimpleDateFormat("dd MMM - HH:mm")));

            return new GetRankedRoutesResponse(top5RoutesOfTheWeek);

        } else if (requestedRankCriterion.equals("topRutasMensual")) { // Serve the top 5 monthly routes

            List<Route> top5RoutesOfTheMonth = routeDAO.getTopRoutesOfTheMonth();

            // Get first 5 results

            top5RoutesOfTheMonth = top5RoutesOfTheMonth
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            // Set route creation date to the format of the top route card container

            top5RoutesOfTheMonth
                    .forEach(route -> route.changeDateFormat(new SimpleDateFormat("dd MMM - HH:mm")));

            return new GetRankedRoutesResponse(top5RoutesOfTheMonth);

        } else { // Unknown rank criterion
            GetRankedRoutesResponse response = new GetRankedRoutesResponse();
            response.flag(GetRankedRoutesResponse.Flags.ERROR_UNKNOWN_RANK_CRITERION, "No se reconoce el parámetro (e)");
            return response;
        }
    }
}

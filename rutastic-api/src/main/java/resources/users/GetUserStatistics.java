package resources.users;

import dao.UserDAO;
import dao.factories.DAOAbstractFactory;
import dao.implementations.DAOImplJDBC;
import model.User;
import model.statistic.UserStatistic;
import resources.users.model.GetUserStatisticsRequest;
import resources.users.model.GetUserStatisticsResponse;

import java.util.List;
import java.util.stream.Collectors;

public class GetUserStatistics {

    public static GetUserStatisticsResponse run(GetUserStatisticsRequest request) {
        UserDAO userDAO = DAOAbstractFactory.get().impl(DAOImplJDBC.class).forModel(User.class);

        String requestedStat = request.getUserStatistic();

        if (requestedStat.equals("top5UsuariosPorTopRutas")) { // Serve top 5 users by top monthly routes

            List<UserStatistic> top5UsersByTopMonthlyRoutes = userDAO.getTopUsersByTopMonthlyRoutes();

            // Get first 5 results

            top5UsersByTopMonthlyRoutes = top5UsersByTopMonthlyRoutes
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            return new GetUserStatisticsResponse(top5UsersByTopMonthlyRoutes);
        }

        if (requestedStat.equals("top5UsuariosPorMediaKudos")) { // Serve top 5 users by average kudo ratings of their routes

            List<UserStatistic> top5UsersByAvgKudos = userDAO.getTopUsersByAvgKudos();

            // Get first 5 results

            top5UsersByAvgKudos = top5UsersByAvgKudos
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());

            return new GetUserStatisticsResponse(top5UsersByAvgKudos);
        }

        GetUserStatisticsResponse response = new GetUserStatisticsResponse();
        response.flag(GetUserStatisticsResponse.Flags.ERROR_UNKNOWN_STATISTIC);
        return response;
    }
}

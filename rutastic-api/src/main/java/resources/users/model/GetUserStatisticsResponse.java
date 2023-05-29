package resources.users.model;

import model.statistic.UserStatistic;
import resources.model.ResponseWithFlags;

import java.util.List;

public class GetUserStatisticsResponse extends ResponseWithFlags<Integer> {
    private List<UserStatistic> userStatistics;

    public GetUserStatisticsResponse() {
        super();
    }

    public GetUserStatisticsResponse(List<UserStatistic> userStatistics) {
        super();
        this.userStatistics = userStatistics;
    }

    public List<UserStatistic> getUserStatistics() {
        return userStatistics;
    }

    public void setUserStatistics(List<UserStatistic> userStatistics) {
        this.userStatistics = userStatistics;
    }

    public static class Flags {
        public static final int ERROR_UNKNOWN_STATISTIC = 10;
    }
}

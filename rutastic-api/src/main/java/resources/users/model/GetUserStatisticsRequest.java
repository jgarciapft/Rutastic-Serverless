package resources.users.model;

public class GetUserStatisticsRequest {

    String userStatistic;

    public GetUserStatisticsRequest() {
    }

    public GetUserStatisticsRequest(String userStatistic) {
        this.userStatistic = userStatistic;
    }

    public String getUserStatistic() {
        return userStatistic;
    }

    public void setUserStatistic(String userStatistic) {
        this.userStatistic = userStatistic;
    }
}

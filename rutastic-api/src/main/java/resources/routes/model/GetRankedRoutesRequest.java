package resources.routes.model;

public class GetRankedRoutesRequest {

    private String requestedRankCriterionName;

    public GetRankedRoutesRequest() {
    }

    public GetRankedRoutesRequest(String requestedRankCriterionName) {
        this.requestedRankCriterionName = requestedRankCriterionName;
    }

    public String getRequestedRankCriterionName() {
        return requestedRankCriterionName;
    }

    public void setRequestedRankCriterionName(String requestedRankCriterionName) {
        this.requestedRankCriterionName = requestedRankCriterionName;
    }
}

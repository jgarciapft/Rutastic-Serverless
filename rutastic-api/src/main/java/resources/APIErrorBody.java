package resources;

/**
 * POJO to use when the API has to return an error code an a reason
 */
public class APIErrorBody {

    private String reason;

    public APIErrorBody() {
    }

    public APIErrorBody(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}

package resources;

/**
 * Common HTTP status codes
 */
public class HTTPStatus {

    // Positive responses
    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    // Redirections
    public static final int NOT_MODIFIED = 304;
    // Client errors
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    // Server errors
    public static final int INTERNAR_SERVER_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;


}

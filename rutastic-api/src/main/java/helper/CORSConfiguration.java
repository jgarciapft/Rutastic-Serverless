package helper;

import java.util.Map;

public class CORSConfiguration {

    public static final String DEFAULT_ALLOWED_ORIGINS = "*";
    public static final String DEFAULT_ALLOWED_HEADERS = "Content-Type,Authorization,Auth,X-Amz-Date,X-Api-Key,X-Amz-Security-Token";

    public static Map<String, String> getCORSHeadersMap(String allowedOrigins) {
        return getCORSHeadersMap(allowedOrigins, DEFAULT_ALLOWED_HEADERS);
    }

    public static Map<String, String> getCORSHeadersMap(String allowedOrigins, String allowedHeaders) {
        allowedOrigins = allowedOrigins == null || allowedOrigins.isEmpty() ? DEFAULT_ALLOWED_ORIGINS : allowedOrigins;
        allowedHeaders = allowedHeaders == null || allowedHeaders.isEmpty() ? DEFAULT_ALLOWED_HEADERS : allowedHeaders;

        return Map.of(
                "Access-Control-Allow-Origin", String.format("%s", allowedOrigins),
                "Access-Control-Allow-Headers", String.format("%s", allowedHeaders)
        );
    }
}

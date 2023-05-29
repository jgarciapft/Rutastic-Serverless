package resources.model;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic POJO to return a well formed API Gateway Lambda proxy response
 *
 * @param <T> Body content converted from Java to JSON
 */
public class APIGatewayProxyResponse<T> {

    private static final Gson gson = new Gson();

    private boolean isBase64Encoded = false;
    private int statusCode;
    private final Map<String, String> headers;
    private final Map<String, List<String>> multiValueHeaders;
    private T body;

    public APIGatewayProxyResponse() {
        headers = new HashMap<>();
        multiValueHeaders = new HashMap<>();
    }

    public APIGatewayProxyResponse(int statusCode) {
        this.statusCode = statusCode;

        headers = new HashMap<>();
        multiValueHeaders = new HashMap<>();
    }


    public APIGatewayProxyResponse(int statusCode, T body) {
        this.statusCode = statusCode;
        this.body = body;

        headers = new HashMap<>();
        multiValueHeaders = new HashMap<>();
    }

    public APIGatewayProxyResponse(boolean isBase64Enconded, int statusCode, T body) {
        this.isBase64Encoded = isBase64Enconded;
        this.statusCode = statusCode;
        this.body = body;

        headers = new HashMap<>();
        multiValueHeaders = new HashMap<>();
    }

    public APIGatewayProxyResponse<T> addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public APIGatewayProxyResponse<T> addMultiValueHeader(String name, String... values) {
        multiValueHeaders.put(name, Arrays.asList(values));
        return this;
    }

    public APIGatewayProxyResponse<T> addCORS() {
        addHeader("Access-Control-Allow-Origin", "*");
        return this;
    }

    public boolean isIsBase64Encoded() {
        return isBase64Encoded;
    }

    public void setBase64Encoded(boolean base64Encoded) {
        isBase64Encoded = base64Encoded;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getMultiValueHeaders() {
        return multiValueHeaders;
    }

    public String getBody() {
        return gson.toJson(body);
    }

    public void setBody(T body) {
        this.body = body;
    }
}

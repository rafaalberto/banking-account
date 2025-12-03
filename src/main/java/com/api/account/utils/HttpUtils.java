package com.api.account.utils;

import com.api.account.exception.BusinessException;
import com.api.account.model.Message;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.api.account.utils.JsonConverter.convertToJson;
import static io.undertow.util.Headers.CONTENT_TYPE;

public final class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    public static final int APP_PORT = 8080;
    public static final String APP_HOST = "localhost";

    public static final int TEST_PORT = 8090;

    public static final int HTTP_OK_STATUS = 200;
    public static final int HTTP_CREATED_STATUS = 201;
    public static final int HTTP_NO_CONTENT_STATUS = 204;

    public static final int HTTP_BAD_REQUEST_STATUS = 400;
    public static final int HTTP_NOT_FOUND_STATUS = 404;

    private static final int HTTP_SERVER_ERROR = 500;

    private static final String HEADER_JSON = "application/json";

    public static String getQueryParameter(HttpServerExchange exchange) {
        // Extract path parameter from Undertow RoutingHandler
        var pathParameters = exchange.getPathParameters();
        if (pathParameters != null && pathParameters.containsKey("id")) {
            return pathParameters.get("id").getFirst();
        }
        // Fallback to query parameter if path parameter not found
        var queryParameters = exchange.getQueryParameters();
        if (queryParameters != null && queryParameters.containsKey("id")) {
            return queryParameters.get("id").getFirst();
        }
        throw new IllegalArgumentException("Parameter 'id' not found in path or query parameters");
    }

    public static void handleStatusAndHeaders(HttpServerExchange exchange, int httpCreatedStatus) {
        exchange.setStatusCode(httpCreatedStatus);
        exchange.getResponseHeaders().put(CONTENT_TYPE, HEADER_JSON);
    }

    public static void handleApplicationException(HttpServerExchange exchange, BusinessException e) {
        try {
            exchange.setStatusCode(e.getHttpStatus());
            exchange.getResponseSender().send(convertToJson(new Message(false, e.getMessage())));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public static void handleApplicationException(HttpServerExchange exchange, Exception e) {
        try {
            exchange.setStatusCode(HTTP_SERVER_ERROR);
            exchange.getResponseSender().send(convertToJson(new Message(false, "Error, please contact the support")));
            LOGGER.error(e.getMessage());
        } catch (Exception ex) {
            LOGGER.error(e.getMessage());
        }
    }
}

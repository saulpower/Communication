package com.saulpower.communication;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Android helper class to send HTTP Requests to remote servers and handle the response.
 */
public class HttpRequest {

    /** Logging tag */
    private static final String TAG = "HttpRequest";

    /** HTTP GET method */
    private static final String GET = "GET";

    /** HTTP POST method */
    private static final String POST = "POST";

    /** HTTP PUT method */
    private static final String PUT = "PUT";

    /** HTTP DELETE method */
    private static final String DELETE = "DELETE";

    /** Connection timeout amount */
    private static final int CONNECTION_TIMEOUT = 30000;

    /** Socket timeout amount */
    private static final int SOCKET_TIMEOUT = 30000;

    /**
     * Send an HTTP GET request with the supplied parameters
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to append to the URL string
     *
     * @return The response from the server
     *
     * @throws IOException When communication issues arise
     */
    public static com.saulpower.communication.HttpResponse sendGet(final String url, final HashMap<String, String> headers,
                                                                   final HashMap<String, String> params) throws IOException {
        return sendRequest(url, GET, headers, params, null, null, null);
    }

    /**
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to append to the URL string
     * @param data The body data to send
     *
     * @return The response from the server
     *
     * @throws IOException IOException When communication issues arise
     */
    public static com.saulpower.communication.HttpResponse sendPost(final String url, final HashMap<String, String> headers,
                                                                    final HashMap<String, String> params, final String data) throws IOException {
        return sendRequest(url, POST, headers, params, data, null, null);
    }

    /**
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to append to the URL string
     * @param data The body data to send
     * @param username The username to authenticate to the server
     * @param password The password to authenticate to the server
     *
     * @return The response from the server
     *
     * @throws IOException IOException When communication issues arise
     */
    public static com.saulpower.communication.HttpResponse sendPost(final String url, final HashMap<String, String> headers,
                                                                    final HashMap<String, String> params, final String data,
                                                                    final String username, final String password) throws IOException {
        return sendRequest(url, POST, headers, params, data, username, password);
    }

    /**
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to append to the URL string
     * @param data The body data to send
     *
     * @return The response from the server
     *
     * @throws IOException IOException When communication issues arise
     */
    public static com.saulpower.communication.HttpResponse sendPut(final String url, final HashMap<String, String> headers,
                                                                   final HashMap<String, String> params, final String data) throws IOException {
        return sendRequest(url, PUT, headers, params, data, null, null);
    }

    /**
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to append to the URL string
     * @param data The body data to send
     *
     * @return The response from the server
     *
     * @throws IOException IOException When communication issues arise
     */
    public static com.saulpower.communication.HttpResponse sendDelete(final String url, final HashMap<String, String> headers,
                                                                      final HashMap<String, String> params, final String data) throws IOException {
        return sendRequest(url, DELETE, headers, params, data, null, null);
    }

    /**
     *
     * @param url The url to send the request to
     * @param method The HTTP request method (i.e. GET, POST, PUT, DELETE)
     * @param headers The headers to add to the request
     * @param params The parameters to append to the URL string
     * @param data The body data to send
     * @param username The username for authentication
     * @param password The password for authentication
     *
     * @return The response from the server
     *
     * @throws IOException IOException When communication issues arise
     */
    private static com.saulpower.communication.HttpResponse sendRequest(final String url, final String method, final HashMap<String, String> headers,
                                                                        final HashMap<String, String> params, final String data,
                                                                        final String username, final String password) throws IOException {

        // Define http parameters
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);

        // Create a new HttpClient
        DefaultHttpClient httpClient = HttpClientFactory.getThreadSafeClient();
        httpClient.setParams(httpParameters);

        String queryUrl = url;

        // Add query string parameters to url
        if (params != null) {
            queryUrl += toQueryString(params);
        }

        // Create request based on method
        HttpUriRequest httpRequest = getRequest(method, queryUrl);

        if (httpRequest == null)
            throw new IllegalArgumentException("Method not supported");

        // Add headers to request
        if (headers != null) {
            for (String key : headers.keySet()) {
                if (headers.get(key) != null) {
                    httpRequest.setHeader(key, headers.get(key));
                }
            }
        }

        // Add body data
        if (data != null) {

            StringEntity se = new StringEntity(data, HTTP.UTF_8);
            ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(se);
        }

        // Execute HTTP Request
        HttpResponse response = httpClient.execute(httpRequest);

        // Return string response
        return new com.saulpower.communication.HttpResponse(response);
    }

    /**
     * Returns the appropriate request class for the supplied method
     *
     * @param method The HTTP request method (i.e. GET, POST, PUT, DELETE)
     * @param url The URL to send the request to
     *
     * @return The appropriate HttpUriRequest object to perform the request with
     */
    private static HttpUriRequest getRequest(final String method, final String url) {

        if (method.equals(GET)) {
            return new HttpGet(url);
        }

        if (method.equals(POST)) {
            return new HttpPost(url);
        }

        if (method.equals(PUT)) {
            return new HttpPut(url);
        }

        if (method.equals(DELETE)) {
            return new HttpDeleteWithBody(url);
        }

        return null;
    }

    /**
     * Append the attributes to the URL string to create the query string
     *
     * @param attributes The attributes to add to the URL string
     *
     * @return The query string
     */
    private static String toQueryString(final HashMap<String, String> attributes) {

        StringBuilder sb = new StringBuilder();
        sb.append("?");

        for (String key : attributes.keySet()) {
            if (attributes.get(key) != null) {
                sb.append(key);
                sb.append("=");
                sb.append(URLEncoder.encode(attributes.get(key)));
                sb.append("&");
            }
        }

        return sb.substring(0, sb.length() - 1);
    }
}

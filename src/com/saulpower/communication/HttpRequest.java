package com.saulpower.communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
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

/**
 * A class used to send HTTP requests to a remote server and parse the response
 */
public class HttpRequest {
	
	public static final String TAG = "HttpRequest";
    
	private static final String GET = "GET";
	private static final String POST = "POST";
	private static final String PUT = "PUT";
	private static final String DELETE = "DELETE";
	
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final int SOCKET_TIMEOUT = 30000;

    /**
     * Send a HTTP GET request to the server at the url provided with the supplied headers and parameters
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to add to the request
     * @return The response from the server as a string
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
	public static String sendGet(String url, HashMap<String, String> headers, HashMap<String, String> params) throws ClientProtocolException, IOException {
		return sendRequest(url, GET, headers, params, null);
	}

    /**
     * Send a HTTP POST request to the server at the url provided with the supplied headers, parameters, and body
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to add to the request
     * @param data The POST body
     *
     * @return The response from the server as a string
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
	public static String sendPost(String url, HashMap<String, String> headers, HashMap<String, String> params, String data) throws ClientProtocolException, IOException {
		return sendRequest(url, POST, headers, params, data);
	}

    /**
     * Send a HTTP PUT request to the server at the url provided with the supplied headers, parameters, and body
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to add to the request
     * @param data The PUT body
     *
     * @return The response from the server as a string
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
	public static String sendPut(String url, HashMap<String, String> headers, HashMap<String, String> params, String data) throws ClientProtocolException, IOException {
		return sendRequest(url, PUT, headers, params, data);
	}

    /**
     * Send a HTTP DELETE request to the server at the url provided with the supplied headers, parameters, and body
     *
     * @param url The url to send the request to
     * @param headers The headers to add to the request
     * @param params The parameters to add to the request
     * @param data The DELETE body
     *
     * @return The response from the server as a string
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
	public static String sendDelete(String url, HashMap<String, String> headers, HashMap<String, String> params, String data) throws ClientProtocolException, IOException {
		return sendRequest(url, DELETE, headers, params, data);
	}

    /**
     * Create and send the specified HTTP method request to the server url
     *
     * @param url The url to send the request to
     * @param method The HTTP method to use (GET, POST, PUT, DELETE)
     * @param headers The headers to add to the request
     * @param params The parameters to add to the request
     * @param data The request body
     *
     * @return The response from the server as a string
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
	private static String sendRequest(String url, String method, HashMap<String, String> headers, HashMap<String, String> params, String data) throws ClientProtocolException, IOException {
		
		// Define http parameters
	    HttpParams httpParameters = new BasicHttpParams();
	    HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECTION_TIMEOUT);
	    HttpConnectionParams.setSoTimeout(httpParameters, SOCKET_TIMEOUT);
	    
	    // Create a new HttpClient
	    DefaultHttpClient httpclient = HttpClientFactory.getThreadSafeClient();
	    httpclient.setParams(httpParameters);
	    
	    // Add query string parameters to url
	    if (params != null)
		    url += toQueryString(params);
	    
	    // Create request based on method
	    HttpUriRequest httpRequest = getRequest(method, url);
	    
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
	        ((HttpEntityEnclosingRequestBase)httpRequest).setEntity(se);
    	}
	    
	    HttpResponse response = null;

        // Execute HTTP Request
        response = httpclient.execute(httpRequest);

        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode() / 100;
        if (statusCode != 2) {

        	//Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase() + " URL: " + url);
        }
        
        // Return string response
	    return getResponseBody(response.getEntity());
	}

    /**
     * Extract the response body string from the http entity
     *
     * @param entity The HTTP response entity
     *
     * @return A string representation of the response body
     *
     * @throws IOException
     */
	private static String getResponseBody(final HttpEntity entity) throws IOException {
		
		try {
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        entity.writeTo(out);
	        out.close();
	        
	        return out.toString();
	        
		} catch (Exception ex) {
			return null;
		}
	}

    /**
     * Create the appropriate request based on the supplied method
     *
     * @param method the HTTP method to use
     * @param url The url to send the request to
     * @return A {@link HttpUriRequest} object to facilitate sending the request to the server
     */
	private static HttpUriRequest getRequest(String method, String url) {
		
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
     * Converts the supplied {@link HashMap} to a query string which can be appended to the request url
     *
     * @param parameters The key value pair parameters to use
     *
     * @return A querystring of the passed in parameters
     */
	private static String toQueryString(HashMap<String, String> parameters) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("?");
		
		for (String key : parameters.keySet()) {
			if (parameters.get(key) != null) {
				sb.append(key);
				sb.append("=");
				sb.append(URLEncoder.encode(parameters.get(key)));
				sb.append("&");
			}
		}
		
		return sb.substring(0, sb.length() - 1);
	}
}

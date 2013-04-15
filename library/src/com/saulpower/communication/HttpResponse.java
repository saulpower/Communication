package com.saulpower.communication;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;

/**
 * MoneyDesktop - MoneyMobile
 * <p/>
 * User: saulhoward
 * Date: 4/15/13
 * <p/>
 * Description:
 */
public class HttpResponse {

    private org.apache.http.HttpResponse mResponse;

    public HttpResponse(org.apache.http.HttpResponse response) {
        mResponse = response;
    }

    public String getBody() {
        return getResponseBody(mResponse.getEntity());
    }

    public Header[] getHeaders() {
        return mResponse.getAllHeaders();
    }

    public int getStatusCode() {
        return mResponse.getStatusLine().getStatusCode();
    }

    public org.apache.http.HttpResponse getRawResponse() {
        return mResponse;
    }
    /**
     * Returns the string body of the response to an HTTP request
     *
     * @param entity The HttpEntity containing the string response
     *
     * @return The body string
     *
     * @throws java.io.IOException
     */
    private String getResponseBody(final HttpEntity entity) {

        try {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            entity.writeTo(out);
            out.close();

            return out.toString();

        } catch (Exception ex) {
            return null;
        }
    }
}

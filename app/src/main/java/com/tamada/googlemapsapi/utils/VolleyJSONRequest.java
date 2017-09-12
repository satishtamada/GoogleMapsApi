package com.tamada.googlemapsapi.utils;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dhruv on 2/11/15.
 */
public class VolleyJSONRequest extends JsonRequest<String> {

    private String mUrl;
    private Response.Listener<String> listener;
    private Map<String, String> headers = new HashMap<>();


    public VolleyJSONRequest(int method, String url, String params, HashMap<String, String> head, Response.Listener<String> listener, Response.ErrorListener errorListener) {

        super(method, url, params, listener, errorListener);
        this.listener = listener;
        this.mUrl = url;

        if(head!=null){
            headers.putAll(head);
        }
        headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3; en-us; google_sdk Build/MR1) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        headers.put("Accept", "application/json");
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            // Volley does not handle null properly, so implement null response
            // check
            if (response.data.length == 0) {
                byte[] responseData = "{}".getBytes("UTF8");
                response = new NetworkResponse(response.statusCode,
                        responseData, response.headers, response.notModified);
            }


            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));


            return Response.success(
                    jsonString,
                    parseIgnoreCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            System.out.println("VolleyQueue: Encoding Error for " + getTag()
                    + " (" + getSequence() + ")");
            return Response.error(new ParseError(e));
        }
    }


    /**
     * Extracts a {@link Cache.Entry} from a {@link NetworkResponse}.
     * Cache-control headers are ignored. SoftTtl == 15 mins, ttl == 24 hours.
     *
     * @param response The network response to parse headers from
     * @return a cache entry for the given response, or null if the response is
     * not cacheable.
     */
    private Cache.Entry parseIgnoreCacheHeaders(NetworkResponse response) {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverEtag;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
        }
        serverEtag = headers.get("ETag");

        long cacheHitButRefreshed;
        long cacheExpired;

        cacheExpired = 0;   //change value if caching required
        cacheHitButRefreshed = 0;

        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return entry;
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }
}

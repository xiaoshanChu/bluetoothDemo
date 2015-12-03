/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seller.yhj.volley.toolbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;

import com.seller.yhj.volley.Request;
import com.seller.yhj.volley.Request.Method;

/**
 * An HttpStack that performs request over an {@link HttpClient}.
 */
public class HttpClientStack implements HttpStack {
	
    protected final HttpClient mClient;

//    private final static String HEADER_CONTENT_TYPE = "Content-Type";
    
    private HttpClientSSlSocketFactory slSocketFactory;

    public HttpClientStack(Context context){
    	mClient = new DefaultHttpClient();
    	slSocketFactory = HttpClientSSlSocketFactory.getSSlSocketFactory(context);
    }

    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    @SuppressWarnings("unused")
    private static List<NameValuePair> getPostParameterPairs(Map<String, String> postParams) {
        List<NameValuePair> result = new ArrayList<NameValuePair>(postParams.size());
        for (String key : postParams.keySet()) {
            result.add(new BasicNameValuePair(key, postParams.get(key)));
        }
        return result;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException{
        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);
        addHeaders(httpRequest, additionalHeaders);
        addHeaders(httpRequest, request.getHeaders());
        onPrepareRequest(httpRequest);
        HttpParams httpParams = httpRequest.getParams();
        int timeoutMs = request.getTimeoutMs();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeoutMs);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMs);
        URL parsedUrl = new URL(request.getUrl());
        if ("https".equals(parsedUrl.getProtocol()) && slSocketFactory != null) {
	        Scheme sch = new Scheme("https", slSocketFactory, 443); 
	        mClient.getConnectionManager().getSchemeRegistry().register(sch); 
        }
        return mClient.execute(httpRequest);
    }

    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     */
    @SuppressWarnings("deprecation")
    /* protected */ static HttpUriRequest createHttpRequest(Request<?> request,
            Map<String, String> additionalHeaders) throws IOException {
        switch (request.getMethod()) {
            case Method.DEPRECATED_GET_OR_POST: {
                // This is the deprecated way that needs to be handled for backwards compatibility.
                // If the request's post body is null, then the assumption is that the request is
                // GET.  Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    HttpPost postRequest = new HttpPost(request.getUrl());
//                    postRequest.addHeader(HEADER_CONTENT_TYPE, request.getPostBodyContentType());
                    HttpEntity entity;
                    entity = new ByteArrayEntity(postBody);
                    postRequest.setEntity(entity);
                    return postRequest;
                } else {
                    return new HttpGet(request.getUrl());
                }
            }
            case Method.GET:
                return new HttpGet(request.getUrl());
            case Method.DELETE:
                return new HttpDelete(request.getUrl());
            case Method.POST: {
                HttpPost postRequest = new HttpPost(request.getUrl());
//                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(postRequest, request);
                return postRequest;
            }
            case Method.PICTURE: {
            	HttpPost postRequest = new HttpPost(request.getUrl());
//                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
            	addBodyIfExistsPicture(postRequest, request);
            	return postRequest;
            }
            case Method.PUT: {
                HttpPut putRequest = new HttpPut(request.getUrl());
//                putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(putRequest, request);
                return putRequest;
            }
            case Method.HEAD:
                return new HttpHead(request.getUrl());
            case Method.OPTIONS:
                return new HttpOptions(request.getUrl());
            case Method.TRACE:
                return new HttpTrace(request.getUrl());
            case Method.PATCH: {
                HttpPatch patchRequest = new HttpPatch(request.getUrl());
//                patchRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(patchRequest, request);
                return patchRequest;
            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequestBase httpRequest,
            Request<?> request) throws IOException {
    	byte[] body = request.getBody();
        if (body != null) {
        	HttpEntity entity = new ByteArrayEntity(body);
        	httpRequest.setEntity(entity);
        }
        	
    }
    
    private static void addBodyIfExistsPicture(HttpEntityEnclosingRequestBase httpRequest,
    		Request<?> request) throws IOException {
    	ArrayList<byte[]> bodys = request.getPostBodyByte();
    	if(bodys != null && bodys.size() > 0){
    		String BOUNDARY = UUID.randomUUID().toString();
    		httpRequest.setHeader("Content-type", "multipart/form-data;boundary=" + BOUNDARY); 
    		String PREFIX = "--";
    		String LINE_END = "\r\n"; 
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    		for(int i = 0;i < bodys.size();i++){
    			byte[] body = bodys.get(i);
    			if (body != null) {
    				StringBuffer start = new StringBuffer();   
    				start.append(PREFIX);   
    				start.append(BOUNDARY); 
    				start.append(LINE_END);   
    				/**  
    				 * 这里重点注意：  
    				 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件  
    				 * filename是文件的名字，包含后缀名的 比如:abc.png  
    				 */   
    				start.append("Content-Disposition: form-data; name=\"img"+i+"\"; filename=\"img"+i+".png\""+LINE_END+LINE_END);
    				out.write(start.toString().getBytes("UTF-8"));
    				out.write(body);
    				out.write(LINE_END.getBytes("UTF-8"));
    			}
    		}
    		String end = PREFIX+BOUNDARY+PREFIX+LINE_END;
    		out.write(end.getBytes("UTF-8")); 
    		out.flush();
    		out.close();
    		byte[] body = out.toByteArray();
    		if (body != null) {
    			ByteArrayEntity entity = new ByteArrayEntity(body);
    			httpRequest.setEntity(entity);
    		}
    		
    	}
    }

    /**
     * Called before the request is executed using the underlying HttpClient.
     *
     * <p>Overwrite in subclasses to augment the request.</p>
     */
    protected void onPrepareRequest(HttpUriRequest request) throws IOException {
        // Nothing.
    }

    /**
     * The HttpPatch class does not exist in the Android framework, so this has been defined here.
     */
    public static final class HttpPatch extends HttpEntityEnclosingRequestBase {

        public final static String METHOD_NAME = "PATCH";

        public HttpPatch() {
            super();
        }

        public HttpPatch(final URI uri) {
            super();
            setURI(uri);
        }

        /**
         * @throws IllegalArgumentException if the uri is invalid.
         */
        public HttpPatch(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }

    }
}

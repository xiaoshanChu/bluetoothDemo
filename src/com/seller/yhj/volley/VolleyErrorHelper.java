package com.seller.yhj.volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;


//正如前面代码看到的，在创建一个请求时，需要添加一个错误监听onErrorResponse。如果请求发生异常，会返回一个VolleyError实例。  
//以下是Volley的异常列表：  
//AuthFailureError：如果在做一个HTTP的身份验证，可能会发生这个错误。  
//NetworkError：Socket关闭，服务器宕机，DNS错误都会产生这个错误。  
//NoConnectionError：和NetworkError类似，这个是客户端没有网络连接。  
//ParseError：在使用JsonObjectRequest或JsonArrayRequest时，如果接收到的JSON是畸形，会产生异常。  
//SERVERERROR：服务器的响应的一个错误，最有可能的4xx或5xx HTTP状态代码。  
//TimeoutError：Socket超时，服务器太忙或网络延迟会产生这个异常。默认情况下，Volley的超时时间为2.5秒。如果得到这个错误可以使用RetryPolicy。  

public class VolleyErrorHelper {  

  /** 
   * Returns appropriate message which is to be displayed to the user against 
   * the specified error object. 
   *  
   * @param error 
   * @param context 
   * @return 
   */  
  public static String getMessage(Object error, Context context) {  
	  if(error != null){
	      if (error instanceof TimeoutError) {  
	          return "";
	      }else if (error instanceof AuthError) {
	    	  return handleServerError(error, context);
	      } else if (isServerProblem(error)) {  
	          return handleServerError(error, context);  
	      } else if (isNetworkProblem(error)) {  
	          return ""; 
	      }  
	  }
      return "";
  }  
  
  public static String getCode(Object error, Context context) { 
	  if(error != null){
		  if (error instanceof TimeoutError) {  
			  return "";  
		  }else if (error instanceof AuthError) {
			  return handleServerErrorCode(error, context);
		  } else if (isServerProblem(error)) {  
			  return handleServerErrorCode(error, context);  
		  } else if (isNetworkProblem(error)) {  
			  return "";    
		  }  
	  }
	  return "";   
  } 
  
  private static String handleServerErrorCode(Object err, Context context) {  
	  VolleyError error = (VolleyError) err;  
	  NetworkResponse response = error.networkResponse;  
	  String msg ="";
	  if (response != null) { 
		  switch (response.statusCode) { 
		  case 402:
		  case 403:
		  case 501:  
		  case 400:  
			  if(response.data != null && response.data.length > 0){
				  String message = new String(response.data);
				  if(null != message && !"".equals(message)){
					  try {
						  JSONObject jsonObject = new JSONObject(message);
						  if(null != jsonObject){
							  if(jsonObject.has("code")){
								  return jsonObject.getString("code");
							  }
						  }
					  } catch (JSONException e) {
						  e.printStackTrace();
					  }
				  }
			  }
			  break;
		  case 422:  
			  if(response.data != null && response.data.length > 0){
				  String message = new String(response.data);
				  if(null != message && !"".equals(message)){
					  try {
						  JSONArray jsonArray = new JSONArray(message);
						  if(null != jsonArray && jsonArray.length() > 0){
							  JSONObject jsonObject = jsonArray.getJSONObject(0);
							  if(null != jsonObject){
								  if(jsonObject.has("code")){
									  return jsonObject.getString("code");
								  }
							  }
						  }
						  
					  } catch (JSONException e) {
						  e.printStackTrace();
					  }
				  }
			  }
			  break;
		  }  
	  }
	  return msg;
  }  

  /** 
   * Determines whether the error is related to network 
   *  
   * @param error 
   * @return 
   */  
  private static boolean isNetworkProblem(Object error) {  
      return (error instanceof NetworkError)  
              || (error instanceof NoConnectionError);  
  }  

  /** 
   * Determines whether the error is related to server 
   *  
   * @param error 
   * @return 
   */  
  private static boolean isServerProblem(Object error) {  
      return (error instanceof ServerError)  
              || (error instanceof AuthFailureError);  
  }  
  /**
   * 是否验证失效
   * @param error
   * @return
   */
  public static boolean isAuthProblem(Object error) {  
	  return (error instanceof AuthError);  
  }  

  /** 
   * Handles the server error, tries to determine whether to show a stock 
   * message or to show a message retrieved from the server. 
   *  
   * @param err 
   * @param context 
   * @return 
   */  
  private static String handleServerError(Object err, Context context) {  
      VolleyError error = (VolleyError) err;  
      NetworkResponse response = error.networkResponse;  
      String msg = "";
      if (response != null) { 
          switch (response.statusCode) { 
          case 422:  
        	  if(response.data != null && response.data.length > 0){
        	  		String message = new String(response.data);
        	  		if(null != message && !"".equals(message)){
        	  			try {
        	  				JSONArray jsonArray = new JSONArray(message);
        	  				if(null != jsonArray && jsonArray.length() > 0){
        	  					JSONObject jsonObject = jsonArray.getJSONObject(0);
        						if(null != jsonObject){
        							if(jsonObject.has("message")){
        								return jsonObject.getString("message");
        							}
        						}
        	  				}
    						
    					} catch (JSONException e) {
    						e.printStackTrace();
    					}
        	  		}
        	  	}
        	  break;
        	  default:
            	  if(response.data != null && response.data.length > 0){
          	  		String message = new String(response.data);
          	  		if(null != message && !"".equals(message)){
          	  			try {
      						JSONObject jsonObject = new JSONObject(message);
      						if(null != jsonObject){
      							if(jsonObject.has("message")){
      								return jsonObject.getString("message");
      							}
      						}
      					} catch (JSONException e) {
      						e.printStackTrace();
      					}
          	  		}
          	  	}
            	break;
          }  
      }
      return msg;
  }  

}  
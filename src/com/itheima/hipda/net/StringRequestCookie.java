package com.itheima.hipda.net;

import java.util.HashMap;
import java.util.Map;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.itheima.hipda.ApplicationController;

/**
 * 保存response中的cookies的volley请求
 * @author KelvinHu
 *
 */
public class StringRequestCookie extends StringRequest {

	private String mCookie;
	

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String s : response.headers.keySet()) {
            if (s.contains("Set-Cookie")) {
            	mCookie = response.headers.get(s);
            	
            	String[] cookieWhole = mCookie.split("; ");
            	
            	for (String string : cookieWhole) {
            		
            		int startIndex = string.indexOf("=");
            		if (startIndex!=-1) {
            			String key = string.substring(0, startIndex);
            			String value = string.substring(startIndex+1, string.length());
            			map.put(key, value);
            		}
				}
            	
            	System.out.println(map);
                break;
            }
        }
		
		ApplicationController.cookies = mCookie;
		return super.parseNetworkResponse(response);
	}

	public StringRequestCookie(String url, Listener<String> listener,
			ErrorListener errorListener) {
		super(url, listener, errorListener);
		
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> map = new HashMap<String, String>();
        map.put("Cookie", ApplicationController.cookies);
		return map;
	}

}

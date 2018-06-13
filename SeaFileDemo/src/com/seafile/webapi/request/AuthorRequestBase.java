package com.seafile.webapi.request;

public abstract class AuthorRequestBase extends RequestBase {
	public static String Authorization = "Authorization";
	
	public void SetAuthorization(String token){
		super.CookieParams.put(Authorization, String.format("Token %s", token));
	}
}

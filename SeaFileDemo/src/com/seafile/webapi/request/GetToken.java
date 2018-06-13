package com.seafile.webapi.request;

public class GetToken extends RequestBase {
	public static String Username = "username";
	
	public static String Password = "password";
	
	@Override
	public void SetURI() {
		// TODO Auto-generated method stub
		super.URI = "api2/auth-token/";
	}
	
	public void SetUsername(String username){
		super.MapParams.put(Username, username);
	}
	
	public void SetPassword(String pwd){
		super.MapParams.put(Password, pwd);
	}
	
}

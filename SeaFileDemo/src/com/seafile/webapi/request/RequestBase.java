package com.seafile.webapi.request;

import java.util.HashMap;
import java.util.Map;

import com.seafile.webapi.SeafileApiHelper;

public abstract class RequestBase {
	public String URI = "";
	
	public Map<String, String> MapParams = new HashMap<>();
	
	public Map<String, String> CookieParams = new HashMap<>();
	
	public abstract void SetURI();
	
	public String GetURL() 
	{
		this.SetURI();
		
		if(this.URI.startsWith("http://"))
			return this.URI.replace("192.168.125.129", "192.168.18.253");
		else
			return SeafileApiHelper.SeaFileServerAddrHeader + this.URI;
		
		
	}
}

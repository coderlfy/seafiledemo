package com.seafile.webapi.request;

public class PingSeaFileServer extends AuthorRequestBase {

	@Override
	public void SetURI() {
		// TODO Auto-generated method stub
		super.URI = "api2/auth/ping/";
	}
	
}

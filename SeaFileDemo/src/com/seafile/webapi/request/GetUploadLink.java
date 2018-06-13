package com.seafile.webapi.request;

public class GetUploadLink  extends AuthorRequestBase {

	private String reposId = "";
	
	public void SetReposId(String reposId){
		this.reposId = reposId;
	}
	
	
	@Override
	public void SetURI() {
		// TODO Auto-generated method stub
		super.URI = String.format("api2/repos/%s/upload-link/", this.reposId);
	}
}

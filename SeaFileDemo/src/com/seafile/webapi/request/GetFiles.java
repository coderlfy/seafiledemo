package com.seafile.webapi.request;

public class GetFiles extends AuthorRequestBase {
	private String libraryId = "";
	
	public void setLibraryId(String libId){
		this.libraryId = libId;
	}
	
	@Override
	public void SetURI() {
		// TODO Auto-generated method stub
		super.URI = String.format("api2/repos/%s/dir/", this.libraryId);
	}
}

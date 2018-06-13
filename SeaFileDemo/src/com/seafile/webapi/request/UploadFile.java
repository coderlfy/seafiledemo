package com.seafile.webapi.request;

import java.io.File;

public class UploadFile extends AuthorRequestBase {

	private String filePath = "";
	
	public void SetFilePath(String path){
		//File file = new File(path);
		//this.filePath = file.getName();
		super.MapParams.put("file", path);
	}
	
	private String dir = "";
	
	public void SetDir(String dir){
		this.dir = dir;
		
		super.MapParams.put("parent_dir", this.dir);
	}
	
	private String customizeURL = "";
	
	public void SetCustomizeURL(String url) {
		
		
		this.customizeURL = url.substring(1, url.length()-1);
	}
	
	
	
	@Override
	public void SetURI() {
		// TODO Auto-generated method stub
		
		super.URI = this.customizeURL;
	}
}

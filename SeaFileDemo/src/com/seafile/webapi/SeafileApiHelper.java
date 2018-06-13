package com.seafile.webapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.seafile.webapi.request.GetFiles;
import com.seafile.webapi.request.GetGroup;
import com.seafile.webapi.request.GetLibrarys;
import com.seafile.webapi.request.GetToken;
import com.seafile.webapi.request.GetUploadLink;
import com.seafile.webapi.request.PingSeaFileServer;
import com.seafile.webapi.request.RequestBase;
import com.seafile.webapi.request.UploadFile;
import com.seafile.webapi.responseentity.ResponseResult;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Okio;
import android.os.Handler;
import android.os.Message;


public class SeafileApiHelper {
	public static String SeaFileServerAddrHeader = "http://192.168.6.224:8000/";
	public static String WDLibraryName = "wd";
	public static String AdminUser = "wd@wdcloud.cc";
	public static String AdminPassword = "123456";
	private Handler mHandler = null;
	
	public SeafileApiHelper(Handler retToHandler){
		this.mHandler = retToHandler;
	}
	
	public void ApiGetToken(String username, String pwd){
		GetToken reqbase = new GetToken();
		reqbase.SetUsername(username);
		reqbase.SetPassword(pwd);
		this.requestAPI(reqbase, RequestType.Post);
	}
	
	public void ApiPingSeaFile(String token){
		PingSeaFileServer reqbase = new PingSeaFileServer();
		reqbase.SetAuthorization(token);
		this.requestAPI(reqbase, RequestType.Get);
	}
	
	public void ApiGetGroups(String token){
		GetGroup reqbase = new GetGroup();
		reqbase.SetAuthorization(token);
		this.requestAPI(reqbase, RequestType.Get);
	}
	
	public void ApiGetLibrarys(String token) {
		GetLibrarys reqbase = new GetLibrarys();
		reqbase.SetAuthorization(token);
		this.requestAPI(reqbase, RequestType.Get);
	}
	
	public void ApiGetFiles(String token, String libId) {
		GetFiles reqbase = new GetFiles();
		reqbase.SetAuthorization(token);
		reqbase.setLibraryId(libId);
		this.requestAPI(reqbase, RequestType.Get);
		
	} 
	
	public void ApiGetUploadLink(String token, String libId){
		GetUploadLink reqbase = new GetUploadLink();
		reqbase.SetAuthorization(token);
		reqbase.SetReposId(libId);
		this.requestAPI(reqbase, RequestType.Get);
	}
	
	public void ApiUploadFile(String token, String url, String filePath){
		UploadFile reqbase = new UploadFile();
		reqbase.SetAuthorization(token);
		reqbase.SetFilePath(filePath);
		reqbase.SetCustomizeURL(url);
		reqbase.SetDir("/");
		this.uploadFileAPI(reqbase);
	}
	
	private void uploadFileAPI(final RequestBase reqbase) {
	    new Thread(new Runnable() {
	        @Override
	        public void run() {
	            try {
	            	
	                OkHttpClient client = new OkHttpClient();
	                
	                okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder();
	                builder.setType(MultipartBody.FORM);
	                Iterator<Map.Entry<String, String>> entries = 
	                		reqbase.MapParams.entrySet().iterator();
	                
	                while (entries.hasNext()) {
	                    Map.Entry<String, String> entry = entries.next();
	                    String key = entry.getKey();
	                    if(!key.equals("file"))
	                    	builder.addFormDataPart(key, entry.getValue());
	                    else{
	                    	final File file = new File(entry.getValue());
	                    	builder.addFormDataPart(key, file.getName(), new RequestBody(){
	                    		public long temp = System.currentTimeMillis();
	                    		@Override
	                            public okhttp3.MediaType contentType() {
	                                return okhttp3.MediaType.parse("application/octet-stream");
	                            }
	                    		
	                    		@Override
	                            public void writeTo(okio.BufferedSink sink) throws IOException {
	                    			okio.Source source;
	                                try {
	                                	InputStream in = new FileInputStream(file);   
	                                    //source = Okio.source(file);
	                                    //okio.Buffer buf = new okio.Buffer();
	                                    // long remaining = contentLength();
	                                    long current = 0;
	                                    int count = 2048;
	                                    byte[] buf = new byte[2048];
	                                    
	                                    int readCount = 0; // 已经成功读取的字节的个数
	                                    int perreadcount = 0;
	                                    while ((perreadcount = in.read(buf, 0, 2048)) != -1) {
	                                    	readCount += perreadcount;
	                                    	
	                                    	sink.write(buf); 
	                                    	
	                                    	if(readCount == (int)file.length())
	                                    		break;
	                                    }
	                                    
	                                    
	                                    ResponseResult r = new ResponseResult();
	                	                r.EntityType = reqbase.getClass().getName();
	                	                
	                	                Message msg = new Message();
	                	                    
	                	                r.Content = String.format("read bytes count = %d", readCount);
	                	                msg.obj = new ObjectMapper().writeValueAsString(r);
	                	                mHandler.sendMessage(msg);  
	                                } catch (Exception e) {
	                                    e.printStackTrace();
	                                }
	                            }
	                            
	                    	});
	                    }
	                    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  	                  
	                }
	                
	                okhttp3.Request.Builder requestbuild = new Request.Builder();
	                
	                Iterator<Map.Entry<String, String>> cookieentries = 
	                		reqbase.CookieParams.entrySet().iterator();  
	                
	                while (cookieentries.hasNext()) {  
	                    Map.Entry<String, String> entry = cookieentries.next();  
	                    requestbuild.addHeader(entry.getKey(), entry.getValue());
	                }	                	                           
	                                
	                Request request = requestbuild.url(reqbase.GetURL())
                    .post(builder.build())
                    .build();
	                	
	                final Response response = client.newCall(request).execute();
	                
	                ObjectMapper mapper = new ObjectMapper();
	                ResponseResult r = new ResponseResult();
	                r.EntityType = reqbase.getClass().getName();
	                
	                if (response.isSuccessful()) {
	                	//需要数据传递，用下面方法；  
	                    Message msg = new Message();
	                    
	                    r.Content = response.body().string();
	                    msg.obj = mapper.writeValueAsString(r);//可以是基本类型，可以是对象，可以是List、map等；  
	                    mHandler.sendMessage(msg);  
	                    
	                //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }).start();
	}
	
	private void requestAPI(final RequestBase reqbase, final RequestType requestType) {
	    new Thread(new Runnable() {
	        @Override
	        public void run() {
	            try {
	            	
	                OkHttpClient client = new OkHttpClient();
	                
	                okhttp3.MultipartBody.Builder builder = new okhttp3.MultipartBody.Builder();
	                
	                okhttp3.Request.Builder requestbuild = new Request.Builder();
	                
	                Iterator<Map.Entry<String, String>> cookieentries = 
	                		reqbase.CookieParams.entrySet().iterator();  
	                
	                while (cookieentries.hasNext()) {  
	                    Map.Entry<String, String> entry = cookieentries.next();  
	                    requestbuild.addHeader(entry.getKey(), entry.getValue());
	                }	                
	                
	                String url = reqbase.GetURL();	                
	                
	                Request request = null;
	                
	                switch(requestType) {
	                case Post:
	                	request = requestbuild.url(url)
                        .post(getBodyBuilder(reqbase).build())
                        .build();
	                	break;
	                case Get:
	                	request = requestbuild.url(url)
                        .get()
                        .build();
	                	break;
	                
	                }                
	                
	                final Response response = client.newCall(request).execute();
	                
	                ObjectMapper mapper = new ObjectMapper();
	                ResponseResult r = new ResponseResult();
	                r.EntityType = reqbase.getClass().getName();
	                
	                if (response.isSuccessful()) {
	                	//需要数据传递，用下面方法；  
	                    Message msg = new Message();
	                    
	                    r.Content = response.body().string();
	                    msg.obj = mapper.writeValueAsString(r);//可以是基本类型，可以是对象，可以是List、map等；  
	                    mHandler.sendMessage(msg);  
	                    
	                //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }).start();
	}
	
	private FormBody.Builder getBodyBuilder(final RequestBase reqbase) {
		FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体

        Iterator<Map.Entry<String, String>> entries = 
        		reqbase.MapParams.entrySet().iterator();
        
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            formBody.add(entry.getKey(), entry.getValue());
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());  	                  
        }
        
        return formBody;
	}
	
	public enum RequestType {
		Post,
		Get
		
	}
}

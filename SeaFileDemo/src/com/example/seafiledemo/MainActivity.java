package com.example.seafiledemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.seafile.webapi.SeafileApiHelper;
import com.seafile.webapi.request.GetGroup;
import com.seafile.webapi.request.GetLibrarys;
import com.seafile.webapi.request.GetToken;
import com.seafile.webapi.request.GetFiles;
import com.seafile.webapi.request.GetUploadLink;
import com.seafile.webapi.request.PingSeaFileServer;
import com.seafile.webapi.responseentity.FileLibrary;
import com.seafile.webapi.responseentity.ResponseResult;
import com.seafile.webapi.responseentity.SFile;
import com.seafile.webapi.responseentity.Token;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private TextView mlogview = null;
	private Handler mHandler = null;
	
	private Token seafileToken = null;
	private SeafileApiHelper apihelper = null;
	private static String wdlibraryId = ""; 
	
	private void addDebug(String txt){
		mlogview.append(txt);
		int offset = mlogview.getLineCount() * mlogview.getLineHeight();
		if(offset > mlogview.getHeight()) {
			mlogview.scrollTo(0, offset - mlogview.getHeight());
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	

		mlogview = (TextView)findViewById(R.id.textView1);
		mlogview.setMovementMethod(ScrollingMovementMethod.getInstance());
		mlogview.setText("");
	
		this.mHandler = new Handler() {  
			private List<FileLibrary> librarys = null;
			private List<SFile> seafiles = null;
	        @Override  
	        public void handleMessage(Message msg) {  
	            super.handleMessage(msg);  
	            switch (msg.what) {  
	            case 0:  
	                //完成主界面更新,拿到数据  
	                String data = (String)msg.obj;  
	                
	                ObjectMapper mapper = new ObjectMapper();
	                ResponseResult response = null;
	                try {
	                	response = mapper.readValue(data, ResponseResult.class);

	                	if(response.EntityType.equals(GetToken.class.getName())) {
	                		seafileToken = mapper.readValue(response.Content, Token.class);
	                		addDebug(String.format("seafileToken = %s \n", seafileToken.token));
	                		
	                		apihelper.ApiGetLibrarys(seafileToken.token);
	                	}
	                	else if(response.EntityType.equals(PingSeaFileServer.class.getName())){
	                		addDebug(String.format("ping response = %s \n", response.Content));
	                	}
	                	else if(response.EntityType.equals(GetGroup.class.getName())){
	                		addDebug(String.format("GetGroup response = %s \n", response.Content));
	                	}	                	
	                	else if(response.EntityType.equals(GetLibrarys.class.getName())){
	                		librarys = mapper.readValue(response.Content, new TypeReference<List<FileLibrary>>(){});
	                		for(FileLibrary l : librarys){
	                			if(l.name.equals(SeafileApiHelper.WDLibraryName))
	                			{
	                				wdlibraryId = l.id;
	                				break;
	                			}
	                			//addDebug(String.format("wd response = {name: %s, owner: %s} \n", l.name, l.owner));
	                		}
	                		
	                		if(!wdlibraryId.equals("")) {
	                			addDebug(String.format("wdlibraryId = %s \n", wdlibraryId));
	                		}
	                		else{
	                			addDebug(String.format("not find libraryName = %s \n", SeafileApiHelper.WDLibraryName));
	                		}
	                		//apihelper.ApiGetFiles(seafileToken.token, librarys.get(0).id);
	                	}
	                	else if(response.EntityType.equals(GetFiles.class.getName())){
	                		seafiles = mapper.readValue(response.Content, new TypeReference<List<SFile>>(){});
	                		for(SFile l : seafiles){
	                			addDebug(String.format("GetFiles response = {name: %s, id: %s} \n", l.name, l.id));
	                		}
	                	}
	                	else if(response.EntityType.equals(GetUploadLink.class.getName())){
	                		String uploadurl = response.Content;
	                		
	                		apihelper.ApiUploadFile(seafileToken.token, uploadurl, mCurrentPhotoPath);
	                	}
	                	else {
	                		addDebug(String.format("response = %s \n", response.Content));
	                	}
	                	
	                	
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                
	                break;  
	            default:  
	                break;  
	            }  
	        }  
	  
	    };  

	    
	    apihelper = new SeafileApiHelper(this.mHandler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/*
	public void log(final String logtext)
	{
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mlogview.append(String.format("%s \n", logtext));
			}  
		});
	}
	*/
	
	public void GetLibrary(View view){
		
		//apihelper.ApiGetLibrarys(seafileToken.token);
		
		apihelper.ApiGetUploadLink(seafileToken.token, wdlibraryId);
	}
	
	public void GetFiles(View view){ 
		//apihelper.ApiGetFiles(seafileToken.token, librarys.get(0).id);
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
		
		
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            }
        }

        startActivityForResult(takePictureIntent, 1);//跳转界面传回拍照所得数据

        /*
        try {
        	Intent imageCaptureIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        	
            File ImgDir = DataManager.createTempDir();

            String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";
            takeCameraPhotoTempFile = new File(ImgDir, fileName);

            Uri photo = null;
            
            if (android.os.Build.VERSION.SDK_INT > 23) {
                photo = FileProvider.getUriForFile(
                		this, 
                		getApplicationContext().getPackageName() + ".provider", takeCameraPhotoTempFile);
            } else {
            
                photo = Uri.fromFile(takeCameraPhotoTempFile);
            }
            imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photo);
            
            startActivityForResult(imageCaptureIntent, 1);
           

        } catch (Exception e) {
            //showShortToast(BrowserActivity.this, R.string.unknow_error);
        } 
        */
	}
	
	@Override
	protected void onActivityResult(int c, int r, Intent data){
		if(r == RESULT_OK)
		{
			if(c == 1)
			{
				addDebug(String.format("capture image, c=%d", c));
				
				apihelper.ApiGetUploadLink(seafileToken.token, wdlibraryId);
			}
			
		}
		
	}
	/*
	public static boolean saveBitmap(Bitmap bmp, String desFilename, Bitmap.CompressFormat format,
            boolean recycleBitmapSavedFile, int quality) {
		try {
			if (bmp == null || bmp.isRecycled() || TextUtils.isEmpty(desFilename)) {
				return false;
			}
			File file = new File(desFilename);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
		
			if (file.exists())
				file.delete();
			file.createNewFile();
		
			FileOutputStream outStream = new FileOutputStream(file);
		
			if (format == null) {
				format = Bitmap.CompressFormat.JPEG;
			}
		
			boolean result = bmp.compress(format, quality, outStream);
			outStream.flush();
			outStream.close();
		
			if (recycleBitmapSavedFile) {
				bmp.recycle();
				bmp = null;
			}
			// 操作成功, 直接返回
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	*/
	
	private String mCurrentPhotoPath = "";
	private File createImageFile() {
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = null;
        try {
            image = File.createTempFile(
                    generateFileName(),  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

	public static String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        return imageFileName;
    }


	/*
	public static File createTempDir() throws IOException {
        String dirName = "dir-" + UUID.randomUUID();
        File dir = new File (storageManager.getTempDir(), dirName);
        if (dir.mkdir()) {
            return dir;
        } else {
            throw new IOException("Could not create temp directory");
        }
    }
	*/
	public void GetToken(View view){
		apihelper.ApiGetToken(SeafileApiHelper.AdminUser, SeafileApiHelper.AdminPassword);
	}
}

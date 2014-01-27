package com.project.make.upload;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.project.make.ListActivity;
import com.project.make.http.Client_Jsp_publish;
import com.project.make.http.Client_Jsp_reset;
import com.project.make.progress.ProgressDlgPage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.project.make.R;

public class UploadActivity extends FragmentActivity {
	
	public final static int PAGES = 15;
	// You can choose a bigger number for LOOPS, but you know, nobody will fling
	// more than 1000 times just in order to test your "infinite" ViewPager :D 
	public final static int LOOPS = 15; 
	public final static int FIRST_PAGE = 0;
	public final static float BIG_SCALE = 1.0f;
	public final static float SMALL_SCALE = 0.8f;
	public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;
	
	public MyPagerAdapter adapter;
	public ViewPager pager;
	
	String fileName="",PanType="",Title="",Name="";
	
	//**
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary = "*****"; 
	
	FileInputStream mFileInputStream =null;
	URL connectUrl=null;
	
	String urlString = "http://localhost:8080/web/application/Mobile/mobile_img_upload.jsp";
	//**
	
	ProgressDialog dialog;
	
	String imgstr[]=null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload_main); 
		
		fileName = getIntent().getStringExtra("fileName");
		PanType = getIntent().getStringExtra("PanType");
		Title = getIntent().getStringExtra("Title");
		Name = getIntent().getStringExtra("Name");
		
		//Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();

		pager = (ViewPager) findViewById(R.id.myviewpager);

		adapter = new MyPagerAdapter(this, this.getSupportFragmentManager(),fileName);
		pager.setAdapter(adapter);
		pager.setOnPageChangeListener(adapter);
		
		// Set current item to the middle page so we can fling to both
		// directions left and right
		pager.setCurrentItem(FIRST_PAGE);
		
		// Necessary or the pager will only have one extra page to show
		// make this at least however many pages you can see
		pager.setOffscreenPageLimit(3);
		
		// Set margin for pages as a negative number, so a part of next and 
		// previous pages will be showed
		pager.setPageMargin(-180);
		
		// upload http multipart
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.permitDiskReads() 
		.permitDiskWrites()
		.permitNetwork().build());
		// *****
		
		
		Button upload_btn=(Button)findViewById(R.id.upload_btn);
		Button publish_btn=(Button)findViewById(R.id.publish_btn);
		
		upload_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ResetWork(Name); //서버 temp 리셋
				
				if(Name.equals("bin")){
					Toast.makeText(getApplicationContext(),"upload 제한 회원가입 해주세요.",Toast.LENGTH_SHORT).show();
					
				}else{
					
					ProgressBar(15);//
					Toast.makeText(getApplicationContext(),"upload 시작합니다.",Toast.LENGTH_SHORT).show();
					imgstr = fileName.split("\\,");//
						
					for(int j=0; j<imgstr.length; j++){
					//	Toast.makeText(getApplicationContext(),Name+"::"+imgstr[j],Toast.LENGTH_SHORT).show(); //삭제부분
						HttpFileUpload(urlString," ",imgstr[j]);
					}
					Toast.makeText(getApplicationContext(),"upload 완료",Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		publish_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String str="";
				try{
					//http upload 부분 구현
					Client_Jsp_publish load = new Client_Jsp_publish();
					load.Setting("http://localhost:8080/web/application/Mobile/mobile_publish.jsp",Name,Title,PanType,fileName);
					load.start();
					load.join();
					str = load.getResult();
					str = str.trim();
					Log.d(UploadActivity.class.getSimpleName(), str);
					
						if("success".equals(str)){
							NextListView();
						}else{
							Toast.makeText(getApplicationContext(), "ID 값이 없습니다.", Toast.LENGTH_SHORT).show();
						}
					
				}catch(Exception e){
					Log.d(UploadActivity.class.getSimpleName(), e.toString());
				}
			}
		});
	}
	
	public void NextListView(){
		Intent i = new Intent(UploadActivity.this, ListActivity.class);
		i.putExtra("Name", Name);
		startActivity(i);
	}
	
	public void HttpFileUpload(String urlString, String params, String fileName) {
		  try {
		   
		   File fileName2 = new File(fileName);
			  
		   mFileInputStream = new FileInputStream(fileName2);   
		   connectUrl = new URL(urlString);
		   Log.d("Test", "mFileInputStream  is " + mFileInputStream);
		   
		   // open connection 
		   HttpURLConnection conn = (HttpURLConnection)connectUrl.openConnection();   
		   conn.setDoInput(true);
		   conn.setDoOutput(true);
		   conn.setUseCaches(false);
		   conn.setRequestMethod("POST");
		   conn.setRequestProperty("Cookie",Name); //추가 Cos.jar 이용 다운로드 경로 변경에 따른 헤더 추가 
		   conn.setRequestProperty("Type",PanType); //추가
		   conn.setRequestProperty("Connection", "Keep-Alive");
		   conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
		   
		   // write data
		   DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
		   
		   dos.writeBytes(twoHyphens + boundary + lineEnd);
		   dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName2+"\"" + lineEnd);
		   dos.writeBytes(lineEnd);
		   
		   int bytesAvailable = mFileInputStream.available();
		   int maxBufferSize = 1024;
		   int bufferSize = Math.min(bytesAvailable, maxBufferSize);
		   
		   byte[] buffer = new byte[bufferSize];
		   int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
		   
		   Log.d("Test", "image byte is " + bytesRead);
		   
		   // read image
		   while (bytesRead > 0) {
		    dos.write(buffer, 0, bufferSize);
		    bytesAvailable = mFileInputStream.available();
		    bufferSize = Math.min(bytesAvailable, maxBufferSize);
		    bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
		   } 
		   
		   dos.writeBytes(lineEnd);
		   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
		   
		   // close streams
		   Log.e("Test" , "File is written");
		   mFileInputStream.close();
		   dos.flush(); // finish upload...   
		   
		   // get response
		   int ch;
		   InputStream is = conn.getInputStream();
		   StringBuffer b =new StringBuffer();
		   while( ( ch = is.read() ) != -1 ){
		    b.append( (char)ch );
		   }
		   String s=b.toString(); 
		   Log.e("Test", "result = " + s);
		  // mEdityEntry.setText(s);
		   dos.close();   
		   
		  } catch (Exception e) {
		   Log.d("Test", "exception " + e.getMessage());
		   // TODO: handle exception
		  }  
		 }
	
	
	public void ProgressBar(int i){
		new ProgressDlgPage(UploadActivity.this).execute(i); //processbar;
		
	}
	
	public void ResetWork(String Name){
		
		String str="";
		try{
			
			//http upload 부분 구현
			Client_Jsp_reset load = new Client_Jsp_reset();
			//load.Setting("http://localhost:8080/web/Application/Mobile/mobile_reset.jsp",Name);
			load.start();
			load.join();
			str = load.getResult();
			str = str.trim();
			Log.d(UploadActivity.class.getSimpleName(), str);
			
		}catch(Exception e){
			Log.d(UploadActivity.class.getSimpleName(), e.toString());
		}
	}
	
}

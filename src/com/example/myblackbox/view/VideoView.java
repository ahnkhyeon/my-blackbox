package com.example.myblackbox.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GeoInfo;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.view.CameraView.LocationItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class VideoView extends MapActivity implements OnClickListener, Runnable, OnSeekBarChangeListener {
	String LOG_TAG = "mapcheck";
	public SeekBar Sbar;
	public View menubar;

	// xml 파싱 관련 변수들
	final static int GEO_DATE = 1;
	final static int GEO_LAT = 2;
	final static int GEO_LNG = 3;
	int Geo = 0;
	int parserEvent;

	// View 관련 객체들
	public MapView mapView; // 맵뷰 객체
	public List<Overlay> listOfOverlays; // 맵에 표시된 오버레이(레이어)들을 가지고 있는 리스트
	public String bestProvider; // 현재 위치값을 가져오기위한 프로바이더. (network, gps)
	public boolean barOn = false;
	public boolean MapOn = true;
	public boolean EventOn = true;
	public boolean PmapOn=true;
	public boolean pauseOn=true;
	

	// 지도 관련 변수들
	public LocationManager locM; // 위치 매니저
	public LocationListener locL; // 위치 리스너
	public Location currentLocation; // 현재 위치
	public MapController mapController; // 맵을 줌시키거나, 이동시키는데 사용될 컨트롤러
	public LocationItemizedOverlay overlayHere; // 현재위치 마커가 표시되어질 오버레이
	public int Maparr = 0;

	// 파일 관련 변수들
	// public File xmlFile;
	private static final String MediaPath = new String("/sdcard/MyBlackBox/"); // 파일 경로 지정
	private static final String XmlPath = new String("/sdcard/MyBlackBox/Data/"); // xml
																					// 파일
																					// 경로
																					// 지정
	private static final String EventPath = new String(
			"/sdcard/MyBlackBox/Event/"); // Event 파일 경로 지정

	private ArrayList<String> video = new ArrayList<String>(); // video 파일 저장할
																// 리스트
	private ArrayList<String> Event = new ArrayList<String>(); // event 파일 저장할
																// 리스트
	private ArrayList<GeoInfo> GeoList = new ArrayList<GeoInfo>(); // Geo 저장할
																	// 리스트

	PlayerMap PM = new PlayerMap();
	MediaPlayer player = null;
	SurfaceHolder holder;
	String filename;
	final Timer PlayerMapTimer = new Timer();
	
	final TimerTask PlayerTask = new TimerTask() {
		public void run() {
			if (Maparr < GeoList.size()) {
				//updateOverlay(GeoList.get(Maparr).getLatitude(),GeoList.get(Maparr).getLongitude());
				Log.e("MapOverlay", "mapping...................");
				Maparr++;
			} else if (Maparr >= GeoList.size())
				Maparr = 0;

		}
	};

	// Player 액티비티 시작
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_view);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// xmlFile = new File("/sdcard/blackbox/xml/data.xml");

		SurfaceView surface = new SurfaceView(this);
		holder = surface.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		Sbar = (SeekBar) findViewById(R.id.Seekbar);

		mapView = (MapView) findViewById(R.id.Pmapview); // 맵뷰 객체를 가져온다.
		// mapView.setBuiltInZoomControls(true); //줌인,줌아웃 컨트롤을 표시한다.

		Sbar.setOnSeekBarChangeListener(this);

		mapController = mapView.getController(); // 맵컨트롤러를 가져온다.
		mapController.setZoom(17); // 초기 확대는 17정도로..
		

		final Button playStopBtn = (Button) findViewById(R.id.playStopBtn);
		final Button MapBtn = (Button) findViewById(R.id.Mapon);
		final Button EventBtn = (Button) findViewById(R.id.eventBtn); 
		final Button FileBtn = (Button) findViewById(R.id.fileBtn);
		final Button StopBtn = (Button) findViewById(R.id.StopBtn);

		FrameLayout frame = (FrameLayout) findViewById(R.id.videoLayout);
		frame.addView(surface);

		File videofiles = new File(MediaPath);
		File Eventfiles = new File(EventPath);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, video);
		ArrayAdapter<String> EventAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Event);

		final ListView list = (ListView) findViewById(R.id.list);
		final ListView Eventlist = (ListView) findViewById(R.id.EventList);

		if (videofiles.listFiles(new Mp4Filter()).length > 0) {
			for (File file : videofiles.listFiles(new Mp4Filter())) {
				video.add(file.getName()); // mp4파일을 ArrayList에 추가
			}
			// 리스트에 adater 적용
			list.setAdapter(adapter);

		}
		if (Eventfiles.listFiles(new Mp4Filter()).length > 0) {
			for (File file : Eventfiles.listFiles(new Mp4Filter())) {
				Event.add(file.getName()); // mp4파일을 ArrayList에 추가
			}

			// 리스트에 adater 적용
			Eventlist.setAdapter(EventAdapter);

		}
		playStopBtn.setVisibility(View.GONE);
		StopBtn.setVisibility(View.GONE);
		MapBtn.setVisibility(View.GONE);
		EventBtn.setVisibility(View.VISIBLE);
		EventBtn.setEnabled(true);		
		FileBtn.setVisibility(View.VISIBLE);
		FileBtn.setEnabled(false);
		list.setVisibility(View.VISIBLE);
		Eventlist.setVisibility(View.GONE);
		Sbar.setVisibility(View.GONE);
		mapView.setVisibility(View.GONE);

		// View 클릭시 메뉴바들 사라지게 함
		frame.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				if (!barOn) {
					Sbar.setVisibility(View.GONE);
					StopBtn.setVisibility(View.GONE);
					playStopBtn.setVisibility(View.GONE);

					barOn = true;
				} else {
					Sbar.setVisibility(View.VISIBLE);
					StopBtn.setVisibility(View.VISIBLE);
					playStopBtn.setVisibility(View.VISIBLE);
					barOn = false;
				}

			}
		});

		// 클릭 이벤트 설정
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 파일명 클릭시 토스트
				//GeoList.clear();
				StringTokenizer ST = new StringTokenizer(video.get(position),
						".");

				filename = GlobalVar.VIDEO_PATH + "/" + video.get(position);
				//Log.e("name", ST.nextToken());
				// Parsing(XmlPath+"data.xml");
				Parsing(GlobalVar.DATA_PATH + "/" + ST.nextToken() + ".xml");
				Toast.makeText(VideoView.this, filename, Toast.LENGTH_SHORT)
						.show();
				if (player == null) {
					player = new MediaPlayer();
				}

				try {
					Eventlist.setVisibility(View.GONE);
					list.setVisibility(View.GONE);
					EventBtn.setVisibility(View.GONE);
					FileBtn.setVisibility(View.GONE);
					
					playStopBtn.setVisibility(View.VISIBLE);
					StopBtn.setVisibility(View.VISIBLE);
					mapView.setVisibility(View.VISIBLE);
					MapBtn.setVisibility(View.VISIBLE);					
					Sbar.setVisibility(View.VISIBLE);

					player.setDataSource(filename);
					player.setDisplay(holder);
					player.prepare();
					// run 메소드를 호출하기 위해서 자기를 쓰레드로 선언
					// Sbar.setVisibility(PorgressBar.VISIBLE);

					Sbar.setProgress(0);
					Sbar.setMax(player.getDuration());
					new Thread(VideoView.this).start();
					player.start();

					list.setVisibility(View.GONE);
					Eventlist.setVisibility(View.GONE);
					// 2초 마다 업데이트
					SetMap();
					PmapOn=true;
					//PM.start();
					//PlayerMapTimer.schedule(PlayerTask, 0, 3000);

				} catch (Exception e) {
					// Log.e(TAG, "Video play failed.", e);
				}

			}
		});

		// 클릭 이벤트 설정
		Eventlist.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//GeoList.clear();
				StringTokenizer ST = new StringTokenizer(Event.get(position),
						".");

				filename = GlobalVar.EVENT_PATH + "/" + Event.get(position);
				//Log.e("name", ST.nextToken());
				// Parsing(XmlPath+"data.xml");
				Parsing(GlobalVar.EVENT_DATA_PATH + "/" + ST.nextToken()
						+ ".xml");
				Toast.makeText(VideoView.this, filename, Toast.LENGTH_SHORT)
						.show();
				if (player == null) {
					player = new MediaPlayer();
				}

				try {
					list.setVisibility(View.GONE);
					Eventlist.setVisibility(View.GONE);
					EventBtn.setVisibility(View.GONE);
					FileBtn.setVisibility(View.GONE);
					
					playStopBtn.setVisibility(View.VISIBLE);
					StopBtn.setVisibility(View.VISIBLE);
					mapView.setVisibility(View.VISIBLE);
					MapBtn.setVisibility(View.VISIBLE);					
					Sbar.setVisibility(View.VISIBLE);							

					player.setDataSource(filename);
					player.setDisplay(holder);
					player.prepare();
					// run 메소드를 호출하기 위해서 자기를 쓰레드로 선언
					// Sbar.setVisibility(PorgressBar.VISIBLE);

					Sbar.setProgress(0);
					Sbar.setMax(player.getDuration());
					new Thread(VideoView.this).start();
					player.start();

					Eventlist.setVisibility(View.GONE);
					list.setVisibility(View.GONE);
					SetMap();
					PmapOn=true;
					// 2초 마다 업데이트
					//PM.start();
					//PlayerMapTimer.schedule(PlayerTask, 0, 3000);

				} catch (Exception e) {
					// Log.e(TAG, "Video play failed.", e);
				}

			}
		});
		
		playStopBtn.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				if(playStopBtn.getText().toString().equalsIgnoreCase("일시정지"))
				{
					
					playStopBtn.setText("재생");
					if(pauseOn)
						player.pause();
				}
				else if(playStopBtn.getText().toString().equalsIgnoreCase("재생"))
				{
					playStopBtn.setText("일시정지");
					
						player.start();
				}
			}
		});

		// 정지 및 리스트 버튼 클릭시 이벤트
		StopBtn.setOnClickListener(new View.OnClickListener() {
			RelativeLayout.LayoutParams theBtnParams;
			public void onClick(View v) {
				if (player == null)
					return;
				playStopBtn.setText("일시정지");
				player.pause();
				player.stop();
				player.reset();
				player.release();
				player = null;
				Maparr = 0;
				PlayerMapTimer.cancel();
				GeoList.clear();
				if(MapBtn.getText().toString().equalsIgnoreCase("<"))
				{
					MapBtn.setText(">");
					mapView.setVisibility(View.VISIBLE);

					theBtnParams = new RelativeLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					theBtnParams.addRule(RelativeLayout.LEFT_OF, R.id.Pmapview);

					MapBtn.setLayoutParams(theBtnParams);
				}
				
				playStopBtn.setVisibility(View.GONE);
				StopBtn.setVisibility(View.GONE);
				MapBtn.setVisibility(View.GONE);
				EventBtn.setVisibility(View.VISIBLE);
				EventBtn.setEnabled(true);		
				FileBtn.setVisibility(View.VISIBLE);
				FileBtn.setEnabled(false);
				list.setVisibility(View.VISIBLE);
				Eventlist.setVisibility(View.GONE);
				Sbar.setVisibility(View.GONE);
				mapView.setVisibility(View.GONE);

			}
		});

		// 맵 버튼 클릭시 이벤트
		MapBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				RelativeLayout.LayoutParams theBtnParams;
				if (MapBtn.getText().equals(">")) {
					MapBtn.setText("<");
					mapView.setVisibility(View.GONE);

					theBtnParams = new RelativeLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					theBtnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

					MapBtn.setLayoutParams(theBtnParams);
				} else {
					MapBtn.setText(">");
					mapView.setVisibility(View.VISIBLE);

					theBtnParams = new RelativeLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					theBtnParams.addRule(RelativeLayout.LEFT_OF, R.id.Pmapview);

					MapBtn.setLayoutParams(theBtnParams);
				}
			}
		});

		EventBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if(!FileBtn.isEnabled()){
					FileBtn.setEnabled(true);
					EventBtn.setEnabled(false);
					Eventlist.setVisibility(View.VISIBLE);
					list.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		FileBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(!EventBtn.isEnabled())
				{
					EventBtn.setEnabled(true);
					FileBtn.setEnabled(false);
					list.setVisibility(View.VISIBLE);
					Eventlist.setVisibility(View.INVISIBLE);
				
				}
			}
		}); 

	}
	public void SetMap(){
		Maparr=GeoList.size()-1;
		while(PmapOn)
		{
			if(Maparr>=0)
			{
				GeoPoint gpoint = getGeoPoint(GeoList.get(Maparr).getLatitude(),GeoList.get(Maparr).getLongitude()); // 이부분이 나중에 내 위치정보를 받아와서 위도경도 정보를 넣어주면 된다.
			     //controller 의 animateTo(); 호출하면서 위체에 대한 정보를 갖고 있는 geoPoint 객체 지정-구글맵이 이동하여 지정된 위치를 보여줍니다
				mapController.animateTo(gpoint);
				Maparr--;		  
				Bitmap image;
			     //  1) 비트맵 이미지 객체 만들기
				if(Maparr<0)
					 image = BitmapFactory.decodeResource(getResources(), R.drawable.mark);
				else 
					image = BitmapFactory.decodeResource(getResources(), R.drawable.mark2);
			    //   2) 오버레이 객체 를 만들기
			     MyOverlay myoverlay = new MyOverlay(image, gpoint);		     
			     //     3) 맵뷰에 추가하기- 오버레이가 추가되는 저장소 얻어오기
			     List<Overlay> overlayList = mapView.getOverlays();
			     overlayList.add(myoverlay);
			    // Log.e("xml",GeoList.get(0).getDate()+"/"+GeoList.get(0).getLatitude()+"/"+GeoList.get(0).getLongitude());
			    // Log.e("xml",GeoList.get(1).getDate()+"/"+GeoList.get(1).getLatitude()+"/"+GeoList.get(1).getLongitude());
			}
			else{
				Maparr=0;
				PmapOn=false;
				GeoList.clear();
			}
		}
	}
	
	private GeoPoint getGeoPoint(Double lati, Double lngi) {
			
			Double lat = lati * 1E6;
			Double lng = lngi * 1E6;
			return new GeoPoint(lat.intValue(), lng.intValue());
		}
	
	private class PlayerMap extends Thread {
		public void run() {
			while(PmapOn)
			{
				if (Maparr < GeoList.size()) {
			//		updateOverlay(GeoList.get(Maparr).getLatitude(),GeoList.get(Maparr).getLongitude());
					Log.e("MapOverlay", "mapping...................");
					Maparr++;
				} else if (Maparr >= GeoList.size()){
					Maparr = 0;
					PmapOn=false;
				}
			}
			
		}
		public void interrupt() {
			
			super.interrupt();
			
		}
	}

	// xml 파싱 함수
	public void Parsing(String xmlFile) {
		try {
			//Log.e("Parsing", "in..............");
			// xml 파일을 버터로 읽음
			FileInputStream In = new FileInputStream(xmlFile);
			BufferedReader bufferReader = new BufferedReader(
					new InputStreamReader(In));

			// xml pull 형식 파싱
			XmlPullParserFactory infoValue = XmlPullParserFactory.newInstance();
			infoValue.setNamespaceAware(true);
			XmlPullParser parser = infoValue.newPullParser(); // 이벤트를 처리해줄 객체선언
			parserEvent = parser.getEventType();
			parser.setInput(bufferReader);

			// Geo 리스트에 저장할 객체 선언
			GeoInfo GI = new GeoInfo();

			// 문서가 끝날때까지 계속 파싱
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				//Log.e("ParsingWhile", "ININININ...............");
				switch (parserEvent) {
				case XmlPullParser.START_TAG: {
					if (parser.getName().equalsIgnoreCase("GeoDate")) {
						Geo = GEO_DATE;
					} else if (parser.getName().equalsIgnoreCase("geoLat")) {
						Geo = GEO_LAT;
					} else if (parser.getName().equalsIgnoreCase("geoLng")) {
						Geo = GEO_LNG;
					}
					break;
				}

				case XmlPullParser.TEXT: {
					switch (Geo) {
					case 1: {
						GI.setTheDate(parser.getText());
						//Log.e("xml", "date" + parser.getText());
						break;
					}
					case 2: {
						//Log.e("xml", "lat" + parser.getText());
						GI.setTheLatitude(Double.parseDouble(parser.getText()));
						break;
					}
					case 3: {

						//Log.e("xml",
						//		"lng" + parser.getText() + "/"
						//				+ parser.getName());
						GI.setTheLongitude(Double.parseDouble(parser.getText()));
						GeoList.add(GI);
						GI = new GeoInfo();
						break;
					}

					}
				}

				case XmlPullParser.END_TAG: {
					Geo = 0;
					break;
				}

				}
				parserEvent = parser.next();
			}

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Log.e("xml",GeoList.get(0).getDate()+"/"+GeoList.get(0).getLatitude()+"/"+GeoList.get(0).getLongitude());
		// Log.e("xml",GeoList.get(1).getDate()+"/"+GeoList.get(1).getLatitude()+"/"+GeoList.get(1).getLongitude());
		// Log.e("xml",
		// GeoList.get(1).getGeoDate()+"/"+GeoList.get(1).getGeoLat()+"/"+GeoList.get(1).getGeoLng());
	}

	// 필요 메소드들
	public void run() {
		int current = 0;
		while (player != null) {
			try {
				Thread.sleep(1000);
				current = player.getCurrentPosition();
			} catch (Exception e) {
				e.printStackTrace();
			}
			//Log.e("player", "is" + player);
			if (player != null) {
				//if (player.isPlaying()) {

					Sbar.setProgress(current);
				//}
			}

		}
	}

	// mp4 파일만 가져옴
	class Mp4Filter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp4")); // 확장자가 mp4인지 확인
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	protected void onPause() {
		Log.e("pause","home.......");
		
	
		releaseMediaPlayer();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.e("destroy","home.......");
		releaseMediaPlayer();
		super.onDestroy();
	}

	public void onBackPressed() {
		Log.e("back","home.......");
		onPause();
		super.onBackPressed();

	}

	private void releaseMediaPlayer() {
		if (player != null) {
			player.stop();
			PlayerMapTimer.cancel();
			Log.e("releas play", "ok");
			pauseOn = false;
			
		}
	}

	private void startVideoPlayback() {
		Log.e("playback","home.......");
		player.start();
	}

	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}

	public void onStartTrackingTouch(SeekBar arg0) {

		// TODO Auto-generated method stub

	}

	public void onStopTrackingTouch(SeekBar Sbar) {
		Log.e("stoptracking","home.......");
		player.seekTo(Sbar.getProgress());
		player.start();
		// TODO Auto-generated method stub

	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
	class MyOverlay extends Overlay {
		 
		 Bitmap bitmap; // 오버레이에 표시할 이미지를 담을 객체
		 GeoPoint GP;
		 
		    public MyOverlay(Bitmap bitmap, GeoPoint gpoint){
		     this.bitmap = bitmap;
		     this.GP = gpoint;
		    }
		    
		    // MapView 에 그림이 추가되는 순간, 호출되는 메소드를 오버라이딩
		 @Override
		 public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		  super.draw(canvas, mapView, shadow);
		  
		  Log.d("BB", "오버레이 그림그리기");
		  
		  //그래픽, 도형, 텍스트 등을 그릴때 적용할 색상, 스타일 등의 정보를 설정할 수 있는 객체
		  Paint paint1 = new Paint();
		  
		  Paint paint2 = new Paint();
		  paint2.setARGB(255, 255, 0, 0);
		  
		  
		  // 어느 위치에 표시할 것인지
		  GeoPoint geopoint = new GeoPoint(GP.getLatitudeE6(), GP.getLongitudeE6());
		  
		  //우리가 알고 있는 위도, 경도값을 좌표로 환산.
		  Point geopix = new Point();
		  
		  // 지리 정보를 이용해서 실제 포인트 정보로 표현하는 작업을 대신 처리해 줄 객체
		  Projection project = mapView.getProjection();
		  
		  //실제 계산 처리를 하는 method(지리 정보를 포인트 정보로 표현)
		  project.toPixels(geopoint, geopix);
		  
		  // 캔버스에 액티비티에서 전달받은 비트맵 이미지(도형도 표현 가능.)를 이용해서 그림을 그린다.
		  canvas.drawBitmap(bitmap, geopix.x, geopix.y, paint1);
		/*  bitmap - 이미지, left - x 좌표, top - y 좌표, paint - 어떤 스타일로 그릴 건가요? */
		  
		 // canvas.drawText("1", geopix.x-40, geopix.y+60, paint2);
		  
		  
		  
		 }
		   
		    
		}



}
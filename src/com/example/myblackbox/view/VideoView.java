package com.example.myblackbox.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GeoInfo;
import com.example.myblackbox.etc.GlobalVar;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class VideoView extends MapActivity implements OnClickListener, Runnable, OnSeekBarChangeListener {
	String LOG_TAG = "mapcheck";
	public SeekBar Sbar;
	public View menubar;
	
	// xml 파싱 관련 변수들
	final static int GEO_DATE=1;
	final static int GEO_LAT=2;
	final static int GEO_LNG=3;
	int Geo=0;       
	int parserEvent;
	
	// View 관련 객체들
	public MapView mapView; //맵뷰 객체 
	public List<Overlay> listOfOverlays; //맵에 표시된 오버레이(레이어)들을 가지고 있는 리스트
	public String bestProvider; //현재 위치값을 가져오기위한 프로바이더. (network, gps)
	public boolean barOn=false;
	public boolean MapOn=true;
	public boolean EventOn=true;
	
	
	// 지도 관련 변수들
	public LocationManager locM; //위치 매니저
	public LocationListener locL; //위치 리스너
	public Location currentLocation; //현재 위치
	public MapController mapController; //맵을 줌시키거나, 이동시키는데 사용될 컨트롤러
	public double lat=37.5544309;	//	위도
	public double lon=126.9226986;	// 경도
	public LocationItemizedOverlay overlayHere; //현재위치 마커가 표시되어질 오버레이
	public int Maparr=0;
	
	// 파일 관련 변수들	
	//public File xmlFile;
	private static final String MediaPath = new String("/sdcard/MyBlackBox/"); // 파일 경로 지정
	private static final String XmlPath = new String("/sdcard/MyBlackBox/Data/");	//xml 파일 경로 지정
	private static final String EventPath = new String("/sdcard/MyBlackBox/Event/");	//Event 파일 경로 지정
	
	private ArrayList<String> video = new ArrayList<String>();		// video 파일 저장할 리스트
	private ArrayList<String> Event = new ArrayList<String>();		// event 파일 저장할 리스트
	private ArrayList<GeoInfo> GeoList = new ArrayList<GeoInfo>();	// Geo 저장할 리스트
	
	
	 MediaPlayer player=null;
	 SurfaceHolder holder;
	 String filename;
	 final Timer PlayerMapTimer = new Timer();
	 final TimerTask PlayerTask = new TimerTask(){
			public void run()
			{
				if(Maparr<GeoList.size())
				{
					updateOverlay(GeoList.get(Maparr).getLatitude(),GeoList.get(Maparr).getLongitude());
					Log.e("play","ok");
					Maparr++;
				}
				else if(Maparr>=GeoList.size())
					Maparr=0;
				
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
        
        Sbar = (SeekBar)findViewById(R.id.Seekbar);
        
        mapView = (MapView) findViewById(R.id.Pmapview); //맵뷰 객체를 가져온다.
		//mapView.setBuiltInZoomControls(true); //줌인,줌아웃 컨트롤을 표시한다.

        Sbar.setOnSeekBarChangeListener(this);
        
		mapController = mapView.getController(); //맵컨트롤러를 가져온다.
		mapController.setZoom(17); //초기 확대는 17정도로..
		
		//위치 매니저를 시스템으로부터 받아온다.
		locM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//사용가능한 적절한 프로바이더를 받아온다.
		//network (보통 3G망,Wifi AP 위치정보)또는 gps 둘중 하나로 설정된다.
		bestProvider = locM.getBestProvider(new Criteria(), true);

		//기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정한다.
		currentLocation = locM.getLastKnownLocation(bestProvider);
		//위치 리스너 초기화
		locL = new MyLocationListener();
		//위치 매니저에 위치 리스너를 셋팅한다.
		//위치 리스너에서 10000ms (10초) 마다 100미터 이상 이동이 발견되면 업데이트를 하려한다.
		//locM.requestLocationUpdates(bestProvider, 10000, 100, locL); 
 
        final Button playStopBtn = (Button) findViewById(R.id.playStopBtn);
        final Button MapBtn = (Button)findViewById(R.id.Mapon);
        final Button EventBtn = (Button)findViewById(R.id.EventBtn);
        //Button PlayBtn = (Button) findViewById(R.id.PlayBtn);
        FrameLayout frame = (FrameLayout) findViewById(R.id.videoLayout);
        frame.addView(surface);
      
        File videofiles = new File(MediaPath);
        File Eventfiles = new File(EventPath);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, video);
        ArrayAdapter<String> EventAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, Event);
        
        
        final ListView list = (ListView)findViewById(R.id.list);
        final ListView Eventlist = (ListView)findViewById(R.id.EventList);
		
	
       if (videofiles.listFiles( new Mp4Filter()).length > 0) {
          for (File file : videofiles.listFiles( new Mp4Filter())) {
          video.add(file.getName()); // mp4파일을 ArrayList에 추가
         }

          // 리스트에 adater 적용
          list.setAdapter(adapter);
          
       }
       if (Eventfiles.listFiles( new Mp4Filter()).length > 0) {
           for (File file : Eventfiles.listFiles( new Mp4Filter())) {
           Event.add(file.getName()); // mp4파일을 ArrayList에 추가
          }

           // 리스트에 adater 적용
           Eventlist.setAdapter(EventAdapter);
           
        }
	   	playStopBtn.setVisibility(View.GONE);
	    MapBtn.setVisibility(View.GONE);
	    EventBtn.setVisibility(View.GONE);
	    Sbar.setVisibility(View.GONE);
       
       
       // View 클릭시 메뉴바들 사라지게 함
       frame.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               
               
               if(!barOn){
            	   Sbar.setVisibility(View.GONE);
            	   EventBtn.setVisibility(View.GONE);
            	   MapBtn.setVisibility(View.GONE);
            	   playStopBtn.setVisibility(View.GONE);
            	   
            	   barOn=true;
               }
               else{
            	   Sbar.setVisibility(View.VISIBLE);
            	   EventBtn.setVisibility(View.VISIBLE);
            	   MapBtn.setVisibility(View.VISIBLE);
            	   playStopBtn.setVisibility(View.VISIBLE);
            	   barOn=false;
               }
            	   
           }
       });
        
       
        //클릭 이벤트 설정
        list.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//파일명 클릭시 토스트
        		 GeoList.clear();
        		StringTokenizer ST = new StringTokenizer(video.get(position),".");
        		
        		filename = GlobalVar.VIDEO_PATH+"/"+video.get(position);
        		Log.e("name",ST.nextToken());
        		//Parsing(XmlPath+"data.xml");
        		Parsing(GlobalVar.DATA_PATH+"/"+ST.nextToken()+".xml");
        		Toast.makeText(VideoView.this, filename, Toast.LENGTH_SHORT).show();
        		if (player == null) {
                    player = new MediaPlayer();
                }                
 
                try {
                	list.setVisibility(View.GONE);
                	playStopBtn.setVisibility(View.GONE);
                    MapBtn.setVisibility(View.GONE);
                    EventBtn.setVisibility(View.GONE);
                    Sbar.setVisibility(View.GONE);
                	
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
                    //2초 마다 업데이트
                    PlayerMapTimer.schedule(PlayerTask, 0,3000);
                    
                    
                } catch (Exception e) {
                    //Log.e(TAG, "Video play failed.", e);
                }
                
        		
               
        	}
        });
        

        //클릭 이벤트 설정
        Eventlist.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		//파일명 클릭시 토스트
        		 GeoList.clear();
        		StringTokenizer ST = new StringTokenizer(Event.get(position),".");
        		
        		filename = GlobalVar.EVENT_PATH+"/"+video.get(position);
        		Log.e("name",ST.nextToken());
        		//Parsing(XmlPath+"data.xml");        		
        		Parsing(GlobalVar.EVENT_DATA_PATH+"/"+ST.nextToken()+".xml");
        		Toast.makeText(VideoView.this, filename, Toast.LENGTH_SHORT).show();
        		if (player == null) {
                    player = new MediaPlayer();
                }                
 
                try {
                	   
                	 playStopBtn.setVisibility(View.GONE);
                     MapBtn.setVisibility(View.GONE);
                     EventBtn.setVisibility(View.GONE);
                     Sbar.setVisibility(View.GONE);
                	
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
                    //2초 마다 업데이트
                    PlayerMapTimer.schedule(PlayerTask, 0,3000);
                    
                    
                } catch (Exception e) {
                    //Log.e(TAG, "Video play failed.", e);
                }
                
        	}
        });
 
  
        // 정지 및 리스트 버튼 클릭시 이벤트
        playStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null)
                    return;
              
                player.stop();
                player.release();
                player = null;
                Maparr=0;
                PlayerMapTimer.cancel();
                playStopBtn.setVisibility(View.GONE);
                GeoList.clear();
                
            }
        });
       
        // 맵 버튼 클릭시 이벤트
        MapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(MapOn==true)
            	{
            		mapView.setVisibility(View.INVISIBLE);
            		MapOn=false;
            	}
            	else
            	{
            		mapView.setVisibility(View.VISIBLE);
            		MapOn=true;
            	}
            }
        });
        
        EventBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				if(EventOn){
					list.setVisibility(View.GONE);
					Eventlist.setVisibility(View.VISIBLE);
					EventOn=false;
					EventBtn.setText("PlayList");
				}
				else
				{
					list.setVisibility(View.VISIBLE);
					Eventlist.setVisibility(View.GONE);
					EventBtn.setText("EventList");
					EventOn=true;
				}
			}
		});
       
		
    }
    
    
    //xml 파싱 함수
    public void Parsing(String xmlFile)
    {
    	 try {
    		 	//xml 파일을 버터로 읽음
	        	FileInputStream In = new FileInputStream(xmlFile);
	        	BufferedReader bufferReader = new BufferedReader(new InputStreamReader(In));
	        		
	        	// xml pull 형식 파싱
				XmlPullParserFactory infoValue = XmlPullParserFactory.newInstance();
				infoValue.setNamespaceAware(true);
	            XmlPullParser parser = infoValue.newPullParser(); //이벤트를 처리해줄 객체선언
	            parserEvent= parser.getEventType();
	            parser.setInput(bufferReader);
         
	            // Geo 리스트에 저장할 객체 선언
	            GeoInfo GI=new GeoInfo();
	            
	            // 문서가 끝날때까지 계속 파싱
	            while(parserEvent != XmlPullParser.END_DOCUMENT){
	            	
	            	switch(parserEvent)
	            	{
		            	case XmlPullParser.START_TAG:
		            	{
		            		if (parser.getName().equalsIgnoreCase("GeoDate"))
		            		{
		            			Geo=GEO_DATE;
		            		}
		            		else if(parser.getName().equalsIgnoreCase("geoLat"))
		            		{
		            			Geo=GEO_LAT;
		            		}
		            		else if(parser.getName().equalsIgnoreCase("geoLng"))
		            		{		            			
		            			Geo=GEO_LNG;
		            		}
		            		break;
		            	}
	            		
	            	case XmlPullParser.TEXT:
	            	{
	            		switch(Geo)
	            		{
	            			case 1:
	            			{
	            				GI.setTheDate(parser.getText());
	            				Log.e("xml","date"+ parser.getText());
	            				break;
	            			}
	            			case 2:
	            			{	  
	            				Log.e("xml", "lat"+parser.getText());
	            				GI.setTheLatitude(Double.parseDouble(parser.getText()));
	            				break;
	            			}
	            			case 3:
	            			{
	            				
	            				Log.e("xml","lng"+parser.getText()+"/"+parser.getName());
	            				GI.setTheLongitude(Double.parseDouble(parser.getText()));
	            				GeoList.add(GI);            				
	            				GI = new GeoInfo();
	            				break;
	            			}       			
	            			
	            		}
	            	}
	            	
	            	case XmlPullParser.END_TAG:
	            	{
	            		Geo=0;
	            		break;
	            	}         		
         		
	            	}
	            	parserEvent=parser.next();
	            }

           

		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	// Log.e("xml", GeoList.get(0).getGeoDate()+"/"+GeoList.get(0).getGeoLat()+"/"+GeoList.get(0).getGeoLng());
        // Log.e("xml", GeoList.get(1).getGeoDate()+"/"+GeoList.get(1).getGeoLat()+"/"+GeoList.get(1).getGeoLng());
    }
    
    
    
    //필요 메소드들 
    public void run(){
    	int current = 0;
    	while(player!=null){
    		try{
    			Thread.sleep(1000);
    			current=player.getCurrentPosition();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		Log.e("player", "is"+player);
    		if(player!=null)
    		{
	    		if(player.isPlaying()){
	    		
	    			Sbar.setProgress(current);
	    		}
    		}
    		
    	}
    }    
    
    //mp4 파일만 가져옴
  	class Mp4Filter implements FilenameFilter {
      public boolean accept(File dir, String name) {
          return (name.endsWith(".mp4")); // 확장자가 mp4인지 확인
      }
  	}

	public class MyLocationListener implements LocationListener {

	public void onLocationChanged(Location location) {
	//위치 이동이 발견되었을때 호출될 메소드.
	//위의 설정에서 10초마다 100미터 이상 이동이 발견되면 호출된다.
		updateOverlay(GeoList.get(Maparr).getLatitude(),GeoList.get(Maparr).getLongitude());
	}  

	
	public void onProviderDisabled(String provider) {
	Log.d(LOG_TAG, "GPS disabled : " + provider); 
	}

	
	public void onProviderEnabled(String provider) {
	Log.d(LOG_TAG, "GPS Enabled : " + provider);
	}


	
	public void onStatusChanged(String provider, int status, Bundle extras) {
	Log.d(LOG_TAG, "onStatusChanged : " + provider + " & status = "
	+ status);
	}


}
	public void updateOverlay(Double lat, Double lng ) {
		//기존에 화면에 찍어둔 오버레이 (마커들)을 싹 지운다.
		listOfOverlays = mapView.getOverlays(); //맵뷰에서 오버레이 리스트를 가져온다.
		if (listOfOverlays.size() > 0) {
		listOfOverlays.clear(); //오버레이가 있을때 싹 지워준다.
		Log.e(LOG_TAG, "clear overlays : " + listOfOverlays.size());
		} else {
		Log.d(LOG_TAG, "empty overlays");
		}
		Location location= new Location(bestProvider);
		
		location.setLatitude(lat);
		location.setLongitude(lng);

		//Location 객체를 가지고 GeoPoint 객체를 얻어내는 메소드
		GeoPoint geoPoint = getGeoPoint(location); 
		//현재위치를 표시할 이미지
		Drawable marker;

		//실제 운영소스엔 분기하여 현재위치와 선택위치 이미지를 변경하게 되어있다.
		marker = getResources().getDrawable(R.drawable.ic_launcher);  
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());

		//LocationItemizedOverlay 를 이용하여 현재위치 마커를 찍을 오버레이를 생성한다.
		overlayHere = new LocationItemizedOverlay(marker);
		//touch event 의 null pointer 버그를 방지하기 위해 마커를 찍고 바로 populate 시켜준다.
		overlayHere.mPopulate();
		//현재위치를 GeoCoder 를 이용하여 대략주소와 위,경도를 Toast 를 통하여 보여준다.
		String geoString = showNowHere(location.getLatitude(), location.getLongitude() , true);

		//현재위치 마커 정의
		OverlayItem overlayItem = new OverlayItem(geoPoint, "here", geoString);
		overlayHere.addOverlay(overlayItem); //현재위치 오버레이 리스트에 현재위치 마커를 넣는다.

		// 지점정보를 HTTP통신을 통해 서버에서 받아와서 전역변수인 brList (지점리스트)에 넣는다.
		// 성능을 고려하여 쓰레드로 구현이 되어있다.
		// 고다음 지점리스트 오버레이에 넣고 화면에 찍어주는 메소드.
		//showBranchMarker(location.getLatitude(), location.getLongitude(),
		//this.searchType, SEARCH_RANGE);

		// 맵뷰에서 터치이벤트를 받을 오버레이를 추가한다.
		// 특정지점을 오래 눌렀을때 특정 지점 기준으로 재검색을 하기 위하여 터치이벤트를 받아와야한다.
		//mapView.getOverlays().add(new MapTouchDetectorOverlay());

		// 마지막으로 생성된 오버레이레이어를 맵뷰에 추가한다.
		mapView.getOverlays().add(overlayHere);
		mapView.getController().animateTo(geoPoint); //현재위치로 화면을 이동한다.
		mapView.postInvalidate(); //맵뷰를 다시 그려준다.
		}
	
	private GeoPoint getGeoPoint(Location location) {
		if (location == null) {
		return null;
		}
		Double lat = location.getLatitude() * 1E6;
		Double lng = location.getLongitude() * 1E6;
		return new GeoPoint(lat.intValue(), lng.intValue());
	}
	
	protected class LocationItemizedOverlay extends
	ItemizedOverlay<OverlayItem> {
	private List<OverlayItem> overlays;

	public LocationItemizedOverlay(Drawable defaultMarker) { //오버레이 생성자
	//마커 이미지의 가운데 아랫부분이 마커에서 표시하는 포인트가 되게 한다.
	super(boundCenterBottom(defaultMarker)); 
	overlays = new ArrayList<OverlayItem>();
	}

	@Override
	protected OverlayItem createItem(int i) {
	return overlays.get(i);
	}

	@Override
	public int size() {
	return overlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
	overlays.add(overlay);
	//null pointer 버그때문에 오버레이 아이템 추가후 가능한 빨리 populate 해줘야한다.
	populate(); 
	}
	public void mPopulate() {
		populate();
		}
	}
	private String showNowHere(double lat, double lng , boolean showOption){
		StringBuilder geoString = new StringBuilder();
		try {
		Geocoder goecoder = new Geocoder(getApplicationContext(),
		Locale.getDefault());

		Address adr = goecoder.getFromLocation(lat,
		lng, 1).get(0);

		if (adr.getLocality() != null) geoString.append(adr.getLocality()).append(" ");
		if (adr.getThoroughfare() != null) geoString.append(adr.getThoroughfare());
		if (!"".equals(geoString.toString())) geoString.append("\n\n");
		} catch (Exception e) { }
		geoString.append("위도 : ").append(lat).append(" ,경도 : ").append(lng);
		if (showOption){
		//Toast.makeText(getApplicationContext(), geoString.toString(),
		//Toast.LENGTH_SHORT).show();
		}
		return geoString.toString();
	}


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}


	 protected void onPause() {
		 
	       
	        super.onPause();
	        releaseMediaPlayer();
	       
	    }

	    @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        releaseMediaPlayer();
	      
	    }
	    public void onBackPressed(){
	    	
	    	onPause();		
			super.onBackPressed();	
			
		}

	    private void releaseMediaPlayer() {
	        if (player != null) {
	        	player.stop();
	        	//player.release();
	        	PlayerMapTimer.cancel();
	        	Log.e("releas play", "ok");
	        	player = null;
	        }
	    }
	    
	    private void startVideoPlayback() {
	             player.start();
	    }

		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
			// TODO Auto-generated method stub
			
		}

		public void onStartTrackingTouch(SeekBar arg0) {
			
			// TODO Auto-generated method stub
			
		}

		public void onStopTrackingTouch(SeekBar Sbar) {
			player.seekTo(Sbar.getProgress());
			player.start();
			// TODO Auto-generated method stub
			
		}

	

		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			
		}
    
}
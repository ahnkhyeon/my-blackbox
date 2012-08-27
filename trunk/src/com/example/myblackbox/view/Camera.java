package  com.example.myblackbox.view;

 
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import com.example.myblackbox.R;
import com.example.myblackbox.etc.GlobalVar;

public class Camera extends Activity {
    public static final String TAG = "Camera";
 
    private static String EXTERNAL_STORAGE_PATH = "";
    private static String RECORDED_FILE = "video_recorded";
    private static int fileIndex = 0;
    private static String filename = "";
    private ArrayList<File> Flist;
    MediaPlayer player;
    MediaRecorder recorder;
 
    private Camera camera = null;
    SurfaceHolder holder;
 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
 
        // check external storage
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Log.d(TAG, "External Storage Media is not mounted.");
        } else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
 
        // create a SurfaceView instance and add it to the layout
        SurfaceView surface = new SurfaceView(this);
        holder = surface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
        FrameLayout frame = (FrameLayout) findViewById(R.id.videoLayout);
        frame.addView(surface);
 
 
        Button recordBtn = (Button) findViewById(R.id.recordBtn);
        Button recordStopBtn = (Button) findViewById(R.id.recordStopBtn);
        Button PlayBtn = (Button) findViewById(R.id.PlayBtn);
       // Spinner spin = (Spinner)findViewById(R.id.playList);
        Button playStopBtn = (Button) findViewById(R.id.playStopBtn);
 
        recordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	 try { filename = createFilename();
            	 startRecording(filename);
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	 stopRecording();
            }
        });
 
        recordStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecording();
 
                
            }
        });
 
        PlayBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null) {
                    player = new MediaPlayer();
                }
                File f = new File("/1한양대/");
                File[] files = f.listFiles();
                
 
                try {
                    player.setDataSource(filename);
                    player.setDisplay(holder);
 
                    player.prepare();
                    player.start();
                } catch (Exception e) {
                    Log.e(TAG, "Video play failed.", e);
                }
            }
        });
 
 
        playStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null)
                    return;
 
                player.stop();
                player.release();
                player = null;
            }
        });
    }
    
    
    private void startRecording(String filename){
    	
    	 try {
             if (recorder == null) {
                 recorder = new MediaRecorder();
             }
            
             //객체 정보 설정
             recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
             recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
             recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
             recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
             recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
             

            
             Log.d(TAG, "current filename : " + filename);
             
             // 미리보기 디스플레이 surfaceview 화면 설정
             recorder.setOutputFile(filename);
             recorder.setPreviewDisplay(holder.getSurface());
             recorder.prepare();
             recorder.start();

            
             
         } catch (Exception ex) {
             Log.e(TAG, "Exception : ", ex);

             recorder.release();
             recorder = null;
         }
    }
    
    private void stopRecording(){
    	if (recorder == null)
            return;
        
        //녹화 중지 후에 리소스 해제
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;

        ContentValues values = new ContentValues(10);

        values.put(MediaStore.MediaColumns.TITLE, "RecordedVideo");
        values.put(MediaStore.Audio.Media.ALBUM, "Video Album");
        values.put(MediaStore.Audio.Media.ARTIST, "Mike");
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Video");
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Audio.Media.DATA, filename);

        // 녹화된 파일을 내용 제공자를 이용 동영상 목록으로 저장
        Uri videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        
        if (videoUri == null) {
            Log.d("SampleVideoRecorder", "Video insert failed.");
            return;
        }

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));
    }
 
 
    private String createFilename() {
       fileIndex++;
 
       SimpleDateFormat sdf = new SimpleDateFormat("MM_dd HH;mm;ss",
				Locale.KOREA);
		Date d = new Date();
		String filename = sdf.format(d);
		
        String newFilename = "";
        if (EXTERNAL_STORAGE_PATH == null || EXTERNAL_STORAGE_PATH.equals("")) {
            // use internal memory
            newFilename = RECORDED_FILE + fileIndex + ".mp4";
        } else {
            newFilename = GlobalVar.VIDEO_PATH+"/" + filename + ".mp4";
        }
 
		
        return newFilename;
    }
 
 
 
    protected void onPause() {
        if (camera != null) {
            //camera.release();
            camera = null;
        }
 
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
 
        if (player != null) {
            player.release();
            player = null;
        }
 
        super.onPause();
    }
 
}
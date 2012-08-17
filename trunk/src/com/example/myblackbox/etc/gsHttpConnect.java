package com.example.myblackbox.etc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class gsHttpConnect 
{
	
	static private HttpURLConnection m_con ;
	
	static private String m_cookies = "" ;
	static boolean m_session = false ;
	
	
	static long m_sessionLimitTime = 360000 ;  	/// 세션 시간제한 (밀리세컨드) 서버 설정시간이랑 맞추는게 좋을듯? 
	static long m_sessionTime = 0 ;    			/// 세션을 얻은 시간 
	
	private String m_request ;

	
	
	/// 주소, 메소드타입("GET" or "POST"), map(변수명, 값) 을 넣어주면 됨
	public String request( URL url, String method, Map<String, Object> params ) throws IOException 
	{
		/// 세션 시간이 넘어가지는 않았는지 확인한다.
		checkSession( ) ;
		
		/// 받아올 인풋스트림	 
		/// POST방식일 경우 데이터를 전송할 아웃풋 스트림
		
		OutputStream out = null ;

		/// url에 연결
		m_con = (HttpURLConnection)url.openConnection( ) ;

		/// 메소드 타입을 지정 "GET"나 "POST"를 넣어야 하겠지~_~? 
		m_con.setRequestMethod( method ) ;
		
		/// 인코딩 정의 HTTP방식으로 전송할때는 urlencoded방식으로 인코딩해서 전송해야한다.
		m_con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" ) ;

		/// 인풋스트림 쓸거라고 지정
		m_con.setDoInput( true ) ;
		
		m_con.setInstanceFollowRedirects( false ) ; // 세션을 사용하려면 false로 설정해둬야함
		
		/// 세션 생성해둔게 있으면 헤더에 셋팅해서 내가 전에 접속했던 녀석이라고 알려준다.
		if( m_session )
		{
			m_con.setRequestProperty( "cookie", m_cookies ) ;
		}

	
		/// 포스트방식일 경우
		if( method.equals( "POST" ) ) 
		{
			/// 데이터를 주소와 별개로 전송한다.
			m_con.setDoOutput( true ) ;						/// 아웃풋 스트림 쓰기위에 아웃풋을 true로 켬
			
			String paramstr = buildParameters( params ) ;	/// 파라메터를 문자열로 치환
			out = m_con.getOutputStream( ) ;				/// 아웃풋 스트림 생성
			out.write(paramstr.getBytes( "UTF-8" ) ) ;		/// UTF-8포멧으로 변경해서 쓴다.
			out.flush( ) ;									/// 플러쉬~
			out.close( ) ;									/// 스트림 닫기 
			Log.d( "-- gsLog ---", "post succes" ) ;		
		}
		
		
		
		return getRequest( ) ;
		//////
		
		
	}
	
	/// 어디선가 퍼온 소스들을 짜맞춰 만든 함수
	/// 파일을 업로드하면서 변수 전달하고 리퀘스트 받는 함수입습죠
//	public String uploadAndRequest( URL url, Map<String, Object> params, Map<String, Object> files, String cookie ) throws IOException
	public String uploadAndRequest( URL url, Map<String, Object> params, Map<String, Object> files) throws IOException
	{
		
		/// 세션 시간이 넘어가지는 않았는지 체크 
		checkSession( ) ;
		
		
		/// 파일 업로드시 사용할 인풋 스트림
		FileInputStream mFileInputStream = null;
	    
		
		/// 변수 조립할때 쓸 문자열들 
	    final String lineEnd = "\r\n" ;
	    final String twoHyphens = "--" ;
	    final String boundary = "*****" ;   

	    
	    /// 연결하고 환경 셋팅
        m_con = (HttpURLConnection)url.openConnection( ) ;            
        m_con.setDoInput( true ) ;
        m_con.setDoOutput( true ) ;
        m_con.setUseCaches( false ) ;
        m_con.setRequestMethod( "POST" ) ;
        m_con.setInstanceFollowRedirects( false ) ;
		
		///세션 생성해둔게 있으면 헤더에 셋팅해서 내가 전에 접속했던 녀석이라고 알려준다.
//		if( !cookie.isEmpty() )
//		{
//			m_con.setRequestProperty( "cookie", cookie ) ;
//		}
        
		m_con.setRequestProperty("Connection", "Keep-Alive");
		m_con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        
        
        ///////////////////////////////////////////////////////////////////////
        /// 변수 전달
		///////////////////////////////////////////////////////////////////////
        DataOutputStream dos = new DataOutputStream( m_con.getOutputStream( ) ) ;
        
        ///키와 값을 차례차례 빼낸다
		for ( Iterator<String> i = params.keySet( ).iterator( ) ; i.hasNext( ) ; ) 
		{
			String key = i.next( ) ;

			/// 대충 생각해보면 감이 올지 않는가?
			/// --*****\r\n
			/// Content-Disposition: form-data; name=\"변수명1\"\r\n변수값1\r\n
			/// --*****\r\n
			/// Content-Disposition: form-data; name=\"변수명2\"\r\n변수값2\r\n
			/// --*****\r\n
			/// Content-Disposition: form-data; name=\"변수명3\"\r\n변수값3\r\n
			
			dos.writeBytes( twoHyphens + boundary + lineEnd ) ; //필드 구분자 시작
			dos.writeBytes( "Content-Disposition: form-data; name=\"" 
					+ key 
            		+ "\""+ lineEnd ) ;
			dos.writeBytes( lineEnd ) ;
            
            dos.writeBytes( String.valueOf( params.get( key ) ) ) ;
            
            dos.writeBytes( lineEnd ) ;
            
		}
        //////////////
        
        
        
        
		///////////////////////////////////////////////////////////////////////
        /// 파일 전달
		///////////////////////////////////////////////////////////////////////
		
		/// 키와 값을 차례차례 빼낸다
		for ( Iterator<String> i = files.keySet( ).iterator( ) ; i.hasNext( ) ; ) 
		{
			String key = i.next( ) ;
			String fileName = String.valueOf( files.get( key ) ) ;
			
			mFileInputStream = new FileInputStream( fileName );            


			/// 위에서 설명한 내용에서 변수값 대신 파일을 쓰는 것만 다르니까 더이상의 설명은 생략한다.
			dos.writeBytes( twoHyphens + boundary + lineEnd ) ;
			dos.writeBytes( "Content-Disposition: form-data; name=\"" 
					+ key
					+ "\";filename=\"" + fileName + "\"" + lineEnd ) ; 
			dos.writeBytes( lineEnd ) ;
        
	        int bytesAvailable = mFileInputStream.available( ) ;
	        int maxBufferSize = 1024 ;
	        int bufferSize = Math.min( bytesAvailable, maxBufferSize ) ;
	        
	        byte[] buffer = new byte[bufferSize] ;
	        int bytesRead = mFileInputStream.read( buffer, 0, bufferSize ) ;

	        /// 그림파일 읽어서 내용을 쏴준다.
	        while( bytesRead > 0 ) 
	        {
	            dos.write( buffer, 0, bufferSize ) ;
	            bytesAvailable = mFileInputStream.available( ) ;
	            bufferSize = Math.min( bytesAvailable, maxBufferSize ) ;
	            bytesRead = mFileInputStream.read( buffer, 0, bufferSize ) ;
	        }    
        
	        dos.writeBytes( lineEnd ) ;
	        dos.writeBytes( twoHyphens + boundary + twoHyphens + lineEnd ) ;
	        /// 변수 하나(파일하나) 전달 끗 
        
	        mFileInputStream.close( ) ;
	        
	        dos.flush( ) ;
	        
	        
		}
	    
		
		return getRequest( ) ;          
	    
	}
	
	
	public String getRequest( ) throws UnsupportedEncodingException, IOException
	{
		InputStream in = null ;
		
		/// 받아온 데이터를 쓰기위한 스트림
		ByteArrayOutputStream bos = new ByteArrayOutputStream( ) ;
		
		/// 리퀘스트 데이터를 저장할 버퍼
		byte[] buf = new byte[2048];
		
		int k = 0 ; /// 읽은 라인수
		
		long ti = System.currentTimeMillis( ) ;	/// == 시간 체크용 == 서버에 따라 리퀘스트 오는 시간이 매우 오래걸림
		
		in = m_con.getInputStream( ) ;     		/// 인풋스트림 생성
		
		/// == 시간 체크용 == inputstream얻는 요기서 시간 10초이상 넘어가면 큰일남 		           
		/// 갤럭시 S에서 어떤앱은 WebView라던가 Http통신에서 15초인가 넘어가면 세션 끊기는 		           
		/// 원인을 알 수 없는 경우도 있었음 다른기기 다 잘되는데 오로지 갤럭시 S만!!! 그랬음 참고 바람요 
		Log.d( "---recTime---", "" + ( System.currentTimeMillis( ) - ti ) ) ;
		
		
		/// 루프를 돌면서 리퀘스트로 받은내용을 저장한다.
		while( true )
		{
			int readlen = in.read( buf ) ;
			if( readlen < 1 )
				break ;
			k += readlen ;
			bos.write( buf, 0, readlen ) ;
		}
			
		/// 리퀘스트 받은 내용을 UTF-8로 변경해서 문자열로 저장
		m_request = new String( bos.toByteArray( ), "UTF-8" ) ;

		Log.i("GET_XML_OK", m_request);
		m_session = requestAndSetSession( ) ;
			
		in.close( ) ;
		m_con.disconnect( ) ;
		
		return m_request ;
	}
	
	
	/// Request를 받되 세션 유지를 위해 쿠키를 저장한다.
	 public boolean requestAndSetSession( )
	 {
	  
		 ///맵에다 Http헤더를 받아냄
	     Map< String, List<String> > imap = m_con.getHeaderFields( ) ;
	     
	     /// 그리고 거길 뒤져서 쿠키를 찾아냄
	     if( imap.containsKey( "Set-Cookie" ) )
	     {
	    	 /// 쿠키를 스트링으로 쫙 저장함
	    	 List<String> lString = imap.get( "Set-Cookie" ) ;
	    	 for( int i = 0 ; i < lString.size() ; i++ )
	    	 {
	    		 m_cookies += lString.get( i ) ;
	    	 }
	    	// 2.3에서 정상작동하지 않습니다 .위의 코드로 대처합니다.
	    	//Collections c = (Collections)imap.get( "Set-Cookie" ) ; 
	    	 //m_cookies = c.toString( ) ;
	      
	    	 ///  세션을 저장했으니
	         return true ;
	     }
	     else
	     {
	    	 return false ;
	     }
	     
	 }

	 
	public boolean checkSession( ) 
	{
		if( !m_session )
		{
			return false ;
		}
		
		if( System.currentTimeMillis( ) < m_sessionTime + m_sessionLimitTime )
		{
			/// 제한시간 아직 안넘었음 세션 유지 연장시킴 
			m_sessionTime = System.currentTimeMillis( ) ;
			return true ; 
		}
		else
		{
			/// 제한시간을 넘겼음 세션을 제거함 
			m_cookies = "" ;
			m_session = false ;
			return false ; 
		}
	}
	
	/// 파라메터 값을  "변수명=변수값&" 형식의 텍스트로 변환해주는 함수 
	protected String buildParameters(Map<String, Object> params) throws IOException 
	{
		StringBuilder sb = new StringBuilder( ) ;
		
		/// 파라메터가 없으면 그냥 쌩~ 한다 
		if( params == null )
		{
			return "" ;
		}
		
		/// 키와 값을 차례차례 빼낸다 
		for ( Iterator<String> i = params.keySet( ).iterator( ) ; i.hasNext( ) ; ) 
		{
			
			/// 변수명=변수값&변수명=변수값&변수명=변수값 이런 형태의 String를 만들기 위한 작업이다  
			
			String key = i.next( ) ;
			sb.append( key ) ;
			sb.append( "=" ) ;
			sb.append( URLEncoder.encode( String.valueOf( params.get( key ) ), "UTF-8" ) ) ;
			
			/// 값이 더 있으면 &를 넣어준다.
			if ( i.hasNext( ) )
			{
				sb.append( "&" ) ;
			}
		}
		
		///만들어낸 문자열을 반환한다.
		return sb.toString( ) ;
	}
	
}

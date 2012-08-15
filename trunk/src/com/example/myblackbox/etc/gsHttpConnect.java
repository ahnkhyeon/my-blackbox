package com.example.myblackbox.etc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;

import android.util.Log;
import android.widget.EditText;

public class gsHttpConnect 
{
	
	static private HttpURLConnection m_con ;
	//private HttpResponse responer ;
	
	static private String m_cookies = "" ;
	static boolean m_session = false ;
	
	
	static long m_sessionLimitTime = 360000 ;  	/// ���� �ð����� (�и�������) ���� �����ð��̶� ���ߴ°� ������?
	static long m_sessionTime = 0 ;    			/// ������ ���� �ð�
	
	private String m_request ;

	
	
	/// �ּ�, �޼ҵ�Ÿ��("GET" or "POST"), map(������, ��) �� �־��ָ� ��
	public String request( URL url, String method, Map<String, Object> params ) throws IOException 
	{
		/// ���� �ð��� �Ѿ���� �ʾҴ��� Ȯ���Ѵ�.
		checkSession( ) ;
		
		/// �޾ƿ� ��ǲ��Ʈ��
		/// POST����� ��� �����͸� ����� �ƿ�ǲ ��Ʈ��
		
		OutputStream out = null ;

		/// url�� ����
		m_con = (HttpURLConnection)url.openConnection( ) ;

		/// �޼ҵ� Ÿ���� ���� "GET"�� "POST"�� �־�� �ϰ���~_~?
		m_con.setRequestMethod( method ) ;
		
		/// ���ڵ� ���� HTTP������� ����Ҷ��� urlencoded������� ���ڵ��ؼ� ����ؾ��Ѵ�.
		m_con.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded" ) ;

		/// ��ǲ��Ʈ�� ���Ŷ�� ����
		m_con.setDoInput( true ) ;
		
		m_con.setInstanceFollowRedirects( false ) ; // ������ ����Ϸ��� false�� �����ص־���
		
		/// ���� ���صа� ������ ����� �����ؼ� ���� �� �����ߴ� �༮�̶�� �˷��ش�.
		if( m_session )
		{
			m_con.setRequestProperty( "cookie", m_cookies ) ;
		}

	
		/// ����Ʈ����� ���
		if( method.equals( "POST" ) ) 
		{
			/// �����͸� �ּҿ� ������ ����Ѵ�.
			m_con.setDoOutput( true ) ;						/// �ƿ�ǲ ��Ʈ�� �������� �ƿ�ǲ�� true�� ��
			
			String paramstr = buildParameters( params ) ;	/// �Ķ���͸� ���ڿ��� ġȯ
			out = m_con.getOutputStream( ) ;				/// �ƿ�ǲ ��Ʈ�� ��
			out.write(paramstr.getBytes( "UTF-8" ) ) ;		/// UTF-8�������� �����ؼ� ����.
			out.flush( ) ;									/// �÷���~
			out.close( ) ;									/// ��Ʈ�� �ݱ�
			Log.d( "-- gsLog ---", "post succes" ) ;			/// �α����
		}
		
		
		
		return getRequest( ) ;
		//////
		
		
	}
	
	/// ��𼱰� �ۿ� �ҽ����� ¥���� ���� �Լ�
	/// ������ ���ε��ϸ鼭 ���� ����ϰ� ����Ʈ �޴� �Լ��Խ���
	public String uploadAndRequest( URL url, Map<String, Object> params, Map<String, Object> files ) throws IOException
	{
		
		/// ���� �ð��� �Ѿ���� �ʾҴ��� üũ
		checkSession( ) ;
		
		
		/// ���� ���ε�� ����� ��ǲ ��Ʈ��
		FileInputStream mFileInputStream = null;
	    
		
		/// ���� �����Ҷ� �� ���ڿ���
	    final String lineEnd = "\r\n" ;
	    final String twoHyphens = "--" ;
	    final String boundary = "*****" ;   

	    
	    /// �����ϰ� ȯ�� ����
        m_con = (HttpURLConnection)url.openConnection( ) ;            
        m_con.setDoInput( true ) ;
        m_con.setDoOutput( true ) ;
        m_con.setUseCaches( false ) ;
        m_con.setRequestMethod( "POST" ) ;
        m_con.setInstanceFollowRedirects( false ) ;
		
		/// ���� ���صа� ������ ����� �����ؼ� ���� �� �����ߴ� �༮�̶�� �˷��ش�.
		if( m_session )
		{
			m_con.setRequestProperty( "cookie", m_cookies ) ;
		}
        
		m_con.setRequestProperty("Connection", "Keep-Alive");
		m_con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

        
        
        ///////////////////////////////////////////////////////////////////////
        /// ���� ���
		///////////////////////////////////////////////////////////////////////
        DataOutputStream dos = new DataOutputStream( m_con.getOutputStream( ) ) ;
        
        /// Ű�� ���� �������� ������
		for ( Iterator<String> i = params.keySet( ).iterator( ) ; i.hasNext( ) ; ) 
		{
			String key = ( String )i.next( ) ;

			/// ���� ���غ��� ���� ���� �ʴ°�?
			/// --*****\r\n
			/// Content-Disposition: form-data; name=\"������1\"\r\n������1\r\n
			/// --*****\r\n
			/// Content-Disposition: form-data; name=\"������2\"\r\n������2\r\n
			/// --*****\r\n
			/// Content-Disposition: form-data; name=\"������3\"\r\n������3\r\n
			
			dos.writeBytes( twoHyphens + boundary + lineEnd ) ; //�ʵ� ������ ����
			dos.writeBytes( "Content-Disposition: form-data; name=\"" 
					+ key 
            		+ "\""+ lineEnd ) ;
			dos.writeBytes( lineEnd ) ;
            
            dos.writeBytes( String.valueOf( params.get( key ) ) ) ;
            
            dos.writeBytes( lineEnd ) ;
            
		}
        //////////////
        
        
        
        
		///////////////////////////////////////////////////////////////////////
        /// ���� ���
		///////////////////////////////////////////////////////////////////////
		
		/// Ű�� ���� �������� ������
		for ( Iterator<String> i = files.keySet( ).iterator( ) ; i.hasNext( ) ; ) 
		{
			String key = ( String )i.next( ) ;
			String fileName = String.valueOf( files.get( key ) ) ;
			
			mFileInputStream = new FileInputStream( fileName );            


			/// ������ ������ ���뿡�� ������ ��� ������ ���� �͸� �ٸ��ϱ� ���̻��� ������ ���Ѵ�.
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

	        /// �׸����� �о ������ ���ش�.
	        while( bytesRead > 0 ) 
	        {
	            dos.write( buffer, 0, bufferSize ) ;
	            bytesAvailable = mFileInputStream.available( ) ;
	            bufferSize = Math.min( bytesAvailable, maxBufferSize ) ;
	            bytesRead = mFileInputStream.read( buffer, 0, bufferSize ) ;
	        }    
        
	        dos.writeBytes( lineEnd ) ;
	        dos.writeBytes( twoHyphens + boundary + twoHyphens + lineEnd ) ;
	        /// ���� �ϳ�(�����ϳ�) ��� ��
        
	        mFileInputStream.close( ) ;
	        
	        dos.flush( ) ;
	        
	        
		}
	    
		
		return getRequest( ) ;          
	    
	}
	
	
	public String getRequest( ) throws UnsupportedEncodingException, IOException
	{
		InputStream in = null ;
		
		/// �޾ƿ� �����͸� �������� ��Ʈ��
		ByteArrayOutputStream bos = new ByteArrayOutputStream( ) ;
		
		/// ����Ʈ �����͸� ������ ����
		byte[] buf = new byte[2048];
		try 
		{
			int k = 0 ; /// ���� ���μ�
			
			long ti = System.currentTimeMillis( ) ;	/// == �ð� üũ�� == ������ ��� ����Ʈ ���� �ð��� �ſ� �����ɸ�
			
			in = m_con.getInputStream( ) ;     		/// ��ǲ��Ʈ�� ��
			
			/// == �ð� üũ�� == inputstream��� ��⼭ �ð� 10���̻� �Ѿ�� ū�ϳ�
            /// ������ S���� ����� WebView��� Http��ſ��� 15���ΰ� �Ѿ�� ���� �����
            /// ������ �� �� ��� ��쵵 �־��� �ٸ���� �� �ߵǴµ� ������ ������ S��!!! �׷��� ��� �ٶ���
			Log.d( "---recTime---", "" + ( System.currentTimeMillis( ) - ti ) ) ;


			/// ������ ���鼭 ����Ʈ�� ���������� �����Ѵ�.
			while( true )
			{
				int readlen = in.read( buf ) ;
				if( readlen < 1 )
					break ;
				k += readlen ;
				bos.write( buf, 0, readlen ) ;
			}
			/// ����Ʈ ���� ������ UTF-8�� �����ؼ� ���ڿ��� ����
			m_request = new String( bos.toByteArray( ), "UTF-8" ) ;
			/*
			File fl = new File( "/sdcard/rec.txt" ) ;
			FileOutputStream fos = new FileOutputStream( fl ) ;
			fos.write( bos.toByteArray( ) ) ;
			/**/
			
			m_session = requestAndSetSession( ) ;
			
			return m_request ;
			
		}
		catch (IOException e) 
		{
			/// ����Ʈ �޴ٰ� ������ ���� �������鼭 ���� �޼����� �д´�.
			if ( m_con.getResponseCode( ) == 500 ) 
			{
				/// ���� �����ϰ� ������ ���� ��ǲ��Ʈ�� ���ؼ� ����޼��� ���
				bos.reset( ) ;
			    InputStream err = m_con.getErrorStream( ) ;
			    while( true ) 
			    {
			    	int readlen = err.read( buf ) ;
			    	
			    	if ( readlen < 1 )
			    		break ;
			    	bos.write( buf, 0, readlen ) ;
			    }
			    
			    /// �����޼����� ���ڿ��� ����
			    String output = new String( bos.toByteArray( ), "UTF-8" ) ;
			    
			    /// ���� �����޼����� ����Ѵ�.
			   	System.err.println( output ) ;
			}
			
			m_request = "error" ;
			
			throw e ;
			
		} 
		finally /// 500������ �ƴϸ� �׳� ���� �������.... -_- �ȵǴµ� ���ֳ�?
		{
			if ( in != null )
				in.close( ) ;
			
			if ( m_con != null )
				m_con.disconnect( ) ;
			
			m_session = false ;
			m_cookies = "" ;
			
			m_request = "error" ;

		}
			
		//return m_request ;
	}
	
	
	/// Request�� �޵� ���� ������ ���� ��Ű�� �����Ѵ�.
	 public boolean requestAndSetSession( )
	 {
	  
		 /// �ʿ��� Http����� �޾Ƴ�
	     Map< String, List<String> > imap = m_con.getHeaderFields( ) ;
	     
	     /// �׸��� �ű� ������ ��Ű�� ã�Ƴ�
	     if( imap.containsKey( "Set-Cookie" ) )
	     {
	    	 /// ��Ű�� ��Ʈ������ �� ������
	    	 List<String> lString = imap.get( "Set-Cookie" ) ;
	    	 for( int i = 0 ; i < lString.size() ; i++ )
	    	 {
	    		 m_cookies += lString.get( i ) ;
	    	 }
	    	 // 2.3���� �����۵����� �ʽ��ϴ� .���� �ڵ�� ��ó�մϴ�.
	    	 //Collections c = (Collections)imap.get( "Set-Cookie" ) ;
	    	 //m_cookies = c.toString( ) ;
	      
	    	 /// ������ ���������� 
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
			/// ���ѽð� ���� �ȳѾ��� ���� ���� �����Ŵ
			m_sessionTime = System.currentTimeMillis( ) ;
			return true ; 
		}
		else
		{
			/// ���ѽð��� �Ѱ��� ������ ������
			m_cookies = "" ;
			m_session = false ;
			return false ; 
		}
	}
	
	/// �Ķ���� ����  "������=������&" ����� �ؽ�Ʈ�� ��ȯ���ִ� �Լ�
	protected String buildParameters(Map<String, Object> params) throws IOException 
	{
		StringBuilder sb = new StringBuilder( ) ;
		
		/// �Ķ���Ͱ� ������ �׳� ��~ �Ѵ�
		if( params == null )
		{
			return "" ;
		}
		
		/// Ű�� ���� �������� ������
		for ( Iterator<String> i = params.keySet( ).iterator( ) ; i.hasNext( ) ; ) 
		{
			
			/// �Լ� �����δ� ������ �� �ʿ��ϳ�.
			/// ������=������&������=������&������=������ �̷� ������ String�� ����� ���� �۾��̴� 
			
			String key = ( String )i.next( ) ;
			sb.append( key ) ;
			sb.append( "=" ) ;
			sb.append( URLEncoder.encode( String.valueOf( params.get( key ) ), "UTF-8" ) ) ;
			
			/// ���� �� ������ &�� �־��ش�.
			if ( i.hasNext( ) )
			{
				sb.append( "&" ) ;
			}
		}
		
		/// ���� ���ڿ��� ��ȯ�Ѵ�.
		return sb.toString( ) ;
	}
	
}

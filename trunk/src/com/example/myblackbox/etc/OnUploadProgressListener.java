package com.example.myblackbox.etc;

public class OnUploadProgressListener {
	//Listener 객체
	private OnUploadProgressListener onDataListener = null;
	//Listener 인터페이스
	public interface OnUploadProgress{
		public abstract void onResult(Object _result);
	}
	//외부에서 Listener 등록 가능하게 노출되는 Method
	public void setOnUploadProgress(OnUploadProgressListener listener) {
		onDataListener = listener;
	}
	public void transferred(long length, long transferred) {
		// TODO Auto-generated method stub
		
	}
}

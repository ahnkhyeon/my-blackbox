package com.example.myblackbox.etc;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

public class CountingMultiPartEntity extends MultipartEntity {
	private OnUploadProgressListener listener_;
	private CountingOutputStream outputStream_;
	private OutputStream lastOutputStream_;

	public CountingMultiPartEntity() {
		super(HttpMultipartMode.BROWSER_COMPATIBLE);
	}

	public CountingMultiPartEntity(OnUploadProgressListener listener) {
		super(HttpMultipartMode.BROWSER_COMPATIBLE);
		listener_ = listener;
	}

	@Override
	public void writeTo(OutputStream out) throws IOException {
		if ((lastOutputStream_ == null) || (lastOutputStream_ != out)) {
			lastOutputStream_ = out;
			outputStream_ = new CountingOutputStream(out,
					this.getContentLength());
		}

		super.writeTo(outputStream_);
	}

	private class CountingOutputStream extends FilterOutputStream {
		private long length = 0;
		private long transferred = 0;
		private OutputStream wrappedOutputStream_;

		public CountingOutputStream(final OutputStream out, long length) {
			super(out);
			wrappedOutputStream_ = out;
			this.length = length;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			wrappedOutputStream_.write(b, off, len);
			transferred += len;
			listener_.transferred(length, transferred);
		}

		public void write(int b) throws IOException {
			super.write(b);
		}
	}
}

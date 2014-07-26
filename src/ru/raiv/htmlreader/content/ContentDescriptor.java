package ru.raiv.htmlreader.content;

import android.net.Uri;

public class ContentDescriptor {

	private Uri uri;
	private String filename;
	private String title;
	
	ContentDescriptor(String filename, String title, Uri uri)
	{
		this.filename=filename;
		this.title=title;
		this.uri=uri;
	}
	
	
	
	public Uri getUri() {
		return uri;
	}

	public String getFilename() {
		return filename;
	}

	public String getTitle() {
		return title;
	}

	
}

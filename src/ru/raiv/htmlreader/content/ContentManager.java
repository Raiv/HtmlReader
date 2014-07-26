package ru.raiv.htmlreader.content;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;

public class ContentManager {
	
	private static ContentManager instance;
	private static final String BOOK_DIR_NAME="book";
	private static final String BOOK_DIR_PATH=BOOK_DIR_NAME+File.separator;
	
	
	private Application app;
	private AssetManager assets; //packed book files
	private Set<String> extractedFiles; //unpacked files
	private String storagePath;
	private List<ContentDescriptor> bookFiles=new ArrayList<ContentDescriptor>();
	
	private static final String NL="\r\n";
	
	public static final String CONTENT_FILE="bookContent.html";
	public static final int CONTENT_INDEX=-1;
	public static final int TITLE_INDEX=0;
	
	
	private static final String CONTENT_START="<!DOCTYPE html>"+NL+
	"<html><head><meta charset=\"utf-8\"><title>Contents</title></head>"+NL+
	"<body><h1>"+"Содержание"+"</h1>"+NL;//TODO move to strings.xml
	
	private static final String CONTENT_PART="<p><a href=\"%s\">%s</a></p>"+NL;
	
	private static final String CONTENT_END="</body></html>";
	
	//private Uri contentUri=null;
	
	private int currentIndex=TITLE_INDEX;
	private double currentPos=0;
	
	private ContentDescriptor contents;
	
	private ContentManager(Application app)
	{
		this.app=app;
		assets=app.getAssets();
		String[] files=null;
		try {
			files =assets.list(BOOK_DIR_NAME);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File storage = app.getDir(BOOK_DIR_NAME, Application.MODE_PRIVATE);
		storagePath = storage.getAbsolutePath();
		extractedFiles = new TreeSet<String>(Arrays.asList(storage.list()));
		boolean contentChanged=false;
		//book files
		//TODO - check for data length to update properly
		try {
			for(String file : files)
			{
				
				if(!extractedFiles.contains(file))
				{
	
						extractData(file,storagePath);	
						extractedFiles.add(file);
						contentChanged=true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		//index list
		
		for(String file: extractedFiles)
			{
				if(!file.endsWith(".html"))  continue;
				if(file.equals(CONTENT_FILE))	continue;
				String title = getHtmlTitle(file);
				File f = new File(storagePath+File.separator+file);
				Uri uri = Uri.fromFile(f);
				ContentDescriptor desc = new ContentDescriptor(file,title,uri);
				bookFiles.add(desc);
			}
		
		//content file
		if(contentChanged)
		{
			try {
				makeContentList();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// content descriptor
		File f = new File(storagePath+File.separator+CONTENT_FILE);
		Uri uri = Uri.fromFile(f);
		contents= new ContentDescriptor(CONTENT_FILE,"Содержание",uri);
		
		
	}
	
	
	
	
	private static final String TITLE_TAG="<title>";
	
	private void makeContentList() throws IOException
	{
		BufferedWriter 	w= new BufferedWriter(new FileWriter(storagePath+File.separator+CONTENT_FILE ,false));		
		try{
			w.write(CONTENT_START);
			for(ContentDescriptor desc: bookFiles)
			{
				String nextPart=String.format(CONTENT_PART, desc.getUri().toString(),desc.getTitle());
				w.write(nextPart);
			}
			w.write(CONTENT_END);
		}finally{
			w.flush();
			w.close();
		}
	}
	
	
	private String getHtmlTitle(String file)
	{
		String titleText="";
		try {

			InputStream istr = null;
			  
			
			
			try{
				
				  istr = assets.open(BOOK_DIR_PATH+file);
				  //robust but short
				  String text = new Scanner( istr, "UTF-8" ).useDelimiter("\\A").next();
				  int start =text.indexOf(TITLE_TAG);
				  int textStart = text.indexOf('>',start)+1;
				  int textEnd=text.indexOf('<',textStart);
				  if(textEnd>textStart)
				  {
					  titleText=text.substring(textStart, textEnd).trim();
				  }
				
			}finally
			{
				if(istr!=null)
				istr.close();
			}	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
		//fallback if no title tag
		if(titleText.isEmpty())
		{
			titleText=file;
		}
		
		return titleText;
	}
	
	//webview could not work directly with assets, so we need to extract all data in app storage folder.
	// now only plain dir structure supported
	private void extractData(String fileToCreate,String storage) throws IOException
	{

		FileOutputStream o= null;
		InputStream i = null;
		try{
			o = new FileOutputStream(storage+File.separator+fileToCreate);
			i =assets.open(BOOK_DIR_PATH+fileToCreate);
			byte[] buffer = new byte[1024];
			int cnt;
			while ((cnt=i.read(buffer))>0)
			{
				o.write(buffer, 0, cnt);
			}
		}
		finally
		{
			if(i!=null)
				i.close();
			if(o!=null)
				o.close();
		}
	}
	
	
	public static ContentManager getInstance(Application app)
	{
		if(instance==null)
		{
			instance=new ContentManager(app);
		}
		else
		{
			if(instance.app!=app)
				throw new RuntimeException("Wrong application object for ContentManager!");
		}
		return instance;
	}


	public int getCurrentIndex() {
		return currentIndex;
	}


	public ContentDescriptor gotoPart(int newIndex) {
		ContentDescriptor desc = getDescriptor(newIndex);
		// after getDescripter because of IndexOutOfBounds exception
		if(currentIndex!=newIndex)
		{
			currentPos=0;
			currentIndex = newIndex;
		}
		
		return desc;
	}
	
	public boolean hasPrev()
	{
		return currentIndex>0;
	}
	
	public boolean hasNext()
	{
		return (currentIndex<bookFiles.size()-1) && (currentIndex!=CONTENT_INDEX);
	}


	public double getCurrentPos() {
		return currentPos;
	}


	public void setCurrentPos(double newPos) {
		currentPos = newPos;
	}
	
	public void setCurrentPos(int current, int max) {
		
		setCurrentPos(((double)current)/((double)max));
	}

	public ContentDescriptor getDescriptor(int idx) 
	{
		if(idx==CONTENT_INDEX)
			return contents;
		return bookFiles.get(idx);
	}


	private String POSITION="position";
	private String INDEX="index";
	
	
	
	public Bundle saveCurrentPos() {

		Bundle state = new Bundle();
		state.putDouble(POSITION, currentPos);
		state.putInt(INDEX, currentIndex);
		
		return state;
	}


	public void restoreCurrentPos(Bundle current) {
		if(current!=null)
		{
			currentIndex=current.getInt(INDEX, TITLE_INDEX);
			currentPos=  current.getDouble(POSITION,0);
		}
	}
	
	
}

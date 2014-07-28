package ru.raiv.htmlreader.content;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ru.raiv.htmlreader.R;



import android.app.Application;
import android.content.res.AssetManager;
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
	private TreeMap<String,Integer> bookLinks = new TreeMap<String,Integer>(); // to get current part by link
	
	private static final String NL="\r\n";
	
	public static final String CONTENT_FILE="bookContent.html";
	public static final int TITLE_INDEX=0;
	public static final int CONTENT_INDEX=-1;
	public static final int NOT_MINE=-666;
	
	
	
	private static final String CONTENT_START="<!DOCTYPE html>"+NL+
	"<html><head><meta charset=\"utf-8\"><title>%s</title></head>"+NL+
	"<body><h1>"+"%s"+"</h1>"+NL;//TODO move to strings.xml
	
	private static final String CONTENT_PART="<p><a href=\"%s\">%s</a></p>"+NL;
	
	private static final String CONTENT_END="</body></html>";
	
	//private Uri contentUri=null;
	
	private int currentIndex=TITLE_INDEX;
	private double currentPos=0;
	
	
	private int prevIndex=TITLE_INDEX;
	private double prevPos=0;
	
	
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
		
		@SuppressWarnings("deprecation")
		File storage = app.getDir(BOOK_DIR_NAME, Application.MODE_WORLD_READABLE);// I know it is deprecated. But this will allow users to see media provided along with book, like mp3 or mpeg4 files 
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
				bookLinks.put(uri.toString(), bookLinks.size());
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
		bookLinks.put(uri.toString(), CONTENT_INDEX);
		
	}
	
	
	
	
	private static final String TITLE_TAG="<title>";
	
	
 
	
	private void makeContentList() throws IOException
	{
		OutputStreamWriter 	w= new OutputStreamWriter(new FileOutputStream(storagePath+File.separator+CONTENT_FILE ,false),Charset.forName("UTF-8"));	
		String contentString = app.getResources().getString(R.string.contents);
		try{
			w.write(String.format(CONTENT_START,contentString,contentString));
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
			prevIndex=currentIndex;
			prevPos=currentPos;
			currentPos=0;
			currentIndex = newIndex;
		}
		
		return desc;
	}
	

	
	
	public int getIndexFromLink(String newUrl)
	{
		if(!bookLinks.containsKey(newUrl))
			return NOT_MINE;
		return bookLinks.get(newUrl);
		
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
	


	public ContentDescriptor getDescriptor(int idx) 
	{
		if(idx==CONTENT_INDEX)
			return contents;
		return bookFiles.get(idx);
	}


	private String POSITION="position";
	private String INDEX="index";
	private String PREV_POSITION="prev_position";
	private String PREV_INDEX="prev_index";
	
	
	
	private boolean needRestore=false;
	
	public void saveCurrentPos(Bundle state) {
		state.putDouble(POSITION, currentPos);
		state.putInt(INDEX, currentIndex); 
		state.putDouble(PREV_POSITION, prevPos);
		state.putInt(PREV_INDEX, prevIndex); 
		
	}


	public void restoreCurrentPos(Bundle current) {
		if(current!=null)
		{
			currentIndex=current.getInt(INDEX, TITLE_INDEX);
			currentPos=  current.getDouble(POSITION,0);
			prevIndex=current.getInt(PREV_INDEX, TITLE_INDEX);
			prevPos = current.getDouble(PREV_POSITION,0);
			needRestore=true;
		}
	}

	
	public boolean processHome()
	{
		if(currentIndex!=CONTENT_INDEX)
		{
			return false;
		}
		
		currentIndex=prevIndex;
		currentPos=prevPos;
		needRestore=true;
		return true;
		
	}

	public boolean isNeedRestore() {
		return needRestore;
	}


	public void setNeedRestore(boolean needRestore) {
		this.needRestore = needRestore;
	}
	
	
}

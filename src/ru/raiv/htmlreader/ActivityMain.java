package ru.raiv.htmlreader;

import ru.raiv.htmlreader.content.ContentDescriptor;
import ru.raiv.htmlreader.content.ContentManager;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMain extends Activity {

	
	
	 
	TextView textViewHeader;
	WebView webViewContent;
		
	Button buttonPrev;
	Button buttonNext;		 
	Button buttonHome;
	Button buttonContent;
	Button buttonLink;
	Button buttonAbout;
	
	ContentManager contentManager; 
	Resources res;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res=getApplication().getResources();
		setContentView(R.layout.activity_main);
		 
		textViewHeader= (TextView)findViewById(R.id.textViewHeader);
		webViewContent= (WebView)findViewById(R.id.webViewContent);
		
		
		webViewContent.setWebViewClient(webViewClient);
		webViewContent.setVerticalScrollBarEnabled(true);

		webViewContent.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				contentManager.setCurrentPos(calculateProgression(webViewContent));
				return false;
			}
		});
		
		buttonPrev=     (Button)findViewById(R.id.buttonPrev);
		buttonPrev.setOnClickListener(new OnNavigationClicked() {
			@Override
			protected int getContentIndex() {
				
				return contentManager.getCurrentIndex()-1;
			}
		});
		
		buttonNext=     (Button)findViewById(R.id.buttonNext);		 
		buttonNext.setOnClickListener(new OnNavigationClicked() {
			@Override
			protected int getContentIndex() {
				
				return contentManager.getCurrentIndex()+1;
			}
		});
		
		buttonHome=     (Button)findViewById(R.id.buttonHome);
		buttonHome.setOnClickListener(new OnNavigationClicked() {
			@Override
			protected int getContentIndex() {
				
				return ContentManager.TITLE_INDEX;
			}
		});
		
		buttonContent=  (Button)findViewById(R.id.buttonContent);
		buttonContent.setOnClickListener(new OnNavigationClicked() {
			@Override
			protected int getContentIndex() {
				
				return ContentManager.CONTENT_INDEX;
			}
		});
		
		buttonLink=     (Button)findViewById(R.id.buttonLink);
		buttonLink.setOnClickListener(new OnClickListener() {
			
			@Override   
			public void onClick(View v) {
				
				makeExternalRequest(res.getString(R.string.siteLinkUrl));
			}
		});
		buttonAbout=    (Button)findViewById(R.id.buttonAbout);
		buttonAbout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
				builder.setCancelable(true).setTitle(R.string.about).setMessage(R.string.aboutContent).setIcon(R.drawable.about_info_normal).create().show();
			}
		});
		 
		contentManager = ContentManager.getInstance(getApplication());
		contentManager.restoreCurrentPos(savedInstanceState);
	}

	
	WebViewClient webViewClient= new WebViewClient(){
		@Override
		public void onPageFinished (WebView view, String url){
			if (contentManager.isNeedRestore()) {
				contentManager.setNeedRestore(false);
	            webViewContent.postDelayed(new Runnable() {
	                @Override
	                public void run() {
	                    double webviewsize = webViewContent.getContentHeight() - webViewContent.getTop();
	                    double positionInWV = webviewsize * contentManager.getCurrentPos();
	                    int positionY = (int) Math.round(webViewContent.getTop() + positionInWV);
	                    webViewContent.scrollTo(0, positionY);
	                }
	            // Delay the scrollTo to make it work
	            }, 300);
			}
		}
		
		@Override
		public boolean  shouldOverrideUrlLoading (WebView view, String url)
		{
			int idx = contentManager.getIndexFromLink(url);
			if(idx!=ContentManager.NOT_MINE)
			{
				switchUiToPart( idx);
				return false;
			}
			makeExternalRequest(url);
			
			return true;
		}

		
	};	

	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	//    webViewContent.restoreState(savedInstanceState);
	}
	
	private abstract class OnNavigationClicked implements OnClickListener{

		protected abstract int getContentIndex(); 
		
		@Override
		public void onClick(View v) {
			displayDataForPart(getContentIndex()); 
		}
		
	}
	
	private double calculateProgression(WebView content) {
		double positionTopView = content.getTop();
		double contentHeight = content.getContentHeight();
		double currentScrollPosition = content.getScrollY();
		double percentWebview = (currentScrollPosition - positionTopView) / contentHeight;
	    return percentWebview;
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		
		
		contentManager.setCurrentPos(calculateProgression(webViewContent));
		contentManager.saveCurrentPos(outState);
	}

	
	
	@Override
	protected void onResume ()
	{
		super.onResume();
		displayDataForPart(contentManager.getCurrentIndex());
	}
	

	private ContentDescriptor switchUiToPart( int index)
	{
		ContentDescriptor cd = contentManager.gotoPart(index);
		textViewHeader.setText(cd.getTitle());
		buttonPrev.setEnabled(contentManager.hasPrev());
		buttonNext.setEnabled(contentManager.hasNext());
		return cd;
	}
	

	private void displayDataForPart(int index)
	{
		ContentDescriptor cd = switchUiToPart(index);
		webViewContent.loadUrl(cd.getUri().toString());
	}
	
	private void makeExternalRequest(String request)
	{
		Intent externalLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(request));
		try{
			startActivity(externalLinkIntent);
		} catch (ActivityNotFoundException e) {
		  Toast.makeText(this, "No application can handle this request,"
		    + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
		  
		}
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

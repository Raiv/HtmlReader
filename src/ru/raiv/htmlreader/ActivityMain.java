package ru.raiv.htmlreader;

import ru.raiv.htmlreader.content.ContentDescriptor;
import ru.raiv.htmlreader.content.ContentManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textViewHeader= (TextView)findViewById(R.id.textViewHeader);
		webViewContent= (WebView)findViewById(R.id.webViewContent);
		
		
		webViewContent.setWebViewClient(webViewClient);
		webViewContent.setVerticalScrollBarEnabled(true);

		webViewContent.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				int height = Math.max(webViewContent.getContentHeight()-webViewContent.getHeight(),1);// 1 to avoid division by zero
				int current = webViewContent.getScrollY();
				contentManager.setCurrentPos(current, height);
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
		buttonAbout=    (Button)findViewById(R.id.buttonAbout);

		
		contentManager = ContentManager.getInstance(getApplication());
		contentManager.restoreCurrentPos(savedInstanceState);
	}

	
	WebViewClient webViewClient= new WebViewClient(){
		@Override
		public void onPageFinished (WebView view, String url){
			int height = view.getContentHeight()-view.getHeight();
			int pos =Math.max( (int)( contentManager.getCurrentPos()*(double)height),0);
			
			view.scrollTo(0, pos);
		}



		
	};	

	
	private abstract class OnNavigationClicked implements OnClickListener{

		protected abstract int getContentIndex(); 
		
		@Override
		public void onClick(View v) {
			displayDataForIndex(getContentIndex()); 
		}
		
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState=contentManager.saveCurrentPos();
	}

	
	
	@Override
	protected void onResume ()
	{
		super.onResume();
		displayDataForIndex(contentManager.getCurrentIndex());
	}
	

	

	private void displayDataForIndex(int index)
	{
		ContentDescriptor cd = contentManager.gotoPart(index);
		textViewHeader.setText(cd.getTitle());
		webViewContent.loadUrl(cd.getUri().toString());
		buttonPrev.setEnabled(contentManager.hasPrev());
		buttonNext.setEnabled(contentManager.hasNext());
		
	}
	
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

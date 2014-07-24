package ru.raiv.htmlreader;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textViewHeader = (TextView)findViewById(R.id.textViewHeader);
		webViewContent= (WebView)findViewById(R.id.webViewContent);
				 
		buttonPrev= (Button)findViewById(R.id.buttonPrev);
		buttonNext= (Button)findViewById(R.id.buttonNext);		 
		buttonHome= (Button)findViewById(R.id.buttonHome);
		buttonContent= (Button)findViewById(R.id.buttonContent);
		buttonLink= (Button)findViewById(R.id.buttonLink);
		buttonAbout= (Button)findViewById(R.id.buttonAbout);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

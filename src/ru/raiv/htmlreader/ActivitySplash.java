package ru.raiv.htmlreader;

import ru.raiv.htmlreader.content.ContentManager;
import ru.russiaxxi.android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;



public class ActivitySplash extends Activity {

	
	private static final long SPLASH_DELAY=500;// ms
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
	
	}
	
	
	private long startTime=0; 
	
	@Override
	protected void onResume ()
	{
		startTime=System.currentTimeMillis();
		super.onResume();
		if(initializer.getStatus()==AsyncTask.Status.FINISHED)
		{
			initializer = new MyAsyncTask();
		}
		
		
		if(initializer.getStatus()==AsyncTask.Status.PENDING)// screen rotation error if not checking
			initializer.execute((Void[])null);
		
	}
	
	MyAsyncTask initializer = new MyAsyncTask();
	
	private class MyAsyncTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			@SuppressWarnings("unused")
			ContentManager cm = ContentManager.getInstance(getApplication());
			return null;
		}
		@Override
		protected void  onPostExecute(Void result)
		{
			
			long diff = System.currentTimeMillis()-startTime;
			
			if(diff>SPLASH_DELAY)// if initialization took longer time then splash should be seen
			{
				activitySwitcher.postAtFrontOfQueue(switchActivity);
			}else
			{
				activitySwitcher.postDelayed(switchActivity, SPLASH_DELAY-diff);
			}
			
			
		}
	};
	
	Handler activitySwitcher=new Handler();
	
	Runnable switchActivity= new Runnable(){
		@Override
		public void run() {
			  Intent intent = new Intent(ActivitySplash.this, ActivityMain.class);
			  startActivity(intent);
		}
	};
	
	
	
}

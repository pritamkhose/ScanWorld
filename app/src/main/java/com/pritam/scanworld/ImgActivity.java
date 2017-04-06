package com.pritam.scanworld;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileInputStream;

public class ImgActivity extends ActionBarActivity {
	
	String imgpath="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_img);
		
		Intent intent = getIntent();
		if(intent.getStringExtra("imgpath")!=null && intent.getStringExtra("imgpath").length()>0)
			imgpath = intent.getStringExtra("imgpath");
		
		try
		{
			(( ImageView ) findViewById(R.id.imageView)).setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(imgpath)));
			//(( ImageView ) findViewById(R.id.imageView)).setImageBitmap(Utility.decodeSampledBitmapFromFile(imgpath, 100, 100));
		}
		catch(Exception e)
		{
			Toast.makeText(ImgActivity.this, "imgpath = "+imgpath+"::"+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onBackPressed(){
		this.finish();
	    super.onBackPressed();  
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.blankmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			// Respond to the toolbar action bar's back button
			case android.R.id.home:
				if (getParentActivityIntent() == null) {
					//Log.i(TAG, "You have forgotten to specify the parentActivityName in the AndroidManifest!");
					onBackPressed();
				} else {
					NavUtils.navigateUpFromSameTask(this);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}

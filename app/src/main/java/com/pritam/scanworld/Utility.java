package com.pritam.scanworld;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;


public class Utility {



	public static void alert(Context context, String title, String msg)
	{
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		//alertDialog.setMessage(msg);
		if(msg.equals("Not subscribed, invalid access") || msg.equals("Invalid username/password, please try again") || msg.equals("Application installed on other mobile, multiple device login not allowed"))
			alertDialog.setMessage(msg);
		else if(msg.equals("Connection to Server lost due to network error"))
			alertDialog.setMessage("Internet connection not available, please check your internet connection and try again");
		else
			alertDialog.setMessage(msg);

		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			// here you can add functions
		}
	});
		//alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
	}
	


    public static boolean isConnected(Context context){ 
    	try{
    		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
       	 	NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); 
       	 	return activeNetworkInfo.isConnected(); 
    	}
    	catch(Exception e)
    	{
    		//System.out.println("isConnected:::ERROR::::"+e.getMessage());
    		return false;
    	}
    	 
    }

	public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}



	public static void SaveImage(Bitmap finalBitmap, String ImageName) {

		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/sbcrm");
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		//String fname = ImageName+ n +".jpg";
		String fname = ImageName;
		File file = new File (myDir, fname);
		if (file.exists ()) file.delete ();
		try {
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT) @SuppressLint("NewApi") public static int BitmapsizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else if (Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT){
			return data.getByteCount();
		} else{
			return data.getAllocationByteCount();
		}
	}

	public static void deleteFiles(String path) {
		File file = new File(path);
		if (file.exists()) {
			String deleteCmd = "rm -r " + path;
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec(deleteCmd);
			} catch (IOException e) { }
		}
	}


}

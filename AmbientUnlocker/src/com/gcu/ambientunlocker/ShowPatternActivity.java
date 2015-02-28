package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.haibison.android.lockpattern.LockPatternActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.WindowManager;

public class ShowPatternActivity extends Activity {

	private char[] savedPattern;
	
	private final int REQ_ENTER_PATTERN = 2;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
	    ComponentName componentInfo = taskInfo.get(0).topActivity;
	    if (!componentInfo.getClassName().contains("LockPatternActivity")){
	    	Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,getBaseContext(), LockPatternActivity.class);
	    	//intent.putExtra(LockPatternActivity.EXTRA_PATTERN, savedPattern);
	    	/*if (savedPattern != null){
        		System.out.println("pattern loaded from file: "+savedPattern);
        	}else{
        		System.out.println("pattern not loaded from file");
        	}*/
	    	startActivityForResult(intent, REQ_ENTER_PATTERN);
	    	//finish();
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent data) {
	   /// System.out.println("entered:"+data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN).toString());
		try{
			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
			Calendar c = Calendar.getInstance();
			Date d = new Date();
			c.setTime(d);
		
		
			File unlockFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_unlocks_usage.csv" );
			PrintWriter	unlockFile = new PrintWriter( new FileWriter( unlockFileName, true ) );
        	unlockFileName.setReadable(true, false);
       		unlockFileName.setWritable(true, false);
       		unlockFileName.setExecutable(true, false);

		
       		switch (requestCode) {
	    		case REQ_ENTER_PATTERN: {
	    			switch (resultCode) {
	        			case LockPatternActivity.RESULT_OK:{
	        				// The user passed
	        				System.out.println("*****Correct result");
	        				String temp = c.getTimeInMillis()+",end,"+d.toString()+",pattern";
    						unlockFile.println(temp);
    						unlockFile.close();
	        				finish();
	        				break;
	        			}
	        			case LockPatternActivity.RESULT_CANCELED:{
	        				System.out.println("*****Cancelled result");
	        				String temp = c.getTimeInMillis()+",error,"+d.toString()+",pattern";
    						unlockFile.println(temp);
    						unlockFile.close();
    						Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,getBaseContext(), LockPatternActivity.class);
	        				startActivityForResult(intent, REQ_ENTER_PATTERN);
	        				// The user cancelled the task
	        				break;
	        			}
	        			case LockPatternActivity.RESULT_FAILED:{
	        				// The user failed to enter the pattern
	        				String temp = c.getTimeInMillis()+",error,"+d.toString()+",pattern";
    						unlockFile.println(temp);
    						unlockFile.close();
	        				Intent intent = new Intent(LockPatternActivity.ACTION_COMPARE_PATTERN, null,getBaseContext(), LockPatternActivity.class);
	        				startActivityForResult(intent, REQ_ENTER_PATTERN);
	        				System.out.println("*****Failed result");
	        				break;
	        			}
	        			case LockPatternActivity.RESULT_FORGOT_PATTERN:
	        				// The user forgot the pattern and invoked your recovery Activity.
	        				break;
	    			}

	    			/*
	    		 	* In any case, there's always a key EXTRA_RETRY_COUNT, which holds
	    		 	* the number of tries that the user did.
	    		 	*/
	    			//int retryCount = data.getIntExtra(LockPatternActivity.EXTRA_RETRY_COUNT, 0);
	    			break;
	    		}// REQ_ENTER_PATTERN
	    	}
		
		}catch(Exception e){}
	}
	
	

}

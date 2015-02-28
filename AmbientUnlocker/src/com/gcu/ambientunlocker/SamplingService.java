package com.gcu.ambientunlocker;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.util.Config;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.Toast;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Date;

import com.haibison.android.lockpattern.LockPatternActivity;



public class SamplingService extends Service implements SensorEventListener {
	static final String LOG_TAG = "AMBIENTUNLOCKER";
	static final boolean KEEPAWAKE_HACK = false;
	static final boolean MINIMAL_ENERGY = false;
	static final long MINIMAL_ENERGY_LOG_PERIOD = 15000L;

	static String currentApp = "";
	static String phoneid ="";

	static Thread currentTouch = null;
	static Process proc=null;
	
    private SensorManager sensorManager;
    private HashMap captureFiles;
    private PrintWriter usageCaptureFile;
	private boolean samplingStarted = false;
	private ScreenOffBroadcastReceiver screenOffBroadcastReceiver = null;
	private long logCounter = 0;
	private Date samplingStartedTimeStamp;

	private KeyguardManager.KeyguardLock kl;
	private Long mStartRX;
	private Long mStartTX;


	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand( intent, flags, startId );
	//public void onCreate(){
		super.onCreate();
		
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
		kl = km.newKeyguardLock("MyKeyguardLock"); 

		
		
		System.out.println("************Starting Sampling service defined in Ambient unlock");
		
		try{
		
			screenOffBroadcastReceiver = new ScreenOffBroadcastReceiver();
			IntentFilter screenOffFilter = new IntentFilter();
			screenOffFilter.addAction( Intent.ACTION_SCREEN_OFF );
			screenOffFilter.addAction( Intent.ACTION_SCREEN_ON );
		
			registerReceiver( screenOffBroadcastReceiver, screenOffFilter );
			sensorManager = (SensorManager)getSystemService( SENSOR_SERVICE  );

		
		}catch(Exception e){e.printStackTrace();}
		
		//startSampling();

		return START_NOT_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();
		stopSampling();
		unregisterReceiver( screenOffBroadcastReceiver );
	}

	public IBinder onBind(Intent intent) {
		return null;	// cannot bind
	}

	// SensorEventListener
    public void onAccuracyChanged (Sensor sensor, int accuracy) {
    }

    private String getCurrentTopActivity() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        return ar.topActivity.getClassName().toString()+";"+ar.topActivity.getPackageName();
    }

    private String getApplicationNameFromPackage(String pack) {
    	PackageManager pm = getApplicationContext().getPackageManager();
    	ApplicationInfo ai;
    	try {
    		ai = pm.getApplicationInfo( pack, 0);
    	} catch (NameNotFoundException e) {
    		ai = null;
    	}
    	String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    	return applicationName;
    }
    public void onSensorChanged(SensorEvent sensorEvent) {
		++logCounter;
    	if( !MINIMAL_ENERGY ) {
    		String[] currentActivity = getCurrentTopActivity().split(";");
    		String foregroundapp = currentActivity[0].toString();

    		try{
    			PrintWriter tCaptureFile = (PrintWriter)captureFiles.get(sensorEvent.sensor.getName());

    			if( tCaptureFile != null ) {
    				Calendar c = Calendar.getInstance();
    				Date d = new Date();
    				c.setTime(d);
    				if (usageCaptureFile!= null){

    					if (!foregroundapp.equals(currentApp)){
    						if (currentApp.length() > 2){
    							long rxBytes = TrafficStats.getTotalRxBytes()- mStartRX;
    							long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;
    							String temp = c.getTimeInMillis()+",end,"+d.toString()+","+currentApp+","+rxBytes+","+txBytes;
    							usageCaptureFile.println(temp);
    						}
    						currentApp = foregroundapp;
    						mStartRX = TrafficStats.getTotalRxBytes();
    						mStartTX = TrafficStats.getTotalTxBytes();
    						Date d2 = new Date();
    						c.setTime(d2);
    						String temp = c.getTimeInMillis()+",start,"+d.toString()+","+foregroundapp+","+currentActivity[1].toString()+","+getApplicationNameFromPackage(currentActivity[1].toString());

    						usageCaptureFile.println(temp);
    					}
    				}
    				String tmp ="";

    				/*tmp = Long.toString( sensorEvent.timestamp)+","+ c.getTimeInMillis()+","+sensorEvent.sensor.getName();
    				for( int i = 0 ; i < sensorEvent.values.length ; ++i ) {
    					tmp = tmp +","+Float.toString( sensorEvent.values[i] );
    				}
    				tCaptureFile.println(tmp);

    				*/
    			}

    		}catch(Exception e){}
    	} else {
    		++logCounter;
    		
    	}
    }



	private void stopSampling() {
		if( !samplingStarted ){
			return;
		}
			
		Calendar c = Calendar.getInstance();
		Date d = new Date();
		c.setTime(d);
		long rxBytes = TrafficStats.getTotalRxBytes()- mStartRX;
		long txBytes = TrafficStats.getTotalTxBytes()- mStartTX;

		String temp = c.getTimeInMillis()+",end,"+d.toString()+","+currentApp+","+rxBytes+","+txBytes;
		usageCaptureFile.println(temp);

		/*
        if( sensorManager != null ) {
        	List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ORIENTATION);
        	for( int i = 0 ; i < sensors.size() ; ++i ){
    		    sensorManager.unregisterListener(this,sensors.get(i));
        	}
		}
        Iterator it = captureFiles.entrySet().iterator();

        while (it.hasNext()){
        	HashMap.Entry pairs = (HashMap.Entry)it.next();
        	PrintWriter tCaptureFile = (PrintWriter)pairs.getValue();
        	if (tCaptureFile != null){
        		tCaptureFile.flush();
        		tCaptureFile.close();
    			tCaptureFile = null;
        	}
        }*/
        captureFiles = null;
        if( usageCaptureFile != null ) {
        	usageCaptureFile.flush();
            usageCaptureFile.close();
			usageCaptureFile = null;
        }

		samplingStarted = false;
		Date samplingStoppedTimeStamp = new Date();
		long secondsEllapsed =
			( samplingStoppedTimeStamp.getTime() -
			  samplingStartedTimeStamp.getTime() ) / 1000L;
	}

	private void startSampling() {
		if( samplingStarted ){
			return;
		}

		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		captureFiles = new HashMap();

		mStartRX = TrafficStats.getTotalRxBytes();
		mStartTX = TrafficStats.getTotalTxBytes();

		/*List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ORIENTATION);

		for( int i = 0 ; i < sensors.size() ; ++i ){
			String tempname = sensors.get(i).getName().replaceAll(" ","_");
			File captureFileName = new File(getBaseContext().getFilesDir(), tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_"+tempname+".csv" );

           	 try {
           		 captureFiles.put(sensors.get(i).getName(), new PrintWriter( new FileWriter( captureFileName, true )));
           	 }catch( IOException ex ) {
           		 Log.e( LOG_TAG, ex.getMessage(), ex );
             }
           	 captureFileName.setReadable(true, false);
      	 	 captureFileName.setWritable(true, false);
      	 	 captureFileName.setExecutable(true, false);
      	 	
           	sensorManager.registerListener(this,sensors.get( i ),SensorManager.SENSOR_DELAY_UI );
        }*/
		
		samplingStartedTimeStamp = new Date();

		File usageCaptureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+"_usage.csv" );
        try {
        	usageCaptureFile = new PrintWriter( new FileWriter( usageCaptureFileName, true ) );
        }catch( IOException ex ) {
            Log.e( LOG_TAG, ex.getMessage(), ex );
        }
        usageCaptureFileName.setReadable(true, false);
       	usageCaptureFileName.setWritable(true, false);
       	usageCaptureFileName.setExecutable(true, false);
       	samplingStarted = true;
	}

	class ScreenOffBroadcastReceiver extends BroadcastReceiver {
		
		private String getCurrentDay(String dir, String userid){
    		String currday="";
    		try{
				File tempf = new File(dir+userid+"_tasklist.txt");
				if (tempf.exists()){
					BufferedReader br = new BufferedReader(new FileReader(tempf));
					String line;
					Date date = new Date();
			    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			    	
					while ((line = br.readLine()) != null) {
						String [] tempbuff = line.split(";");
						if (line.contains(dateFormat.format(date)) ){
				    		currday = tempbuff[0].split(" ")[0]+" "+tempbuff[0].split(" ")[1];
							break;
						}
					}
					br.close();
				}	
			}catch(Exception e){e.printStackTrace();}
    		return currday;
    	}
		
		public String tail( File file ) {
		    RandomAccessFile fileHandler = null;
		    try {
		        fileHandler = new RandomAccessFile( file, "r" );
		        long fileLength = fileHandler.length() - 1;
		        StringBuilder sb = new StringBuilder();

		        for(long filePointer = fileLength; filePointer != -1; filePointer--){
		            fileHandler.seek( filePointer );
		            int readByte = fileHandler.readByte();

		            if( readByte == 0xA ) {
		                if( filePointer == fileLength ) {
		                    continue;
		                }
		                break;

		            } else if( readByte == 0xD ) {
		                if( filePointer == fileLength - 1 ) {
		                    continue;
		                }
		                break;
		            }

		            sb.append( ( char ) readByte );
		        }

		        String lastLine = sb.reverse().toString();
		        return lastLine;
		    } catch( java.io.FileNotFoundException e ) {
		        e.printStackTrace();
		        return null;
		    } catch( java.io.IOException e ) {
		        e.printStackTrace();
		        return null;
		    } finally {
		        if (fileHandler != null )
		            try {
		                fileHandler.close();
		            } catch (IOException e) {
		                /* ignore */
		            }
		    }
		}

		
		
		public void onReceive(Context context, Intent intent) {
			
			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	    	String currentday = getCurrentDay(getBaseContext().getFilesDir()+"/", tm.getDeviceId());
	    	if (currentday.length() > 1){
	    	
			
			System.out.println("************received a broadcast receiver in Ambient unlock");

			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			    if( sensorManager != null && samplingStarted ) {
					stopSampling();
					currentApp = "";
				}

			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				
          		if ((tm.getCallState() == tm.CALL_STATE_OFFHOOK) || (tm.getCallState() == tm.CALL_STATE_RINGING)){
          			
          		}else{
          			
          			System.out.println("************Screen switched on !!!");				
          			
					CustomClassifiers custom=((CustomClassifiers)getApplicationContext());
		 	 		
			    	String filedir=getBaseContext().getFilesDir().getPath()+"/";
			    
			    	File tempf = new File(filedir+"savedPass.txt");
			    	String method1 = "";
			    	String method2 = "";
			    
			    	try{
			    		if (tempf.exists()){
			    			
			    			
			    			if (custom.getMethod1() == null && custom.getMethod2() == null){
			    			
			    				BufferedReader br = new BufferedReader(new FileReader(tempf));
			    				String line;
			            	
			    				while ((line = br.readLine()) != null) {
			    					try{
			    						String [] tempbuff = line.split("=");
			    						if (tempbuff[0].contains("method1")){
			    							method1 = tempbuff[1].toString();
			    							custom.setMethod1(method1);
			    						}
			    						if (tempbuff[0].contains("method2")){
			    							method2 = tempbuff[1].toString();
			    							custom.setMethod2(method2);
			    						}
			    					}catch(Exception e){}
			    				}
			    				br.close();
			    			}else{
			    				method1=custom.getMethod1();
			    				method2=custom.getMethod2();
			    			}
			    		
			    			Calendar c = Calendar.getInstance();
			    			Date d = new Date();
			    			c.setTime(d);
			    		
			    		
	    					File unlockFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_unlocks_usage.csv" );
	    					PrintWriter	unlockFile = new PrintWriter( new FileWriter( unlockFileName, true ) );
    					
	    					try{
	    						unlockFileName.setReadable(true, false);
	    						unlockFileName.setWritable(true, false);
	    						unlockFileName.setExecutable(true, false);
	    					}catch(Exception e){}
			    		
	    					// show method based on day
	    					//if (currentday.contains("Day 00") || currentday.contains("Day 01") || currentday.contains("Day 02") || currentday.contains("Day 03") || currentday.contains("Day 04") || currentday.contains("Day 05") ){

	    					if (currentday.contains("Day 00") || currentday.contains("Day 01") || currentday.contains("Day 02") || currentday.contains("Day 03") || currentday.contains("Day 04") || currentday.contains("Day 05") || currentday.contains("Day 06") || currentday.contains("Day 07")){
	    		       	
	    		       			if (method1.length()> 1){
	    		       				if (method1.equals("pattern")){
			    				
	    								String temp = c.getTimeInMillis()+",start,"+d.toString()+",pattern,Phase1";
	    								unlockFile.println(temp);
	    						
			    						kl.disableKeyguard(); 

			    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
			    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
			    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
			    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
			    						wakeLock.acquire();
			    						Intent i = new Intent();
			    		        		i.setClass(getBaseContext(), ShowPatternActivity.class);
			    		        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    		        		startActivity(i);
			    		        		//finish();
			    					}
			    					if (method1.equals("PIN")){
			    				
			    						String temp = c.getTimeInMillis()+",start,"+d.toString()+",PIN,Phase1";
	    								unlockFile.println(temp);
	    						
			    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
			    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
			    						kl.disableKeyguard(); 

			    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
			    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
			    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
			    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
			    						wakeLock.acquire();
			    				
			    						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			    					    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
			    					    ComponentName componentInfo = taskInfo.get(0).topActivity;
			    					    if (!componentInfo.getClassName().contains("PasscodeUnlockActivity")){
			    					    	Intent i = new Intent(getBaseContext(), PasscodeUnlockActivity.class);
				    	            		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    	            		startActivity(i);
			    					    }
			    					}
			    					if (method1.equals("no lock")){
			    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
			    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
			    						kl.reenableKeyguard();
			    					
			    						String temp = c.getTimeInMillis()+",start-end,"+d.toString()+",no lock,Phase1";
	    								unlockFile.println(temp);
			    					}
			    					unlockFile.close();		    			
	    		       			}
	    		       		}
	    					//if (currentday.contains("Day 08") || currentday.contains("Day 09") || currentday.contains("Day 10") || currentday.contains("Day 11") ){
		    		
	    		       		//if (currentday.contains("Day 06") || currentday.contains("Day 07") || currentday.contains("Day 08") ){

	    		       		if (currentday.contains("Day 08") || currentday.contains("Day 09") || currentday.contains("Day 10") || currentday.contains("Day 11") || currentday.contains("Day 12") || currentday.contains("Day 13") || currentday.contains("Day 14")){
	    		       			File fz = new File(getBaseContext().getFilesDir()+"/processedx/", tm.getDeviceId()+"_allwekaresults.txt");
	    		       			if (fz.exists()){
	    		       				String currentstate="";
	    		       				if (custom.getCurrentContext() == null){
	    		       					String lastline = tail(fz);
	    		       					String tempbuff[] = lastline.split(";");
	    		       					currentstate=tempbuff[tempbuff.length-1];
	    		       				}else{
	    		       					currentstate=String.valueOf(custom.getCurrentState());
	    		       				}
		    						if (currentstate.contains("false")){
		    							if (method1.length()> 1){
					    					if (method2.equals("pattern")){
					    				
			    								String temp = c.getTimeInMillis()+",start,"+d.toString()+",cs-pattern,Phase2";
			    								unlockFile.println(temp);
			    						
					    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
					    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
					    						kl.disableKeyguard(); 

					    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
					    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
					    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
					    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
					    						wakeLock.acquire();
					    					
					    						Intent i = new Intent();
					    		        		i.setClass(getBaseContext(), ShowPatternActivity.class);
					    		        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    		        		startActivity(i);
					    		        		//finish();
					    					}
					    					if (method2.equals("PIN")){
					    				
					    						String temp = c.getTimeInMillis()+",start,"+d.toString()+",cs-PIN,Phase2";
			    								unlockFile.println(temp);
			    						
					    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
					    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
					    						kl.disableKeyguard(); 

					    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
					    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
					    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
					    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
					    						wakeLock.acquire();
					    				
					    				
					    						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
					    					    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
					    					    ComponentName componentInfo = taskInfo.get(0).topActivity;
					    					    if (!componentInfo.getClassName().contains("PasscodeUnlockActivity")){
					    					    	Intent i = new Intent(getBaseContext(), PasscodeUnlockActivity.class);
						    	            		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						    	            		startActivity(i);
					    					    }
					    					}	
		    							}
		    						}else{
		    							/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
			    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
			    						kl.reenableKeyguard();

		    							String temp = c.getTimeInMillis()+",start-end,"+d.toString()+",cs-no lock,Phase2";
		    							unlockFile.println(temp);
		    						}
		    						unlockFile.close();
		    					}else{
		    						if (method1.length()> 1){
				    					if (method1.equals("pattern")){
				    				
		    								String temp = c.getTimeInMillis()+",start,"+d.toString()+",pattern,Phase1";
		    								unlockFile.println(temp);
		    						
				    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
				    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    						kl.disableKeyguard(); 

				    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
				    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
				    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
				    						wakeLock.acquire();
				    						Intent i = new Intent();
				    		        		i.setClass(getBaseContext(), ShowPatternActivity.class);
				    		        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    		        		startActivity(i);
				    		        		//finish();
				    					}
				    					if (method1.equals("PIN")){
				    				
				    						String temp = c.getTimeInMillis()+",start,"+d.toString()+",PIN,Phase1";
		    								unlockFile.println(temp);
		    						
				    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
				    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    						kl.disableKeyguard(); 

				    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
				    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
				    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
				    						wakeLock.acquire();
				    				
				    				
				    						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				    					    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
				    					    ComponentName componentInfo = taskInfo.get(0).topActivity;
				    					    if (!componentInfo.getClassName().contains("PasscodeUnlockActivity")){
				    					    	Intent i = new Intent(getBaseContext(), PasscodeUnlockActivity.class);
					    	            		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    	            		startActivity(i);
				    					    }
				    					}
				    					if (method1.equals("no lock")){
				    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
				    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    						kl.reenableKeyguard();
				    					
				    						String temp = c.getTimeInMillis()+",start-end,"+d.toString()+",no lock,Phase1";
		    								unlockFile.println(temp);
				    					}
				    					unlockFile.close();		    			
				    				}
		    					}
	    		       		}
	    					//if (currentday.contains("Day 12") || currentday.contains("Day 13") || currentday.contains("Day 14") || currentday.contains("Day 15") ){
	    		       		if (currentday.contains("Day 15") || currentday.contains("Day 16") || currentday.contains("Day 17") || currentday.contains("Day 18") || currentday.contains("Day 19") || currentday.contains("Day 20") || currentday.contains("Day 21")){
	    		       		//if (currentday.contains("Day 09") || currentday.contains("Day 10") || currentday.contains("Day 11") || currentday.contains("Day 12") ){
	    		       			
	    		       			File fzm = new File(  getBaseContext().getFilesDir(), tm.getDeviceId()+"_context_selections.dat");
	    		       			if (fzm.exists()){
		    						File fz = new File(  getBaseContext().getFilesDir()+"/processedx", tm.getDeviceId()+"_allwekaresults.txt");
		    						if (fz.exists()){
		    		       				String currentstate="";
		    		       				String readcontext="";
		    		       				if (custom.getCurrentContext() == null){
		    		       					String lastline = tail(fz);
		    		       					String tempbuff[] = lastline.split(";");
		    		       					String tempbuffrest[] = tempbuff[0].split(",");
		    		       					readcontext = tempbuffrest[tempbuffrest.length-1];
		    		       					currentstate=tempbuff[tempbuff.length-1];
		    		       				}else{
		    		       					currentstate=String.valueOf(custom.getCurrentState());
		    		       					readcontext = custom.getCurrentContext();
		    		       				}
		    		       					
		    		       				String method3="";
		    							//tm.getDeviceId()+"_context_selections.dat"
		    						
		    		       				BufferedReader br2 = new BufferedReader(new FileReader(fzm));
		    			    			String line2;
		    			            	
		    			    			while ((line2 = br2.readLine()) != null) {
		    			    				try{
		    			    					String [] tempbuff2 = line2.split("=");
		    			    					if (tempbuff2[0].contains(readcontext)){
		    			    						method3 = tempbuff2[1].toString();
		    			    						System.out.println(method3);
		    			    					}
		    			    				}catch(Exception e){}
		    			    			}
		    			    			br2.close();
		    			    			//System.out.println("readcontext:"+readcontext+", method3:"+method3);
		    							
		    			    			
		    							//"Location-sensitive","PIN","Pattern","No Unlock"
		    			    			if (method3.contains("Location-sensitive")){
		    			    				if (currentstate.contains("false")){
				    							if (method1.length()> 1){
							    					if (method2.equals("pattern")){
							    				
					    								String temp = c.getTimeInMillis()+",start,"+d.toString()+",cs-pattern,Phase3";
					    								unlockFile.println(temp);
					    						
							    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
							    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
							    						kl.disableKeyguard(); 

							    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
							    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
							    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
							    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
							    						wakeLock.acquire();
							    						Intent i = new Intent();
							    		        		i.setClass(getBaseContext(), ShowPatternActivity.class);
							    		        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							    		        		startActivity(i);
							    		        		//finish();
							    					}
							    					if (method2.equals("PIN")){
							    				
							    						String temp = c.getTimeInMillis()+",start,"+d.toString()+",cs-PIN,Phase3";
					    								unlockFile.println(temp);
					    						
							    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
							    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
							    						kl.disableKeyguard(); 

							    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
							    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
							    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
							    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
							    						wakeLock.acquire();
							    				
							    				
							    						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
							    					    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
							    					    ComponentName componentInfo = taskInfo.get(0).topActivity;
							    					    if (!componentInfo.getClassName().contains("PasscodeUnlockActivity")){
							    					    	Intent i = new Intent(getBaseContext(), PasscodeUnlockActivity.class);
								    	            		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								    	            		startActivity(i);
							    					    }
							    					}	
				    							}
				    						}else{
				    							/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
					    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
					    						kl.reenableKeyguard();
				    						
				    							String temp = c.getTimeInMillis()+",start-end,"+d.toString()+",cs-no lock,Phase3";
			    								unlockFile.println(temp);
				    						}
		    			    			}
		    			    			if (method3.contains("PIN")){
					    				
				    						String temp = c.getTimeInMillis()+",start,"+d.toString()+",PIN,Phase3";
		    								unlockFile.println(temp);
		    						
		    								/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
				    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    						kl.disableKeyguard(); 

				    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
				    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
				    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
				    						wakeLock.acquire();
				    				
				    				
				    						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				    					    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
				    					    ComponentName componentInfo = taskInfo.get(0).topActivity;
				    					    if (!componentInfo.getClassName().contains("PasscodeUnlockActivity")){
				    					    	Intent i = new Intent(getBaseContext(), PasscodeUnlockActivity.class);
					    	            		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    	            		startActivity(i);
				    					    }
		    			    			}
		    			    			if (method3.contains("pattern")){
		    								String temp = c.getTimeInMillis()+",start,"+d.toString()+",pattern,Phase3";
		    								unlockFile.println(temp);
		    						
				    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
				    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    						kl.disableKeyguard(); 

				    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
				    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
				    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
				    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
				    						wakeLock.acquire();
				    						Intent i = new Intent();
				    		        		i.setClass(getBaseContext(), ShowPatternActivity.class);
				    		        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    		        		startActivity(i);
				    		        		//finish();
		    			    			}
		    			    		
		    			    			if (method3.contains("No lock")){
		    			    				/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
				    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    						kl.reenableKeyguard();
		    			    			
				    						String temp = c.getTimeInMillis()+",start-end,"+d.toString()+",no lock,Phase3";
		    								unlockFile.println(temp);
		    			    			}
		    						}
			    					unlockFile.close();		    			
		    					}else{
		    						File fz = new File(  getBaseContext().getFilesDir()+"/processedx", tm.getDeviceId()+"_allwekaresults.txt");
			    					if (fz.exists()){
			    						String lastline = tail(fz);
			    						String tempbuff[] = lastline.split(";");
			    						if (tempbuff[tempbuff.length-1].contains("false")){
			    							if (method1.length()> 1){
						    					if (method2.equals("pattern")){
						    				
				    								String temp = c.getTimeInMillis()+",start,"+d.toString()+",cs-pattern,Phase2";
				    								unlockFile.println(temp);
				    						
						    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
						    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
						    						kl.disableKeyguard(); 

						    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
						    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
						    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
						    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
						    						wakeLock.acquire();
						    						Intent i = new Intent();
						    		        		i.setClass(getBaseContext(), ShowPatternActivity.class);
						    		        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						    		        		startActivity(i);
						    		        		//finish();
						    					}
						    					if (method2.equals("PIN")){
						    				
						    						String temp = c.getTimeInMillis()+",start,"+d.toString()+",cs-PIN,Phase2";
				    								unlockFile.println(temp);
				    						
						    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
						    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    								kl.disableKeyguard(); 

						    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
						    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
						    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
						    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
						    						wakeLock.acquire();
						    				
						    				
						    						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
						    					    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
						    					    ComponentName componentInfo = taskInfo.get(0).topActivity;
						    					    if (!componentInfo.getClassName().contains("PasscodeUnlockActivity")){
						    					    	Intent i = new Intent(getBaseContext(), PasscodeUnlockActivity.class);
							    	            		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							    	            		startActivity(i);
						    					    }
						    					}	
			    							}
			    						}else{
			    							/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
				    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
				    						kl.reenableKeyguard();
			    						
			    							String temp = c.getTimeInMillis()+",start-end,"+d.toString()+",cs-no lock,Phase2";
		    								unlockFile.println(temp);
			    						}
			    						unlockFile.close();
			    					}else{
			    						if (method1.length()> 1){
					    					if (method1.equals("pattern")){
					    				
			    								String temp = c.getTimeInMillis()+",start,"+d.toString()+",pattern,Phase1";
			    								unlockFile.println(temp);
			    						
					    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
					    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
					    						kl.disableKeyguard(); 

					    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
					    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
					    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
					    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
					    						wakeLock.acquire();
					    						Intent i = new Intent();
					    		        		i.setClass(getBaseContext(), ShowPatternActivity.class);
					    		        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    		        		startActivity(i);
					    		        		//finish();
					    					}
					    					if (method1.equals("PIN")){
					    				
					    						String temp = c.getTimeInMillis()+",start,"+d.toString()+",PIN,Phase1";
			    								unlockFile.println(temp);
			    						
					    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
					    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
					    						kl.disableKeyguard(); 

					    						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
					    						WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
					    				                                 | PowerManager.ACQUIRE_CAUSES_WAKEUP
					    				                                 | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
					    						wakeLock.acquire();
					    				
					    				
					    						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
					    					    List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
					    					    ComponentName componentInfo = taskInfo.get(0).topActivity;
					    					    if (!componentInfo.getClassName().contains("PasscodeUnlockActivity")){
					    					    	Intent i = new Intent(getBaseContext(), PasscodeUnlockActivity.class);
						    	            		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						    	            		startActivity(i);
					    					    }
					    					}
					    					if (method1.equals("no lock")){
					    						/*KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
					    						final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock"); */
					    						kl.reenableKeyguard();
					    					
					    						String temp = c.getTimeInMillis()+",start-end,"+d.toString()+",no lock,Phase1";
			    								unlockFile.println(temp);
					    					}
					    					unlockFile.close();		    			
					    				}
			    					}
			    				
		    					}
	    		       		}
			    		}
			    	}catch(Exception e){}
			    	
			    	samplingStarted = false;
			    	if( sensorManager != null){
						startSampling();
					}else{
						sensorManager = (SensorManager)getSystemService( SENSOR_SERVICE  );
						startSampling();
					}
			    	
			    	
			    	
          		}
			}

		}
	    	
		}
	}


}


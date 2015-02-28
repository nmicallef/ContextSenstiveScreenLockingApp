package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class HeartBeat extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    	String currentday = getCurrentDay(getBaseContext().getFilesDir()+"/", tm.getDeviceId());
    	if (currentday.length() > 1){
    		(new Thread(new Runnable() {
    			public void run() {
    				beat.run();
    				stopSelf();
    			}
    		})).start();
    	}
    }
    
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

    //public static final String PREF_FILE2 = "prefs2";
	//private static final String SAMPLING_SERVICE_POSITION_KEY = "samplingServicePositon";

    public Runnable beat = new Runnable() {

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

		
    	
    	private void checkThatEverythingIsSwitchedOn(int samplingServiceRate){
    		
    		String dir=getBaseContext().getFilesDir().getPath()+"/";
 			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
 		 	String userid=tm.getDeviceId();
 			String currentday=getCurrentDay(dir,userid);
			
    		// check accelerometer
 			try{
    			File tempf = new File(dir+userid+"_accelerometer_capture"+currentday+".csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 900000 ){
							Intent iAccelerometerService = new Intent(getApplicationContext(), Accelerometer.class);
	 		    	    	PendingIntent piAccelerometerService = PendingIntent.getService(getApplicationContext(), 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piAccelerometerService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piAccelerometerService);
						}
    				}
    			}
    		}catch(Exception e){}
 			
    		// check magneticfield
 			try{
    			File tempf = new File(dir+userid+"_magneticfield_capture"+currentday+".csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 900000 ){
							Intent iMagneticFieldService = new Intent(getApplicationContext(), MagneticField.class);
	 		    	    	PendingIntent piMagneticFieldService = PendingIntent.getService(getApplicationContext(), 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piMagneticFieldService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);
						}
    				}
    			}
    		}catch(Exception e){}
 			
 			// check noise
 			try{
    			File tempf = new File(dir+userid+"_noise_capture"+currentday+".csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 900000 ){
							Intent iNoiseService = new Intent(getApplicationContext(), Sound.class);
	 		    	    	PendingIntent piNoiseService = PendingIntent.getService(getApplicationContext(), 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piNoiseService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piNoiseService);
						}
    				}
    			}
    		}catch(Exception e){}
 			
  
    		// wifi
    		try{
    			File tempf = new File(dir+userid+"_wifi_capture"+currentday+".csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 900000 ){
							Intent iWifiService = new Intent(getApplicationContext(), Wifi.class);
	 		    	    	PendingIntent piWifiService = PendingIntent.getService(getApplicationContext(), 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
	 		    	    	AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
	 		    	    	alarmManager.cancel(piWifiService);
	 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);
						}
    				}
    			}
    		}catch(Exception e){}
    		
    		// light
    		try{
    			File tempf = new File(dir+userid+"_light_capture.csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
    					String [] tempbuff = lastline.split(",");
    					SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		Date lastdate = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
	            		Calendar c_lastdate = Calendar.getInstance();
						c_lastdate.setTime(lastdate);
						
						Calendar c_currdate = Calendar.getInstance();
						c_currdate.setTime(new Date());
						
						if ((c_currdate.getTimeInMillis()-c_lastdate.getTimeInMillis()) > 900000 ){
							Intent iLightService = new Intent(getApplicationContext(), Lightv3.class);
					    	PendingIntent piLightService = PendingIntent.getService(getApplicationContext(), 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
					    	AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					    	alarmManager.cancel(piLightService);
					    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);
						}
    				}
    			}
    		}catch(Exception e){}
    		
    	}
    	
    	/*private void checkRunningTimeOfProcess(){
    		ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker")) {
 					  if (service.service.getClassName().toLowerCase().contains("samplingservice") || service.service.getClassName().toLowerCase().contains("accelerometer") || service.service.getClassName().toLowerCase().contains("magneticfield") || service.service.getClassName().toLowerCase().contains("lightv3") || service.service.getClassName().toLowerCase().contains("rotation") || service.service.getClassName().toLowerCase().contains("sound")) {
 						  if (((android.os.SystemClock.elapsedRealtime() - service.activeSince)/1000) > 300){
 							  System.out.println(service.service.getClassName()+"   here");
 							  Intent ithisService = new Intent(getApplicationContext(), service.service.getClass());
 	 	 	 				  PendingIntent pithisService = PendingIntent.getService(getApplicationContext(), 0, ithisService, PendingIntent.FLAG_UPDATE_CURRENT);
 	 	 	 			      stopService(ithisService);
 						  }  
 					  }
 				  }
 			}
    	}*/
    	
    	
        public void run() {
        	      
        	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
 		    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
 		    boolean found = false;
 		    ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker")) {
 			          found = true;
 			      }
 			}
 			String message="";
 			if (found){
 			   message="Sensors are running!!!";
 			   
 			    boolean f = false;
 	 		    manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.samplingservice")) {
 	 			          f = true;
 	 			      }
 	 			}
 			   
 	 			if (f){
 	 				message="Sensors are running!!! aa";
 	 			}else{
 	 				message="Sensors are running!!! bb";
 	 				(new Thread(new Runnable() {
 	 		        	public void run() {
 	 		        		Intent i = new Intent();
 	 		        		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
 	 		        		getApplicationContext().startService( i );
 	 		        	}
 	 		        })).start();
 	 				
 	 				/*boolean flag = false;
 	 				manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 	 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	 	 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.dailyanalysis")) {
 	 	 			          flag = true;
 	 	 				  }
 	 	 			}
 	 	 			if (flag){
 	 	 				Intent iDailyAnalysisService = new Intent(getApplicationContext(), DailyAnalysis.class);
 	 	 				PendingIntent piDailyAnalysisService = PendingIntent.getService(getApplicationContext(), 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
 	 	 				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 	 	 			    alarmManager.cancel(piDailyAnalysisService);
 	 	 			    stopService(iDailyAnalysisService);
 	 	 			    
 	 	 			    System.out.println("killed iDailyAnalysisService");
 	 	 			    
 	 	 			    Calendar threeamCalendar = Calendar.getInstance();
		    		    threeamCalendar.set(Calendar.HOUR_OF_DAY, 2);
		    		    threeamCalendar.set(Calendar.MINUTE, 0);
		    		    threeamCalendar.set(Calendar.SECOND, 0);
		    		    iDailyAnalysisService = new Intent(getApplicationContext(), DailyAnalysis.class);
		    		    piDailyAnalysisService = PendingIntent.getService(getApplicationContext(), 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
		    		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    		    alarmManager.cancel(piDailyAnalysisService);
		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendar.getTimeInMillis() , 21600000, piDailyAnalysisService);
		    		    
 	 	 			}
 	 	 			
 	 	 			flag = false;
 	 				manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 	 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	 	 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.dailybucketgenerator")) {
 	 	 			          flag = true;
 	 	 				  }
 	 	 			}
 	 	 			if (flag){
 	 	 				Intent iDailyBGService = new Intent(getApplicationContext(), DailyBucketGenerator.class);
 	 	 				PendingIntent piDailyBGService = PendingIntent.getService(getApplicationContext(), 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
 	 	 				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 	 	 			    alarmManager.cancel(piDailyBGService);
 	 	 			    stopService(iDailyBGService);
 	 	 			    
 	 	 			    System.out.println("killed iDailyBGService");
 	 	 			    	
 	 	 			    Calendar twoamCalendar = Calendar.getInstance();
		    		    twoamCalendar.set(Calendar.HOUR_OF_DAY, 0);
		    		    twoamCalendar.set(Calendar.MINUTE, 0);
		    		    twoamCalendar.set(Calendar.SECOND, 0);
		    		    iDailyBGService = new Intent(getApplicationContext(), DailyBucketGenerator.class);
		    		    piDailyBGService = PendingIntent.getService(getApplicationContext(), 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
		    		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    		    alarmManager.cancel(piDailyBGService);
		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, twoamCalendar.getTimeInMillis() , 21600000, piDailyBGService);
		    		    
 	 	 			}
 	 	 			
 	 	 			// add create arff files
 	 	 			flag = false;
 	 				manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 	 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	 	 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.createarfffiles")) {
 	 	 			          flag = true;
 	 	 				  }
 	 	 			}
 	 	 			if (flag){
 	 	 				Intent iCreateArffFilesService = new Intent(getApplicationContext(), CreateArffFiles.class);
 	 	 				PendingIntent piCreateArffFilesService = PendingIntent.getService(getApplicationContext(), 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
 	 	 				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 	 	 			    alarmManager.cancel(piCreateArffFilesService);
 	 	 			    stopService(iCreateArffFilesService);
 	 	 			    
 	 	 			    System.out.println("killed iCreateArffFilesService");
 	 	 			    
 	 	 			    Calendar threeamCalendarb = Calendar.getInstance();
 	 	 			    threeamCalendarb.set(Calendar.HOUR_OF_DAY, 4);
 	 	 			    threeamCalendarb.set(Calendar.MINUTE, 0);
 	 	 			    threeamCalendarb.set(Calendar.SECOND, 0);
 	 	 			    iCreateArffFilesService = new Intent(getApplicationContext(), CreateArffFiles.class);
 	 	 			    piCreateArffFilesService = PendingIntent.getService(getApplicationContext(), 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
 	 	 			    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 	 	 			    alarmManager.cancel(piCreateArffFilesService);
 	 	 			    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendarb.getTimeInMillis() , 20*60000, piCreateArffFilesService);
 	 	 			}*/
 	 	 			
 	 	 			//fileuploader
 	 	 			Boolean flag = false;
 	 				manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 	 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	 	 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.filesuploader")) {
 	 	 			          flag = true;
 	 	 				  }
 	 	 			}
 	 	 			if (flag){
 	 	 				Intent iFilesUploaderService = new Intent(getApplicationContext(), FilesUploader.class);
 	 	 				PendingIntent piFilesUploaderService = PendingIntent.getService(getApplicationContext(), 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
 	 	 				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 	 	 			    alarmManager.cancel(piFilesUploaderService);
 	 	 			    stopService(iFilesUploaderService);
 	 	 			    
 	 	 			    System.out.println("killed iFilesUploaderService");   
 	 	 			 }
 	 	 			
 	 	 			
 	 	 			
 	 	 			flag = false;
 	 				manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
 	 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
 	 	 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.buildclassifiersforcontexts")) {
 	 	 			          flag = true;
 	 	 				  }
 	 	 			}
 	 	 			if (flag){
 	 	 				
 	 	 				Intent iBuildClassifiersService = new Intent(getApplicationContext(), BuildClassifiersForContexts.class);
 	 	 				stopService(iBuildClassifiersService);
 	 	 			    
 	 	 			    System.out.println("killed iBuildClassifiersService");
 	 	 			    
 	 	 			    (new Thread(new Runnable() {
 	 	 			    	public void run() {
 	 	 			    		Intent i = new Intent();
 	 	 			    		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.BuildClassifiersForContexts" );
 	 	 			    		getApplicationContext().startService( i );
 	 	 			    	}
 	 	 			    })).start();
 	 	 			    
 	 	 			}
 	 	 			
 	 	 			
 	 			}
 			    
 			}else{
 			   message="Sensors are switched off!!!";

 			    (new Thread(new Runnable() {
 		        	public void run() {
 		        		Intent i = new Intent();
 		         		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
 		        		getApplicationContext().startService( i );
 		        	}
 		        })).start();
 			    
 		    	Intent iHeartBeatService = new Intent(getApplicationContext(), HeartBeat.class);
 				PendingIntent piHeartBeatService = PendingIntent.getService(getApplicationContext(), 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
 				AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 			    alarmManager.cancel(piHeartBeatService);
 			    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piHeartBeatService);

 			    HashMap sensorsList = new HashMap();
 		        String filedir=getBaseContext().getFilesDir().getPath()+"/";
 		        int samplingServiceRate =1;
 		       
 		        try{
 		        	File tempfz = new File(filedir+"settings.txt");
 				
 		        	if (tempfz.exists()){
 		        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
 		        		String line;
 		    	    	while ((line = br.readLine()) != null) {
 					
 		        			String [] tempbuff = line.split(",");
 		        			if (tempbuff[0].equals("samplingServiceRate")){
 		        				samplingServiceRate = Integer.parseInt(tempbuff[1]);         				
 		    	    		}
 		    	    	}
 		    	    	br.close();
 		         			    
 		    	    	Intent iWifiService = new Intent(getApplicationContext(), Wifi.class);
 		    	    	PendingIntent piWifiService = PendingIntent.getService(getApplicationContext(), 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piWifiService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);
 		    	    	
 		    	    	Intent iBatteryLevelService = new Intent(getApplicationContext(), BatteryLevel.class);
 		    	    	PendingIntent piBatteryLevelService = PendingIntent.getService(getApplicationContext(), 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piBatteryLevelService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piBatteryLevelService);

 		    	    	Intent iRunningApplicationsService = new Intent(getApplicationContext(), RunningApplications.class);
 		    	    	PendingIntent piRunningApplicationsService = PendingIntent.getService(getApplicationContext(), 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piRunningApplicationsService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRunningApplicationsService);
 			   
 		    	    	Intent iMagneticFieldService = new Intent(getApplicationContext(), MagneticField.class);
 		    	    	PendingIntent piMagneticFieldService = PendingIntent.getService(getApplicationContext(), 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piMagneticFieldService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);
 		    	    	
 		    	    	Intent iLightService = new Intent(getApplicationContext(), Lightv3.class);
 		    	    	PendingIntent piLightService = PendingIntent.getService(getApplicationContext(), 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piLightService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);
 		    	    	
 		    	    	Intent iAccelerometerService = new Intent(getApplicationContext(), Accelerometer.class);
 		    	    	PendingIntent piAccelerometerService = PendingIntent.getService(getApplicationContext(), 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piAccelerometerService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piAccelerometerService);
 		    	    	
 		    	    	Intent iRotationService = new Intent(getApplicationContext(), Rotation.class);
 		    	    	PendingIntent piRotationService = PendingIntent.getService(getApplicationContext(), 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piRotationService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRotationService);
 		    	    	
 		    	    	Intent iNoiseService = new Intent(getApplicationContext(), Sound.class);
 		    	    	PendingIntent piNoiseService = PendingIntent.getService(getApplicationContext(), 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piNoiseService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piNoiseService);
 		    	    	
 		    	    	Intent iGatherLRService = new Intent(getApplicationContext(), GatherLatestReadings.class);
 		    	    	PendingIntent piGatherLRService = PendingIntent.getService(getApplicationContext(), 0, iGatherLRService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    	    	alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
 		    	    	alarmManager.cancel(piGatherLRService);
 		    	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piGatherLRService);
 		    	    	
 		    	    	/*
 		    	    	// 2am 
 		    		    // between 2 and 3 run schedule to generate buckets and full view (do this for days 5,6,7 & 8)
 		    		    Calendar twoamCalendar = Calendar.getInstance();
 		    		    twoamCalendar.set(Calendar.HOUR_OF_DAY, 2);
 		    		    twoamCalendar.set(Calendar.MINUTE, 0);
 		    		    twoamCalendar.set(Calendar.SECOND, 0);
 		    		    Intent iDailyBGService = new Intent(getApplicationContext(), DailyBucketGenerator.class);
 		    		    PendingIntent piDailyBGService = PendingIntent.getService(getApplicationContext(), 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    		    alarmManager.cancel(piDailyBGService);
 		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, twoamCalendar.getTimeInMillis() , 21600000, piDailyBGService);
 		    		    
 		    		    // 3-5am
 		    		    // this should only work between 3 and 5 am only (on days 5,6,7 & 8)
 		    		    Calendar threeamCalendara = Calendar.getInstance();
 		    		    threeamCalendara.set(Calendar.HOUR_OF_DAY, 3);
 		    		    threeamCalendara.set(Calendar.MINUTE, 0);
 		    		    threeamCalendara.set(Calendar.SECOND, 0);
 		    		    Intent iDailyAnalysisService = new Intent(getApplicationContext(), DailyAnalysis.class);
 		    		    PendingIntent piDailyAnalysisService = PendingIntent.getService(getApplicationContext(), 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    		    alarmManager.cancel(piDailyAnalysisService);
 		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendara.getTimeInMillis() , 21600000, piDailyAnalysisService);
 		    		    
 		    		    // 3-5am
 		    		    // this should run every 20 minutes until 7am (on day 8)
 		    		    Calendar threeamCalendarb = Calendar.getInstance();
 		    		    threeamCalendarb.set(Calendar.HOUR_OF_DAY, 3);
 		    		    threeamCalendarb.set(Calendar.MINUTE, 0);
 		    		    threeamCalendarb.set(Calendar.SECOND, 0);
 		    		    Intent iCreateArffFilesService = new Intent(getApplicationContext(), CreateArffFiles.class);
 		    		    PendingIntent piCreateArffFilesService = PendingIntent.getService(getApplicationContext(), 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    		    alarmManager.cancel(piCreateArffFilesService);
 		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendarb.getTimeInMillis() , 20*60000, piCreateArffFilesService);
 		    		    */
 		    		    // 5am
 		    		    Calendar fiveamCalendar = Calendar.getInstance();
 		    		    fiveamCalendar.set(Calendar.HOUR_OF_DAY, 6);
 		    		    fiveamCalendar.set(Calendar.MINUTE, 0);
 		    		    fiveamCalendar.set(Calendar.SECOND, 0);
 		    		    Intent iFilesUploaderService = new Intent(getApplicationContext(), FilesUploader.class);
 		    			PendingIntent piFilesUploaderService = PendingIntent.getService(getApplicationContext(), 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
 		    			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    		    alarmManager.cancel(piFilesUploaderService);
 		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fiveamCalendar.getTimeInMillis() , 43200000, piFilesUploaderService);
 		    	    	
 		    	    	
 		    		    Calendar eightpmCalendar = Calendar.getInstance();
 		    		    eightpmCalendar.set(Calendar.HOUR_OF_DAY, 20);
 		    		    eightpmCalendar.set(Calendar.MINUTE, 00);
 		    		    eightpmCalendar.set(Calendar.SECOND, 0);
 		    		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    		    Intent reminderintent = new Intent(getApplicationContext(), ReminderService.class);
 		    		    PendingIntent pireminder = PendingIntent.getService(getApplicationContext(), 0, reminderintent, PendingIntent.FLAG_UPDATE_CURRENT);
 		    		    alarmManager.cancel(pireminder);
 		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,eightpmCalendar.getTimeInMillis() ,86400000, pireminder);
 		    		    
 		    		    Calendar sevenamCalendar = Calendar.getInstance();
 		    		    sevenamCalendar.set(Calendar.HOUR_OF_DAY, 7);
 		    		    sevenamCalendar.set(Calendar.MINUTE, 00);
 		    		    sevenamCalendar.set(Calendar.SECOND, 0);
 		    		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		    		    Intent iStopStudyService = new Intent(getApplicationContext(), StopStudy.class);
 		    		    PendingIntent piStopStudyService = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
 		    		    alarmManager.cancel(piStopStudyService);
 		    		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,sevenamCalendar.getTimeInMillis() , 86400000, piStopStudyService);
 		        	}
 		        }catch(Exception e){e.printStackTrace();}

 		    }
 			
        	checkThatEverythingIsSwitchedOn(1);
        	//checkRunningTimeOfProcess();    
 			//Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
 			stopSelf();

        }
    };
}

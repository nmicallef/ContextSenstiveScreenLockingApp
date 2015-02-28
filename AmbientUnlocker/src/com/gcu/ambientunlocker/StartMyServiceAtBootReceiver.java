package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class StartMyServiceAtBootReceiver extends BroadcastReceiver {


	//public static final String PREF_FILE2 = "prefs2";
	//private static final String SAMPLING_SERVICE_POSITION_KEY = "samplingServicePositon";
    @Override
    public void onReceive(final Context context, Intent intent) {
    	
    	HashMap sensorsList = new HashMap();
        String filedir=context.getApplicationContext().getFilesDir().getPath()+"/";
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
        		
        	Intent iBatteryLevelService = new Intent(context, BatteryLevel.class);
        	PendingIntent piBatteryLevelService = PendingIntent.getService(context, 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
        	AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        	alarmManager.cancel(piBatteryLevelService);
        	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piBatteryLevelService);

        	Intent iRunningApplicationsService = new Intent(context, RunningApplications.class);
        	PendingIntent piRunningApplicationsService = PendingIntent.getService(context, 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
        	alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        	alarmManager.cancel(piRunningApplicationsService);
        	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRunningApplicationsService);

        	Intent iMagneticFieldService = new Intent(context, MagneticField.class);
    	    PendingIntent piMagneticFieldService = PendingIntent.getService(context, 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piMagneticFieldService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);
    	    	
    	    Intent iWifiService = new Intent(context, Wifi.class);
    	    PendingIntent piWifiService = PendingIntent.getService(context, 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piWifiService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);
    	    
    	    Intent iLightService = new Intent(context, Lightv3.class);
    	    PendingIntent piLightService = PendingIntent.getService(context, 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piLightService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);
    	    
    	    Intent iAccelerometerService = new Intent(context, Accelerometer.class);
    	    PendingIntent piAccelerometerService = PendingIntent.getService(context, 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piAccelerometerService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piAccelerometerService);
    	    
    	    Intent iRotationService = new Intent(context, Rotation.class);
    	    PendingIntent piRotationService = PendingIntent.getService(context, 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piRotationService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRotationService);
    	    
    	    Intent iNoiseService = new Intent(context, Sound.class);
    	    PendingIntent piNoiseService = PendingIntent.getService(context, 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piNoiseService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piNoiseService);
    	    
    	    Intent iGatherLRService = new Intent(context, GatherLatestReadings.class);
    	    PendingIntent piGatherLRService = PendingIntent.getService(context, 0, iGatherLRService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piGatherLRService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piGatherLRService);
    	    
    	    // 2am 
    	    // between 2 and 3 run schedule to generate buckets and full view (do this for days 5,6,7 & 8)
    	    /*Calendar twoamCalendar = Calendar.getInstance();
    	    twoamCalendar.set(Calendar.HOUR_OF_DAY, 2);
    	    twoamCalendar.set(Calendar.MINUTE, 0);
    	    twoamCalendar.set(Calendar.SECOND, 0);
    	    Intent iDailyBGService = new Intent(context, DailyBucketGenerator.class);
    	    PendingIntent piDailyBGService = PendingIntent.getService(context, 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piDailyBGService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, twoamCalendar.getTimeInMillis() , 21600000, piDailyBGService);
    	    
    	    // 3-5am
    	    // this should only work between 3 and 5 am only (on days 5,6,7 & 8)
    	    Calendar threeamCalendara = Calendar.getInstance();
    	    threeamCalendara.set(Calendar.HOUR_OF_DAY, 3);
    	    threeamCalendara.set(Calendar.MINUTE, 0);
    	    threeamCalendara.set(Calendar.SECOND, 0);
    	    Intent iDailyAnalysisService = new Intent(context, DailyAnalysis.class);
    	    PendingIntent piDailyAnalysisService = PendingIntent.getService(context, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piDailyAnalysisService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendara.getTimeInMillis() , 21600000, piDailyAnalysisService);
    	    
    	    // 3-5am
    	    // this should run every 20 minutes until 7am (on day 8)
    	    Calendar threeamCalendarb = Calendar.getInstance();
    	    threeamCalendarb.set(Calendar.HOUR_OF_DAY, 3);
    	    threeamCalendarb.set(Calendar.MINUTE, 0);
    	    threeamCalendarb.set(Calendar.SECOND, 0);
    	    Intent iCreateArffFilesService = new Intent(context, CreateArffFiles.class);
    	    PendingIntent piCreateArffFilesService = PendingIntent.getService(context, 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piCreateArffFilesService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendarb.getTimeInMillis() , 20*60000, piCreateArffFilesService);
    	    
    	    
    	    */
    	    // 5am
    	    Calendar fiveamCalendar = Calendar.getInstance();
    	    fiveamCalendar.set(Calendar.HOUR_OF_DAY, 6);
    	    fiveamCalendar.set(Calendar.MINUTE, 0);
    	    fiveamCalendar.set(Calendar.SECOND, 0);
    	    Intent iFilesUploaderService = new Intent(context, FilesUploader.class);
    		PendingIntent piFilesUploaderService = PendingIntent.getService(context, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
    		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    alarmManager.cancel(piFilesUploaderService);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fiveamCalendar.getTimeInMillis() , 43200000, piFilesUploaderService);
    	    

    	    Calendar eightpmCalendar = Calendar.getInstance();
    	    eightpmCalendar.set(Calendar.HOUR_OF_DAY, 20);
    	    eightpmCalendar.set(Calendar.MINUTE, 00);
    	    eightpmCalendar.set(Calendar.SECOND, 0);
    	    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    	    Intent reminderintent = new Intent(context, ReminderService.class);
    	    PendingIntent pendingIntent = PendingIntent.getService(context, 0, reminderintent, PendingIntent.FLAG_UPDATE_CURRENT);
    	    alarmManager.cancel(pendingIntent);
    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,eightpmCalendar.getTimeInMillis() , 86400000, pendingIntent);
	    
	    	(new Thread(new Runnable() {
        		public void run() {
        			Intent i = new Intent();
        			i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
        			context.startService( i );
        		}
        	})).start();


    		Intent iHeartBeatService = new Intent(context, HeartBeat.class);
			PendingIntent piHeartBeatService = PendingIntent.getService(context, 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	    	alarmManager.cancel(piHeartBeatService);
	    	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piHeartBeatService);
	    	
	    	Calendar sevenamCalendar = Calendar.getInstance();
		    sevenamCalendar.set(Calendar.HOUR_OF_DAY, 7);
		    sevenamCalendar.set(Calendar.MINUTE, 00);
		    sevenamCalendar.set(Calendar.SECOND, 0);
		    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    Intent iStopStudyService = new Intent(context, StopStudy.class);
		    PendingIntent piStopStudyService = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		    alarmManager.cancel(piStopStudyService);
		    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,sevenamCalendar.getTimeInMillis() , 86400000, piStopStudyService);
	    
        	}	
        }catch(Exception e){}    	
    	
    }
}

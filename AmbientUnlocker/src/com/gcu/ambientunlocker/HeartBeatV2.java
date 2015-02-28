package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class HeartBeatV2 extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent i, int startId) {
		this.beat.run();
		this.stopSelf();
	}

	
	private void switchOffAllIntents(){
		
    	Intent iHeartBeatService = new Intent(this, HeartBeatV2.class);
		PendingIntent piHeartBeatService = PendingIntent.getService(this, 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piHeartBeatService);

	    Intent iWifiService = new Intent(this, Wifi.class);
		PendingIntent piWifiService = PendingIntent.getService(this, 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piWifiService);

	    Intent iNoiseService = new Intent(this, Sound.class);
		PendingIntent piNoiseService = PendingIntent.getService(this, 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piNoiseService);
	    

	    Intent iBatteryLevelService = new Intent(this, BatteryLevel.class);
		PendingIntent piBatteryLevelService = PendingIntent.getService(this, 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piBatteryLevelService);

	    Intent iRunningApplicationsService = new Intent(this, RunningApplications.class);
		PendingIntent piRunningApplicationsService = PendingIntent.getService(this, 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piRunningApplicationsService);

	    Intent iMagneticFieldService = new Intent(this, MagneticField.class);
		PendingIntent piMagneticFieldService = PendingIntent.getService(this, 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piMagneticFieldService);

	    Intent iLightService = new Intent(this, Lightv3.class);
		PendingIntent piLightService = PendingIntent.getService(this, 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piLightService);

	    Intent iAccelerometerService = new Intent(this, Accelerometer.class);
		PendingIntent piAccelerometerService = PendingIntent.getService(this, 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piAccelerometerService);

	    Intent iRotationService = new Intent(this, Rotation.class);
		PendingIntent piRotationService = PendingIntent.getService(this, 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piRotationService);
	}
	
	private void switchOnAllIntents(){
		
		Intent iHeartBeatService = new Intent(getApplicationContext(), HeartBeatV2.class);
		PendingIntent piHeartBeatService = PendingIntent.getService(getApplicationContext(), 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(piHeartBeatService);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piHeartBeatService);
		
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
	    	
	    	
	    	    /*Calendar fouramCalendar = Calendar.getInstance();
	    	    //set the time to midnight tonight
	    	    fouramCalendar.set(Calendar.HOUR_OF_DAY, 4);
	    	    fouramCalendar.set(Calendar.MINUTE, 0);
	    	    fouramCalendar.set(Calendar.SECOND, 0);
	    	    Intent iFilesUploaderService = new Intent(this, FilesUploader.class);
	    	    //intent.putExtra("MyClass", obj);
	    		PendingIntent piFilesUploaderService = PendingIntent.getService(this, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
	    		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	    alarmManager.cancel(piFilesUploaderService);
	    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fouramCalendar.getTimeInMillis() , 21600000, piFilesUploaderService);

	    	    
	    	    //testing daily_analysis
	    	    Intent iDailyAnalysisService = new Intent(this, DailyAnalysis.class);
	    	    PendingIntent piDailyAnalysisService = PendingIntent.getService(this, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
	    	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    	    alarmManager.cancel(piDailyAnalysisService);
	    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piDailyAnalysisService);
	    	    */
	    	    // set reminders to 8pm
	    	    
	    	    /*Calendar eightpmCalendar = Calendar.getInstance();
	    	    eightpmCalendar.set(Calendar.HOUR_OF_DAY, 20);
	    	    eightpmCalendar.set(Calendar.MINUTE, 00);
	    	    eightpmCalendar.set(Calendar.SECOND, 0);
	    	    alarmManager = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
	    	    Intent intent = new Intent(this, ReminderService.class);
	    	    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    	    alarmManager.cancel(pendingIntent);
	    	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,eightpmCalendar.getTimeInMillis() , 21600000, pendingIntent);*/

	    	    

	        }
		}catch(Exception e){}
	}
	
	public Runnable beat = new Runnable() {
		public void run() {
			
			String message="";
			boolean f = false;
			ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
	 		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	 			if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.samplingservice")) {
	 				f = true;
	 			}
	 		}
			   
	 		if (f){
	 			message="Sensors are running!!!";
	 		}else{
	 			message="Sensors are not running!!!";
	 			(new Thread(new Runnable() {
	 		        public void run() {
	 		        	Intent i = new Intent();
	 		        	i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
	 		        	getApplicationContext().startService( i );
	 		        }
	 			})).start();
	 		}
			
	 		switchOffAllIntents();
	 		
	 		switchOnAllIntents();
	 		
		}
	};
}

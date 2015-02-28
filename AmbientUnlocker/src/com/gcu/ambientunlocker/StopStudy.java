package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class StopStudy extends Service {
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
	

    public Runnable beat = new Runnable() {
    	
    	public void run() {
        	try{
        	
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        		String filedir=getBaseContext().getFilesDir().getPath();
        		String currentday=getCurrentDay(filedir+"/",tm.getDeviceId());
        		
        		if (currentday.length() > 2){
        		
        			String previousday ="";
        			int prevday = Integer.valueOf(currentday.split(" ")[1]);
        			prevday = prevday-1;
    			
        			if (prevday > 24){
        				//stop everything
        			
        				Intent i = new Intent();
        	    		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
        	    	    stopService( i );        	    	    
        				
        	        	Intent iHeartBeatService = new Intent(getApplicationContext(), HeartBeat.class);
        				PendingIntent piHeartBeatService = PendingIntent.getService(getApplicationContext(), 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
        				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piHeartBeatService);

        			    Intent iWifiService = new Intent(getApplicationContext(), Wifi.class);
        				PendingIntent piWifiService = PendingIntent.getService(getApplicationContext(), 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piWifiService);

        			    Intent iNoiseService = new Intent(getApplicationContext(), Sound.class);
        				PendingIntent piNoiseService = PendingIntent.getService(getApplicationContext(), 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piNoiseService);
        			    
        			    /*Intent iGPSService = new Intent(this, GPS.class);
        				PendingIntent piGPSService = PendingIntent.getService(this, 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piGPSService);*/

        			    Intent iBatteryLevelService = new Intent(getApplicationContext(), BatteryLevel.class);
        				PendingIntent piBatteryLevelService = PendingIntent.getService(getApplicationContext(), 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piBatteryLevelService);

        			    Intent iRunningApplicationsService = new Intent(getApplicationContext(), RunningApplications.class);
        				PendingIntent piRunningApplicationsService = PendingIntent.getService(getApplicationContext(), 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piRunningApplicationsService);

        			    Intent iMagneticFieldService = new Intent(getApplicationContext(), MagneticField.class);
        				PendingIntent piMagneticFieldService = PendingIntent.getService(getApplicationContext(), 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piMagneticFieldService);

        			    Intent iLightService = new Intent(getApplicationContext(), Lightv3.class);
        				PendingIntent piLightService = PendingIntent.getService(getApplicationContext(), 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piLightService);

        			    Intent iAccelerometerService = new Intent(getApplicationContext(), Accelerometer.class);
        				PendingIntent piAccelerometerService = PendingIntent.getService(getApplicationContext(), 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piAccelerometerService);

        			    Intent iRotationService = new Intent(getApplicationContext(), Rotation.class);
        				PendingIntent piRotationService = PendingIntent.getService(getApplicationContext(), 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piRotationService);
        			    
        			    Intent iGatherLRService = new Intent(getApplicationContext(), GatherLatestReadings.class);
        				PendingIntent piGatherLRService = PendingIntent.getService(getApplicationContext(), 0, iGatherLRService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piGatherLRService);
        			    
        			    Intent iDailyBGService = new Intent(getApplicationContext(), DailyBucketGenerator.class);
        				PendingIntent piDailyBGService = PendingIntent.getService(getApplicationContext(), 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piDailyBGService);
        			    
        			    Intent iDailyAnalysisService = new Intent(getApplicationContext(), DailyAnalysis.class);
        				PendingIntent piDailyAnalysisService = PendingIntent.getService(getApplicationContext(), 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piDailyAnalysisService);

        			    
        			    Intent iCreateArffFilesService = new Intent(getApplicationContext(), CreateArffFiles.class);
        			    PendingIntent piCreateArffFilesService = PendingIntent.getService(getApplicationContext(), 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
        			    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piCreateArffFilesService);
        			    
        			    
        			    Intent iFilesUploaderService = new Intent(getApplicationContext(), FilesUploader.class);
        				PendingIntent piFilesUploaderService = PendingIntent.getService(getApplicationContext(), 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piFilesUploaderService);
        			    
        			    Intent iReminderService = new Intent(getApplicationContext(), ReminderService.class);
        				PendingIntent piReminderService = PendingIntent.getService(getApplicationContext(), 0, iReminderService, PendingIntent.FLAG_UPDATE_CURRENT);
        				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        			    alarmManager.cancel(piReminderService);
        			}
        		}else{
        			//stop everything
    				Intent i = new Intent();
    	    		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
    	    	    stopService( i );        	    	    
    				
    	        	Intent iHeartBeatService = new Intent(getApplicationContext(), HeartBeat.class);
    				PendingIntent piHeartBeatService = PendingIntent.getService(getApplicationContext(), 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
    				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piHeartBeatService);

    			    Intent iWifiService = new Intent(getApplicationContext(), Wifi.class);
    				PendingIntent piWifiService = PendingIntent.getService(getApplicationContext(), 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piWifiService);

    			    Intent iNoiseService = new Intent(getApplicationContext(), Sound.class);
    				PendingIntent piNoiseService = PendingIntent.getService(getApplicationContext(), 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piNoiseService);
    			    
    			    /*Intent iGPSService = new Intent(this, GPS.class);
    				PendingIntent piGPSService = PendingIntent.getService(this, 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piGPSService);*/

    			    Intent iBatteryLevelService = new Intent(getApplicationContext(), BatteryLevel.class);
    				PendingIntent piBatteryLevelService = PendingIntent.getService(getApplicationContext(), 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piBatteryLevelService);

    			    Intent iRunningApplicationsService = new Intent(getApplicationContext(), RunningApplications.class);
    				PendingIntent piRunningApplicationsService = PendingIntent.getService(getApplicationContext(), 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piRunningApplicationsService);

    			    Intent iMagneticFieldService = new Intent(getApplicationContext(), MagneticField.class);
    				PendingIntent piMagneticFieldService = PendingIntent.getService(getApplicationContext(), 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piMagneticFieldService);

    			    Intent iLightService = new Intent(getApplicationContext(), Lightv3.class);
    				PendingIntent piLightService = PendingIntent.getService(getApplicationContext(), 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piLightService);

    			    Intent iAccelerometerService = new Intent(getApplicationContext(), Accelerometer.class);
    				PendingIntent piAccelerometerService = PendingIntent.getService(getApplicationContext(), 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piAccelerometerService);

    			    Intent iRotationService = new Intent(getApplicationContext(), Rotation.class);
    				PendingIntent piRotationService = PendingIntent.getService(getApplicationContext(), 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piRotationService);
    			    
    			    Intent iGatherLRService = new Intent(getApplicationContext(), GatherLatestReadings.class);
    				PendingIntent piGatherLRService = PendingIntent.getService(getApplicationContext(), 0, iGatherLRService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piGatherLRService);
    			    
    			    Intent iDailyBGService = new Intent(getApplicationContext(), DailyBucketGenerator.class);
    				PendingIntent piDailyBGService = PendingIntent.getService(getApplicationContext(), 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piDailyBGService);
    			    
    			    Intent iDailyAnalysisService = new Intent(getApplicationContext(), DailyAnalysis.class);
    				PendingIntent piDailyAnalysisService = PendingIntent.getService(getApplicationContext(), 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piDailyAnalysisService);

    			    
    			    Intent iCreateArffFilesService = new Intent(getApplicationContext(), CreateArffFiles.class);
    			    PendingIntent piCreateArffFilesService = PendingIntent.getService(getApplicationContext(), 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
    			    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piCreateArffFilesService);
    			    
    			    
    			    Intent iFilesUploaderService = new Intent(getApplicationContext(), FilesUploader.class);
    				PendingIntent piFilesUploaderService = PendingIntent.getService(getApplicationContext(), 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piFilesUploaderService);
    			    
    			    Intent iReminderService = new Intent(getApplicationContext(), ReminderService.class);
    				PendingIntent piReminderService = PendingIntent.getService(getApplicationContext(), 0, iReminderService, PendingIntent.FLAG_UPDATE_CURRENT);
    				alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    			    alarmManager.cancel(piReminderService);
        		}
        		
        	}catch(Exception e){}
    	}
    };

}

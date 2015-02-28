package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class Wifi extends Service {


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
	    				try{
	    					Thread.sleep(15000);
	    				}catch(Exception e){}
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
	            // Do something
	        	try{
	        		final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

	        		try{
	        			if (wifi.isWifiEnabled() == false)
	        			{
	        				//Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
	        				wifi.setWifiEnabled(true);
	        			}
	        		}catch(Exception ex){
	        			Log.e( "wifi_writing", "problem enabling wifi: "+ex.getMessage(), ex );
	        		}

	        		try {

	        			TelephonyManager m_manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	        			if (m_manager.getDeviceId() != null){
	        				PrintWriter captureFile;
	        				File captureFileName = new File( getBaseContext().getFilesDir(), m_manager.getDeviceId()+"_celltower_capture.csv" );

	        				captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
               	 			Date d = new Date();
               	 			Calendar c = Calendar.getInstance();
               	 			c.setTime(d);
               	 			GsmCellLocation loc = (GsmCellLocation)m_manager.getCellLocation();
               	 			String temp = c.getTimeInMillis()+","+d.toString()+",";
               	 			if (loc != null)
               	 			{
               	 				temp = temp+ loc.getCid()+","+loc.getLac();
               	 			}
               	 			captureFile.println(temp);
               	 			//captureFile.println(SimpleCrypto.encrypt(SimpleCrypto.PASSWORD, temp));
               	 			captureFile.close();
               	 			captureFileName.setReadable(true, false);
               	 			captureFileName.setWritable(true, false);
               	 			captureFileName.setExecutable(true, false);
	        			}
	        		} catch( IOException ex ) {
	        			Log.e( "wifi_writing", ex.getMessage(), ex );
	        		}catch( Exception e ) {
	        			Log.e( "wifi_writing","cell tower exception: "+ e.getMessage(), e );
	        		}



	        		registerReceiver(new BroadcastReceiver()
	        		{
	        			@Override
	        			public void onReceive(Context c, Intent intent)
	        			{
	        				try{
	        					PrintWriter captureFile;
	        					PrintWriter captureFile2;
	        					final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	        					//File captureFileName = new File( "/data", tm.getDeviceId()+"_capture_"+c.getTimeInMillis()+".csv" );
	        					try {
	        						if (tm.getDeviceId() != null){
	        							String currentday = getCurrentDay(getBaseContext().getFilesDir()+"/", tm.getDeviceId());
	        							File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_wifi_capture"+currentday+".csv" );
	        							File captureFileName2 = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_wifi_capture_all.csv" );
	        						
	        							captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
	        							captureFile2 = new PrintWriter( new FileWriter( captureFileName2, true ) );
	        							Date d = new Date();
	        							Calendar cal = Calendar.getInstance();
	        							cal.setTime(d);
	        							List<ScanResult> results = wifi.getScanResults();
	        							String temp = cal.getTimeInMillis()+","+d.toString()+",";
	        							for (ScanResult result : results) {
	        								//Toast.makeText(this, result.SSID + " " + result.level,Toast.LENGTH_SHORT).show();
	        								temp =  temp + result.SSID + "," + result.BSSID+","+result.level+","+result.frequency+",";
	        							}



	        							captureFile.println(temp);
	        							captureFile.close();
	        							try{
	        								captureFileName.setReadable(true, false);
	                   	 					captureFileName.setWritable(true, false);
	                   	 					captureFileName.setExecutable(true, false);
	        							}catch(Exception e){}
	        							
	                   	 				captureFile2.println(temp);
	                   	 				captureFile2.close();
	                   	 				try{
	                   	 					captureFileName2.setReadable(true, false);
	                   	 					captureFileName2.setWritable(true, false);
	                   	 					captureFileName2.setExecutable(true, false);
	                   	 				}catch(Exception e){}	
	        						}
	        					} catch( IOException ex ) {
	        						Log.e( "wifi_writing", ex.getMessage(), ex );
	        					}catch( Exception e ) {
	        						Log.e( "wifi_writing", e.getMessage(), e );
	        					}
	        					
	        					c.unregisterReceiver(this);
	        				}catch(Exception e){}
	        				stopSelf();
	        			}
	        		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

	        		wifi.startScan();

                }catch(Exception ex){
                	Log.e( "wifi_writing", "onreceive error: "+ex.getMessage(), ex );
                }
	        }
	    };

}

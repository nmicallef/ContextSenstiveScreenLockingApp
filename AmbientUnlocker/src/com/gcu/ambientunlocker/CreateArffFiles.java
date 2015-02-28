package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class CreateArffFiles extends Service {
	@Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onStart(Intent i, int startId) {
    	(new Thread(new Runnable() {
        	public void run() {
        		beat.run();
    	        stopSelf();
        	}
        })).start();
    }

    
    public Runnable beat = new Runnable() {
    	
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
    	
    	private int getCount(String text){
    		Pattern p = Pattern.compile("0.0");
    		Matcher m = p.matcher(text);
    		int count = 0;
    		while (m.find()){
    			count +=1;
    		}
    		return count;
    	}
    	
    	private void prepareSecondMethod(String userid, String directory){
    		
    		try{
    			File tempf = new File(directory+"processedx/"+userid+"_profile_full_method1.csv");
    	    
    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    				String line;
    				HashMap contexts = new HashMap(); 
    				while ((line = br.readLine()) != null) {
    					try{
    						String [] tempbuff = line.split(",");
    						if (tempbuff.length> 1){
    							if (!contexts.containsKey(tempbuff[tempbuff.length-1])){
    								if (!tempbuff[tempbuff.length-1].equals("location")){
    									contexts.put(tempbuff[tempbuff.length-1], "");
    								}
    							}
    						}
    						
    					}catch(Exception e){}
    				}
    				
    				List<String> sortedContexts=new ArrayList(contexts.keySet());
    				Collections.sort(sortedContexts);
    				for (String loc : sortedContexts) {
    					System.out.println("contexts extracted from profile: "+loc);
    					
    					File fz=new File(directory+"processedx/");
    					if(fz.isDirectory()){
    						String files[]=  fz.list();
    						for(int i=0;i<files.length;i++){
    							if ((files[i].contains("_profile_full_method1") && files[i].contains(".csv"))){
    								File tempfz = new File(directory+"processedx/"+files[i]);
    								if (tempfz.exists()){
    									
    									try{
    										String thisfilename = files[i].replaceAll("_method1.csv", "_"+loc+"_method2.csv");
    										BufferedWriter allout = new BufferedWriter(new FileWriter(directory+"processedx/"+thisfilename));
    										BufferedReader brz = new BufferedReader(new FileReader(tempfz));
    										String linez;
    										while ((linez = brz.readLine()) != null) {
    				    						if (getCount(linez) <= 42 ){
    				    							String [] tempbuff = linez.split(",");
    												if (tempbuff.length> 1){
    													try{
    														if (!tempbuff[tempbuff.length-1].equals("location")){
    															if (!tempbuff[tempbuff.length-1].equals(loc)){
    																linez = linez.replaceAll(tempbuff[tempbuff.length-1], "therest");
    															}
    														}
    														allout.write(linez);
    														allout.newLine();
    													}catch(Exception e){}
    												}
    				    						}
    										}
    										allout.close();
    										brz.close();

    		    							try{
    		    			    				File captureFileName = new File(directory+"processedx/"+thisfilename);
    		    			    				captureFileName.setReadable(true, false);
    		    			       	 			captureFileName.setWritable(true, false);
    		    			       	 			captureFileName.setExecutable(true, false);
    		    			    			}catch(Exception e){}
    										
    									}catch(Exception e){e.printStackTrace();}
    								}
    							}
    						}
    					}
    				
    				
    				}
    				
    				
    			}
    		}catch(Exception e){}    	
    	
    	}
    	private void cleanupSecondMethod(String userid, String directory){
    		
    		try{
    		File fz=new File(directory+"processedx/");
    			if(fz.isDirectory()){
    			
    				String files[]=  fz.list();
    				for(int i=0;i<files.length;i++){
    					if ((files[i].contains("_profile_full_") && files[i].contains(".csv"))){
    						File tempf = new File(directory+"processedx/"+files[i]);
    						if (tempf.exists()){
    							CSVLoader loader = new CSVLoader();
    							loader.setSource(tempf);
    							Instances data = loader.getDataSet();
    					 
    							// save ARFF
    							ArffSaver saver = new ArffSaver();
    							saver.setInstances(data);
    							saver.setFile(new File(directory+"processedx/"+files[i].replaceAll(".csv",".arff")));
    							//saver.setDestination(new File(directory+samplingrate+"\\"+files[i].replaceAll(".csv",".arff")));
    							saver.writeBatch();
    						
    							System.out.println("just created: "+directory+"processedx/"+files[i].replaceAll(".csv",".arff"));
    							try{
    			    				File captureFileName = new File(directory+"processedx/", files[i].replaceAll(".csv",".arff") );
    			    				captureFileName.setReadable(true, false);
    			       	 			captureFileName.setWritable(true, false);
    			       	 			captureFileName.setExecutable(true, false);
    			    			}catch(Exception e){}
    						}
    					}
    				}
    			}
    		}catch(Exception e){}
    		    	
    	}
    	
    	public void run() {
        	try{
        		
        		String dir = getBaseContext().getFilesDir().getPath()+"/";
          		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
				String currentday=getCurrentDay(dir,tm.getDeviceId());

				int currday = Integer.parseInt(currentday.split(" ")[1]);
				// to fix both sets of numbers and last check as well
				if (currday > 7){
        		
					Date dt = new Date();
					
					if ((dt.getHours() >=3) && (dt.getHours() < 7)){
					
						if (tm.getDeviceId() != null){
							
							boolean flag = false;
							try{
								File tempf = new File(dir+"/processedx/"+tm.getDeviceId()+"_profileflag.txt");
								if (tempf.exists()){
									BufferedReader br = new BufferedReader(new FileReader(tempf));
									String line;
	            	
									while ((line = br.readLine()) != null) {
										if (line.contains("processed1")){
											flag = true;
										}
									}
									br.close();
								}
			
								if (flag){
									/*if (tm.getDeviceId().equals("358568057560165") && (currday == 8)){
										
										try{
											InputStream is = getAssets().open(tm.getDeviceId()+"_profile_full_home_method2.arff");
											BufferedReader br = new BufferedReader(new InputStreamReader(is));
											String line;
											PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_profile_full_home_method2.arff" ), false ) );
											while ((line = br.readLine()) != null) {
												allout.println(line);
											}
											br.close();
											allout.close();

										}catch(Exception e){}			
										
										try{
											InputStream is = getAssets().open(tm.getDeviceId()+"_profile_full_other_method2.arff");
											BufferedReader br = new BufferedReader(new InputStreamReader(is));
											String line;
											PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_profile_full_other_method2.arff" ), false ) );
											while ((line = br.readLine()) != null) {
												allout.println(line);
											}
											br.close();
											allout.close();

										}catch(Exception e){}			
										
										try{
											InputStream is = getAssets().open(tm.getDeviceId()+"_profile_full_transition_method2.arff");
											BufferedReader br = new BufferedReader(new InputStreamReader(is));
											String line;
											PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_profile_full_transition_method2.arff" ), false ) );
											while ((line = br.readLine()) != null) {
												allout.println(line);
											}
											br.close();
											allout.close();

										}catch(Exception e){}			
										
										try{
											InputStream is = getAssets().open(tm.getDeviceId()+"_profile_full_work_method2.arff");
											BufferedReader br = new BufferedReader(new InputStreamReader(is));
											String line;
											PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_profile_full_work_method2.arff" ), false ) );
											while ((line = br.readLine()) != null) {
												allout.println(line);
											}
											br.close();
											allout.close();

										}catch(Exception e){}			

										
									}else{
										prepareSecondMethod(tm.getDeviceId(),dir);
										System.gc();
										cleanupSecondMethod(tm.getDeviceId(),dir);
										System.gc();
									}*/
									
									prepareSecondMethod(tm.getDeviceId(),dir);
									System.gc();
									cleanupSecondMethod(tm.getDeviceId(),dir);
									System.gc();
									
									File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
									PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
									captureFile.println("processed2");
									captureFile.close();
									try{
										captureFileName.setReadable(true, false);
		           	 					captureFileName.setWritable(true, false);
		           	 					captureFileName.setExecutable(true, false);
									}catch(Exception e){}
									
									(new Thread(new Runnable() {
							        	public void run() {
							        		Intent i = new Intent();
							        		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.BuildClassifiersForContexts" );
							        		startService( i );
							        	}
							        })).start();
								}
								
								/*Intent iCreateArffFilesService = new Intent(getApplicationContext(), CreateArffFiles.class);
		 	 	 				PendingIntent piCreateArffFilesService = PendingIntent.getService(getApplicationContext(), 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
		 	 	 				AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		 	 	 			    alarmManager.cancel(piCreateArffFilesService);*/
								
							}catch(Exception e){e.printStackTrace();}
							
							
							
							
							// need to include a check which passes control from the previous method
							
							
							
						}
					}
				}
        		
        	}catch(Exception e){}
        	stopSelf();
    	}
    };

}

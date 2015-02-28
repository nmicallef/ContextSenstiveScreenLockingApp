package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class GatherLatestReadings extends Service {
	
	@Override
	public IBinder onBind(Intent arg0) {
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

		public String tail2( File file, int lines) {
		    java.io.RandomAccessFile fileHandler = null;
		    try {
		        fileHandler = 
		            new java.io.RandomAccessFile( file, "r" );
		        long fileLength = fileHandler.length() - 1;
		        StringBuilder sb = new StringBuilder();
		        int line = 0;

		        for(long filePointer = fileLength; filePointer != -1; filePointer--){
		            fileHandler.seek( filePointer );
		            int readByte = fileHandler.readByte();

		            if( readByte == 0xA ) {
		                line = line + 1;
		                if (line == lines) {
		                    if (filePointer == fileLength) {
		                        continue;
		                    }
		                    break;
		                }
		            } else if( readByte == 0xD ) {
		                line = line + 1;
		                if (line == lines) {
		                    if (filePointer == fileLength - 1) {
		                        continue;
		                    }
		                    break;
		                }
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
		    }
		    finally {
		        if (fileHandler != null )
		            try {
		                fileHandler.close();
		            } catch (IOException e) {
		            }
		    }
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
		
		private String retrieveTypeOfContext(String userid, String directory, String currentday){
    		HashMap wifi = new HashMap();
    		String returnedresult="";
    		
    		HashMap allbuckets = new HashMap();
    		try{
   				File tempf = new File(directory+"processedx/"+userid+"_buckets_details.txt");
    			
    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    				String line;
    	        	
    				while ((line = br.readLine()) != null) {
    					try{
    						String [] tempbuff = line.split(";");
    						if (!allbuckets.containsKey(tempbuff[0])){
    							allbuckets.put(tempbuff[0], new ArrayList());
    							System.out.println(tempbuff[0]);
    						}
    						
    						String allwif = "";
    						for (int z=1; z < tempbuff.length; z++){
    							if (!tempbuff[z].startsWith(",")){
    								allwif=allwif+tempbuff[z]+";";
    							}
    						}
    						ArrayList al = (ArrayList)allbuckets.get(tempbuff[0]);
    						al.add(allwif);
    						allbuckets.put(tempbuff[0], al);
    					}catch(Exception e){}
    				}
    				
    				br.close();
    			}
    		}catch(Exception e){}
    		
    		try{
    			File tempf = new File(directory+userid+"_wifi_capture"+currentday+".csv");
    			
    			if (tempf.exists()){
    				String connectedwifi ="-1";
    				if (userid.equals("354245052625194")){
    					ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    					NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    					if (networkInfo.isConnected()) {
    					    final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    					    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
    					    if (connectionInfo != null && connectionInfo.getSSID().length() >1) {
    					    	connectedwifi = connectionInfo.getSSID()+","+connectionInfo.getBSSID();
    					    }
    					}
    					
    					if (connectedwifi.equals("-1")){
    	    				String lastline = tail(tempf);
    	    				if (lastline.contains(",")){
    	    					String [] tempbuff = lastline.split(",");
    	    					String nameid = "";
    							for (int z =2; z < tempbuff.length; z=z+4){
    								if (z+4 <= tempbuff.length ){
    									tempbuff[z] = tempbuff[z].replace(" ","_");
    									tempbuff[z] = tempbuff[z].replace("'","_");
    									if (tempbuff[z].trim().length() > 1){
    										nameid=nameid+tempbuff[z].trim()+","+tempbuff[z+1].trim()+";";			
    									}
    								}
    							}
    							
    							String maxcontext="newplace";
    							int matchno=0;
    							List<String> sortedBuckets=new ArrayList(allbuckets.keySet());
    			    			Collections.sort(sortedBuckets);
    			    			for (String items : sortedBuckets) {
    			    				ArrayList allitems = (ArrayList) allbuckets.get(items);
    			    				for (int k=0; k < allitems.size(); k++){
    			    					String itemtocompare = allitems.get(k).toString();
    			    					String[] tpbuff = nameid.split(";"); 
    			    					int counter =0;
    			    					for (int z=0; z < tpbuff.length; z++){
    			    						if (itemtocompare.contains(tpbuff[z])){
    			    							counter++;
    			    						}
    			    					}
    			    					if (counter > 0){
    			    						if (counter>matchno){
    			    							maxcontext=items;
    			    							matchno=counter;
    			    						}
    			    					}
    			    				}
    			    			}
    		    				returnedresult = maxcontext;
    	    				}
    					}else{
    						String maxcontext="newplace";
							List<String> sortedBuckets=new ArrayList(allbuckets.keySet());
			    			Collections.sort(sortedBuckets);
			    			for (String items : sortedBuckets) {
			    				ArrayList allitems = (ArrayList) allbuckets.get(items);
			    				for (int k=0; k < allitems.size(); k++){
			    					String itemtocompare = allitems.get(k).toString();
			    					if (itemtocompare.contains(connectedwifi)){
			    						maxcontext=items;
			    					}			    					
			    				}
			    			}
		    				returnedresult = maxcontext;
    					}
    				}else{		
    					
    					String lastline = tail(tempf);
    					if (lastline.contains(",")){
    						String [] tempbuff = lastline.split(",");
    						String nameid = "";
							for (int z =2; z < tempbuff.length; z=z+4){
								if (z+4 <= tempbuff.length ){
									tempbuff[z] = tempbuff[z].replace(" ","_");
									tempbuff[z] = tempbuff[z].replace("'","_");
									if (tempbuff[z].trim().length() > 1){
										nameid=nameid+tempbuff[z].trim()+","+tempbuff[z+1].trim()+";";			
									}
								}
							}
						
							System.out.println(nameid);
							String maxcontext="newplace";
							int matchno=0;
							List<String> sortedBuckets=new ArrayList(allbuckets.keySet());
		    				Collections.sort(sortedBuckets);
		    				for (String items : sortedBuckets) {
		    					ArrayList allitems = (ArrayList) allbuckets.get(items);
		    					for (int k=0; k < allitems.size(); k++){
		    						String itemtocompare = allitems.get(k).toString();
		    						String[] tpbuff = nameid.split(";"); 
		    						int counter =0;
		    						for (int z=0; z < tpbuff.length; z++){
		    							if (itemtocompare.contains(tpbuff[z].split(",")[1])){
		    								counter++;
		    							}
		    						}
		    						if (counter > 0){
		    							if (counter>matchno){
		    								maxcontext=items;
		    								matchno=counter;
		    							}
		    						}
		    					}
		    				}
		    				
		    				System.out.println(maxcontext);
	    					returnedresult = maxcontext;
    					}
    				}
    			}   			
    		}catch(Exception e){e.printStackTrace();}
    		
    		return returnedresult;
    	}
    	
    	
    	private String collectAccelerometerData(String userid, String directory, String currentday){
    		HashMap accelerometer = new HashMap();
    		String returnedresult="";
    		
    		try{
    			File tempf = new File(directory+userid+"_accelerometer_capture"+currentday+".csv");
    			String lastitem="";
    			if (tempf.exists()){
    				String lastlines = tail2(tempf,15);
    				String lines[] = lastlines.split("\\r?\\n");
    				for (int i=0; i < lines.length; i++){
    					String line = lines[i];
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");
    	            	
    							SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    	            		
    							Date date = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
    							
        		    			
        						String currdate="";
    							int currminutes;
    							int currmonth = date.getMonth()+1;
    							if (currmonth < 10){
    								if (date.getDate() < 10){
    									currdate ="0"+currmonth+"/"+"0"+date.getDate();
    								}else{
    									currdate ="0"+currmonth+"/"+date.getDate();
    								}
    							}else{
    								if (date.getDate() < 10){
    									currdate =currmonth+"/"+"0"+date.getDate();
    								}else{
    									currdate =currmonth+"/"+date.getDate();
    								}
    							}
    							currminutes= date.getMinutes();
    	            		
    							String currhours = "";
    							if (date.getHours() < 10){
    								currhours ="0"+String.valueOf(date.getHours());
    							}else{
    								currhours =String.valueOf(date.getHours());
    							}
    	            		
    							double avalue1 = -10000.0;
    							double avalue2 = -10000.0;
    							double avalue3 = -10000.0;
    	            		
    							if (tempbuff.length > 5){
    								try{
    									avalue1 = Double.parseDouble(tempbuff[4]);
    									avalue2 = Double.parseDouble(tempbuff[5]);
    									avalue3 = Double.parseDouble(tempbuff[6]);
    									
    									if ((avalue1 != -10000.0) && (avalue2 != -10000.0) && (avalue3 != -10000.0)){
    										if (!accelerometer.containsKey(currdate))
    										{
    											accelerometer.put(currdate, new HashMap());
    										}
    										HashMap temphm = (HashMap) accelerometer.get(currdate);
    										
    										String thistime = "";
    										if (currminutes < 10){
    											thistime="0"+String.valueOf(currminutes);
    										}else{
    											thistime=String.valueOf(currminutes);
    										}
    										thistime=currhours+":"+thistime;
    										
    										if (!temphm.containsKey(thistime)){
    											temphm.put(thistime, new ArrayList());
    										}
    										ArrayList al = (ArrayList)temphm.get(thistime);
    										al.add(avalue1+","+avalue2+","+avalue3);
    										temphm.put(thistime,al);
    										accelerometer.put(currdate,temphm);
    										lastitem=currdate+";"+thistime;
    									}
    								}catch(Exception exx){}
        						}
    						}
    					}catch(Exception e){}
    				}
    				
    				//get lastitem and prepare it as a string
    				if (accelerometer.containsKey(lastitem.split(";")[0])){
    					HashMap accdetails = (HashMap) accelerometer.get(lastitem.split(";")[0]);
    					if (accdetails.containsKey(lastitem.split(";")[1])){
    						ArrayList al_acc = (ArrayList)accdetails.get(lastitem.split(";")[1]);
    						returnedresult = computeAverageMultipleValues(al_acc);
    					}
    				}
    			}

    			    			
    		}catch(Exception e){e.printStackTrace();}
    		
    		return returnedresult;
    	}
    	
    	private String collectLightData(String userid, String directory, String currentday){
    		HashMap light = new HashMap();
    		String returnedresult="";
    		
    		try{
    			File tempf = new File(directory+userid+"_light_capture"+currentday+".csv");
    			String lastitem="";
    			if (tempf.exists()){
    				String lastlines = tail2(tempf,15);
    				String lines[] = lastlines.split("\\r?\\n");
    				for (int i=0; i < lines.length; i++){
    					String line = lines[i];
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");
    	            	
    							SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    	            		
    							Date date = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
    							
        		        		
        						String currdate="";
    							int currminutes;
    							int currmonth = date.getMonth()+1;
    							if (currmonth < 10){
    								if (date.getDate() < 10){
    									currdate ="0"+currmonth+"/"+"0"+date.getDate();
    								}else{
    									currdate ="0"+currmonth+"/"+date.getDate();
    								}
    							}else{
    								if (date.getDate() < 10){
    									currdate =currmonth+"/"+"0"+date.getDate();
    								}else{
    									currdate =currmonth+"/"+date.getDate();
    								}
    							}
    							currminutes= date.getMinutes();
    	            		
    							String currhours = "";
    							if (date.getHours() < 10){
    								currhours ="0"+String.valueOf(date.getHours());
    							}else{
    								currhours =String.valueOf(date.getHours());
    							}
    	            		
    							try{
    								double lvalue = -1.0;
    								if (tempbuff.length > 6){
    									try{
    										lvalue = Double.parseDouble(tempbuff[6]);
    									}catch(Exception exx){}
    								}else{
    									try{
    										lvalue = Double.parseDouble(tempbuff[3]);
    									}catch(Exception exx){}
    								}
    			        		
    								if (!light.containsKey(currdate))
    								{
    									light.put(currdate, new HashMap());
    								}
    								HashMap temphm = (HashMap) light.get(currdate);
    							
    								String thistime = "";
    								if (currminutes < 10){
    									thistime="0"+String.valueOf(currminutes);
    								}else{
    									thistime=String.valueOf(currminutes);
    								}
    								thistime=currhours+":"+thistime;
    							
    								if (!temphm.containsKey(thistime)){
    									temphm.put(thistime, new ArrayList());
    								}
    								ArrayList al = (ArrayList)temphm.get(thistime);
    								al.add(lvalue);
    								temphm.put(thistime,al);
    								light.put(currdate,temphm);
    								lastitem=currdate+";"+thistime;
    							}catch(Exception e){}
        					}
    					}catch(Exception e){}
    				}
    				//get lastitem and prepare it as a string
    				if (light.containsKey(lastitem.split(";")[0])){
    					HashMap lightdetails = (HashMap) light.get(lastitem.split(";")[0]);
    					if (lightdetails.containsKey(lastitem.split(";")[1])){
    						ArrayList al_light = (ArrayList)lightdetails.get(lastitem.split(";")[1]);
    						returnedresult = computeAverageSingleValue(al_light);
    					}
    				}
    				
    			}

    			
    			
    		}catch(Exception e){e.printStackTrace();}
    		
    		return returnedresult;
    	}
    	
    	private String collectNoiseData(String userid, String directory, String currentday){
    		HashMap noise = new HashMap();
    		String returnedresult="";
    		try{
    			File tempf = new File(directory+userid+"_noise_capture"+currentday+".csv");
    			
    			if (tempf.exists()){
    				
    				String lastline = tail(tempf);
    				if (lastline.contains(",")){
						String [] tempbuff = lastline.split(",");
						
						SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
	            		
						Date date = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
						
						String currdate="";
						int currminutes;
						int currmonth = date.getMonth()+1;
						if (currmonth < 10){
							if (date.getDate() < 10){
								currdate ="0"+currmonth+"/"+"0"+date.getDate();
							}else{
								currdate ="0"+currmonth+"/"+date.getDate();
							}
						}else{
							if (date.getDate() < 10){
								currdate =currmonth+"/"+"0"+date.getDate();
							}else{
								currdate =currmonth+"/"+date.getDate();
							}
						}
						currminutes= date.getMinutes();
            		
						String currhours = "";
						if (date.getHours() < 10){
							currhours ="0"+String.valueOf(date.getHours());
						}else{
							currhours =String.valueOf(date.getHours());
						}
						String thistime = "";
            			if (currminutes < 10){
            				thistime="0"+String.valueOf(currminutes);
            			}else{
            				thistime=String.valueOf(currminutes);
            			}
            			thistime=currhours+":"+thistime;
            			String lastitem= currdate+";"+thistime;
						
						ArrayList avgnoise = new ArrayList();
	            		for (int z=2; z < tempbuff.length; z++){
	            			try{
	            				avgnoise.add(Double.parseDouble(tempbuff[z]));
	            			}catch(Exception exx){exx.printStackTrace();}
	            		}
	            		
	            		if ((avgnoise.size() > 0)){
	            			returnedresult = lastitem.split(";")[0].split("/")[0].toString()+","+lastitem.split(";")[0].split("/")[1].toString()+","+
	            			lastitem.split(";")[1].split(":")[0].toString()+","+lastitem.split(";")[1].split(":")[1].toString()+",";
	            			returnedresult = returnedresult +computeAverageSingleValue(avgnoise);
	            		}
    				}
    			}
    			
    		}catch(Exception e){e.printStackTrace();}
    		
    		return returnedresult;
    	}
    	
    	
    	private String collectMagneticFieldData(String userid, String directory, String currentday){
    		HashMap magneticfield = new HashMap();
    		String returnedresult="";
    		
    		try{
    			File tempf = new File(directory+userid+"_magneticfield_capture"+currentday+".csv");
    			String lastitem="";
    			if (tempf.exists()){
    				String lastlines = tail2(tempf,15);
    				String lines[] = lastlines.split("\\r?\\n");
    				for (int i=0; i < lines.length; i++){
    					String line = lines[i];
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");
    	            	
    							SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    	            		
    							Date date = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);
    							
    							
        						String currdate="";
    							int currminutes;
    							int currmonth = date.getMonth()+1;
    							if (currmonth < 10){
    								if (date.getDate() < 10){
    									currdate ="0"+currmonth+"/"+"0"+date.getDate();
    								}else{
    									currdate ="0"+currmonth+"/"+date.getDate();
    								}
    							}else{
    								if (date.getDate() < 10){
    									currdate =currmonth+"/"+"0"+date.getDate();
    								}else{
    									currdate =currmonth+"/"+date.getDate();
    								}
    							}
    							currminutes= date.getMinutes();
    	            		
    							String currhours = "";
    							if (date.getHours() < 10){
    								currhours ="0"+String.valueOf(date.getHours());
    							}else{
    								currhours =String.valueOf(date.getHours());
    							}
    	            		
    							double mvalue1 = -10000.0;
    							double mvalue2 = -10000.0;
    							double mvalue3 = -10000.0;
    	            		
    							if (tempbuff.length > 5){
    								try{
    									mvalue1 = Double.parseDouble(tempbuff[3]);
    									mvalue2 = Double.parseDouble(tempbuff[4]);
    									mvalue3 = Double.parseDouble(tempbuff[5]);
    									
    									if ((mvalue1 != -10000.0) && (mvalue2 != -10000.0) && (mvalue3 != -10000.0)){
    										if (!magneticfield.containsKey(currdate))
    										{
    											magneticfield.put(currdate, new HashMap());
    										}
    										HashMap temphm = (HashMap) magneticfield.get(currdate);
    										
    										String thistime = "";
    										if (currminutes < 10){
    											thistime="0"+String.valueOf(currminutes);
    										}else{
    											thistime=String.valueOf(currminutes);
    										}
    										thistime=currhours+":"+thistime;
    										
    										if (!temphm.containsKey(thistime)){
    											temphm.put(thistime, new ArrayList());
    										}
    										ArrayList al = (ArrayList)temphm.get(thistime);
    										al.add(mvalue1+","+mvalue2+","+mvalue3);
    										temphm.put(thistime,al);
    										magneticfield.put(currdate,temphm);
    										lastitem= currdate+";"+thistime;
    									}
    								}catch(Exception exx){}
    							}
        						
    						}
    					}catch(Exception e){}
    				}
    				
    				//get lastitem and prepare it as a string
    				if (magneticfield.containsKey(lastitem.split(";")[0])){
    					HashMap mfdetails = (HashMap) magneticfield.get(lastitem.split(";")[0]);
    					if (mfdetails.containsKey(lastitem.split(";")[1])){
    						ArrayList al_mf = (ArrayList)mfdetails.get(lastitem.split(";")[1]);
    						returnedresult = computeAverageMultipleValues(al_mf);
    					}
    				}
    			}

    			
    		}catch(Exception e){e.printStackTrace();}
    		
    		return returnedresult;
    	}
		
    	private String computeAverageSingleValue(ArrayList al){
    		String result="";
    		
    		DescriptiveStatistics stats = new DescriptiveStatistics();
    		
    		if (al!= null){
    			for (int i =0; i < al.size(); i++){
        			double curr = (Double)al.get(i);
        			stats.addValue(curr);
        		}

    			result = stats.getMean()+","+mode(stats.getValues())+","+stats.getPercentile(50)+","+stats.getStandardDeviation()+","+stats.getMin()+","+stats.getMax()+","+(stats.getMax()-stats.getMin());
    			
        	}
        	return result;
        }
    	
    	private String computeAverageMultipleValues(ArrayList al){
    		String result="";
    		if (al!= null){
    			
    			DescriptiveStatistics datastats1 = new DescriptiveStatistics();
    			DescriptiveStatistics datastats2 = new DescriptiveStatistics();
    			DescriptiveStatistics datastats3 = new DescriptiveStatistics();
    			
    			ArrayList<Double> data1 = new ArrayList();
    			ArrayList<Double> data2= new ArrayList();
    			ArrayList<Double> data3 = new ArrayList();
    			for (int i =0; i < al.size(); i++){
    				String[] tempbuff = al.get(i).toString().split(","); 
        			double curr1 = Double.parseDouble(tempbuff[0]);
        			datastats1.addValue(curr1);
        			double curr2 = Double.parseDouble(tempbuff[1]);
        			datastats2.addValue(curr2);
        			double curr3 = Double.parseDouble(tempbuff[2]);
        			datastats3.addValue(curr3);
        		}
    				
    			result = result+ datastats1.getMean()+","+mode(datastats1.getValues())+","+datastats1.getPercentile(50)+","+datastats1.getStandardDeviation()+","+datastats1.getMin()+","+datastats1.getMax()+","+(datastats1.getMax()-datastats1.getMin())+",";
    			result = result+ datastats2.getMean()+","+mode(datastats2.getValues())+","+datastats2.getPercentile(50)+","+datastats2.getStandardDeviation()+","+datastats2.getMin()+","+datastats2.getMax()+","+(datastats2.getMax()-datastats2.getMin())+",";
    			result = result+ datastats3.getMean()+","+mode(datastats3.getValues())+","+datastats3.getPercentile(50)+","+datastats3.getStandardDeviation()+","+datastats3.getMin()+","+datastats3.getMax()+","+(datastats3.getMax()-datastats3.getMin());			
    		}
    		return result;
        }
    	
    	private double mode(double a[]) {
    	    double maxValue = -1;
    	    double maxCount = -1;

    	    for (int i = 0; i < a.length; ++i) {
    	        int count = 0;
    	        for (int j = 0; j < a.length; ++j) {
    	            if (a[j] == a[i]) ++count;
    	        }
    	        if (count > maxCount) {
    	            maxCount = count;
    	            maxValue = a[i];
    	        }
    	    }

    	    return maxValue;
    	}

    	
    	private void copyLocationLineofClassifier(String userid, String dir, String context){
    		
    		try{
				String locationline = "";
    			File tempf = new File(dir+"processedx/"+userid+"_profile_full_"+context+"_method2.arff");
				if (tempf.exists()){
					BufferedReader br = new BufferedReader(new FileReader(tempf));
					String line;

					while ((line = br.readLine()) != null) {
						if (line.contains("attribute location") ){
							locationline = line;
							break;
						}
					}
					br.close();
					tempf = new File(dir+"processedx/"+userid+"_latest_readings.arff");
	 				if (tempf.exists()){
	 					File captureFileName = new File( dir+"processedx", userid+"_latest_readings_fixed.arff" );
	 					PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
	 					br = new BufferedReader(new FileReader(tempf));
						line ="";
						while ((line = br.readLine()) != null) {
							if (line.contains("attribute location")){
								captureFile.println(locationline);
							}else{
								captureFile.println(line);
							}
						}
 	 					captureFile.close();
 	 					br.close();
 	 					
 	 					try{
 	 						captureFileName.setReadable(true, false);
 	 						captureFileName.setWritable(true, false);
 	 						captureFileName.setExecutable(true, false);
 	 					}catch(Exception e){}
	 				}
				}
			}catch(Exception e){}
	
    	}
    	
    	public boolean deleteDir(File dir) {
    	    if (dir != null && dir.isDirectory()) {
    	       
    	    	File fzz=new File(dir, "");
  	          	try{
					 fzz.setReadable(true, false);
					 fzz.setWritable(true, false);
					 fzz.setExecutable(true, false);
  	          	}catch(Exception e){System.out.println("exception delete dir 1");e.printStackTrace();}
    	    	
    	    	
    	       String[] children = dir.list();
    	       for (int i = 0; i < children.length; i++) {
    	          System.out.println(children[i]);
    	          File fz=new File(dir, children[i]);
    	          try{
  					  fz.setReadable(true, false);
  					  fz.setWritable(true, false);
  					  fz.setExecutable(true, false);
  				  }catch(Exception e){System.out.println("exception delete dir 2");e.printStackTrace();}
    	          boolean success = deleteDir(fz);
    	          if (!success) {
    	             return false;
    	          }
    	       }
    	    }

    	    // The directory is now empty so delete it
    	    return dir.delete();
    	 }
    	
    	public void run() {
			try {
				String filedir=getBaseContext().getFilesDir().getPath()+"/";
	 			final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
	 		 	
	 			String currentday=getCurrentDay(filedir,tm.getDeviceId());
				
 	 			int currday = Integer.parseInt(currentday.split(" ")[1]);
			
				CustomClassifiers custom=((CustomClassifiers)getApplicationContext());
	 	 		if (!custom.checkState()){	
	 	 			if ((currday > 7) && (currday < 25)){
	 	 				
	 	 				boolean flag = false;
	 	 				ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
	 	 	 			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	 	 	 				  if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker.buildclassifiersforcontexts")) {
	 	 	 			          flag = true;
	 	 	 				  }
	 	 	 			}
	 	 	 			if (!flag){
	 	 	 				(new Thread(new Runnable() {
	 	 	 					public void run() {
	 	 	 						Intent i = new Intent();
	 	 	 						i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.BuildClassifiersForContexts" );
	 	 	 						startService( i );
	 	 	 					}
	 	 	 				})).start();
	 	 	 			}
	 	 			}
	 	 		}else{
				
	 	 			if ((currday > 7) && (currday < 25)){
	    		       
	 	 				PrintWriter captureFile;
	 	 				if (tm.getDeviceId() != null){
        		
        			
	 	 					String currentline = "";
	 	 					String dir = getBaseContext().getFilesDir().getPath()+"/";
	 	 					String userid = tm.getDeviceId();
	 	 						
	 	 					
	 	 					String no =collectNoiseData(userid,dir,currentday);
	 	 					String li=collectLightData(userid,dir,currentday);
	 	 					String mf = collectMagneticFieldData(userid,dir,currentday);
	 	 					String acc= collectAccelerometerData(userid,dir,currentday);
	 	 					
	 	 					if (li.length() < 2){
	 	    					li = "0.0,0.0,0.0,0.0,0.0,0.0,0.0";
	 	    				}
	 	    				if (acc.length() < 2){
	 	    					acc = "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0";
	 	    				}
	 	    				if (mf.length() < 2){
	 	    					mf = "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0";
	 	    				}
	 	    				if (no.length() < 2){
	 	    					no = "0.0,0.0,0.0,0.0,0.0,0.0,0.0";
	 	    				}
	 	 					
	 	 					
	 	 					currentline=currentline+no+",";
	 	 					currentline=currentline+li+",";
	 	 					currentline=currentline+mf+",";
	 	 					currentline=currentline+acc+",";
           	 		
	 	 					System.out.println("GLR: Got all readings");
	 	 					
	 	 					String context =retrieveTypeOfContext(userid,dir,currentday);
	 	 					currentline=currentline+context;
        			
	 	 					System.out.println("GLR: Got context");
	 	 					
	 	 					File captureFileName = new File( getBaseContext().getFilesDir()+"/processedx", userid+"_latest_readings.csv" );
	 	 					captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
	 	 					captureFile.println("month,day,hour,minutes,nmean,nmode,nmedian,nstddev,nmin,nmax,nrange,lmean,lmode,lmedian,lstddev,lmin,lmax,lrange,mf1mean,mf1mode,mf1median,mf1stddev,mf1min,mf1max,mf1range,mf2mean,mf2mode,mf2median,mf2stddev,mf2min,mf2max,mf2range,mf3mean,mf3mode,mf3median,mf3stddev,mf3min,mf3max,mf3range,a1mean,a1mode,a1median,a1stddev,a1min,a1max,a1range,a2mean,a2mode,a2median,a2stddev,a2min,a2max,a2range,a3mean,a3mode,a3median,a3stddev,a3min,a3max,a3range,location");
	 	 					captureFile.println(currentline);
	 	 					captureFile.close();

	 	 					try{
	 	 						captureFileName.setReadable(true, false);
	 	 						captureFileName.setWritable(true, false);
	 	 						captureFileName.setExecutable(true, false);
	 	 					}catch(Exception e){}
        	 		
	 	 					System.out.println("GLR: written latest readings to file");

	 	 					File tempf = new File(dir+"processedx/"+userid+"_latest_readings.csv");
	 	 					if (tempf.exists()){
	 	 						System.out.println("GLR: latest_reading.csv exists: ");
	 	 						
	 	 						
	 	 						CSVLoader loader = new CSVLoader();
	 	 						loader.setSource(tempf);
	 	 						Instances data = loader.getDataSet();
	 	 						
	 	 						System.out.println("GLR: loaded dataset");
					 
	 	 						// save ARFF
	 	 						ArffSaver saver = new ArffSaver();
	 	 						saver.setInstances(data);
	 	 						saver.setFile(new File(dir+"processedx/"+userid+"_latest_readings.arff"));
	 	 						saver.writeBatch();					
	 	 						System.out.println("GLR: write batch");
					    
	 	 						try{
	 	 							captureFileName = new File( getBaseContext().getFilesDir()+"/processedx", userid+"_latest_readings.arff" );
	 	 							captureFileName.setReadable(true, false);
	 	 							captureFileName.setWritable(true, false);
	 	 							captureFileName.setExecutable(true, false);
	 	 						}catch(Exception e){}
					    
	 	 					}
					
	 	 					// might add 'therest' attribute to latestreadings.arff
	 	 					copyLocationLineofClassifier(userid, dir, context);
	 	 					
	 	 					
	 	 					System.out.println("GLR: Converted latest readings to arff");

 	 						if (!context.equals("newplace")){
 	 							try{
	 								BufferedReader reader = new BufferedReader(new FileReader(dir+"processedx/"+userid+"_latest_readings_fixed.arff"));
	 								Instances test = new Instances(reader);
	 								reader.close();
	 								test.setClassIndex(test.numAttributes() - 1);
	 								test.deleteAttributeAt(3);
	 								test.deleteAttributeAt(1);
	 								test.deleteAttributeAt(0);
	 	 								
	 								System.gc();
	 	 								
	 		 	 					System.out.println("GLR: deleted attributes from classifier");
	 	 		 	 			
	 		 	 					J48 cls = null;
	 		 	 					if (context.contains("home")){
	 		 	 						cls = custom.getHomeClassifier();
	 		 	 					}
                                    
	 		 	 					if (context.contains("work")){
	 		 	 						cls = custom.getWorkClassifier();
	 		 	 					}
                                    
	 		 	 					if (context.contains("other")){
	 		 	 						cls=custom.getOtherClassifier();
	 		 	 					}
                                    
	 		 	 					if (context.contains("transition")){
	 		 	 						cls=custom.getTransitionClassifier();
	 		 	 					}
    								
	 								System.out.println("GLR: loaded classifier");
	 	 								
	 								if (cls != null){
	 									boolean result = false;	
	 									for (int i = 0; i < test.numInstances(); i++) {
	 										double pred = cls.classifyInstance(test.instance(i));
	 										String st1 = test.classAttribute().value((int) test.instance(i).classValue());
	 										if (!st1.equalsIgnoreCase(test.classAttribute().value((int) pred).toString())){
	 											result = false;
	 										}else{
	 											result = true;
	 										}
	 										
	 										System.out.println("GLR: got result; "+pred+" ,"+st1);
	 									}
	 	 								
	 									captureFileName = new File( getBaseContext().getFilesDir()+"/processedx", userid+"_allwekaresults.txt" );
	 									captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
	 									captureFile.println(currentline+";"+result);
	 									captureFile.close();
		           	 			
	 									try{
	 										captureFileName.setReadable(true, false);
	 	 									captureFileName.setWritable(true, false);
	 	 									captureFileName.setExecutable(true, false);
	 	 								}catch(Exception e){}
	 									
	 									custom.setCurrentContext(context);
	 	 								custom.setCurrentState(result);
	 	 								
	 	 								System.out.println("GLR: wrote result in file");
	 	 							}
	 	 						}catch(Exception e){}
								
	 	 					}else{
	 	 						// handle new place as well	
	 	 						captureFileName = new File( getBaseContext().getFilesDir()+"/processedx", userid+"_allwekaresults.txt" );
	 	 						captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
	 	 						captureFile.println(currentline+";"+false);
	 	 						captureFile.close();
	           	 		
	 	 						try{
	 	 							captureFileName.setReadable(true, false);
	 	 							captureFileName.setWritable(true, false);
	 	 							captureFileName.setExecutable(true, false);
	 	 						}catch(Exception e){}	           	 		
	 	 						
	 	 						custom.setCurrentContext(context);
	 	 						custom.setCurrentState(false);
	 	 						
	 	 						System.out.println("GLR: wrote result in file new place");
	 	 					}
	 	 				}	        		 
	 	 			}
	 	 			System.out.println("GLR: call stopSelf");
		 		}	 
			}catch(Exception e){System.out.println(e.getMessage());e.printStackTrace();}
			stopSelf();
    	}
	};

}

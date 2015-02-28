package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DailyAnalysis extends Service {
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

    	private <K,V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

    		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

    		Collections.sort(sortedEntries, new Comparator<Entry<K,V>>() {
    			@Override
    			public int compare(Entry<K,V> e1, Entry<K,V> e2) {
    				return e2.getValue().compareTo(e1.getValue());
    			}
    		});

    		return sortedEntries;
    	}

    	private HashMap loadAllBuckets(String userid, String directory){

    		HashMap allbuckets = new HashMap();
    		try{
				File tempf = new File(directory+"processedx/"+userid+"_buckets_details.txt");

				if (tempf.exists()){
					BufferedReader br = new BufferedReader(new FileReader(tempf));
					String line;

					while ((line = br.readLine()) != null) {
						try{
							String [] tempbuff = line.split(";");
							if (!allbuckets.containsKey(tempbuff[1])){
								allbuckets.put(tempbuff[1], new ArrayList());
							}
							String allwif = "";
							for (int z=2; z < tempbuff.length; z++){
								allwif=allwif+tempbuff[z]+";";
							}
							ArrayList al = (ArrayList)allbuckets.get(tempbuff[1]);
							al.add(allwif);
							allbuckets.put(tempbuff[1], al);
						}catch(Exception e){}
					}

					br.close();
				}
			}catch(Exception e){}
    		return allbuckets;
    	}

    	private HashMap loadAllContexts(String userid, String directory){

    		HashMap allcontexts = new HashMap();
    		try{
				File tempf = new File(directory+"processedx/"+userid+"_fullview_intervals.csv");

				if (tempf.exists()){
					BufferedReader br = new BufferedReader(new FileReader(tempf));
					String line;

					while ((line = br.readLine()) != null) {
						try{
							String [] tempbuff = line.split(",");
							HashMap temp = new HashMap();
							for (int z=1; z < tempbuff.length; z++){
								temp.put(tempbuff[z].split(";")[0], tempbuff[z].split(";")[1]);
							}
							allcontexts.put(tempbuff[0], temp);
						}catch(Exception e){}
					}

					br.close();
				}
			}catch(Exception e){}
    		return allcontexts;
    	}


    	private HashMap processWifiData(String userid, String directory, HashMap allbuckets,String day){
    		HashMap wifi = new HashMap();

    		try{
    			File tempf = new File(directory+userid+"_wifi_capture"+day+".csv");

    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    				String line;

    				while ((line = br.readLine()) != null) {
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");

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

    								//get bucket for this one
    							List<String> sortedBuckets=new ArrayList(allbuckets.keySet());
    							Collections.sort(sortedBuckets);

    							for (String item : sortedBuckets) {
    								if (nameid.contains(item)){
    									//System.out.println(item+" : "+nameid);

    									if (!wifi.containsKey(currdate))
    									{
    										wifi.put(currdate, new HashMap());
    									}
    									HashMap temphm = (HashMap) wifi.get(currdate);

    									String thistime = "";
    									if (currminutes < 10){
    										thistime="0"+String.valueOf(currminutes);
    									}else{
    										thistime=String.valueOf(currminutes);
    									}
    									thistime=currhours+":"+thistime;

    									if (!temphm.containsKey(thistime)){
    										temphm.put(thistime, "");
    									}

    										//ArrayList al = (ArrayList)temphm.get(thistime);
    										//al.add(avalue1+","+avalue2+","+avalue3);
    									temphm.put(thistime,item);
    									wifi.put(currdate,temphm);
    								}
    							}

    						}
    					}catch(Exception e){e.printStackTrace();}
    				}
    				br.close();
    			}


    			/*List<String> sortedDays=new ArrayList(wifi.keySet());
    			Collections.sort(sortedDays);
    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) wifi.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					System.out.println(days+","+timeframes+";"+daydetails.get(timeframes).toString());
    				}
    			}*/


    		}catch(Exception e){e.printStackTrace();}

    		return wifi;
    	}


    	private HashMap processAccelerometerData(String userid, String directory,String day, int hour){
    		HashMap accelerometer = new HashMap();

    		try{
    			File tempf = new File(directory+userid+"_accelerometer_capture"+day+".csv");

    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    				String line;

    				while ((line = br.readLine()) != null) {
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");

    							SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    							Date date = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);

    							if (date.getHours() == hour){

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
    											//System.gc();
    										}
    									}catch(Exception exx){}
    								}
    							}
    							if (date.getHours() > hour){
    								break;
    							}
    						}
    					}catch(Exception e){}
    				}
    				br.close();
    			}


    			/*List<String> sortedDays=new ArrayList(accelerometer.keySet());
    			Collections.sort(sortedDays);
    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) accelerometer.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					ArrayList al = (ArrayList)daydetails.get(timeframes);
    					for (int i =0; i < al.size(); i++){
    						System.out.println(days+","+timeframes+";"+al.get(i).toString());
    					}
    				}
    			}*/


    		}catch(Exception e){e.printStackTrace();}

    		return accelerometer;
    	}

    	private HashMap processLightData(String userid, String directory,String day, int hour){
    		HashMap light = new HashMap();

    		try{
    			File tempf = new File(directory+userid+"_light_capture"+day+".csv");

    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    				String line;


    				while ((line = br.readLine()) != null) {
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");

    							SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    							Date date = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);

    							if (date.getHours() == hour){
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
    								}catch(Exception e){}
    							}
    							if (date.getHours() > hour){
    								break;
    							}
    						}
    					}catch(Exception e){}
    				}
    				br.close();
    			}


    			/*List<String> sortedDays=new ArrayList(light.keySet());
    			Collections.sort(sortedDays);
    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) light.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					ArrayList al = (ArrayList)daydetails.get(timeframes);
    					for (int i =0; i < al.size(); i++){
    						System.out.println(days+","+timeframes+";"+al.get(i).toString());
    					}
    				}
    			}*/


    		}catch(Exception e){e.printStackTrace();}

    		return light;
    	}

    	private HashMap processNoiseData(String userid, String directory,String day, int hour){
    		HashMap noise = new HashMap();

    		try{
    			File tempf = new File(directory+userid+"_noise_capture"+day+".csv");

    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    				String line;

    				while ((line = br.readLine()) != null) {
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");

    							SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    							Date date = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);

    							if (date.getHours() == hour){

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
    									ArrayList avgnoise = new ArrayList();
    									for (int z=2; z < tempbuff.length; z++){
    										try{
    											avgnoise.add(Double.parseDouble(tempbuff[z]));
    										}catch(Exception exx){exx.printStackTrace();}
    									}

    									if ((avgnoise.size() > 0)){

    										if (!noise.containsKey(currdate))
    										{
    											noise.put(currdate, new HashMap());
    										}
    										HashMap temphm = (HashMap) noise.get(currdate);

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
    										al.addAll(avgnoise);
    										temphm.put(thistime,al);
    										noise.put(currdate,temphm);
    									}
    			            		}catch(Exception e){}
    							}
    							if (date.getHours() > hour){
    								break;
    							}
    						}
    					}catch(Exception e){}
    				}
    				br.close();
    			}


    			/*List<String> sortedDays=new ArrayList(noise.keySet());
    			Collections.sort(sortedDays);
    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) noise.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					ArrayList al = (ArrayList)daydetails.get(timeframes);
    					for (int i =0; i < al.size(); i++){
    						System.out.println(days+","+timeframes+";"+al.get(i).toString());
    					}
    				}
    			}*/


    		}catch(Exception e){e.printStackTrace();}

    		return noise;
    	}


    	private HashMap processMagneticFieldData(String userid, String directory,String day, int hour){
    		HashMap magneticfield = new HashMap();

    		try{
    			File tempf = new File(directory+userid+"_magneticfield_capture"+day+".csv");

    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    				String line;

    				while ((line = br.readLine()) != null) {
    					try{
    						if (line.contains(",")){
    							String [] tempbuff = line.split(",");

    							SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");

    							Date date = format.parse(tempbuff[2].split(" ")[0]+" "+tempbuff[2].split(" ")[1]+" "+tempbuff[2].split(" ")[2]+" "+tempbuff[2].split(" ")[3]+" "+tempbuff[2].split(" ")[5]);

    							if (date.getHours() == hour){

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
     										}
    									}catch(Exception exx){System.out.println("ERROR MAGNETIC FIELD 1");}
    								}
    							}
    							if (date.getHours() > hour){
    								break;
    							}
    						}
    					}catch(Exception e){System.out.println("ERROR MAGNETIC FIELD 2");}
    				}
    				br.close();
    			}


    			/*List<String> sortedDays=new ArrayList(magneticfield.keySet());
    			Collections.sort(sortedDays);
    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) magneticfield.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					ArrayList al = (ArrayList)daydetails.get(timeframes);
    					for (int i =0; i < al.size(); i++){
    						System.out.println(days+","+timeframes+";"+al.get(i).toString());
    					}
    				}
    			}*/


    		}catch(Exception e){e.printStackTrace();}

    		return magneticfield;
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

    	private HashMap defineLineItemsForGivenSamplingRate(HashMap noise, HashMap light, HashMap magneticfield, HashMap accelerometer, HashMap alldayscontexts, int rate, int hour){
    		HashMap lineitems = new HashMap();

    		HashMap samplingtimes =  new HashMap();

    		for (int i=0; i < 60; i=i+rate){
    			String time = "";
    			if (hour < 10){ time = "0"+String.valueOf(hour); }else{ time = String.valueOf(hour); }
    			if (i < 10){ time = time+":0"+String.valueOf(i); }else{ time = time+":"+String.valueOf(i);}
    			if (!samplingtimes.containsKey(time)){ samplingtimes.put(time, ""); }
    		}


    		int counter=0;
    		HashMap wrong = new HashMap();
    		List<String> sortedDays=new ArrayList(noise.keySet());
    		Collections.sort(sortedDays);
    		for (String days : sortedDays) {
        		System.out.println("defineLineItems: "+days);

    			HashMap daydetails = null;
    			if(alldayscontexts.containsKey(days)){
    				 daydetails = (HashMap) alldayscontexts.get(days);
    			}
    			HashMap noisedetails = null;
    			if (noise.containsKey(days)){
    				noisedetails = (HashMap) noise.get(days);
    			}

    			HashMap lightdetails = null;
    			if (light.containsKey(days)){
    				lightdetails = (HashMap) light.get(days);
    			}
    			HashMap mfdetails = null;
    			if (magneticfield.containsKey(days)){
    				mfdetails = (HashMap) magneticfield.get(days);
    			}

    			HashMap accdetails = null;
    			if (accelerometer.containsKey(days)){
    				accdetails = (HashMap) accelerometer.get(days);
    			}

    			if (!lineitems.containsKey(days)){
    				lineitems.put(days, new HashMap());
    			}

    			List<String> sortedTimes=new ArrayList(samplingtimes.keySet());
    			Collections.sort(sortedTimes);
    			for (String timeframes : sortedTimes) {

    				ArrayList al_noise = null;
    				if (noisedetails != null){
    					if (noisedetails.containsKey(timeframes)){
    						al_noise = (ArrayList)noisedetails.get(timeframes);
    					}
    				}

    				ArrayList al_light = null;
    				if (lightdetails!= null){
    					if (lightdetails.containsKey(timeframes)){
    						al_light = (ArrayList)lightdetails.get(timeframes);
    					}
    				}
    				ArrayList al_mf = null;
    				if (mfdetails != null){
    					if (mfdetails.containsKey(timeframes)){
    						al_mf = (ArrayList)mfdetails.get(timeframes);
    					}
    				}
    				ArrayList al_acc = null;
    				if (accdetails!= null){
    					if (accdetails.containsKey(timeframes)){
    						al_acc = (ArrayList)accdetails.get(timeframes);
    					}
    				}


    				String context = "";
    				if (daydetails != null){
    					if(daydetails.containsKey(timeframes)){
    						context = daydetails.get(timeframes).toString();
    					}
    				}

    				String no="";
    				if (al_noise != null){
    					no = computeAverageSingleValue(al_noise);
    				}
    				String li="";
    				if (al_light != null){
    					li = computeAverageSingleValue(al_light);
    				}
    				String mf="";
    				if (al_mf != null){
    					mf = computeAverageMultipleValues(al_mf);
    				}
    				String acc="";
    				if (al_acc != null){
    					acc = computeAverageMultipleValues(al_acc);
    				}

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

					if (getCount(li+","+acc+","+mf+","+no) <= 42 ){
    				
						HashMap hmli = (HashMap)lineitems.get(days);
    					String alldetails = "";

    					alldetails = days.split("/")[0].toString()+","+days.split("/")[1].toString()+",";
    					alldetails = alldetails+timeframes.split(":")[0].toString()+","+timeframes.split(":")[1].toString()+","+no+","+li+","+mf+","+acc+","+context;
    					hmli.put(hmli.size(), alldetails);
    					counter++;
					}
    			}
    		}

    		return lineitems;
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

    						if (!contexts.containsKey(tempbuff[tempbuff.length-1])){
    							if (!tempbuff[tempbuff.length-1].equals("location")){
    								contexts.put(tempbuff[tempbuff.length-1], "");
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
    											String [] tempbuff = linez.split(",");
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

    	private void createModels(String userid, String directory){

    		try{
    		File fz=new File(directory+"processedx/");
    			if(fz.isDirectory()){

    				String files[]=  fz.list();
    				for(int i=0;i<files.length;i++){
    					if ((files[i].contains("_method2.arff"))){
    						File tempf = new File(directory+"processedx/"+files[i]);
    						if (tempf.exists()){
    							try{

    								BufferedReader br = new BufferedReader(new FileReader(directory+"processedx/"+files[i]));
    								Instances train = new Instances(br);
    								br.close();
    								train.setClassIndex(train.numAttributes() - 1);
    								train.deleteAttributeAt(5);
    								train.deleteAttributeAt(3);
    								train.deleteAttributeAt(2);
    								train.deleteAttributeAt(1);
    								train.deleteAttributeAt(0);

    								J48 j48 = new J48();
    								j48.setUnpruned(false);
    								j48.setConfidenceFactor(new Float("0.1"));
    								j48.buildClassifier(train);
    								weka.core.SerializationHelper.write(directory+"processedx/"+files[i].replaceAll(".csv",".model"), j48);


        							System.out.println("just created: "+directory+"processedx/"+files[i].replaceAll(".csv",".model"));
        							try{
        			    				File captureFileName = new File(directory+"processedx/"+files[i].replaceAll(".csv",".model"));
        			    				captureFileName.setReadable(true, false);
        			       	 			captureFileName.setWritable(true, false);
        			       	 			captureFileName.setExecutable(true, false);
        			    			}catch(Exception e){}

    								System.gc();
    							}catch(Exception e){System.out.println("Error while saving model: "+e.getMessage());}
    							/*CSVLoader loader = new CSVLoader();
    							loader.setSource(tempf);
    							Instances data = loader.getDataSet();

    							// save ARFF
    							ArffSaver saver = new ArffSaver();
    							saver.setInstances(data);
    							saver.setFile(new File(directory+"processedx/"+files[i].replaceAll(".csv",".arff")));
    							//saver.setDestination(new File(directory+samplingrate+"\\"+files[i].replaceAll(".csv",".arff")));
    							saver.writeBatch();


    							try{
    			    				File captureFileName = new File(directory+"processedx/", files[i].replaceAll(".csv",".arff") );
    			    				captureFileName.setReadable(true, false);
    			       	 			captureFileName.setWritable(true, false);
    			       	 			captureFileName.setExecutable(true, false);
    			    			}catch(Exception e){}*/
    						}
    					}
    				}
    			}
    		}catch(Exception e){}

    	}

    	private void saveLineItemsToFiles(String userid, String directory, HashMap lineitems/*,HashMap allbuckets,HashMap wifi*/){

    		// first loop and save per day files (and all files)

    		List<String> sortedDays=new ArrayList(lineitems.keySet());
    		Collections.sort(sortedDays);

    		try{
    			int counter =0;

    			boolean isfirstline = true;
    			File f = new File( directory+"processedx", userid+"_profile_full_method1.csv");

				if (f.exists()){
					isfirstline = false;
				}

				PrintWriter allout = new PrintWriter( new FileWriter( new File( directory+"processedx", userid+"_profile_full_method1.csv" ), true ) );
    			if (isfirstline){
    				allout.println("month,day,hour,minutes,nmean,nmode,nmedian,nstddev,nmin,nmax,nrange,lmean,lmode,lmedian,lstddev,lmin,lmax,lrange,mf1mean,mf1mode,mf1median,mf1stddev,mf1min,mf1max,mf1range,mf2mean,mf2mode,mf2median,mf2stddev,mf2min,mf2max,mf2range,mf3mean,mf3mode,mf3median,mf3stddev,mf3min,mf3max,mf3range,a1mean,a1mode,a1median,a1stddev,a1min,a1max,a1range,a2mean,a2mode,a2median,a2stddev,a2min,a2max,a2range,a3mean,a3mode,a3median,a3stddev,a3min,a3max,a3range,location");
    			}else{
    				allout.println();
    			}
    			int week = 0;


    			try{
    				File captureFileName = new File(directory+"processedx/", userid+"_profile_full_method1.csv" );
    				captureFileName.setReadable(true, false);
       	 			captureFileName.setWritable(true, false);
       	 			captureFileName.setExecutable(true, false);
    			}catch(Exception e){}

    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) lineitems.get(days);
    				System.out.println(days+","+daydetails.size());
    				try{

    					for (int i = 0; i < daydetails.size(); i++){
    						String line = daydetails.get(i).toString();
    						allout.println(line);
    					}
    					counter++;
    				}catch(Exception e){e.printStackTrace();}


    			}

    			allout.close();

    		}catch(Exception e){e.printStackTrace();}
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

    	private String getDayForDate(String dir, String userid,String sdate){
    		String day="";
    		try{
				File tempf = new File(dir+userid+"_tasklist.txt");
				if (tempf.exists()){
					BufferedReader br = new BufferedReader(new FileReader(tempf));
					String line;

					while ((line = br.readLine()) != null) {
						String [] tempbuff = line.split(";");
						if (line.contains(sdate) ){
				    		day = tempbuff[0].split(" ")[0]+" "+tempbuff[0].split(" ")[1];
							break;
						}
					}
					br.close();
				}
			}catch(Exception e){e.printStackTrace();}
    		return day;
    	}

    	private String getDateForDay(String dir, String userid,String sday){
    		String date="";
    		try{
				File tempf = new File(dir+userid+"_tasklist.txt");
				if (tempf.exists()){
					BufferedReader br = new BufferedReader(new FileReader(tempf));
					String line;

					while ((line = br.readLine()) != null) {
						String [] tempbuff = line.split(";");
						if (line.contains(sday) ){
							date=tempbuff[0].split(" ")[2];
							date = date.substring(1, date.length()-1);
							break;
						}
					}
					br.close();
				}
			}catch(Exception e){e.printStackTrace();}
    		return date;
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

    	private void generateProfile(String userid, String dir){

    		// create directory
    		File fz=new File(dir+"processedx");
			if(!fz.isDirectory()){
				fz.mkdir();
			}

			if(fz.isDirectory()){
				  try{
					  fz.setReadable(true, false);
					  fz.setWritable(true, false);
					  fz.setExecutable(true, false);
				  }catch(Exception e){System.out.println("exception 1");e.printStackTrace();}
			}

			String currentday=getCurrentDay(dir,userid);
			int currday = Integer.parseInt(currentday.split(" ")[1]);
			if (currday > 8){
				currday =8;
			}


			HashMap hm = new HashMap();

			// understand how many days you have to process

			try{
				File tempf = new File(dir+"processedx/"+userid+"_profile_full_method1.csv");
				if (tempf.exists()){
					String lastline = tail(tempf);
					if (lastline.contains(",")){
						String tempbuff[] = lastline.split(",");
						String lastlinedate = "2014/"+tempbuff[0]+"/"+tempbuff[1];
						String res = getDayForDate(dir,userid,lastlinedate);
						if (res.length() > 0){

							int time = Integer.valueOf(tempbuff[2]);
							if (time < 23){
								String timestring = "";
								for (int x=time+1; x< 24; x++){
									if (x< 10){
										timestring = timestring+"0"+String.valueOf(x)+",";
									}else{
										timestring = timestring+String.valueOf(x)+",";
									}
								}
								int num = Integer.valueOf(res.split(" ")[1]);
								hm.put("Day 0"+num, timestring);
								for (int x=num+1; x < currday; x++){
									hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
								}
							}else{
								int num = Integer.valueOf(res.split(" ")[1]);
								for (int x=num+1; x < currday; x++){
									hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
								}
							}
						}
					}else{
						for (int x=1; x < currday; x++){
							hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
						}
					}
				}else{
					for (int x=1; x < currday; x++){
						hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
					}
				}
			}catch(Exception e){e.printStackTrace();}
			
			// add check to regenerate data for 4 users
			/*if (userid.contains("862070020941415") && (currday == 8)){
				try{
					hm.clear();
					for (int x=1; x < currday; x++){
						hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
					}
					PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1.csv" ), false ) );
					allout.close();
				}catch(Exception e){}
			}
			
			if (userid.contains("358240057008799") && (currday == 9)){
				try{
					hm.clear();
					for (int x=1; x < currday; x++){
						hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
					}
					PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1.csv" ), false ) );
					allout.close();
				}catch(Exception e){}
			}*/
			
			/*if (userid.contains("353720059369732") && (currday == 8)){
				try{
					hm.clear();
					for (int x=1; x < currday; x++){
						hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
					}
					PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1.csv" ), false ) );
					allout.println("month,day,hour,minutes,nmean,nmode,nmedian,nstddev,nmin,nmax,nrange,lmean,lmode,lmedian,lstddev,lmin,lmax,lrange,mf1mean,mf1mode,mf1median,mf1stddev,mf1min,mf1max,mf1range,mf2mean,mf2mode,mf2median,mf2stddev,mf2min,mf2max,mf2range,mf3mean,mf3mode,mf3median,mf3stddev,mf3min,mf3max,mf3range,a1mean,a1mode,a1median,a1stddev,a1min,a1max,a1range,a2mean,a2mode,a2median,a2stddev,a2min,a2max,a2range,a3mean,a3mode,a3median,a3stddev,a3min,a3max,a3range,location");
					allout.close();
				}catch(Exception e){}
			}*/
			
			/*if ((userid.contains("356633052505342") || userid.contains("862070020941415") || userid.contains("353720059369732") ) && (currday == 8)){
				
				//File captureFileName = new File(dir+"/processedx", userid+"_profileflag.txt" );
				//if (captureFileName.exists()){
				//	try{
				//		PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
				//		captureFile.close();
				//		captureFileName.setReadable(true, false);
				//		captureFileName.setWritable(true, false);
				//		captureFileName.setExecutable(true, false);
				//	}catch(Exception e){}
				//}
				
				//try{
				//	hm.clear();
				//	for (int x=1; x < currday; x++){
				//		hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
				//	}
				//	PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1.csv" ), false ) );
				//	allout.close();
				//}catch(Exception e){}
				
				try{
					hm.clear();
					File tempfz = new File(dir+"processedx", userid+"_profile_full_method1.csv");
					if (tempfz.exists()){
						PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1_new.csv" ), false ) );
						allout.println("month,day,hour,minutes,nmean,nmode,nmedian,nstddev,nmin,nmax,nrange,lmean,lmode,lmedian,lstddev,lmin,lmax,lrange,mf1mean,mf1mode,mf1median,mf1stddev,mf1min,mf1max,mf1range,mf2mean,mf2mode,mf2median,mf2stddev,mf2min,mf2max,mf2range,mf3mean,mf3mode,mf3median,mf3stddev,mf3min,mf3max,mf3range,a1mean,a1mode,a1median,a1stddev,a1min,a1max,a1range,a2mean,a2mode,a2median,a2stddev,a2min,a2max,a2range,a3mean,a3mode,a3median,a3stddev,a3min,a3max,a3range,location");
						BufferedReader brz = new BufferedReader(new FileReader(tempfz));
						String linez;
						while ((linez = brz.readLine()) != null) {
							allout.println(linez);
						}
						brz.close();
						allout.close();
						tempfz = new File(dir+"processedx", userid+"_profile_full_method1_new.csv");
						if (tempfz.exists()){
							allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1.csv" ), false ) );
							brz = new BufferedReader(new FileReader(tempfz));
							linez="";
							while ((linez = brz.readLine()) != null) {
								allout.println(linez);
							}
							brz.close();
							allout.close();
						}
						
						
					}else{
						for (int x=1; x < currday; x++){
							hm.put("Day 0"+String.valueOf(x), "00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
						}
						PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1.csv" ), false ) );
						allout.println("month,day,hour,minutes,nmean,nmode,nmedian,nstddev,nmin,nmax,nrange,lmean,lmode,lmedian,lstddev,lmin,lmax,lrange,mf1mean,mf1mode,mf1median,mf1stddev,mf1min,mf1max,mf1range,mf2mean,mf2mode,mf2median,mf2stddev,mf2min,mf2max,mf2range,mf3mean,mf3mode,mf3median,mf3stddev,mf3min,mf3max,mf3range,a1mean,a1mode,a1median,a1stddev,a1min,a1max,a1range,a2mean,a2mode,a2median,a2stddev,a2min,a2max,a2range,a3mean,a3mode,a3median,a3stddev,a3min,a3max,a3range,location");
						allout.close();
					}
				}catch(Exception e){}
				
			}*/
			
			// add kevins code
			/*if (userid.contains("354245052625194") && (currday == 8)){
				File captureFileName = new File(dir+"/processedx", userid+"_profileflag.txt" );
				if (captureFileName.exists()){
					try{
						PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
						captureFile.close();
						captureFileName.setReadable(true, false);
						captureFileName.setWritable(true, false);
						captureFileName.setExecutable(true, false);
					}catch(Exception e){}
				}
				hm.clear();
				
				try{
					InputStream is = getAssets().open(userid+"_profile_full_method1.csv");
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					String line;
					PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", userid+"_profile_full_method1.csv" ), false ) );
					while ((line = br.readLine()) != null) {
						allout.println(line);
					}
					br.close();
					allout.close();

				}catch(Exception e){}				
			}*/			

			HashMap allbuckets =loadAllBuckets(userid,dir);
			HashMap alldays_contextmap =loadAllContexts(userid,dir);


			List<String> sortedDays=new ArrayList(hm.keySet());
			Collections.sort(sortedDays);

			for (String day : sortedDays) {
				String hourstring = hm.get(day).toString();
				String [] tbuff = hourstring.split(",");

				for (int i =0; i < tbuff.length; i++){
					int hour = Integer.valueOf(tbuff[i]);
					System.out.println("hour start "+hour);
					HashMap magneticfield = processMagneticFieldData(userid,dir,day,hour);

					System.gc();
					HashMap accelerometer = processAccelerometerData(userid,dir,day,hour);

					System.gc();
					HashMap noise = processNoiseData(userid,dir,day,hour);

					System.gc();
					HashMap light = processLightData(userid,dir,day,hour);

					System.gc();
					HashMap lines_method1 = defineLineItemsForGivenSamplingRate(noise, light, magneticfield, accelerometer, alldays_contextmap, 1,hour);

					System.gc();
					saveLineItemsToFiles(userid,dir,lines_method1);

					System.gc();

				}
			}

			/*if (currentday.contains("Day 09")){
				prepareSecondMethod(userid,dir);
				System.gc();
				cleanupSecondMethod(userid,dir);
				System.gc();
				createModels(userid,dir);
				System.gc();

			}	*/

    	}


    	private void computeLocationBucketsFile (String userid, String directory){

    		try{

    			HashMap mostcommonmap = new HashMap();
    			HashMap mostcommonprocessed = new HashMap();

    			File tempf = new File(directory+userid+"_wifi_capture_all.csv");

    			HashMap all_wifis = new HashMap();
    			int linecounter =0;

    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    		        String line;

    		        HashMap prevlist= new HashMap();
    	        	int count = 0;

    		        while ((line = br.readLine()) != null) {
    		        	try{
    		           		String [] tempbuff = line.split(",");
    		           		//all_list_wifis.add(line);
    		           		for (int z =2; z < tempbuff.length; z=z+4){
    							if (z+4 <= tempbuff.length ){
    								tempbuff[z] = tempbuff[z].replace(" ","_");
									tempbuff[z] = tempbuff[z].replace("'","_");
    								if (!mostcommonmap.containsKey(tempbuff[z]+","+tempbuff[z+1])){
    									mostcommonmap.put(tempbuff[z]+","+tempbuff[z+1], 0);
    									mostcommonprocessed.put(tempbuff[z]+","+tempbuff[z+1], 0);
    									all_wifis.put(tempbuff[z]+","+tempbuff[z+1], new HashMap());
    								}
    								int ct = (Integer) mostcommonmap.get(tempbuff[z]+","+tempbuff[z+1]);
    								ct++;
    								mostcommonmap.put(tempbuff[z]+","+tempbuff[z+1], ct);
    								HashMap tpal = (HashMap) all_wifis.get(tempbuff[z]+","+tempbuff[z+1]);
    								if (!tpal.containsKey(line)){
    									tpal.put(line,"");
    								}
    								all_wifis.put(tempbuff[z]+","+tempbuff[z+1], tpal);
    							}
    		           		}
    		           		linecounter++;
    		        	}catch(Exception e){}
    		        }
        			br.close();
    			}

    			HashMap buckets = new HashMap();

    			List<Entry<String,Integer>> sortedEntries = entriesSortedByValues(mostcommonmap);
    			for (Entry<String,Integer> e1 : sortedEntries) {
    				//System.out.println(e1.getKey()+","+mostcommonmap.get(e1.getKey()));
    				int state = (Integer) mostcommonprocessed.get(e1.getKey());
    				if (state == 0){
    					mostcommonprocessed.put(e1.getKey(), 1);
    					List<Entry<String,Integer>> sortedEntries2 = entriesSortedByValues(mostcommonmap);
    					for (Entry<String,Integer> e2 : sortedEntries2) {
    						int state2 = (Integer) mostcommonprocessed.get(e2.getKey());
    						if (state2 == 0){
    							HashMap linedata = (HashMap)all_wifis.get(e1.getKey());
    							//System.out.println("----"+e2.getKey()+","+linedata.size());
    							List<String> sortedLines=new ArrayList(linedata.keySet());
    							for (String line : sortedLines) {
    								if (line.contains(e1.getKey()) && line.contains(e2.getKey())){
    									mostcommonprocessed.put(e2.getKey(), 1);
    									if (!buckets.containsKey(e1.getKey())){
    										buckets.put(e1.getKey(), new ArrayList());
    									}
    									ArrayList tempal = (ArrayList)buckets.get(e1.getKey());
    									tempal.add(e2.getKey());
    									buckets.put(e1.getKey(), tempal);
    									break;
    								}
    							}
    						}
    					}
    				}
    			}

    			System.out.println("linecounter: "+linecounter+", buckets size: "+buckets.size());

    			List<Entry<String,Integer>> sortedEntriez = entriesSortedByValues(mostcommonmap);
    			BufferedWriter outdat = new BufferedWriter(new FileWriter(directory+userid+"_locationbucket_list.dat"));
    			for (Entry<String,Integer> ez : sortedEntriez) {
    				if (buckets.containsKey(ez.getKey())){
    					String line = ez.getKey()+";";
    					ArrayList al = (ArrayList) buckets.get(ez.getKey());
    					for (int i =0; i < al.size(); i++){
    						line = line+al.get(i).toString()+";";
    					}
    					outdat.write(line);
    					outdat.newLine();
    				}
    			}
    			outdat.close();

    			try{
					File gfile =new File(directory+userid+"_locationbucket_list.dat");
					gfile.setReadable(true, false);
					gfile.setWritable(true, false);
					gfile.setExecutable(true, false);
				}catch(Exception e){
					e.printStackTrace();
				}

    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}


        public void run() {
        	try{

        		String dir = getBaseContext().getFilesDir().getPath()+"/";
          		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
				String currentday=getCurrentDay(dir,tm.getDeviceId());

				int currday = Integer.parseInt(currentday.split(" ")[1]);

				// to fix both sets of numbers and last check as well
				if ((currday > 2) && (currday < 10)){

					Date dt = new Date();

					if ((dt.getHours() >= 2) && (dt.getHours() < 7)){

						if (tm.getDeviceId() != null){
							// if home and wifi lists were not selected compute lists of wifi networks and
							// save them to file, list them in order of how much they appeared
							boolean flag = false;
							try{
								File tempf = new File(dir+tm.getDeviceId()+"_locationbucket_list.dat");
								if (tempf.exists()){
									BufferedReader br = new BufferedReader(new FileReader(tempf));
									String line;
									int found =0;		
									while ((line = br.readLine()) != null) {
										if (line.contains("wifi_home=") || line.contains("wifi_work=")){
											found++;
										}
									}
									br.close();
									if (found >= 2){
										flag = true;
									}
								}

								if (!flag){
									computeLocationBucketsFile(tm.getDeviceId(),dir);
								}
							}catch(Exception e){e.printStackTrace();}

							if (flag){

								if ((currday >= 4) && (currday < 10)){

									boolean f = false;
									File tempf = new File(dir+"/processedx/"+tm.getDeviceId()+"_profileflag.txt");
									if (tempf.exists()){
										BufferedReader br = new BufferedReader(new FileReader(tempf));
										String line;

										while ((line = br.readLine()) != null) {
											if (line.contains("processed2")){
												f = true;
											}
										}
										br.close();
									}

									if (!f){
										generateProfile(tm.getDeviceId(), dir);
										System.out.println("Got into the profile generation section.");

										if (currday > 7){

											File captureFileName = new File(dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
											if (!captureFileName.exists()){
												PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
												captureFile.println("processed1");
												captureFile.close();
												try{
													captureFileName.setReadable(true, false);
                   	 								captureFileName.setWritable(true, false);
                   	 								captureFileName.setExecutable(true, false);
												}catch(Exception e){}
											}
										}
									}

								}
							}
						}
					}
				}
        	}catch (Exception e){e.printStackTrace();}
        	stopSelf();
        }
    };
}


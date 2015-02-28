package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class DailyBucketGenerator extends Service {
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
    	
    	private HashMap identifyTransitions (String userid, String directory,String day1, String day7){
    		HashMap allfulldays = new HashMap();
    		
    		try{
    	
    			// loop through everything to fill in all the details in hashmap
    	
    			
    			File tempf = new File(directory+userid+"_wifi_capture_all.csv");
    		    
    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    		        String line;
    		        
    		        HashMap prevlist= new HashMap();  
    	        	int count = 0;
    		            	
    	        	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    	        	Date day1start = dateFormat.parse(day1+" "+"00:00");
					Date day7end = dateFormat.parse(day7+" "+"23:59");
					Calendar c_day1 = Calendar.getInstance();
					c_day1.setTime(day1start);
					Calendar c_day7 = Calendar.getInstance();
					c_day7.setTime(day7end);
					
    	        	
    		        while ((line = br.readLine()) != null) {
    		        	try{
    		        		
    		        		int matched = 0;
    						int notmatched = 0;
    						HashMap currentlist = new HashMap();
    						
    		        		String [] tempbuff = line.split(",");
    		        		SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    		        		Date date = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
    		        		Calendar c_check = Calendar.getInstance();
    		        		c_check.setTime(date);
    						
    						
    						
    						if ((c_check.getTimeInMillis() >= c_day1.getTimeInMillis()) && (c_check.getTimeInMillis() <= c_day7.getTimeInMillis())){
    							
    							String currdate="";
    		        			int currminutes;
    		        			int currmonth =  date.getMonth()+1;
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
    		        		
    		        			if (!allfulldays.containsKey(currdate)){
    		        				HashMap daydetails = new HashMap();
    		        				for (int j=0; j < 24; j++){
    		                			for (int i=0; i < 60; i++){
    		                				String time = "";
    		                				if (j < 10){ time = "0"+String.valueOf(j); }else{ time = String.valueOf(j); }
    		                				if (i < 10){ time = time+":0"+String.valueOf(i); }else{ time = time+":"+String.valueOf(i);}
    		                				if (!daydetails.containsKey(time)){ daydetails.put(time, ""); }
    		                			}	
    		                		}
    		        				allfulldays.put(currdate, daydetails);
    		        			}
    		        		
    		        			HashMap daydetails = (HashMap) allfulldays.get(currdate);
    		        			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    		        			SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
    		        		
    		        			for (int z =2; z < tempbuff.length; z=z+4){
    								String nameid = "";
    								if (z+4 <= tempbuff.length ){
    									tempbuff[z] = tempbuff[z].replace(" ","_");
    									tempbuff[z] = tempbuff[z].replace("'","_");
    									if (tempbuff[z].trim().length() > 1){
    										nameid=nameid+tempbuff[z].trim()+"-"+tempbuff[z+1].trim()+",";			
    								
    										if (prevlist.containsKey(nameid)){
    											matched++;
    										}else{
    											notmatched++;
    										}
    										currentlist.put(nameid, "");
    									}
    								}
    							}
    					
    							if ((notmatched > matched) && (count != 0)){
    						
    								String currtime = sdf2.format(date);
    							
    								daydetails.put(currtime, "transition");
    								allfulldays.put(currdate,daydetails);
    							}
    		        		
    							prevlist= new HashMap();
    							prevlist.putAll(currentlist);
    						
    							count++;
    						}
    		            }catch(Exception e){System.out.println("1 "+e.getMessage());e.printStackTrace();}
    		        }
    		        br.close();
    			}
    			
    			
    			// print out contents of full days map
    			/*List<String> sortedDays=new ArrayList(allfulldays.keySet());
    			Collections.sort(sortedDays);
    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) allfulldays.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					System.out.println(days+","+timeframes+","+daydetails.get(timeframes).toString());
    				}
    			}*/
    			
    			
    		}catch(Exception e){
    			System.out.println("2 "+e.getMessage());
    			e.printStackTrace();
    		}
    		
    		return allfulldays;
    	}
    	
    	private boolean checkIfInArrayList(String check, ArrayList al){
    		boolean flag = false;
    		
    		for (int i =0; i < al.size(); i++){
    			if (al.get(i).toString().contains(check)){
    				flag = true;
    			}
    		}
    		return flag;
    	}
    	
    	
    	private HashMap computeBuckets (String userid, String directory, String wifi_home, String wifi_work, String day1, String day7){
    		
    		HashMap mostcommon_networks = new HashMap();
    		
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
    		         
    	        	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    	        	Date day1start = dateFormat.parse(day1+" "+"00:00");
					Date day7end = dateFormat.parse(day7+" "+"23:59");
					Calendar c_day1 = Calendar.getInstance();
					c_day1.setTime(day1start);
					Calendar c_day7 = Calendar.getInstance();
					c_day7.setTime(day7end);
    	        	
    		        while ((line = br.readLine()) != null) {
    		        	try{
    		           		String [] tempbuff = line.split(",");
    		           		
    		        		SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    		        		Date date = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
    		        		Calendar c_check = Calendar.getInstance();
    		        		c_check.setTime(date);
    						
    						if ((c_check.getTimeInMillis() >= c_day1.getTimeInMillis()) && (c_check.getTimeInMillis() <= c_day7.getTimeInMillis())){
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
    						}
    		        	}catch(Exception e){}
    		        }
    			}
    				
    			System.out.println("finished processing mostcommonmap");
    			
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
    			for (Entry<String,Integer> ez : sortedEntriez) {
    				if (buckets.containsKey(ez.getKey())){
    					if (ez.getKey().contains(wifi_home)){
    						HashMap temphm = new HashMap();
    						temphm.put(ez.getKey(),(ArrayList) buckets.get(ez.getKey()));
    						mostcommon_networks.put("home", temphm);
    						//System.out.println("*********** "+ez.getKey()+","+mostcommonmap.get(ez.getKey()).toString());
    					}else if (checkIfInArrayList(wifi_home, (ArrayList) buckets.get(ez.getKey()))){
    	    				HashMap temphm = new HashMap();
    						temphm.put(ez.getKey(),(ArrayList) buckets.get(ez.getKey()));
    						mostcommon_networks.put("home", temphm);
    					}else if (ez.getKey().contains(wifi_work)){
    						HashMap temphm = new HashMap();
    						temphm.put(ez.getKey(),(ArrayList) buckets.get(ez.getKey()));
    						mostcommon_networks.put("work", temphm);
    					}else if (checkIfInArrayList(wifi_work, (ArrayList) buckets.get(ez.getKey()))){
    						HashMap temphm = new HashMap();
    						temphm.put(ez.getKey(),(ArrayList) buckets.get(ez.getKey()));
    						mostcommon_networks.put("work", temphm);		
    					}else{
    						if (!mostcommon_networks.containsKey("other")){
    							mostcommon_networks.put("other", new HashMap());
    						}
    						HashMap temphm = (HashMap) mostcommon_networks.get("other");
    						temphm.put(ez.getKey(),(ArrayList) buckets.get(ez.getKey()));
    						mostcommon_networks.put("other", temphm);
    					}
    					
    					/*if (Integer.parseInt(mostcommonmap.get(ez.getKey()).toString()) > ((linecounter*15)/100)){
    						HashMap temphm = new HashMap();
    						temphm.put(ez.getKey(),(ArrayList) buckets.get(ez.getKey()));
    						mostcommon_networks.put("Location"+(mostcommon_networks.size()+1), temphm);
    						System.out.println("*********** "+ez.getKey()+","+mostcommonmap.get(ez.getKey()).toString());
    					}else{
    						if (!mostcommon_networks.containsKey("other")){
    							mostcommon_networks.put("other", new HashMap());
    						}
    						HashMap temphm = (HashMap) mostcommon_networks.get("other");
    						temphm.put(ez.getKey(),(ArrayList) buckets.get(ez.getKey()));
    						mostcommon_networks.put("other", temphm);
    					}*/
    				}
    				
    				
    			}
    			
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		return mostcommon_networks;
    	}
    	
    	private HashMap computeContexts (String userid, String directory, HashMap allfulldays, HashMap allcontexts,String day1, String day7){
    		
    		try{
    			

    			File tempf = new File(directory+userid+"_wifi_capture_all.csv");
    		    
    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    		        String line;
    		        
    		        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    	        	Date day1start = dateFormat.parse(day1+" "+"00:00");
					Date day7end = dateFormat.parse(day7+" "+"23:59");
					Calendar c_day1 = Calendar.getInstance();
					c_day1.setTime(day1start);
					Calendar c_day7 = Calendar.getInstance();
					c_day7.setTime(day7end);
    		        
    		        while ((line = br.readLine()) != null) {
    		        	try{
    		        		
    		        		HashMap currentlist = new HashMap();
    						
    		        		String [] tempbuff = line.split(",");
    		        		SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    		        		Date date = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
    		        		
    		        		Calendar c_check = Calendar.getInstance();
    		        		c_check.setTime(date);
    						
    						if ((c_check.getTimeInMillis() >= c_day1.getTimeInMillis()) && (c_check.getTimeInMillis() <= c_day7.getTimeInMillis())){
    		        		
    							String currdate="";
    		        			int currminutes;
    		        			int currmonth =  date.getMonth()+1;
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
    		        		
    		        		
    		        			HashMap daydetails = (HashMap) allfulldays.get(currdate);
    		        			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    		        			SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm");
    		        		
    		        			List<String> sortedLocations=new ArrayList(allcontexts.keySet());
    		    				Collections.sort(sortedLocations);
    		    				for (String loc : sortedLocations) {
    		    					if (loc.contains("home")||loc.contains("work")){
    		    						HashMap hm = (HashMap)allcontexts.get(loc);
    		    						List<String> sortedWifis=new ArrayList(hm.keySet());
    				    				Collections.sort(sortedWifis);
    				    				for (String wifi : sortedWifis) {
    				    					if (line.contains(wifi)){
    				    						String currtime = sdf2.format(date);
    											daydetails.put(currtime, loc);
    											allfulldays.put(currdate,daydetails);
    				    					}
    				    				}
    		    					}
    		    					//System.out.println(bucket+","+((ArrayList) buckets.get(bucket)).size());
    		    				}
    						}
    		        	}catch(Exception e){}
    		        }
    			}
    			
    			
    			// fill in the gaps
    			String prevavailableitem="-1";
    			String emptylist="";
    			List<String> sortedDetails=new ArrayList(allfulldays.keySet());
    			Collections.sort(sortedDetails);
    			for (String days : sortedDetails) {
    				HashMap daydetails = (HashMap) allfulldays.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					String content = daydetails.get(timeframes).toString();
    					if (content.isEmpty()){
    						emptylist = emptylist+days+","+timeframes+";";
    					}else{
    						if (emptylist.length() > 1){
    							String[] tempbuff = emptylist.split(";");
    							if (tempbuff.length <= 2){
    								for (int i=0; i < tempbuff.length; i++){
    									String[] item = tempbuff[i].split(",");
    									HashMap hm = (HashMap)allfulldays.get(item[0]);
    									if (prevavailableitem.contains("-1")){
    										hm.put(item[1], content);
    									}else{
    										hm.put(item[1], prevavailableitem);
    									}
    									allfulldays.put(item[0], hm);
    								}
    							}else{
    								for (int i=0; i < tempbuff.length; i++){
    									String[] item = tempbuff[i].split(",");
    									HashMap hm = (HashMap)allfulldays.get(item[0]);
    									hm.put(item[1], "other");
    									allfulldays.put(item[0], hm);
    								}
    							}
    						}
    						prevavailableitem = content;
    						emptylist = "";
    					}
    				}
    			}
    			if (emptylist.length() > 1){
    				String[] tempbuff = emptylist.split(";");
    				if (tempbuff.length <= 2){
    					for (int i=0; i < tempbuff.length; i++){
    						String[] item = tempbuff[i].split(",");
    						HashMap hm = (HashMap)allfulldays.get(item[0]);
    						hm.put(item[1], prevavailableitem);
    						allfulldays.put(item[0], hm);
    					}
    				}else{
    					for (int i=0; i < tempbuff.length; i++){
    						String[] item = tempbuff[i].split(",");
    						HashMap hm = (HashMap)allfulldays.get(item[0]);
    						hm.put(item[1], "other");
    						allfulldays.put(item[0], hm);
    					}
    				}
    			}
    			
    			//construct full view
    			HashMap fullview = new HashMap();
    			List<String> sortedD=new ArrayList(allfulldays.keySet());
    			Collections.sort(sortedD);
    			for (String days : sortedD) {
    				List<String> sortedTimes=new ArrayList(((HashMap)allfulldays.get(days)).keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					//System.out.println(timeframes+","+fullday.get(timeframes).toString());
    					if (!fullview.containsKey(timeframes)){
    						fullview.put(timeframes, "");
    					}
    					String currentcontent = fullview.get(timeframes).toString();
    					fullview.put(timeframes, currentcontent+((HashMap)allfulldays.get(days)).get(timeframes).toString()+",");
    				}
    			}
    			
    			
    			
    			
    			
    			HashMap writers = new HashMap();
    			
    			List<String> sortedLocations=new ArrayList(allcontexts.keySet());
    			Collections.sort(sortedLocations);
    			for (String loc : sortedLocations) {
    				BufferedWriter outd1 = new BufferedWriter(new FileWriter(directory+"processedx/"+userid+"_"+loc+"_intervals.txt"));
    				writers.put(loc, outd1);
    				try{
    					File captureFileName = new File( directory+"processedx/", userid+"_"+loc+"_intervals.txt" );
           	 			captureFileName.setReadable(true, false);
           	 			captureFileName.setWritable(true, false);
           	 			captureFileName.setExecutable(true, false);
    				}catch(Exception e){}
    			}
    			BufferedWriter outdx = new BufferedWriter(new FileWriter(directory+"processedx/"+userid+"_transition_intervals.txt"));
    			writers.put("transition", outdx);
    			try{
    				File captureFileName = new File( directory+"processedx/", userid+"_transition_intervals.txt" );
       	 			captureFileName.setReadable(true, false);
       	 			captureFileName.setWritable(true, false);
       	 			captureFileName.setExecutable(true, false);
    			}catch(Exception e){}
    			
    			sortedD=new ArrayList(allfulldays.keySet());
    			Collections.sort(sortedD);
    			for (String days : sortedD) {
    			
    				HashMap results = new HashMap();
    				
    				HashMap fullday = (HashMap)allfulldays.get(days);
    				String range=""; 
    				List<String> sortedTimes=new ArrayList(fullday.keySet());
    				Collections.sort(sortedTimes);
    				String previousloc ="";
    				String previoustime ="";
    			
    				for (String timeframes : sortedTimes) {
    				
    					if (range.length() < 1){
    						range = timeframes;
    					}else{
    						if (!previousloc.equals(fullday.get(timeframes).toString())){
    							//System.out.println();
    							range = range+"-"+previoustime;
    							if (!results.containsKey(previousloc)){
    								results.put(previousloc, "");
    							}
    							results.put(previousloc, results.get(previousloc).toString()+range+",");
    							range = timeframes;
    						}
    					}
    					previousloc = fullday.get(timeframes).toString();
    					previoustime = timeframes;
    				}
    				range = range+"-"+previoustime;
    				
    				if (results.containsKey(previousloc)){
    					results.put(previousloc, results.get(previousloc).toString()+range+",");
    				}
    				
    				List<String> sortedLocs=new ArrayList(results.keySet());
    				Collections.sort(sortedLocs);
    				for (String loclabels : sortedLocs) {
    					//System.out.println(loclabels+"-"+results.get(loclabels).toString());
    					if (writers.containsKey(loclabels)){
    						BufferedWriter outd = (BufferedWriter) writers.get(loclabels);
    						outd.write(days+";"+results.get(loclabels).toString());
    						outd.newLine();
    					}	
    				}
    			}
    			
    			List<String> sortedWriters=new ArrayList(writers.keySet());
    			Collections.sort(sortedWriters);
    			for (String loc : sortedWriters) {
    				BufferedWriter outd = (BufferedWriter) writers.get(loc);
    				outd.close();
    			}
    			
    			
    			/*List<String> sortedDays=new ArrayList(allfulldays.keySet());
    			Collections.sort(sortedDays);
    			for (String days : sortedDays) {
    				HashMap daydetails = (HashMap) allfulldays.get(days);
    				List<String> sortedTimes=new ArrayList(daydetails.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					System.out.println(days+","+timeframes+","+daydetails.get(timeframes).toString());
    				}
    			}*/
    			
    			// to be improved further
    			BufferedWriter outd4 = new BufferedWriter(new FileWriter(directory+"processedx/"+userid+"_fullview_intervals.csv"));
    			List<String> sortedDays=new ArrayList(allfulldays.keySet());
    			Collections.sort(sortedDays);
    			//outd4.write(allfulldays_title);
    			//outd4.newLine();
    			for (String dayframes : sortedDays) {
    				HashMap fuday = (HashMap) allfulldays.get(dayframes);
    				String fullviewstring = dayframes+",";
    				List<String> sortedTimes=new ArrayList(fuday.keySet());
    				Collections.sort(sortedTimes);
    				for (String timeframes : sortedTimes) {
    					fullviewstring = fullviewstring+timeframes+";"+fuday.get(timeframes).toString()+",";
    				}
    				System.out.println(fullviewstring);
    				outd4.write(fullviewstring);
    				outd4.newLine();
    			}
    			outd4.close();
    			try{
    				File captureFileName = new File( directory+"processedx/", userid+"_fullview_intervals.csv" );
       	 			captureFileName.setReadable(true, false);
       	 			captureFileName.setWritable(true, false);
       	 			captureFileName.setExecutable(true, false);
    			}catch(Exception e){}
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    		
    		
    		return allfulldays;
    	}	
    	
    	private ArrayList getTimeStampsForWiFiNetwork(String userid, String directory, String wifi,String day1, String day7){
    		ArrayList result = new ArrayList();
    		
    		try{
    		
    			File tempf = new File(directory+userid+"_wifi_capture_all.csv");
    	    
    			if (tempf.exists()){
    				BufferedReader br = new BufferedReader(new FileReader(tempf));
    	        	String line;
    	        	
    	        	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    	        	Date day1start = dateFormat.parse(day1+" "+"00:00");
					Date day7end = dateFormat.parse(day7+" "+"23:59");
					Calendar c_day1 = Calendar.getInstance();
					c_day1.setTime(day1start);
					Calendar c_day7 = Calendar.getInstance();
					c_day7.setTime(day7end);
    	        
    	        	while ((line = br.readLine()) != null) {
    	        		try{
    	        		
    	        		
    	        			String [] tempbuff = line.split(",");
    	        			
    	        			if (line.contains(wifi)){
    	        				SimpleDateFormat  format = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    	        				Date date = format.parse(tempbuff[1].split(" ")[0]+" "+tempbuff[1].split(" ")[1]+" "+tempbuff[1].split(" ")[2]+" "+tempbuff[1].split(" ")[3]+" "+tempbuff[1].split(" ")[5]);
        		        		Calendar c_check = Calendar.getInstance();
        		        		c_check.setTime(date);
        						
        						if ((c_check.getTimeInMillis() >= c_day1.getTimeInMillis()) && (c_check.getTimeInMillis() <= c_day7.getTimeInMillis())){
    	        					String currdate="";
    	        					int currminutes;
    	        					int currmonth =  date.getMonth()+1;
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
    	        			
    	        					result.add(currdate+" "+tempbuff[1].split(" ")[3]);
        						}
        					}
    	        		}catch(Exception e){}
    	        	}
    	        
    	        	br.close();
    			}
    		}catch(Exception e){}
    		return result;
    	}
    	
    	private HashMap writeBucketsToFile (String userid, String directory, HashMap allfulldays, HashMap allcontexts,String day1, String day7){
    		HashMap results = new HashMap();
    		
    		try{
    			
    			BufferedWriter outd = new BufferedWriter(new FileWriter(directory+"processedx/"+userid+"_buckets_details.txt"));
    			
    			List<String> sortedContexts=new ArrayList(allcontexts.keySet());
    			Collections.sort(sortedContexts);
    			for (String cont : sortedContexts) {
    				HashMap buckets = (HashMap) allcontexts.get(cont);
    				List<String> sortedBuckets=new ArrayList(buckets.keySet());
    				Collections.sort(sortedBuckets);
    				
    				for (String bucket : sortedBuckets) {
    					String bucketstring = "";
    					bucketstring=bucketstring+bucket+";";
    					
    					ArrayList al = (ArrayList)buckets.get(bucket);
    					for (int i=0; i < al.size(); i++) {
    						bucketstring=bucketstring+al.get(i).toString()+";";
    					}
    					//System.out.println(cont+": "+bucketstring);
    					if (cont.contains("home") || cont.contains("work")){
    						try{
    							// just write to file
    							outd.write(cont+";"+bucketstring);
    							results.put(bucket, cont+";"+bucketstring);
    							outd.newLine();
    						}catch(Exception e){}
    					}else{
    						try{
    							ArrayList timestamps =getTimeStampsForWiFiNetwork(userid,directory,bucket,day1,day7);
    							//String timestampstring = "";
    							HashMap contexts = new HashMap();
    							for (int i=0; i < timestamps.size(); i++) {
    								//timestampstring=timestampstring+timestamps.get(i).toString().substring(0, timestamps.get(i).toString().lastIndexOf(":"))+";";
    								String currtimestamp =timestamps.get(i).toString().substring(0, timestamps.get(i).toString().lastIndexOf(":"));
    								String [] datebuff = currtimestamp.split(" ");
    								if (allfulldays.containsKey(datebuff[0])){
    									HashMap thisday = (HashMap)allfulldays.get(datebuff[0]);
    									if (thisday.containsKey(datebuff[1])){
    										String thisminute = thisday.get(datebuff[1]).toString();
    										if (!contexts.containsKey(thisminute)){
    											contexts.put(thisminute, 0);
    										}
    										int x = (Integer) contexts.get(thisminute);
    										x++;
    										contexts.put(thisminute, x);
    									}
    								}
    							}
    						
    							String mostcontexts="";
    							int number=0;
    							List<String> sortedConte=new ArrayList(contexts.keySet());
    							Collections.sort(sortedConte);
    						
    							for (String item : sortedConte) {
    								if ((Integer)contexts.get(item) > number){
    									mostcontexts = item;
    									number = (Integer)contexts.get(item); 
    								}
    							}
    						
    							//System.out.println(cont+": "+bucket+","+mostcontexts+","+number);
    							outd.write(mostcontexts+";"+bucketstring);
    							results.put(bucket, mostcontexts+";"+bucketstring);
    							outd.newLine();
    						}catch(Exception e){}
    					}
    					
    				}
    			}
    			outd.close();
    			try{
    				File captureFileName = new File( directory+"processedx/", userid+"_buckets_details.txt" );
    				captureFileName.setReadable(true, false);
       	 			captureFileName.setWritable(true, false);
       	 			captureFileName.setExecutable(true, false);
    			}catch(Exception e){}
    		}catch(Exception e){
    			System.out.println("Error while writing buckets file");
    		}
    		
    		return results;
    	}

    	
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
    	
    	 public void run() {
         	try{
         
         		String dir = getBaseContext().getFilesDir().getPath()+"/";
          		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
				String currentday=getCurrentDay(dir,tm.getDeviceId());

				int currday = Integer.parseInt(currentday.split(" ")[1]);
				
				if ((currday >= 4) && (currday < 10)){
          		
					Date dt = new Date();
					
					if ((dt.getHours() >= 0) && (dt.getHours() < 7)){

						// TO DO: add a check that it won't run if it's not between 2 to 3 am
						if (tm.getDeviceId() != null){

							String userid = tm.getDeviceId();
							boolean flag = false;
							String wifi_home ="";
							String wifi_work ="";
							try{
    					
								File tempf = new File(dir+tm.getDeviceId()+"_locationbucket_list.dat");
								if (tempf.exists()){
									BufferedReader br = new BufferedReader(new FileReader(tempf));
									String line;
									int found =0;
									while ((line = br.readLine()) != null) {
										if (line.contains("wifi_home=") || line.contains("wifi_work=")){
											found++;
											String[] tempbuff = line.split("="); 
											if (line.contains("wifi_home=")){
												wifi_home=tempbuff[1];
											}
											if (line.contains("wifi_work=")){
												wifi_work=tempbuff[1];
											}
										}
									}
									br.close();
									if (found >= 2){
										flag = true;
									}
								}
							}catch(Exception e){e.printStackTrace();}
        				
							String day1="";
							String day7="";
				
							if (flag){
								try{
    					
									File tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
									if (tempf.exists()){
										BufferedReader br = new BufferedReader(new FileReader(tempf));
										String line;
										Date date = new Date();
										DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
					    	
										while ((line = br.readLine()) != null) {
											String [] tempbuff = line.split(";");
											if (line.contains("Day 01")){
												day1= tempbuff[0].split(" ")[2];
												day1 = day1.substring(1, day1.length()-1);
											}
											if (line.contains("Day 07")){
												day7=tempbuff[0].split(" ")[2];
												day7 = day7.substring(1, day7.length()-1);
											}
										}
										br.close();
									}	
        			
        			
								}catch(Exception e){}
        			
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
	
								
									System.out.println(day1+" "+day7);
								
									System.out.println("processing transitioning time periods for: "+userid);
									HashMap alldays_contextmap = identifyTransitions(userid,dir,day1,day7);
									System.gc();
    		
									// get most common wifis and other wifis
									System.out.println("processing most common wifis for: "+userid);
									HashMap allthecontexts = computeBuckets(userid,dir, wifi_home, wifi_work,day1,day7);
									System.gc();
								
									// given the obtained data compute the full view
									System.out.println("given the obtained data compute the full view for: "+userid);
									alldays_contextmap = computeContexts(userid,dir,alldays_contextmap,allthecontexts,day1,day7);
									System.gc();
								
									System.out.println("given all data write the bucket list: "+userid);
									HashMap allbuckets = writeBucketsToFile(userid,dir,alldays_contextmap,allthecontexts,day1,day7);
									System.gc();
								}
								
							}
						}	
					}
				}
        	}catch(Exception e){}
         	         	
    	 }
    	
    };

}

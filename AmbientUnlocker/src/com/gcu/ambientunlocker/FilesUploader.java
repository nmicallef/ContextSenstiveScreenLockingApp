package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.net.ftp.FTPClient;




import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FilesUploader extends Service {

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
        			try{
						Thread.sleep(120000);
					}catch(Exception e){}
        			beat.run();
    	        	stopSelf();
        		}
        	})).start();
    	}
    }
    private static final int BUFFER = 2048;

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
        	try  {
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        		if (tm.getDeviceId() != null){

        			String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
					File fbase =new File(baseDir,"");
        			if (fbase != null && fbase.isDirectory()) {
        				String files[]=  fbase.list();
        				for(int i=0;i<files.length;i++){
        					try{
        						if (files[i].contains(".tmp") && files[i].contains("arffOut") ){
        							File tempf = new File(baseDir+"/"+files[i]);
        							if (tempf.exists()){
        								tempf.delete();  
        								//System.out.println("just deleted: "+baseDir+"/"+files[i]);
        							}
        						}
        					}catch(Exception e){}
        				}
        			}
        			
        			
        			// filelist will contain all files in the files and processedx directory. 
        			ArrayList fileslist = new ArrayList();
        			/*ArrayList fileslistx0 = new ArrayList();
        			ArrayList fileslistx1 = new ArrayList();
        			ArrayList fileslistx2 = new ArrayList();
        			ArrayList fileslistx3 = new ArrayList();
        			ArrayList fileslistx4 = new ArrayList();
        			ArrayList fileslistx5 = new ArrayList();
        			ArrayList fileslistx6 = new ArrayList();
        			ArrayList fileslistx7 = new ArrayList();*/

        			String filedir=getBaseContext().getFilesDir().getPath();
        			//ToDo: change this part
        			//String currentday="Day 08";
        			String currentday=getCurrentDay(filedir+"/",tm.getDeviceId());
        			File f=new File(getBaseContext().getFilesDir(),"");
        			if(f.isDirectory()){
        				String files[]=  f.list();
        				for(int i=0;i<files.length;i++){
        					try{
        						if (files[i].contains(tm.getDeviceId())){
        							if (!files[i].contains("zip")){
        								fileslist.add(new File(filedir+"/"+files[i]));
        							/*if (files[i].contains("Day 00") ){
        								fileslistx0.add(new File(filedir+"/"+files[i]));	
        							}
        							if (files[i].contains("Day 01") ){
        								fileslistx1.add(new File(filedir+"/"+files[i]));	
        							}
        							if (files[i].contains("Day 02") ){
        								fileslistx2.add(new File(filedir+"/"+files[i]));	
        							}
        							if (files[i].contains("Day 03") ){
        								fileslistx3.add(new File(filedir+"/"+files[i]));	
        							}
        							if (files[i].contains("Day 04") ){
        								fileslistx4.add(new File(filedir+"/"+files[i]));	
        							}
        							if (files[i].contains("Day 05") ){
        								fileslistx5.add(new File(filedir+"/"+files[i]));	
        							}
        							if (files[i].contains("Day 06") ){
        								fileslistx6.add(new File(filedir+"/"+files[i]));	
        							}
        							if (files[i].contains("Day 07") ){
        								fileslistx7.add(new File(filedir+"/"+files[i]));	
        							}*/
        							}
        						}
        					}catch(Exception e){}
        				}
        			}
        			String filedir2=getBaseContext().getFilesDir().getPath()+"/processedx";
        			f=new File(filedir2,"");
        			if(f.isDirectory()){
        				String files[]=  f.list();
        				for(int i=0;i<files.length;i++){
        					try{
        						if (files[i].contains(tm.getDeviceId())){
        							if (!files[i].contains("zip")){
        								fileslist.add(new File(filedir2+"/"+files[i]));
        							}
        						}
        					}catch(Exception e){}
        				}
        			}
        			
        			String previousday ="";
        			int prevday = Integer.valueOf(currentday.split(" ")[1]);
        			prevday = prevday-1;
        			if (prevday < 10){
        				previousday = "0"+String.valueOf(prevday);
        			}else{
        				previousday = String.valueOf(prevday);        				
        			}
        			
        			previousday = currentday.split(" ")[0]+" "+previousday;
        			//filelist2 will contain the files that will be emailed to me every night
        			ArrayList fileslist2 = new ArrayList();
        			File fz1 = new File(filedir+"/"+tm.getDeviceId()+"_accelerometer_capture"+previousday+".csv");
        			if (fz1.exists()){
        				fileslist2.add(fz1);
        			}
        			File fz2 = new File(filedir+"/"+tm.getDeviceId()+"_light_capture"+previousday+".csv");
        			if (fz2.exists()){
        				fileslist2.add(fz2);
        			}
        			File fz3 = new File(filedir+"/"+tm.getDeviceId()+"_magneticfield_capture"+previousday+".csv");
        			if (fz3.exists()){
        				fileslist2.add(fz3);
        			}
        			File fz4 = new File(filedir+"/"+tm.getDeviceId()+"_noise_capture"+previousday+".csv");
        			if (fz4.exists()){
        				fileslist2.add(fz4);
        			}
        			File fz5 = new File(filedir+"/"+tm.getDeviceId()+"_wifi_capture"+previousday+".csv");
        			if (fz5.exists()){
        				fileslist2.add(fz5);
        			}
        			File fz6 = new File(filedir+"/"+tm.getDeviceId()+"_locationbucket_list.dat");
        			if (fz6.exists()){
        				fileslist2.add(fz6);
        			}
        			File fz7 = new File(filedir+"/"+tm.getDeviceId()+"_tasklist.txt");
        			if (fz7.exists()){
        				fileslist2.add(fz7);
        			}
        			File fz8 = new File(filedir+"/"+tm.getDeviceId()+"_unlocks_usage.csv");
        			if (fz8.exists()){
        				fileslist2.add(fz8);
        			}
        			File fz9 = new File(filedir+"/savedPass.txt");
        			if (fz9.exists()){
        				fileslist2.add(fz9);
        			}
        			File fz10 = new File(filedir+"/"+tm.getDeviceId()+"_context_selections.dat");
        			if (fz10.exists()){
        				fileslist2.add(fz10);
        			}
        			File fz11 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_allwekaresults.txt");
        			if (fz11.exists()){
        				fileslist2.add(fz11);
        			}
        			File fz12 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_buckets_details.txt");
        			if (fz12.exists()){
        				fileslist2.add(fz12);
        			}
        			File fz13 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_fullview_intervals.csv");
        			if (fz13.exists()){
        				fileslist2.add(fz13);
        			}
        			/*File fz14= new File(filedir+"/processedx/"+tm.getDeviceId()+"_home_intervals.txt");
        			if (fz14.exists()){
        				fileslist2.add(fz14);
        			}
        			File fz15 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_other_intervals.txt");
        			if (fz15.exists()){
        				fileslist2.add(fz15);
        			}
        			File fz16 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_transition_intervals.txt");
        			if (fz16.exists()){
        				fileslist2.add(fz16);
        			}
        			File fz17 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_work_intervals.txt");
        			if (fz17.exists()){
        				fileslist2.add(fz17);
        			}*/
        			
        			/*File fz18 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_profile_full_method1.csv");
        			if (fz18.exists()){
        				fileslist2.add(fz18);
        			}*/
        			/*File fz19= new File(filedir+"/processedx/"+tm.getDeviceId()+"_profile_full_home_method2.arff");
        			if (fz19.exists()){
        				fileslist2.add(fz19);
        			}
        			File fz20 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_profile_full_other_method2.arff");
        			if (fz20.exists()){
        				fileslist2.add(fz20);
        			}
        			File fz21 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_profile_full_transition_method2.arff");
        			if (fz21.exists()){
        				fileslist2.add(fz21);
        			}
        			File fz22 = new File(filedir+"/processedx/"+tm.getDeviceId()+"_profile_full_work_method2.arff");
        			if (fz22.exists()){
        				fileslist2.add(fz22);
        			}*/
        			//BufferedWriter outdat = new BufferedWriter(new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+tm.getDeviceId()+"_currentfilesdirectory.txt"));
        			
        			
        			Date dt = new Date();
        			String title = "Email Notification - User: "+tm.getDeviceId()+", "+dt.toString();
        			String content="";
        			try{
        				for (int i = 0 ; i < fileslist.size(); i++){
        					File tempfile = (File)fileslist.get(i);
        					Date lastModDate = new Date(tempfile.lastModified());
        					content= content+ tempfile.getPath()+":"+lastModDate.toString()+", size:"+tempfile.length()+"\n";
        					//outdat.write(tempfile.getPath()+":"+lastModDate.toString()+", size:"+tempfile.length());
        					//outdat.newLine();
        				}
        			}catch(Exception e){}
        			//outdat.close();

        			Calendar c = Calendar.getInstance();
        			c.setTime(new Date());
        			
        			
        			/*if (tm.getDeviceId().equals("353720059369732") || tm.getDeviceId().equals("355830052986396") || tm.getDeviceId().equals("356633052505342") || tm.getDeviceId().equals("359021044237843")){
        				
        				String fileNamex0 = tm.getDeviceId()+"_attachment0_zip_"+c.getTimeInMillis()+".zip";

        				try{
        					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
        					if(fz.isDirectory()){
        						try{
        						  fz.setReadable(true, false);
        						  fz.setWritable(true, false);
        						  fz.setExecutable(true, false);
        						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
        						
        						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
        						fz=new File(filedir,"");
      	      			      	if(!fz.isDirectory()){
      	      			      		fz.mkdir();
      	      			      	}
      	      			      	try{
      	      			      		fz.setReadable(true, false);
      	      			      		fz.setWritable(true, false);
      	      			      		fz.setExecutable(true, false);
      	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
        					}

        					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex0);
        					  ZipParameters parameters = new ZipParameters();
        					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

        					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
        					  parameters.setEncryptFiles(true);
        					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

        					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

        					  // Set password
        					  parameters.setPassword("abcd1234");

        					  zipFile.addFiles(fileslistx0, parameters);

        				  } catch (ZipException e) {
        					System.out.println("exception 6");
        					  e.printStackTrace();
        				  }

        				  try{
        					  File fzip =new File(filedir+"/"+fileNamex0);
        					  System.out.println("Zipped all files in: "+fileNamex0);
        					  fzip.setReadable(true, false);
        					  fzip.setWritable(true, false);
        					  fzip.setExecutable(true, false);
        				  }catch(Exception e){
        					System.out.println("exception 7");
        					  e.printStackTrace();
        				  }

        				  ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        				  NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


        				  if (CheckInternet()){
        	  				  if (mWifi.isConnected()) {
        	  					  try {
        	  						  System.out.println("sending email 1");
        	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
        	  						  sender.addAttachment(filedir+"/"+fileNamex0, content);
        	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
        	  						  //System.out.println("sending email 2");
        	  					  } catch (Exception e) {
        	  						  System.out.println("SendMail: "+ e.getMessage());
        				  	  	  }
        	  				  }
        				  }
        				  
        				  
          				String fileNamex1 = tm.getDeviceId()+"_attachment1_zip_"+c.getTimeInMillis()+".zip";

          				try{
          					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
          					if(fz.isDirectory()){
          						try{
          						  fz.setReadable(true, false);
          						  fz.setWritable(true, false);
          						  fz.setExecutable(true, false);
          						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
          						
          						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
          						fz=new File(filedir,"");
        	      			      	if(!fz.isDirectory()){
        	      			      		fz.mkdir();
        	      			      	}
        	      			      	try{
        	      			      		fz.setReadable(true, false);
        	      			      		fz.setWritable(true, false);
        	      			      		fz.setExecutable(true, false);
        	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
          					}

          					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex1);
          					  ZipParameters parameters = new ZipParameters();
          					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

          					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
          					  parameters.setEncryptFiles(true);
          					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

          					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

          					  // Set password
          					  parameters.setPassword("abcd1234");

          					  zipFile.addFiles(fileslistx1, parameters);

          				  } catch (ZipException e) {
          					System.out.println("exception 6");
          					  e.printStackTrace();
          				  }

          				  try{
          					  File fzip =new File(filedir+"/"+fileNamex1);
          					  System.out.println("Zipped all files in: "+fileNamex1);
          					  fzip.setReadable(true, false);
          					  fzip.setWritable(true, false);
          					  fzip.setExecutable(true, false);
          				  }catch(Exception e){
          					System.out.println("exception 7");
          					  e.printStackTrace();
          				  }

          				  
          				  if (CheckInternet()){
          	  				  if (mWifi.isConnected()) {
          	  					  try {
          	  						  System.out.println("sending email 2");
          	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
          	  						  sender.addAttachment(filedir+"/"+fileNamex1, content);
          	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
          	  						  //System.out.println("sending email 2");
          	  					  } catch (Exception e) {
          	  						  System.out.println("SendMail: "+ e.getMessage());
          				  	  	  }
          	  				  }
          				  }
            				String fileNamex2 = tm.getDeviceId()+"_attachment2_zip_"+c.getTimeInMillis()+".zip";

              				try{
              					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
              					if(fz.isDirectory()){
              						try{
              						  fz.setReadable(true, false);
              						  fz.setWritable(true, false);
              						  fz.setExecutable(true, false);
              						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
              						
              						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
              						fz=new File(filedir,"");
            	      			      	if(!fz.isDirectory()){
            	      			      		fz.mkdir();
            	      			      	}
            	      			      	try{
            	      			      		fz.setReadable(true, false);
            	      			      		fz.setWritable(true, false);
            	      			      		fz.setExecutable(true, false);
            	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
              					}

              					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex2);
              					  ZipParameters parameters = new ZipParameters();
              					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

              					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
              					  parameters.setEncryptFiles(true);
              					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

              					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

              					  // Set password
              					  parameters.setPassword("abcd1234");

              					  zipFile.addFiles(fileslistx2, parameters);

              				  } catch (ZipException e) {
              					System.out.println("exception 6");
              					  e.printStackTrace();
              				  }

              				  try{
              					  File fzip =new File(filedir+"/"+fileNamex2);
              					  System.out.println("Zipped all files in: "+fileNamex2);
              					  fzip.setReadable(true, false);
              					  fzip.setWritable(true, false);
              					  fzip.setExecutable(true, false);
              				  }catch(Exception e){
              					System.out.println("exception 7");
              					  e.printStackTrace();
              				  }

              				  
              				  if (CheckInternet()){
              	  				  if (mWifi.isConnected()) {
              	  					  try {
              	  						  //System.out.println("sending email 1");
              	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
              	  						  sender.addAttachment(filedir+"/"+fileNamex2, content);
              	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
              	  						  //System.out.println("sending email 2");
              	  					  } catch (Exception e) {
              	  						  System.out.println("SendMail: "+ e.getMessage());
              				  	  	  }
              	  				  }
              				  }
                				String fileNamex3 = tm.getDeviceId()+"_attachment3_zip_"+c.getTimeInMillis()+".zip";

                  				try{
                  					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
                  					if(fz.isDirectory()){
                  						try{
                  						  fz.setReadable(true, false);
                  						  fz.setWritable(true, false);
                  						  fz.setExecutable(true, false);
                  						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
                  						
                  						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
                  						fz=new File(filedir,"");
                	      			      	if(!fz.isDirectory()){
                	      			      		fz.mkdir();
                	      			      	}
                	      			      	try{
                	      			      		fz.setReadable(true, false);
                	      			      		fz.setWritable(true, false);
                	      			      		fz.setExecutable(true, false);
                	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
                  					}

                  					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex3);
                  					  ZipParameters parameters = new ZipParameters();
                  					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

                  					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
                  					  parameters.setEncryptFiles(true);
                  					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

                  					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

                  					  // Set password
                  					  parameters.setPassword("abcd1234");

                  					  zipFile.addFiles(fileslistx3, parameters);

                  				  } catch (ZipException e) {
                  					System.out.println("exception 6");
                  					  e.printStackTrace();
                  				  }

                  				  try{
                  					  File fzip =new File(filedir+"/"+fileNamex3);
                  					  System.out.println("Zipped all files in: "+fileNamex3);
                  					  fzip.setReadable(true, false);
                  					  fzip.setWritable(true, false);
                  					  fzip.setExecutable(true, false);
                  				  }catch(Exception e){
                  					System.out.println("exception 7");
                  					  e.printStackTrace();
                  				  }

                  				  
                  				  if (CheckInternet()){
                  	  				  if (mWifi.isConnected()) {
                  	  					  try {
                  	  						  //System.out.println("sending email 1");
                  	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
                  	  						  sender.addAttachment(filedir+"/"+fileNamex3, content);
                  	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
                  	  						  //System.out.println("sending email 2");
                  	  					  } catch (Exception e) {
                  	  						  System.out.println("SendMail: "+ e.getMessage());
                  				  	  	  }
                  	  				  }
                  				  }
                    				String fileNamex4 = tm.getDeviceId()+"_attachment4_zip_"+c.getTimeInMillis()+".zip";

                      				try{
                      					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
                      					if(fz.isDirectory()){
                      						try{
                      						  fz.setReadable(true, false);
                      						  fz.setWritable(true, false);
                      						  fz.setExecutable(true, false);
                      						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
                      						
                      						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
                      						fz=new File(filedir,"");
                    	      			      	if(!fz.isDirectory()){
                    	      			      		fz.mkdir();
                    	      			      	}
                    	      			      	try{
                    	      			      		fz.setReadable(true, false);
                    	      			      		fz.setWritable(true, false);
                    	      			      		fz.setExecutable(true, false);
                    	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
                      					}

                      					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex4);
                      					  ZipParameters parameters = new ZipParameters();
                      					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

                      					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
                      					  parameters.setEncryptFiles(true);
                      					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

                      					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

                      					  // Set password
                      					  parameters.setPassword("abcd1234");

                      					  zipFile.addFiles(fileslistx4, parameters);

                      				  } catch (ZipException e) {
                      					System.out.println("exception 6");
                      					  e.printStackTrace();
                      				  }

                      				  try{
                      					  File fzip =new File(filedir+"/"+fileNamex4);
                      					  System.out.println("Zipped all files in: "+fileNamex4);
                      					  fzip.setReadable(true, false);
                      					  fzip.setWritable(true, false);
                      					  fzip.setExecutable(true, false);
                      				  }catch(Exception e){
                      					System.out.println("exception 7");
                      					  e.printStackTrace();
                      				  }

                      				  
                      				  if (CheckInternet()){
                      	  				  if (mWifi.isConnected()) {
                      	  					  try {
                      	  						  //System.out.println("sending email 1");
                      	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
                      	  						  sender.addAttachment(filedir+"/"+fileNamex4, content);
                      	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
                      	  						  //System.out.println("sending email 2");
                      	  					  } catch (Exception e) {
                      	  						  System.out.println("SendMail: "+ e.getMessage());
                      				  	  	  }
                      	  				  }
                      				  }
                        				String fileNamex5 = tm.getDeviceId()+"_attachment5_zip_"+c.getTimeInMillis()+".zip";

                          				try{
                          					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
                          					if(fz.isDirectory()){
                          						try{
                          						  fz.setReadable(true, false);
                          						  fz.setWritable(true, false);
                          						  fz.setExecutable(true, false);
                          						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
                          						
                          						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
                          						fz=new File(filedir,"");
                        	      			      	if(!fz.isDirectory()){
                        	      			      		fz.mkdir();
                        	      			      	}
                        	      			      	try{
                        	      			      		fz.setReadable(true, false);
                        	      			      		fz.setWritable(true, false);
                        	      			      		fz.setExecutable(true, false);
                        	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
                          					}

                          					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex5);
                          					  ZipParameters parameters = new ZipParameters();
                          					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

                          					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
                          					  parameters.setEncryptFiles(true);
                          					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

                          					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

                          					  // Set password
                          					  parameters.setPassword("abcd1234");

                          					  zipFile.addFiles(fileslistx5, parameters);

                          				  } catch (ZipException e) {
                          					System.out.println("exception 6");
                          					  e.printStackTrace();
                          				  }

                          				  try{
                          					  File fzip =new File(filedir+"/"+fileNamex5);
                          					  System.out.println("Zipped all files in: "+fileNamex5);
                          					  fzip.setReadable(true, false);
                          					  fzip.setWritable(true, false);
                          					  fzip.setExecutable(true, false);
                          				  }catch(Exception e){
                          					System.out.println("exception 7");
                          					  e.printStackTrace();
                          				  }

                          				  
                          				  if (CheckInternet()){
                          	  				  if (mWifi.isConnected()) {
                          	  					  try {
                          	  						  //System.out.println("sending email 1");
                          	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
                          	  						  sender.addAttachment(filedir+"/"+fileNamex5, content);
                          	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
                          	  						  //System.out.println("sending email 2");
                          	  					  } catch (Exception e) {
                          	  						  System.out.println("SendMail: "+ e.getMessage());
                          				  	  	  }
                          	  				  }
                          				  }
                            				String fileNamex6 = tm.getDeviceId()+"_attachment6_zip_"+c.getTimeInMillis()+".zip";

                              				try{
                              					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
                              					if(fz.isDirectory()){
                              						try{
                              						  fz.setReadable(true, false);
                              						  fz.setWritable(true, false);
                              						  fz.setExecutable(true, false);
                              						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
                              						
                              						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
                              						fz=new File(filedir,"");
                            	      			      	if(!fz.isDirectory()){
                            	      			      		fz.mkdir();
                            	      			      	}
                            	      			      	try{
                            	      			      		fz.setReadable(true, false);
                            	      			      		fz.setWritable(true, false);
                            	      			      		fz.setExecutable(true, false);
                            	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
                              					}

                              					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex6);
                              					  ZipParameters parameters = new ZipParameters();
                              					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

                              					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
                              					  parameters.setEncryptFiles(true);
                              					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

                              					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

                              					  // Set password
                              					  parameters.setPassword("abcd1234");

                              					  zipFile.addFiles(fileslistx6, parameters);

                              				  } catch (ZipException e) {
                              					System.out.println("exception 6");
                              					  e.printStackTrace();
                              				  }

                              				  try{
                              					  File fzip =new File(filedir+"/"+fileNamex6);
                              					  System.out.println("Zipped all files in: "+fileNamex6);
                              					  fzip.setReadable(true, false);
                              					  fzip.setWritable(true, false);
                              					  fzip.setExecutable(true, false);
                              				  }catch(Exception e){
                              					System.out.println("exception 7");
                              					  e.printStackTrace();
                              				  }

                              				  
                              				  if (CheckInternet()){
                              	  				  if (mWifi.isConnected()) {
                              	  					  try {
                              	  						  //System.out.println("sending email 1");
                              	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
                              	  						  sender.addAttachment(filedir+"/"+fileNamex6, content);
                              	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
                              	  						  //System.out.println("sending email 2");
                              	  					  } catch (Exception e) {
                              	  						  System.out.println("SendMail: "+ e.getMessage());
                              				  	  	  }
                              	  				  }
                              				  }
                                				String fileNamex7 = tm.getDeviceId()+"_attachment7_zip_"+c.getTimeInMillis()+".zip";

                                  				try{
                                  					File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
                                  					if(fz.isDirectory()){
                                  						try{
                                  						  fz.setReadable(true, false);
                                  						  fz.setWritable(true, false);
                                  						  fz.setExecutable(true, false);
                                  						}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
                                  						
                                  						filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
                                  						fz=new File(filedir,"");
                                	      			      	if(!fz.isDirectory()){
                                	      			      		fz.mkdir();
                                	      			      	}
                                	      			      	try{
                                	      			      		fz.setReadable(true, false);
                                	      			      		fz.setWritable(true, false);
                                	      			      		fz.setExecutable(true, false);
                                	      			      	}catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
                                  					}

                                  					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamex7);
                                  					  ZipParameters parameters = new ZipParameters();
                                  					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

                                  					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
                                  					  parameters.setEncryptFiles(true);
                                  					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

                                  					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

                                  					  // Set password
                                  					  parameters.setPassword("abcd1234");

                                  					  zipFile.addFiles(fileslistx7, parameters);

                                  				  } catch (ZipException e) {
                                  					System.out.println("exception 6");
                                  					  e.printStackTrace();
                                  				  }

                                  				  try{
                                  					  File fzip =new File(filedir+"/"+fileNamex7);
                                  					  System.out.println("Zipped all files in: "+fileNamex7);
                                  					  fzip.setReadable(true, false);
                                  					  fzip.setWritable(true, false);
                                  					  fzip.setExecutable(true, false);
                                  				  }catch(Exception e){
                                  					System.out.println("exception 7");
                                  					  e.printStackTrace();
                                  				  }

                                  				  
                                  				  if (CheckInternet()){
                                  	  				  if (mWifi.isConnected()) {
                                  	  					  try {
                                  	  						  //System.out.println("sending email 1");
                                  	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
                                  	  						  sender.addAttachment(filedir+"/"+fileNamex7, content);
                                  	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
                                  	  						  //System.out.println("sending email 2");
                                  	  					  } catch (Exception e) {
                                  	  						  System.out.println("SendMail: "+ e.getMessage());
                                  				  	  	  }
                                  	  				  }
                                  				  }

          				  
          				  
        				
        			}
        			*/
        			
    				String fileName2 = tm.getDeviceId()+"_attachment_zip_"+c.getTimeInMillis()+".zip";

      				  try{


      					  File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
    					  if(fz.isDirectory()){
    						  try{
    							  fz.setReadable(true, false);
    							  fz.setWritable(true, false);
    							  fz.setExecutable(true, false);
    						  }catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
        					  filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
    	        			  fz=new File(filedir,"");
    	      			      if(!fz.isDirectory()){
    	      			    	  fz.mkdir();
    	      				  }
    	      			      try{
    	      			    	  fz.setReadable(true, false);
    	      			    	  fz.setWritable(true, false);
    	      			    	  fz.setExecutable(true, false);
    	      			      }catch(Exception e){System.out.println("exception 5");e.printStackTrace();}
    					  }

      					  ZipFile zipFile = new ZipFile(filedir+"/"+fileName2);
      					  ZipParameters parameters = new ZipParameters();
      					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

      					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
      					  parameters.setEncryptFiles(true);
      					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

      					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

      					  // Set password
      					  parameters.setPassword("abcd1234");

      					  zipFile.addFiles(fileslist2, parameters);

      				  } catch (ZipException e) {
      					System.out.println("exception 6");
      					  e.printStackTrace();
      				  }

      				  try{
      					  File fzip =new File(filedir+"/"+fileName2);
      					  System.out.println("Zipped all files in: "+fileName2);
      					  fzip.setReadable(true, false);
      					  fzip.setWritable(true, false);
      					  fzip.setExecutable(true, false);
      				  }catch(Exception e){
      					System.out.println("exception 7");
      					  e.printStackTrace();
      				  }

      				  ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
      				  NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


      				  if (CheckInternet()){
      	  				  if (mWifi.isConnected()) {
      	  					  try {
      	  						  System.out.println("sending email 1");
      	  						  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
      	  						  sender.addAttachment(filedir+"/"+fileName2, content);
      	  						  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
      	  						  System.out.println("sending email 2");
      	  					  } catch (Exception e) {
      	  						  System.out.println("SendMail: "+ e.getMessage());
      				  	  	  }
      	  				  }
      				  }


  				  String fileName = tm.getDeviceId()+"_zip_"+c.getTimeInMillis()+".zip";

  				  try{
  					
  					DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
  					DecimalFormat twoDForm = new DecimalFormat("##");  
  					
  					  System.out.println("downloads folder: "+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
  					  File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
  					  if(fz.isDirectory()){
      					  try{
      						  fz.setReadable(true, false);
      						  fz.setWritable(true, false);
      						  fz.setExecutable(true, false);
      					  }catch(Exception e){System.out.println("exception 1");e.printStackTrace();}
  						  filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors";
	    				  fz=new File(filedir,"");
	      				  if(!fz.isDirectory()){
	      					fz.mkdir();
	      				  }
	      				  try{
	      					  fz.setReadable(true, false);
	      					  fz.setWritable(true, false);
	      					  fz.setExecutable(true, false);
	      				  }catch(Exception e){System.out.println("exception 2");e.printStackTrace();}
	        		  }

  					  ZipFile zipFile = new ZipFile(filedir+"/"+fileName);
  					  ZipParameters parameters = new ZipParameters();
  					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

  					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
  					  parameters.setEncryptFiles(true);
  					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

  					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

  					  // Set password
  					  parameters.setPassword("abcd1234");

  					  zipFile.addFiles(fileslist, parameters);

  				  } catch (ZipException e) {
  					  System.out.println("problem zipping");
  					  e.printStackTrace();
  				  }

  				  try{
  					  File fzip =new File(filedir+"/"+fileName);
  					  System.out.println("Zipped all files in: "+fileName);
  					  fzip.setReadable(true, false);
  					  fzip.setWritable(true, false);
  					  fzip.setExecutable(true, false);
  				  }catch(Exception e){
  					System.out.println("exception 3");
  					  e.printStackTrace();
  				  }

  				  
  				  

  				  for(int i=0; i < fileslist.size(); i++) {
  					 if (!fileslist.get(i).toString().contains("light") && !fileslist.get(i).toString().contains("magneticfield") && !fileslist.get(i).toString().contains("wifi") && !fileslist.get(i).toString().contains("noise") && !fileslist.get(i).toString().contains("unlocks") && !fileslist.get(i).toString().contains("tasklist") && !fileslist.get(i).toString().contains("reminderslist") && !fileslist.get(i).toString().contains("locationbucket_list") && !fileslist.get(i).toString().contains("context_selections") && !fileslist.get(i).toString().contains("accelerometer")){
  						 if (!fileslist.get(i).toString().contains("processedx")){ 
  							 File dfile=new File(fileslist.get(i).toString());
  							 if(dfile.exists()){
  								 boolean ans = dfile.delete();
  								 System.out.println("deleted: "+fileslist.get(i).toString()+", "+ans);
  							 }else{
  								 System.out.println(fileslist.get(i).toString()+", file not found");
  							 }
  						 }
  					  }
  				  }
			  
  			  	}
        	 } catch(Exception e) {
        		 System.out.println("exception 8: "+e.getMessage()); 
        	    e.printStackTrace();
        	 }
        	
        	stopSelf();
        }




        public boolean CheckInternet()
        {
            ConnectivityManager connec = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            // Here if condition check for wifi and mobile network is available or not.
            // If anyone of them is available or connected then it will return true, otherwise false;

            if (wifi.isConnected()) {
                return true;
            } else if (mobile.isConnected()) {
                return true;
            }
            return false;
        }

        public void replaceWrongBytesInZip(File zip) throws IOException {
            byte find[] = new byte[] { 0, 0x08, 0x08, 0x08, 0 };
            int index;
            while( (index = indexOfBytesInFile(zip,find)) != -1) {
                replaceWrongZipByte(zip, index + 2);
            }
        }

        private int indexOfBytesInFile(File file,byte find[]) throws IOException {
            byte fileContent[] = new byte[(int) file.length()];
            FileInputStream fin = new FileInputStream(file);
            fin.read(fileContent);
            fin.close();
            return KMPMatch.indexOf(fileContent, find);
        }

        /**
         * Replace wrong byte http://sourceforge.net/tracker/?func=detail&aid=3477810&group_id=14481&atid=114481
         * @param zip file
         * @throws IOException
         */
        private void replaceWrongZipByte(File zip, int wrongByteIndex) throws IOException {
            RandomAccessFile  r = new RandomAccessFile(zip, "rw");
            int flag = Integer.parseInt("00001000", 2);
            r.seek(wrongByteIndex);
            int realFlags = r.read();
            if( (realFlags & flag) > 0) { // in latest versions this bug is fixed, so we're checking is bug exists.
                r.seek(wrongByteIndex);
                flag = (~flag & 0xff);
                // removing only wrong bit, other bits remains the same.
                r.write(realFlags & flag);
            }
            r.close();
        }


    };
}

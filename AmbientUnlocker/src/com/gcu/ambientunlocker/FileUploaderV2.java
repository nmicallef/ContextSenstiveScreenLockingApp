package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class FileUploaderV2 extends Service {

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
        					if (files[i].contains(".tmp") && files[i].contains("arffOut") ){
        						File tempf = new File(baseDir+"/"+files[i]);
            					if (tempf.exists()){
            						tempf.delete();  
            						System.out.println("just deleted: "+baseDir+"/"+files[i]);
            					}
        					}
        				}
        			}
        			
        			
        			// filelist will contain all files in the files and processedx directory. 
        			ArrayList fileslistother = new ArrayList();
        			ArrayList fileslistcpumem = new ArrayList();
        			ArrayList fileslistcapture = new ArrayList();
        			ArrayList fileslistprocessed = new ArrayList();
        			HashMap filelistdays = new HashMap();

        			String filedir=getBaseContext().getFilesDir().getPath();
        			File f=new File(getBaseContext().getFilesDir(),"");
        			if(f.isDirectory()){
        				String files[]=  f.list();
        				for(int i=0;i<files.length;i++){
        					try{
        						if (!files[i].contains("zip")){
        							if (files[i].contains("cpumem")){
        								fileslistcpumem.add(new File(filedir+"/"+files[i]));
        							}else if (files[i].contains("capture_")){
        								fileslistcapture.add(new File(filedir+"/"+files[i]));
        							}else{
        								if (!files[i].contains("Day")){
        									fileslistother.add(new File(filedir+"/"+files[i]));
        								}else{
           								
        									String currday=files[i].substring(files[i].length()-10, files[i].length()-4);
        									if (!filelistdays.containsKey(currday)){
        										filelistdays.put(currday, new ArrayList());
        									} 
        									ArrayList al = (ArrayList)filelistdays.get(currday);
        									al.add(new File(filedir+"/"+files[i]));
        									filelistdays.put(currday, al);
        								}
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
        						if (!files[i].contains("zip")){
        							fileslistprocessed.add(new File(filedir2+"/"+files[i]));
        						}
        					}catch(Exception e){}
        				}
        			}
        			
        			Calendar c = Calendar.getInstance();
        			c.setTime(new Date());
           			
    				/*  String fileNamecpu = tm.getDeviceId()+"_zipcpumem_"+c.getTimeInMillis()+".zip";

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

      					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamecpu);
      					  ZipParameters parameters = new ZipParameters();
      					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

      					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
      					  parameters.setEncryptFiles(true);
      					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

      					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

      					  // Set password
      					  parameters.setPassword("abcd1234");

      					  zipFile.addFiles(fileslistcpumem, parameters);

      				  } catch (ZipException e) {
      					  System.out.println("problem zipping");
      					  e.printStackTrace();
      				  }

      				  try{
      					  File fzip =new File(filedir+"/"+fileNamecpu);
      					  System.out.println("Zipped all files in: "+fileNamecpu);
      					  fzip.setReadable(true, false);
      					  fzip.setWritable(true, false);
      					  fzip.setExecutable(true, false);
      				  }catch(Exception e){
      					System.out.println("exception 3");
      					  e.printStackTrace();
      				  }*/
      				  
      				  String fileNamecap = tm.getDeviceId()+"_zipcapture_"+c.getTimeInMillis()+".zip";

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

      					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamecap);
      					  ZipParameters parameters = new ZipParameters();
      					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

      					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
      					  parameters.setEncryptFiles(true);
      					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

      					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

      					  // Set password
      					  parameters.setPassword("abcd1234");

      					  zipFile.addFiles(fileslistcapture, parameters);

      				  } catch (ZipException e) {
      					  System.out.println("problem zipping");
      					  e.printStackTrace();
      				  }

      				  try{
      					  File fzip =new File(filedir+"/"+fileNamecap);
      					  System.out.println("Zipped all files in: "+fileNamecap);
      					  fzip.setReadable(true, false);
      					  fzip.setWritable(true, false);
      					  fzip.setExecutable(true, false);
      				  }catch(Exception e){
      					System.out.println("exception 3");
      					  e.printStackTrace();
      				  }
				
  				  String fileNameot = tm.getDeviceId()+"_zipothers_"+c.getTimeInMillis()+".zip";

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

  					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNameot);
  					  ZipParameters parameters = new ZipParameters();
  					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

  					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
  					  parameters.setEncryptFiles(true);
  					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

  					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

  					  // Set password
  					  parameters.setPassword("abcd1234");

  					  zipFile.addFiles(fileslistother, parameters);

  				  } catch (ZipException e) {
  					  System.out.println("problem zipping");
  					  e.printStackTrace();
  				  }

  				  try{
  					  File fzip =new File(filedir+"/"+fileNameot);
  					  System.out.println("Zipped all files in: "+fileNameot);
  					  fzip.setReadable(true, false);
  					  fzip.setWritable(true, false);
  					  fzip.setExecutable(true, false);
  				  }catch(Exception e){
  					System.out.println("exception 3");
  					  e.printStackTrace();
  				  }

  				  String fileNameproc = tm.getDeviceId()+"_zipprocessedx_"+c.getTimeInMillis()+".zip";

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

  					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNameproc);
  					  ZipParameters parameters = new ZipParameters();
  					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

  					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
  					  parameters.setEncryptFiles(true);
  					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

  					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

  					  // Set password
  					  parameters.setPassword("abcd1234");

  					  zipFile.addFiles(fileslistprocessed, parameters);

  				  } catch (ZipException e) {
  					  System.out.println("problem zipping");
  					  e.printStackTrace();
  				  }

  				  try{
  					  File fzip =new File(filedir+"/"+fileNameproc);
  					  System.out.println("Zipped all files in: "+fileNameproc);
  					  fzip.setReadable(true, false);
  					  fzip.setWritable(true, false);
  					  fzip.setExecutable(true, false);
  				  }catch(Exception e){
  					System.out.println("exception 3");
  					  e.printStackTrace();
  				  }
  				  
  				  
  				List<String> sortedKeys=new ArrayList(filelistdays.keySet());
  				Collections.sort(sortedKeys);
  			
  				for (String days : sortedKeys) {
  				  
  				  ArrayList aldays = (ArrayList) filelistdays.get(days);	
  				  String fileNamedays = tm.getDeviceId()+"_zip"+days.replace(" ", "")+"_"+c.getTimeInMillis()+".zip";

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

  					  ZipFile zipFile = new ZipFile(filedir+"/"+fileNamedays);
  					  ZipParameters parameters = new ZipParameters();
  					  parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression

  					  parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
  					  parameters.setEncryptFiles(true);
  					  parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

  					  parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

  					  // Set password
  					  parameters.setPassword("abcd1234");

  					  zipFile.addFiles(aldays, parameters);

  				  } catch (ZipException e) {
  					  System.out.println("problem zipping");
  					  e.printStackTrace();
  				  }

  				  try{
  					  File fzip =new File(filedir+"/"+fileNamedays);
  					  System.out.println("Zipped all files in: "+fileNamedays);
  					  fzip.setReadable(true, false);
  					  fzip.setWritable(true, false);
  					  fzip.setExecutable(true, false);
  				  }catch(Exception e){
  					System.out.println("exception 3");
  					  e.printStackTrace();
  				  }

  				}
  				
    			BufferedWriter outdat = new BufferedWriter(new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+tm.getDeviceId()+"_finalfilesdirectory.txt"));
    			outdat.write("ready");
				outdat.newLine();
    			outdat.close();
    			
  				//send email to say that all files were processed
  				
  				  try{	
  					
  					  ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
  					  NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


  					  if (CheckInternet()){
  						  if (mWifi.isConnected()) {
  							  try {
  								  System.out.println("sending email 1");
  								  Date dt = new Date();
  								  String title = "Email Processed - User: "+tm.getDeviceId()+", "+dt.toString();
  								  String content="Zipped all files!!!";
  								  GMailSender sender = new GMailSender("sensorsdatacollection@gmail.com", "caley5678");
  								  sender.sendMail(title,content,"sensorsdatacollection@gmail.com","nicholas.micallef@gcu.ac.uk");
  								  System.out.println("sending email 2");
  							  }catch (Exception e) {
  								  System.out.println("SendMail: "+ e.getMessage());
  							  }
  						  }
  					  }
				  
  				  }catch(Exception e){}
        			 
        			
        			
        			
        			/*File fz=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"");
        			if(fz.isDirectory()){
        				try{
        					fz.setReadable(true, false);
        					fz.setWritable(true, false);
        					fz.setExecutable(true, false);
        				}catch(Exception e){System.out.println("exception 4");e.printStackTrace();}
   					  	
        				String filedir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors/allthefiles";
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
        			
        			String filedir=getBaseContext().getFilesDir().getPath();
        			String fdir2=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/sensors/allthefiles";
        			File f=new File(getBaseContext().getFilesDir(),"");
        			if(f.isDirectory()){
        				String files[]=  f.list();
        				for(int i=0;i<files.length;i++){
        					try{
        						if (!files[i].contains("zip")){
        								BufferedReader br = new BufferedReader(new FileReader(filedir+"/"+files[i]));
        								String line;
        								File fz1 = new File( fdir2, files[i] );
        								PrintWriter allout = new PrintWriter( new FileWriter( fz1, false ) );
        								while ((line = br.readLine()) != null) {
        									allout.println(line);
        								}
        								br.close();
        								allout.close();        								
        								
        		        				try{
        		        					fz1.setReadable(true, false);
        		        					fz1.setWritable(true, false);
        		        					fz1.setExecutable(true, false);
        			      			    }catch(Exception e){System.out.println("exception 5");e.printStackTrace();}		
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
        						BufferedReader br = new BufferedReader(new FileReader(filedir2+"/"+files[i]));
								String line;
								File fz1  = new File( fdir2, files[i] );
								PrintWriter allout = new PrintWriter( new FileWriter( fz1, false ) );
								while ((line = br.readLine()) != null) {
									allout.println(line);
								}
								br.close();
								allout.close();
		        				try{
		        					fz1.setReadable(true, false);
		        					fz1.setWritable(true, false);
		        					fz1.setExecutable(true, false);
			      			    }catch(Exception e){System.out.println("exception 5");e.printStackTrace();}

								
        					}catch(Exception e){}
        				}
        			}*/
  			  	}
        		BufferedWriter outdat = new BufferedWriter(new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+"/"+tm.getDeviceId()+"_finalfilesdirectory.txt"));
    			outdat.write("ready");
				outdat.newLine();
    			outdat.close();
        		
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

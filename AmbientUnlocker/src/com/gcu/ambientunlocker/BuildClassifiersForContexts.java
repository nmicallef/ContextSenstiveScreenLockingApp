package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class BuildClassifiersForContexts extends Service {
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
    							
    								System.out.println("loading: "+directory+"processedx/"+files[i]);
    								
    								BufferedReader br = new BufferedReader(new FileReader(directory+"processedx/"+files[i]));
    								Instances train = new Instances(br);
    								br.close();
    								train.setClassIndex(train.numAttributes() - 1);
    								train.deleteAttributeAt(3);
    								train.deleteAttributeAt(1);
    								train.deleteAttributeAt(0);
    								System.out.println("deleted attributes");
    						
    								J48 cls = new J48();
    								cls.setUnpruned(false);
    								cls.setConfidenceFactor(new Float("0.1"));
    								cls.buildClassifier(train);
    								System.out.println("built classifier");
    								
    								CustomClassifiers custom=((CustomClassifiers)getApplicationContext());
                                    if (files[i].contains("home")){
                                    	custom.setHomeClassifier(cls);
                                    }
                                    
                                    if (files[i].contains("work")){
                                    	custom.setWorkClassifier(cls);
                                    }
                                    
                                    if (files[i].contains("other")){
                                    	custom.setOtherClassifier(cls);
                                    }
                                    
                                    if (files[i].contains("transition")){
                                    	custom.setTransitionClassifier(cls);
                                    }
    								    							
    								System.gc();
    							}catch(Exception e){System.out.println("Error while loading classifier: "+e.getMessage());}
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
					
				if (tm.getDeviceId() != null){
									
					boolean flag = false;
					try{
						File tempf = new File(dir+"/processedx/"+tm.getDeviceId()+"_profileflag.txt");
						if (tempf.exists()){
							BufferedReader br = new BufferedReader(new FileReader(tempf));
							String line;
	            	
							while ((line = br.readLine()) != null) {
								if (line.contains("processed2")){
									flag = true;
								}
							}
							br.close();
						}
								
						if (flag){
							createModels(tm.getDeviceId(),dir);
							System.gc();						
						}
					}catch(Exception e){}
				}
        		
        	}catch(Exception e){}
        	stopSelf();
    	}
    };

}

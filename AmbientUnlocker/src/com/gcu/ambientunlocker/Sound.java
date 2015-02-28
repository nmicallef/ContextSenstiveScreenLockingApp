package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.musicg.fingerprint.FingerprintManager;
import com.musicg.processor.TopManyPointsProcessorChain;
import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.telephony.TelephonyManager;

public class Sound extends Service {
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

        		String fileName =  "";
        		Date d = new Date();
    			Calendar c = Calendar.getInstance();
    			c.setTime(d);
        		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        		ArrayList allAmplitude = new ArrayList();
        		if (tm.getDeviceId() != null){
        			try{
        				// Start recording
        				//ExtAudioRecorder extAudioRecorder = ExtAudioRecorder.getInstanse(true);    // Compressed recording (AMR)
        				fileName =  getBaseContext().getFilesDir()+"/"+"sound_capture.wav";
        				ExtAudioRecorder extAudioRecorder = ExtAudioRecorder.getInstanse(false); // Uncompressed recording (WAV)
        				extAudioRecorder.setOutputFile(fileName);
        				extAudioRecorder.prepare();
        				extAudioRecorder.start();

        				try{
        					Thread.sleep(15000);
        				}catch(Exception e){}

        				// Stop recording
        				extAudioRecorder.stop();
        				allAmplitude = extAudioRecorder.getAllAmplitude();
        				extAudioRecorder.release();

        				File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_"+c.getTimeInMillis()+"_sound_capture.wav" );
        				captureFileName.setReadable(true, false);
        				captureFileName.setWritable(true, false);
        				captureFileName.setExecutable(true, false);
        				System.out.println("finished recording: "+allAmplitude.size());

        			}catch(Exception ex){}

        			/*Wave wave = null;
        			String filecapturename =  getBaseContext().getFilesDir()+"/"+tm.getDeviceId()+"_"+c.getTimeInMillis()+"_";
        			try{

        				File file = new File(fileName);
        				if(file.exists()){
        					wave= new Wave(fileName);


        					FingerprintManager fingerprintManager=new FingerprintManager();
        					byte[] fingerprint=wave.getFingerprint();
        					fingerprintManager.saveFingerprintAsFile(fingerprint, filecapturename+"fingerprint.fingerprint");

        					System.out.println("*file:"+filecapturename+"fingerprint.fingerprint");
        					File captureFileName2 = new File( filecapturename+"fingerprint.fingerprint" );
        					captureFileName2.setReadable(true, false);
        					captureFileName2.setWritable(true, false);
        					captureFileName2.setExecutable(true, false);
        				}
        			}catch(Exception ex){ }*/

        			/*try{
        				if (wave != null){
        					Spectrogram spectrogram = new Spectrogram(wave);
        					File captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_"+c.getTimeInMillis()+"_spectogramdata.csv" );

        					PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
                   	 		String temp = c.getTimeInMillis()+","+d.toString()+",time,frequency,intensity";
                   	 		captureFile.println(temp);
        					double [][] amplitudes = spectrogram.getAbsoluteSpectrogramData();
        					for (int row = 0; row < amplitudes.length; row++) {
        			            for (int col = 0; col < amplitudes[row].length; col++) {
        			                temp = row+","+col+","+amplitudes[row][col];
        			            	captureFile.println(temp);
        			            }
        			        }
        					captureFile.close();

                   	 		captureFileName.setReadable(true, false);
                   	 		captureFileName.setWritable(true, false);
                	 		captureFileName.setExecutable(true, false);

        					// Graphic render
        					GraphicRender render = new GraphicRender();
        					render.renderWaveform(wave,filecapturename+"waveform.jpg");
        					System.out.println("*file:"+filecapturename+"waveform.jpg");
        					File captureFileName2 = new File( filecapturename+"waveform.jpg" );
        					captureFileName2.setReadable(true, false);
        					captureFileName2.setWritable(true, false);
        					captureFileName2.setExecutable(true, false);
        					// render.setHorizontalMarker(1);
        					// render.setVerticalMarker(1);
        					render.renderSpectrogram(spectrogram, filecapturename+"spectogram.jpg");
        					System.out.println("*file:"+filecapturename+"spectogram.jpg");

        					captureFileName2 = new File( filecapturename+"spectogram.jpg" );
        					captureFileName2.setReadable(true, false);
        					captureFileName2.setWritable(true, false);
        					captureFileName2.setExecutable(true, false);
   	 					}
        			}catch(Exception ex){ System.out.println(ex.getMessage()); }*/
        		}
        		try{
        			File captureFileName = new File( getBaseContext().getFilesDir(), "sound_capture.wav" );
        			if(captureFileName.exists()){
        				boolean ans= captureFileName.delete();
        				System.out.println("deleted file: "+ans);
        			}

        			if (tm.getDeviceId() != null){
        				PrintWriter captureFile;
        				String currentday = getCurrentDay(getBaseContext().getFilesDir()+"/", tm.getDeviceId());
						
        				captureFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_noise_capture"+currentday+".csv" );
        				captureFile = new PrintWriter( new FileWriter( captureFileName, true ) );
       	 				d = new Date();
       	 				c = Calendar.getInstance();
       	 				c.setTime(d);
       	 				String temp = c.getTimeInMillis()+","+d.toString()+",";
       	 				for (int z=0; z < allAmplitude.size(); z++ ){
       	 					try{
       	 						Integer value = (Integer)allAmplitude.get(z);
       	 						double dB = 20 * Math.log10(value.doubleValue());
       	 						temp = temp+dB+",";
       	 					}catch(Exception ez){
       	 						ez.printStackTrace();
       	 					}
       	 				}
       	 				captureFile.println(temp);
       	 				captureFile.close();

       	 				captureFileName.setReadable(true, false);
       	 				captureFileName.setWritable(true, false);
       	 				captureFileName.setExecutable(true, false);
        			}
        		}catch(Exception ex){}

        	}catch(Exception e){}

        }
    };

}

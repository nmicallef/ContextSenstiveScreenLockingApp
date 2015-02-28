package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


public class PasscodeUnlockActivity extends AbstractPasscodeKeyboardActivity {
	
	private static String password_enc_secret="";
    private static String password_salt="";
    private static String password_preferecence_key="";
    
    protected void loadCurrentPasswordSettings(){
    	try{
        	File localFileName = new File( getBaseContext().getFilesDir(), "local.properties" );
        	
        	if (localFileName.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(localFileName));
        		String line;
        		while ((line = br.readLine()) != null) {
        			String[] tempbuff = line.split("=");
        			if (tempbuff[0].contains("password_enc_secret")){
        				password_enc_secret = tempbuff[1].toString();
        			}
        			if (tempbuff[0].contains("password_salt")){
        				password_salt = tempbuff[1].toString();
        			}
        			if (tempbuff[0].contains("password_preferecence_key")){
        				password_preferecence_key = tempbuff[1].toString();;
        			}
        		}
        		br.close();
        	}else{
        		InputStream is = getAssets().open("local.properties");
        		//InputStream is = getAssets().open(tm.getDeviceId()+"_locations_definitions_set7touse.dat");
        		BufferedReader br = new BufferedReader(new InputStreamReader(is));
        		String line;
        		while ((line = br.readLine()) != null) {
        			String[] tempbuff = line.split("=");
        			if (tempbuff[0].contains("password_enc_secret")){
        				password_enc_secret = tempbuff[1].toString();
        			}
        			if (tempbuff[0].contains("password_salt")){
        				password_salt = tempbuff[1].toString();
        			}
        			if (tempbuff[0].contains("password_preferecence_key")){
        				password_preferecence_key = tempbuff[1].toString();;
        			}
        		}
        		br.close();
        		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(currentApp); 
        		//this.settings = settings;
        	}
        	
        }catch(Exception e){}
    }
    
    @Override
    public void onBackPressed() {
        /*AppLockManager.getInstance().getCurrentAppLock().forcePasswordLock();
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        this.startActivity(i);
        finish();*/
    }
    
    private String decryptPassword(String encryptedPwd) {
        try {
            DESKeySpec keySpec = new DESKeySpec(password_enc_secret.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            byte[] encryptedWithoutB64 = Base64.decode(encryptedPwd, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainTextPwdBytes = cipher.doFinal(encryptedWithoutB64);
            return new String(plainTextPwdBytes);
        } catch (Exception e) {
        }
        return encryptedPwd;
    }

    public boolean verifyPassword( String password ){
    	String storedPassword = "";
    	
    	try{
    		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
    		Calendar c = Calendar.getInstance();
    		Date d = new Date();
    		c.setTime(d);
	
	
    		File unlockFileName = new File( getBaseContext().getFilesDir(), tm.getDeviceId()+"_unlocks_usage.csv" );
    		PrintWriter	unlockFile = new PrintWriter( new FileWriter( unlockFileName, true ) );
    		unlockFileName.setReadable(true, false);
    		unlockFileName.setWritable(true, false);
    		unlockFileName.setExecutable(true, false);
    	
    	
    		if (password_preferecence_key.length() > 1) {
    			//read the password from the new key 
    			storedPassword = password_preferecence_key;
    			storedPassword = decryptPassword(storedPassword);
    			password = password_salt + password +  password_salt;
    		}
        
    		if( password.equalsIgnoreCase(storedPassword) ) {
    			//lostFocusDate = new Date();
    			String temp = c.getTimeInMillis()+",end,"+d.toString()+",PIN";
				unlockFile.println(temp);
				unlockFile.close();	
    			return true;
    		} else {
    			String temp = c.getTimeInMillis()+",error,"+d.toString()+",PIN";
				unlockFile.println(temp);
				unlockFile.close();
    			return false;
    		}
    		
    	}catch(Exception e){return false;}
    }
    
    

    @Override
    protected void onPinLockInserted() {
        String passLock = pinCodeField1.getText().toString() + pinCodeField2.getText().toString() +
                pinCodeField3.getText().toString() + pinCodeField4.getText();
        
        loadCurrentPasswordSettings();
        
        if( verifyPassword(passLock) ) {
            setResult(RESULT_OK);
            finish();
        } else {
            Thread shake = new Thread() {
                public void run() {
                    Animation shake = AnimationUtils.loadAnimation(PasscodeUnlockActivity.this, R.anim.shake);
                    findViewById(R.id.AppUnlockLinearLayout1).startAnimation(shake);
                    showPasswordError();
                    pinCodeField1.setText("");
                    pinCodeField2.setText("");
                    pinCodeField3.setText("");
                    pinCodeField4.setText("");
                    pinCodeField1.requestFocus();
                }
            };
            runOnUiThread(shake);
        }
    }
}
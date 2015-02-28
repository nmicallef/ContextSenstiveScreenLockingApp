package com.gcu.ambientunlocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;


public class PasscodeManagePasswordActivity extends AbstractPasscodeKeyboardActivity {
    private int type = -1;
    private String unverifiedPasscode = null;
    
    private static String password_enc_secret="";
    private static String password_salt="";
    private static String password_preferecence_key="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            type = extras.getInt("type", -1);
        }
        
    }
    @Override
    public void onBackPressed() {
    	Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    	
    	finish();
    }
    
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
    
    private String encryptPassword(String clearText) {
        try {
            DESKeySpec keySpec = new DESKeySpec(password_enc_secret.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            String encrypedPwd = Base64.encodeToString(cipher.doFinal(clearText
                    .getBytes("UTF-8")), Base64.DEFAULT);
            return encrypedPwd;
        } catch (Exception e) {
        }
        return clearText;
    }
    
    protected void savePassword(String password){
    	File localFileName = new File(getBaseContext().getFilesDir(), "local.properties" );
        try {
        	PrintWriter	localFile = new PrintWriter( new FileWriter( localFileName, false ) );
        	
        	password = password_salt + password +  password_salt;
            password = encryptPassword(password);
        	
        	localFile.println("passcodelock.password_preferecence_key="+password);
        	localFile.println("passcodelock.password_salt="+password_salt);
        	localFile.println("passcodelock.password_enc_secret="+password_enc_secret);
        
        	localFile.close();
        }catch( IOException ex ) {
            ex.printStackTrace();
        }
        localFileName.setReadable(true, false);
        localFileName.setWritable(true, false);
        localFileName.setExecutable(true, false);
    }

    
    @Override
    protected void onPinLockInserted() {
        String passLock = pinCodeField1.getText().toString() + pinCodeField2.getText().toString() +
                pinCodeField3.getText().toString() + pinCodeField4.getText();
        
        pinCodeField1.setText("");
        pinCodeField2.setText("");
        pinCodeField3.setText("");
        pinCodeField4.setText("");
        pinCodeField1.requestFocus();
        
        switch (type) {
            
            case PasscodePreferencesActivity.DISABLE_PASSLOCK:
                if( AppLockManager.getInstance().getCurrentAppLock().verifyPassword(passLock) ) {
                    setResult(RESULT_OK);
                    AppLockManager.getInstance().getCurrentAppLock().setPassword(null);
                    finish();
                } else {
                    showPasswordError();
                }
                break;
                
            case PasscodePreferencesActivity.ENABLE_PASSLOCK:
                if( unverifiedPasscode == null ) {
                    ((TextView) findViewById(R.id.top_message)).setText(R.string.passcode_re_enter_passcode);
                    unverifiedPasscode = passLock;
                } else {
                    if( passLock.equals(unverifiedPasscode)) {
                        setResult(RESULT_OK);
                        loadCurrentPasswordSettings();
                        savePassword(passLock);
                        finish();
                    } else {
                        unverifiedPasscode = null;
                        topMessage.setText(R.string.passcode_enter_passcode);
                        showPasswordError();
                    }
                }
                break;
                
            case PasscodePreferencesActivity.CHANGE_PASSWORD:
                //verify old password
                if( AppLockManager.getInstance().getCurrentAppLock().verifyPassword(passLock) ) {
                    topMessage.setText(R.string.passcode_enter_passcode);
                    type = PasscodePreferencesActivity.ENABLE_PASSLOCK;
                } else {
                    showPasswordError();
                } 
                break;
                
            default:
                break;
        }
    }
}
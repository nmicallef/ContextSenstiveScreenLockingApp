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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import weka.classifiers.trees.J48;
import weka.core.Instances;

import com.haibison.android.lockpattern.LockPatternActivity;
import com.haibison.android.lockpattern.util.Settings;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	// This is your preferred flag
	private static final int REQ_CREATE_PATTERN = 1;

	private static char[] savedPattern;

	private static String method1="";
	private static String method2="";

	private boolean samplingServiceRunning = false;
	private int samplingServiceRate = 1;

	private TaskListAdapter listAdapter;
	private TextView tViewFurtherDate;
	private TextView tViewFurtherDevice;
	private TextView tViewFurtherDescription;
	private Button btnTag;
	private boolean homeandworkselected = false;
	private PopupWindow popUp;
	private Button btnConfigure;
	private boolean configuredcontexts = false;
	private boolean configuredcontexts2 = false;
	private PopupWindow popUpConf;

	private Context currentContext;

	//protected void onCreate(Bundle savedInstanceState) {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// load settings from file

		popUp = new PopupWindow(this);
		popUpConf = new PopupWindow(this);
		currentContext = this;

		String filedir=getBaseContext().getFilesDir().getPath()+"/";

        try{
        	File tempfz = new File(filedir+"settings.txt");

        	if (tempfz.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
        		String line;

        		while ((line = br.readLine()) != null) {

        			String [] tempbuff = line.split(",");
        			if (tempbuff[0].equals("samplingServiceRunning")){
        				samplingServiceRunning = Boolean.parseBoolean(tempbuff[1]);
        			}else if (tempbuff[0].equals("samplingServiceRate")){
        				samplingServiceRate = Integer.parseInt(tempbuff[1]);
        			}
        		}
        		br.close();
        	}else{
        		BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));
				outdat.write("samplingServiceRunning,false");
				outdat.newLine();
				outdat.write("samplingServiceRate,1");
				outdat.newLine();
        	}
        }catch(Exception e){}

        //Todo: remove comment
        
		if (isServiceRunning()){
			samplingServiceRunning = true;
		}else{
			samplingServiceRunning = false;
			// not running .. start all sensors
			startSamplingService();
		}
        
        //Todo remove the following part
	    /*Calendar fiveamCalendar = Calendar.getInstance();
	    fiveamCalendar.set(Calendar.HOUR_OF_DAY, 6);
	    fiveamCalendar.set(Calendar.MINUTE, 0);
	    fiveamCalendar.set(Calendar.SECOND, 0);
	    Intent iFilesUploaderService = new Intent(this, FilesUploader.class);
		PendingIntent piFilesUploaderService = PendingIntent.getService(this, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piFilesUploaderService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fiveamCalendar.getTimeInMillis() , 21600000, piFilesUploaderService);*/
        

		// then display screen
		displayScreen();
	}

	public boolean isServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	if (service.service.getClassName().toLowerCase().contains("com.gcu.ambientunlocker")) {
	            return true;
	        }
	    }
	    return false;
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

	protected void buildDefaultScreen(){

		final LinearLayout lLayout = new LinearLayout(this);
        lLayout.setOrientation(LinearLayout.VERTICAL);
        lLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        TextView tView = new TextView(this);
        tView.setText("Location-sensitive protection");
        tView.setTextColor(Color.BLACK);
        tView.setTextSize(16);
        tView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tViewlps  = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        tViewlps.setMargins(5, 0, 0, 0);
        tView.setLayoutParams(tViewlps);
        lLayout.addView(tView);

        TextView tViewSpace1 = new TextView(this);
        tViewSpace1.setText("");
        tViewSpace1.setTextColor(Color.BLACK);
        tViewSpace1.setTextSize(5);
        tViewSpace1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        lLayout.addView(tViewSpace1);

        TextView tDescrView = new TextView(this);
        tDescrView.setText("The following table summarizes all the tasks that you need to perform everyday throughout the duration of this study. The current date is marked in green. Tasks that require your immediate attention are marked in red. Click on the item to get a detailed description.");
        tDescrView.setTextColor(Color.BLACK);
        tDescrView.setTextSize(12);
        tDescrView.setTypeface(null, Typeface.ITALIC);
        LinearLayout.LayoutParams tDescrViewlps  = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        tDescrViewlps.setMargins(5, 0, 0, 0);
        tDescrView.setLayoutParams(tDescrViewlps);
        lLayout.addView(tDescrView);

        TextView tViewSpace2 = new TextView(this);
        tViewSpace2.setText("");
        tViewSpace2.setTextColor(Color.BLACK);
        tViewSpace2.setTextSize(5);
        tViewSpace2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        lLayout.addView(tViewSpace2);

        TextView tViewHeader = new TextView(this);
        tViewHeader.setText("Day                    Phone protection    Tasks");
        tViewHeader.setTextSize(11);
        tViewHeader.setTextColor(Color.BLACK);
        tViewHeader.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tViewHeaderlps  = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        tViewHeaderlps.setMargins(5, 0, 0, 0);
        tViewHeader.setLayoutParams(tViewHeaderlps);
        lLayout.addView(tViewHeader);

        
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        String dir = getBaseContext().getFilesDir().getPath()+"/";
		try{

    		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

    		File tempf = new File(dir+tm.getDeviceId()+"_reminderslist.txt");
			if (!tempf.exists()){
				BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
				Date date = new Date();
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				c.add(Calendar.DATE, 3);
				outdat.write("0;Day 03 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
				c.add(Calendar.DATE, 4);
				outdat.write("1;Day 07 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("2;Day 08 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("3;Day 09 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("4;Day 10 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("5;Day 11 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("6;Day 12 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("7;Day 13 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("8;Day 14 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("9;Day 15 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("10;Day 16 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("11;Day 17 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("12;Day 18 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("13;Day 19 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("14;Day 20 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("15;Day 21 ("+dateFormat.format(c.getTime())+");Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
				outdat.close();
			}

			try{
				File gfile =new File(dir+tm.getDeviceId()+"_reminderslist.txt");
				gfile.setReadable(true, false);
				gfile.setWritable(true, false);
				gfile.setExecutable(true, false);
			}catch(Exception e){
				e.printStackTrace();
			}


			tempf = new File(dir+tm.getDeviceId()+"_locationbucket_list.dat");
			final String [] wifilist = new String[8];
			final HashMap wifimap = new HashMap();
			final String [] wifilistnames = new String[8];
			
			if (tempf.exists()){
				BufferedReader br = new BufferedReader(new FileReader(tempf));
				String line;
        		int counter =0;
        		int found =0;
				while ((line = br.readLine()) != null) {
					if (line.contains("wifi_home=") || line.contains("wifi_work=")){
						found++;
					}
					String [] tempbuff = line.split(";");
					
					String tempstr = "";
					for (int i =1; i < tempbuff.length; i++){
						if (tempbuff[i].split(",")[0].length() > 2){
							tempstr = tempstr +tempbuff[i].split(",")[0]+",";
						}
					}
					
					if (tempbuff[0].split(",")[0].length() > 2){
						if (counter < 8){
							wifilist[counter] = tempbuff[0].split(",")[0];
							wifimap.put(counter, tempbuff[0]);
						}
						counter++;
					}
				}
				if (found >= 2){
					homeandworkselected = true;
				}
				br.close();
			}


			System.out.println(homeandworkselected);

			tempf = new File(dir+tm.getDeviceId()+"_context_selections.dat");
			if (tempf.exists()){
				BufferedReader br = new BufferedReader(new FileReader(tempf));
				String line;
        		int counter =0;
				while ((line = br.readLine()) != null) {
					if (line.contains("Date=")){
						counter++;
					}
				}
				if (counter == 1){
					configuredcontexts = true;
				}else if (counter == 2){
					configuredcontexts = true;
					configuredcontexts2 = true;
				}else{
					configuredcontexts = false;
					configuredcontexts2 = false;
				}
				br.close();

			}
			
			/*if (tm.getDeviceId().contains("351680069824021") || tm.getDeviceId().contains("353975050450846") ){
				File tempf3 = new File(dir+tm.getDeviceId()+"_lookup.txt");
				if (!tempf3.exists()){
					File tempf2 = new File(dir+tm.getDeviceId()+"_context_selections.dat");
					if (tempf2.exists()){
						try{
							tempf2.setReadable(true, false);
							tempf2.setWritable(true, false);
							tempf2.setExecutable(true, false);
						}catch(Exception e){}
						tempf2.delete();
					}
					
					PrintWriter captureFile = new PrintWriter( new FileWriter( tempf3, false ) );
					captureFile.println("done");
					captureFile.close();
					try{
						tempf3.setReadable(true, false);
   	 					tempf3.setWritable(true, false);
   	 					tempf3.setExecutable(true, false);
					}catch(Exception e){}
				}				
			}*/

			tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
			if (!tempf.exists()){
				BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));

				Date date = new Date();
				outdat.write("Day 00 ("+dateFormat.format(date)+");"+method1+";Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				c.add(Calendar.DATE, 1);
				outdat.write("Day 01 ("+dateFormat.format(c.getTime())+");"+method1+";Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 02 ("+dateFormat.format(c.getTime())+");"+method1+";Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 03 ("+dateFormat.format(c.getTime())+");"+method1+";Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 04 ("+dateFormat.format(c.getTime())+");"+method1+";Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 05 ("+dateFormat.format(c.getTime())+");"+method1+";Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 06 ("+dateFormat.format(c.getTime())+");"+method1+";Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 07 ("+dateFormat.format(c.getTime())+");"+method1+";SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 08 ("+dateFormat.format(c.getTime())+");Location-sensitive + "+method2+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 09 ("+dateFormat.format(c.getTime())+");Location-sensitive + "+method2+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 10 ("+dateFormat.format(c.getTime())+");Location-sensitive + "+method2+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 11 ("+dateFormat.format(c.getTime())+");Location-sensitive + "+method2+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 12 ("+dateFormat.format(c.getTime())+");Location-sensitive + "+method2+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 13 ("+dateFormat.format(c.getTime())+");Location-sensitive + "+method2+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 14 ("+dateFormat.format(c.getTime())+");Location-sensitive + "+method2+";SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 15 ("+dateFormat.format(c.getTime())+");User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 16 ("+dateFormat.format(c.getTime())+");User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 17 ("+dateFormat.format(c.getTime())+");User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 18 ("+dateFormat.format(c.getTime())+");User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 19 ("+dateFormat.format(c.getTime())+");User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 20 ("+dateFormat.format(c.getTime())+");User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 21 ("+dateFormat.format(c.getTime())+");User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 22 ("+dateFormat.format(c.getTime())+");");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 23 ("+dateFormat.format(c.getTime())+");");outdat.newLine();
				c.add(Calendar.DATE, 1);
				outdat.write("Day 24 ("+dateFormat.format(c.getTime())+");");outdat.newLine();
    			outdat.close();

    			try{
					File gfile =new File(dir+tm.getDeviceId()+"_tasklist.txt");
					gfile.setReadable(true, false);
					gfile.setWritable(true, false);
					gfile.setExecutable(true, false);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{

				/*if (tm.getDeviceId().equals("355994059370879")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					boolean flag = false;
					
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 09") &&(line2.contains("2014/08/15"))){
								flag = true;
							}
						}
						br2.close();
						if (!flag){
							tempf.delete();
						}
					}
					if (!flag){
							// do rewrite
						
						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
						outdat.write("Day 00 (2014/08/04);Pattern;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 01 (2014/08/05);Pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 02 (2014/08/06);Pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 03 (2014/08/07);Pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 04 (2014/08/08);Pattern;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 05 (2014/08/09);Pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 06 (2014/08/10);Pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 07 (2014/08/11);Pattern;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 08 (2014/08/14);Location-sensitive + Pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 09 (2014/08/15);Location-sensitive + Pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 10 (2014/08/16);Location-sensitive + Pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 11 (2014/08/17);Location-sensitive + Pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 12 (2014/08/18);Location-sensitive + Pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 13 (2014/08/19);Location-sensitive + Pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 14 (2014/08/20);Location-sensitive + Pattern;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 15 (2014/08/21);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 16 (2014/08/22);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 17 (2014/08/23);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 18 (2014/08/24);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 19 (2014/08/25);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 20 (2014/08/26);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 21 (2014/08/27);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 22 (2014/08/28);");outdat.newLine();
						outdat.write("Day 23 (2014/08/29);");outdat.newLine();
						outdat.write("Day 24 (2014/08/30);");outdat.newLine();
						outdat.close();
					
						// rewrite reminders as well
						outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
						outdat.write("0;Day 03 (2014/08/07);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
						outdat.write("1;Day 07 (2014/08/11);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("2;Day 08 (2014/08/14);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("3;Day 09 (2014/08/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
						outdat.write("4;Day 10 (2014/08/16);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("5;Day 11 (2014/08/17);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("6;Day 12 (2014/09/18);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("7;Day 13 (2014/08/19);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("8;Day 14 (2014/08/20);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
						outdat.write("9;Day 15 (2014/08/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("10;Day 16 (2014/08/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("11;Day 17 (2014/08/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
						outdat.write("12;Day 18 (2014/08/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.write("13;Day 19 (2014/08/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("14;Day 20 (2014/08/26);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("15;Day 21 (2014/08/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.close();
						
												
						
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

						File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
						PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
						captureFile.println("processed2");
						captureFile.close();
						try{
							captureFileName.setReadable(true, false);
       	 					captureFileName.setWritable(true, false);
       	 					captureFileName.setExecutable(true, false);
						}catch(Exception e){}
					}
				}*/
				
				/*if (tm.getDeviceId().equals("357800052537794")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					boolean flag = false;
					
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 08") &&(line2.contains("2014/08/13"))){
								flag = true;
							}
						}
						br2.close();
						if (!flag){
							tempf.delete();
						}
					}
					if (!flag){
							// do rewrite
						
						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
						outdat.write("Day 00 (2014/07/29);PIN;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 01 (2014/07/30);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 02 (2014/07/31);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 03 (2014/08/01);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 04 (2014/08/02);PIN;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 05 (2014/08/03);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 06 (2014/08/04);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 07 (2014/08/05);PIN;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/08/13);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/08/14);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/08/15);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/08/16);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/08/17);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/08/18);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/08/19);Location-sensitive + PIN;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/08/20);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/08/21);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/08/22);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/08/25);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/08/26);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/27);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/28);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/29);");outdat.newLine();
							outdat.write("Day 23 (2014/08/30);");outdat.newLine();
							outdat.write("Day 24 (2014/08/31);");outdat.newLine();
							outdat.close();
					
							// rewrite reminders as well
					
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/08/13);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/08/14);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/08/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/08/16);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/09/17);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/08/18);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/08/19);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/08/20);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/87/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/08/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/07/26);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
						
							try{
								InputStream is = getAssets().open(tm.getDeviceId()+"_buckets_details.txt");
								BufferedReader br = new BufferedReader(new InputStreamReader(is));
								String line;
								PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_buckets_details.txt" ), false ) );
								while ((line = br.readLine()) != null) {
									allout.println(line);
								}
								br.close();
								allout.close();

							}catch(Exception e){}		
						
						
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

							File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
							PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
							captureFile.println("processed2");
							captureFile.close();
							try{
								captureFileName.setReadable(true, false);
       	 						captureFileName.setWritable(true, false);
       	 						captureFileName.setExecutable(true, false);
							}catch(Exception e){}
					}
				}*/
				/*if (tm.getDeviceId().equals("353975050450846")){
					tempf = new File(dir+"processedx/"+tm.getDeviceId()+"_profile_full_home_method2.arff");
					if (!tempf.exists()){				
								
						try{
							InputStream is = getAssets().open(tm.getDeviceId()+"_bucket_details.txt");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_bucket_details.txt" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);	
							}
							br.close();
							allout.close();

						}catch(Exception e){}
							
							
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

						File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
						PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
						captureFile.println("processed2");
						captureFile.close();
						try{
							captureFileName.setReadable(true, false);
           	 				captureFileName.setWritable(true, false);
           	 				captureFileName.setExecutable(true, false);
						}catch(Exception e){}

						
					}
				}*/
				
				
				
				/*if (tm.getDeviceId().equals("353720059369732")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						boolean flag = false;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 08") &&(line2.contains("2014/08/12"))){
								flag = true;
							}
						}
						br2.close();
				
						if (!flag){
							// do rewrite
						
							tempf.delete();
							BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
							outdat.write("Day 00 (2014/07/29);no lock;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 01 (2014/07/30);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 02 (2014/07/31);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 03 (2014/08/01);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 04 (2014/08/02);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 05 (2014/08/03);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 06 (2014/08/04);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 07 (2014/08/11);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/08/12);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/08/13);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/08/14);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/08/15);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/08/16);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/08/17);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/08/18);Location-sensitive + PIN;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/08/19);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/08/20);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/08/21);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/08/22);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/08/23);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/24);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/25);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/26);");outdat.newLine();
							outdat.write("Day 23 (2014/08/27);");outdat.newLine();
							outdat.write("Day 24 (2014/08/28);");outdat.newLine();
							outdat.close();
							
							// rewrite reminders as well
							
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/08/12);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/08/13);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/08/14);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/08/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/09/16);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/08/17);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/08/18);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/08/19);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/87/20);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/08/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
					
						}
					}
				}*/
			
				
				/*if (tm.getDeviceId().equals("351680061364794")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						boolean flag = false;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 08") &&(line2.contains("2014/08/11"))){
								flag = true;
							}
						}
						br2.close();
					
						if (!flag){
							// do rewrite
							
							tempf.delete();
							BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
							outdat.write("Day 00 (2014/07/29);PIN;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 01 (2014/07/30);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 02 (2014/07/31);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 03 (2014/08/01);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 04 (2014/08/02);PIN;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 05 (2014/08/03);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 06 (2014/08/04);PIN;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 07 (2014/08/05);PIN;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/08/11);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/08/12);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/08/13);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/08/14);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/08/15);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/08/16);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/08/17);Location-sensitive + PIN;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/08/18);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/08/19);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/08/20);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/08/21);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/08/22);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/23);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/24);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/25);");outdat.newLine();
							outdat.write("Day 23 (2014/08/26);");outdat.newLine();
							outdat.write("Day 24 (2014/08/27);");outdat.newLine();
							outdat.close();
						
							// rewrite reminders as well
						
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/08/11);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/08/12);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/08/13);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/08/14);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/09/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/08/16);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/08/17);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/08/18);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/87/19);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/08/20);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
							
							try{
								InputStream is = getAssets().open(tm.getDeviceId()+"_bucket_details.txt");
								BufferedReader br = new BufferedReader(new InputStreamReader(is));
								String line;
								PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_bucket_details.txt" ), false ) );
								while ((line = br.readLine()) != null) {
									allout.println(line);
								}
								br.close();
								allout.close();

							}catch(Exception e){}		
							
							
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

							File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
							PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
							captureFile.println("processed2");
							captureFile.close();
							try{
								captureFileName.setReadable(true, false);
           	 					captureFileName.setWritable(true, false);
           	 					captureFileName.setExecutable(true, false);
							}catch(Exception e){}

						}
					}
				}*/
				
				/*if (tm.getDeviceId().equals("355830052986396")){
					
					String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
					File fbase =new File(baseDir,"");
        			if (fbase != null && fbase.isDirectory()) {
        				String files[]=  fbase.list();
        				for(int i=0;i<files.length;i++){
        					if (files[i].contains(".tmp") && files[i].contains("arffOut") ){
        						File tempfz = new File(baseDir+"/"+files[i]);
            					if (tempfz.exists()){
            						tempfz.delete();  
            						System.out.println("just deleted: "+baseDir+"/"+files[i]);
            					}
        					}
        				}
        			}
					
					BufferedReader br2 = new BufferedReader(new FileReader(tempf));
					String line2;
					boolean flag = false;
					while ((line2 = br2.readLine()) != null) {
						if (line2.contains("Day 15") &&(line2.contains("2014/08/07"))){
							flag = true;
						}
					}
					
					if (!flag){
						// do rewrite
						tempf.delete();

						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
						outdat.write("Day 00 (2014/07/09);pattern;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 01 (2014/07/10);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 02 (2014/07/11);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 03 (2014/07/12);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 04 (2014/07/13);pattern;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 05 (2014/07/19);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 06 (2014/07/20);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 07 (2014/07/21);pattern;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 08 (2014/07/27);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 09 (2014/07/28);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 10 (2014/07/29);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 11 (2014/07/30);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 12 (2014/07/31);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 13 (2014/08/01);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 14 (2014/08/06);Location-sensitive + pattern;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 15 (2014/08/07);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 16 (2014/08/08);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 17 (2014/08/09);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 18 (2014/08/10);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 19 (2014/08/11);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 20 (2014/08/12);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 21 (2014/08/13);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 22 (2014/08/14);");outdat.newLine();
						outdat.write("Day 23 (2014/08/15);");outdat.newLine();
						outdat.write("Day 24 (2014/08/16);");outdat.newLine();
						outdat.close();
						
						File captureFileName = new File(dir+tm.getDeviceId()+"_tasklist.txt");
						if (captureFileName.exists()){
							try{
								captureFileName.setReadable(true, false);
								captureFileName.setWritable(true, false);
								captureFileName.setExecutable(true, false);
							}catch(Exception e){}
						}
					
						// rewrite reminders as well
					
						outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
						outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
						outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("2;Day 08 (2014/07/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("3;Day 09 (2014/07/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
						outdat.write("4;Day 10 (2014/07/29);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("5;Day 11 (2014/07/30);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("6;Day 12 (2014/07/31);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("7;Day 13 (2014/08/01);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("8;Day 14 (2014/08/06);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
						outdat.write("9;Day 15 (2014/08/07);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("10;Day 16 (2014/08/08);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("11;Day 17 (2014/08/09);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
						outdat.write("12;Day 18 (2014/08/10);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.write("13;Day 19 (2014/08/11);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("14;Day 20 (2014/08/12);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("15;Day 21 (2014/08/13);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.close();
					
						captureFileName = new File(dir+tm.getDeviceId()+"_reminderslist.txt");
						if (captureFileName.exists()){
							try{
								captureFileName.setReadable(true, false);
								captureFileName.setWritable(true, false);
								captureFileName.setExecutable(true, false);
							}catch(Exception e){}
						}
						
						
						for (int i =0; i < 14; i++){
							String day="";
							if (i < 10){
								day="0"+String.valueOf(day);
							}else{
								day=String.valueOf(day);
							}
							File tempf2 = new File(dir+tm.getDeviceId()+"_noise_captureDay "+day+".csv");
							if (tempf2.exists()){
								tempf2.delete();
							}
							tempf2 = new File(dir+tm.getDeviceId()+"_accelerometer_captureDay "+day+".csv");
							if (tempf2.exists()){
								tempf2.delete();
							}
							tempf2 = new File(dir+tm.getDeviceId()+"_magneticfield_captureDay "+day+".csv");
							if (tempf2.exists()){
								tempf2.delete();
							}
							tempf2 = new File(dir+tm.getDeviceId()+"_wifi_captureDay "+day+".csv");
							if (tempf2.exists()){
								tempf2.delete();
							}
						}
						
						File fdbase =new File(dir,"");
	        			if (fdbase != null && fdbase.isDirectory()) {
	        				String files[]=  fdbase.list();
	        				for(int i=0;i<files.length;i++){
	        					if (files[i].contains("cpumem") ){
	        						File tempfz = new File(dir+files[i]);
	            					if (tempfz.exists()){
	            						tempfz.delete();  
	            						System.out.println("just deleted: "+dir+files[i]);
	            					}
	        					}
	        				}
	        			}
						
					}


				}*/
				/*if (tm.getDeviceId().equals("354245052625194")){
					boolean flag = false;
					tempf = new File(dir+tm.getDeviceId()+"_context_selections.dat");
					if (tempf.exists()){
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Date=2014/08/06")){
								flag = true;
							}
						}
					}
					
					if (!flag){
						// do rewrite

						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_context_selections.dat"));
						outdat.write("Date=2014/08/06");outdat.newLine();
						outdat.write("home=no lock");outdat.newLine();
						outdat.write("work=no lock");outdat.newLine();
						outdat.write("other=Location-sensitive");outdat.newLine();
						outdat.write("transition=Location-sensitive");outdat.newLine();
						outdat.write("newplace=Location-sensitive");outdat.newLine();
						outdat.close();
						
					}						
					
					File captureFileName = new File(dir+tm.getDeviceId()+"_context_selections.dat" );
					if (captureFileName.exists()){
						try{
							captureFileName.setReadable(true, false);
							captureFileName.setWritable(true, false);
							captureFileName.setExecutable(true, false);
						}catch(Exception e){}
					}
					
				}*/
				
				
				/*if (tm.getDeviceId().equals("354244053911059")){
					boolean flag = false;
					tempf = new File(dir+tm.getDeviceId()+"_context_selections.dat");
					if (tempf.exists()){
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Date=2014/08/05")){
								flag = true;
							}
						}
					}
					
					if (!flag){
						// do rewrite

						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_context_selections.dat"));
						outdat.write("Date=2014/08/05");outdat.newLine();
						outdat.write("home=Location-sensitive");outdat.newLine();
						outdat.write("work=Location-sensitive");outdat.newLine();
						outdat.write("other=Location-sensitive");outdat.newLine();
						outdat.write("transition=Location-sensitive");outdat.newLine();
						outdat.write("newplace=Location-sensitive");outdat.newLine();
						outdat.close();
						
					}						
					
				}*/
				/*if (tm.getDeviceId().equals("353771052780318")){
					
					// check if day 15 is 11/08 otherwise rewrite file.
					BufferedReader br2 = new BufferedReader(new FileReader(tempf));
					String line2;
					boolean flag = false;
					while ((line2 = br2.readLine()) != null) {
						if (line2.contains("Day 08") &&(line2.contains("2014/08/08"))){
							flag = true;
						}
					}
					
					if (!flag){
						// do rewrite
						tempf.delete();

						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
						outdat.write("Day 00 (2014/07/31);no lock;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 01 (2014/08/01);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 02 (2014/08/02);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 03 (2014/08/03);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 04 (2014/08/04);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 05 (2014/08/05);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 06 (2014/08/06);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 07 (2014/08/07);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 08 (2014/08/08);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 09 (2014/08/09);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 11 (2014/08/10);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 10 (2014/08/11);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 12 (2014/08/12);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 13 (2014/08/13);");outdat.newLine();
						outdat.write("Day 14 (2014/08/14);");outdat.newLine();
						outdat.write("Day 15 (2014/08/15);");outdat.newLine();
						outdat.close();
						
						// rewrite reminders as well
						
						outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
						outdat.write("0;Day 03 (2014/07/12);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
						outdat.write("1;Day 05 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("2;Day 06 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("3;Day 07 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
						outdat.write("4;Day 08 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("5;Day 09 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("7;Day 10 (2014/08/13);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
						outdat.write("8;Day 11 (2014/08/14);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.write("9;Day 12 (2014/08/17);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.close();
					
					}
				}*/
				
				/*if (tm.getDeviceId().equals("353720059369732")){
					
					// check if day 15 is 11/08 otherwise rewrite file.
					BufferedReader br2 = new BufferedReader(new FileReader(tempf));
					String line2;
					boolean flag = false;
					while ((line2 = br2.readLine()) != null) {
						if (line2.contains("Day 08") &&(line2.contains("2014/07/22"))){
							flag = true;
						}
					}
					
					if (!flag){
						// do rewrite
						tempf.delete();

						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
						outdat.write("Day 00 (2014/07/09);no lock;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 01 (2014/07/10);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 02 (2014/07/11);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 03 (2014/07/12);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 04 (2014/07/13);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 05 (2014/07/14);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 06 (2014/07/15);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 07 (2014/07/21);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 08 (2014/07/22);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 09 (2014/07/23);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 10 (2014/07/24);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 11 (2014/07/25);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 15 (2014/08/11);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 16 (2014/08/12);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 17 (2014/08/13);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 18 (2014/08/14);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 19 (2014/08/15);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 20 (2014/08/16);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 21 (2014/08/17);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 22 (2014/08/18);");outdat.newLine();
						outdat.write("Day 23 (2014/08/19);");outdat.newLine();
						outdat.write("Day 24 (2014/08/20);");outdat.newLine();
						outdat.close();
						
						// rewrite reminders as well
						
						outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
						outdat.write("0;Day 03 (2014/07/12);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
						outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("2;Day 08 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("3;Day 09 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
						outdat.write("4;Day 10 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("5;Day 11 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("6;Day 15 (2014/08/11);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("7;Day 16 (2014/08/12);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("8;Day 17 (2014/08/13);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
						outdat.write("9;Day 18 (2014/08/14);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.write("10;Day 19 (2014/08/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("11;Day 20 (2014/08/16);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("12;Day 21 (2014/08/17);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.close();
						
						
						File captureFileName = new File(dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
						if (captureFileName.exists()){
							try{
								PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
								captureFile.close();
								captureFileName.setReadable(true, false);
								captureFileName.setWritable(true, false);
								captureFileName.setExecutable(true, false);
							}catch(Exception e){}
						}
					}
				}*/
				
				/*if (tm.getDeviceId().equals("356297050626706") || tm.getDeviceId().equals("359544057526125") || tm.getDeviceId().equals("357634056508109") || tm.getDeviceId().equals("353918059443677")){

					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						boolean flag = false;
						HashMap prevdetails = new HashMap();
						while ((line2 = br2.readLine()) != null) {
							prevdetails.put(prevdetails.size(), line2);
							if (line2.contains("Day 08") &&(line2.contains("2014/07/22"))){
								flag = true;
							}
						}
						br2.close();

						if (!flag){
							// do rewrite
							tempf.delete();
							
							BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
							
							for (int i=0; i < 7; i++){
								outdat.write(prevdetails.get(i).toString());outdat.newLine();									
							}
							
							outdat.write("Day 07 (2014/07/21);"+prevdetails.get(7).toString().split(";")[1]+";SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/07/22);"+prevdetails.get(8).toString().split(";")[1]+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/07/23);"+prevdetails.get(9).toString().split(";")[1]+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/07/24);"+prevdetails.get(10).toString().split(";")[1]+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/07/25);"+prevdetails.get(11).toString().split(";")[1]+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/07/26);"+prevdetails.get(12).toString().split(";")[1]+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/07/27);"+prevdetails.get(13).toString().split(";")[1]+";SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/07/28);"+prevdetails.get(14).toString().split(";")[1]+";SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/07/29);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/07/30);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/07/31);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/08/01);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/08/02);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/03);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/04);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/05);");outdat.newLine();
							outdat.write("Day 23 (2014/08/06);");outdat.newLine();
							outdat.write("Day 24 (2014/08/07);");outdat.newLine();
							outdat.close();
						
							// rewrite reminders as well
						
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/07/26);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/07/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/07/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/07/29);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/07/30);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/07/31);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/08/01);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/08/02);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/03);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/04);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
						}
					}
					
				}*/
				
				/*if (tm.getDeviceId().equals("355830052986396")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						boolean flag = false;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 08") &&(line2.contains("2014/07/27"))){
								flag = true;
							}
						}
						br2.close();
					
						if (!flag){
							// do rewrite
							
							tempf.delete();
							BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
							outdat.write("Day 00 (2014/07/09);pattern;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 01 (2014/07/10);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 02 (2014/07/11);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 03 (2014/07/12);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 04 (2014/07/13);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 05 (2014/07/19);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 06 (2014/07/20);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 07 (2014/07/21);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/07/27);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/07/28);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/07/29);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/07/30);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/07/31);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/08/01);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/08/02);Location-sensitive + pattern;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/08/03);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/08/04);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/08/05);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/08/06);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/08/07);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/08);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/09);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/10);");outdat.newLine();
							outdat.write("Day 23 (2014/08/11);");outdat.newLine();
							outdat.write("Day 24 (2014/08/12);");outdat.newLine();
							outdat.close();
						
							// rewrite reminders as well
						
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/07/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/07/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/07/29);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/07/30);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/07/31);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/08/01);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/08/02);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/08/03);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/87/04);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/08/05);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/07/06);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/07/07);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/08);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/09);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
						}
					}
				}*/
				
				/*if (tm.getDeviceId().equals("354245052625194") || tm.getDeviceId().equals("359021044237843")){
					boolean fl = false;
					tempf = new File(dir+"354245052625194_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						while ((line2 = br2.readLine()) != null) {
							
							if (line2.contains("Day 08") &&(line2.contains("2014/07/28"))){
								fl = true;
							}
						}
						br2.close();
						if (!fl){
							tempf.delete();
						}
					}	
					
					if (!fl){
						
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
						
						try{
							InputStream is = getAssets().open("processedx/354245052625194_buckets_details.txt");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", "354245052625194_buckets_details.txt" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						
						try{
							InputStream is = getAssets().open("processedx/354245052625194_profile_full_home_method2.arff");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", "354245052625194_profile_full_home_method2.arff" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("processedx/354245052625194_profile_full_other_method2.arff");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", "354245052625194_profile_full_other_method2.arff" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("processedx/354245052625194_profile_full_transition_method2.arff");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", "354245052625194_profile_full_transition_method2.arff" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("processedx/354245052625194_profile_full_work_method2.arff");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", "354245052625194_profile_full_work_method2.arff" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_accelerometer_captureDay 07.csv");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_accelerometer_captureDay 07.csv" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_light_captureDay 07.csv");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_light_captureDay 07.csv" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}									
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_magneticfield_captureDay 07.csv");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_magneticfield_captureDay 07.csv" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_noise_captureDay 07.csv");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_noise_captureDay 07.csv" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_wifi_captureDay 07.csv");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_wifi_captureDay 07.csv" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_reminderslist.txt");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_reminderslist.txt" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_tasklist.txt");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_tasklist.txt" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						
						
						try{
							InputStream is = getAssets().open("rootdir/354245052625194_unlocks_usage.csv");
							BufferedReader br = new BufferedReader(new InputStreamReader(is));
							String line;
							PrintWriter allout = new PrintWriter( new FileWriter( new File( dir, "354245052625194_unlocks_usage.csv" ), false ) );
							while ((line = br.readLine()) != null) {
								allout.println(line);
							}
							br.close();
							allout.close();

						}catch(Exception e){}			
						

						File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
						PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
						captureFile.println("processed2");
						captureFile.close();
						try{
							captureFileName.setReadable(true, false);
       	 					captureFileName.setWritable(true, false);
       	 					captureFileName.setExecutable(true, false);
						}catch(Exception e){}

					}
				
				}*/
				
				/*if (tm.getDeviceId().equals("356633052505342")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						boolean flag = false;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 08") &&(line2.contains("2014/07/27"))){
								flag = true;
							}
						}
						br2.close();
					
						if (!flag){
							// do rewrite
							
							tempf.delete();
							BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
							outdat.write("Day 00 (2014/07/09);pattern;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 01 (2014/07/10);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 02 (2014/07/11);pattern;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 03 (2014/07/12);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 04 (2014/07/13);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 05 (2014/07/19);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 06 (2014/07/20);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 07 (2014/07/21);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/07/27);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/07/28);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/07/29);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/07/30);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/07/31);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/08/01);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/08/02);Location-sensitive + pattern;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/08/03);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/08/04);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/08/05);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/08/06);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/08/07);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/08);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/09);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/10);");outdat.newLine();
							outdat.write("Day 23 (2014/08/11);");outdat.newLine();
							outdat.write("Day 24 (2014/08/12);");outdat.newLine();
							outdat.close();
						
							// rewrite reminders as well
						
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/07/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/07/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/07/29);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/07/30);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/07/31);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/08/01);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/08/02);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/08/03);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/87/04);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/08/05);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/07/06);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/07/07);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/08);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/09);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
							
							try{
								InputStream is = getAssets().open(tm.getDeviceId()+"_bucket_details.txt");
								BufferedReader br = new BufferedReader(new InputStreamReader(is));
								String line;
								PrintWriter allout = new PrintWriter( new FileWriter( new File( dir+"processedx", tm.getDeviceId()+"_bucket_details.txt" ), false ) );
								while ((line = br.readLine()) != null) {
									allout.println(line);
								}
								br.close();
								allout.close();

							}catch(Exception e){}			
							
							
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

							File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
							PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
							captureFile.println("processed2");
							captureFile.close();
							try{
								captureFileName.setReadable(true, false);
           	 					captureFileName.setWritable(true, false);
           	 					captureFileName.setExecutable(true, false);
							}catch(Exception e){}

							
							
						}
					}
				}*/
				
				/*if (tm.getDeviceId().equals("353720059369732")){
					
					// check if day 15 is 11/08 otherwise rewrite file.
					BufferedReader br2 = new BufferedReader(new FileReader(tempf));
					String line2;
					boolean flag = false;
					while ((line2 = br2.readLine()) != null) {
						if (line2.contains("Day 08") &&(line2.contains("2014/07/19"))){
							flag = true;
						}
					}
					
					if (!flag){
						// do rewrite
						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
						outdat.write("Day 00 (2014/07/09);no lock;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 01 (2014/07/10);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 02 (2014/07/11);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 03 (2014/07/12);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 04 (2014/07/13);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 05 (2014/07/14);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 06 (2014/07/15);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 07 (2014/07/21);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 08 (2014/07/22);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 09 (2014/07/23);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 10 (2014/07/24);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 11 (2014/07/25);Location-sensitive + PIN;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 15 (2014/08/11);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 16 (2014/08/12);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 17 (2014/08/13);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 18 (2014/08/14);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 19 (2014/08/15);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 20 (2014/08/16);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 21 (2014/08/17);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 22 (2014/08/18);");outdat.newLine();
						outdat.write("Day 23 (2014/08/19);");outdat.newLine();
						outdat.write("Day 24 (2014/08/20);");outdat.newLine();
						outdat.close();
						
						// rewrite reminders as well
						
						outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
						outdat.write("0;Day 03 (2014/07/12);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
						outdat.write("1;Day 07 (2014/07/16);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("2;Day 08 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("3;Day 09 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
						outdat.write("4;Day 10 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("5;Day 11 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("6;Day 12 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("7;Day 13 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("8;Day 14 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
						outdat.write("9;Day 15 (2014/08/11);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("10;Day 16 (2014/08/12);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("11;Day 17 (2014/08/13);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
						outdat.write("12;Day 18 (2014/08/14);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.write("13;Day 19 (2014/08/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("14;Day 20 (2014/08/16);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("15;Day 21 (2014/08/17);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.close();
						
						
						File captureFileName = new File(dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
						if (captureFileName.exists()){
							try{
								PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
								captureFile.close();
								captureFileName.setReadable(true, false);
								captureFileName.setWritable(true, false);
								captureFileName.setExecutable(true, false);
							}catch(Exception e){}
						}
					}
				}*/
				
				/*if (tm.getDeviceId().equals("356633052505342")  || tm.getDeviceId().equals("862070020941415")){
					
					BufferedReader br2 = new BufferedReader(new FileReader(tempf));
					String line2;
					boolean flag = false;
					while ((line2 = br2.readLine()) != null) {
						if (line2.contains("Day 08") &&(line2.contains("2014/07/19"))){
							flag = true;
						}
					}
					
					if (!flag){
						// do rewrite
						BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
						outdat.write("Day 00 (2014/07/08);no lock;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 01 (2014/07/09);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 02 (2014/07/10);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 03 (2014/07/11);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 04 (2014/07/12);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 05 (2014/07/13);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 06 (2014/07/14);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
						outdat.write("Day 07 (2014/07/15);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 08 (2014/07/19);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 09 (2014/07/20);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 10 (2014/07/21);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 11 (2014/07/22);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 12 (2014/07/23);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 13 (2014/07/24);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 14 (2014/07/25);Location-sensitive + pattern;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 15 (2014/07/26);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 16 (2014/07/27);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 17 (2014/07/28);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 18 (2014/07/29);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 19 (2014/07/30);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 20 (2014/07/31);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 21 (2014/08/01);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
						outdat.write("Day 22 (2014/08/02);");outdat.newLine();
						outdat.write("Day 23 (2014/08/03);");outdat.newLine();
						outdat.write("Day 24 (2014/08/04);");outdat.newLine();
						outdat.close();
						
						// rewrite reminders as well
						
						outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
						outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
						outdat.write("1;Day 07 (2014/07/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("2;Day 08 (2014/07/19);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("3;Day 09 (2014/07/20);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
						outdat.write("4;Day 10 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("5;Day 11 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("6;Day 12 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("7;Day 13 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("8;Day 14 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
						outdat.write("9;Day 15 (2014/07/26);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("10;Day 16 (2014/07/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("11;Day 17 (2014/07/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
						outdat.write("12;Day 18 (2014/07/29);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.write("13;Day 19 (2014/07/30);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("14;Day 20 (2014/07/31);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
						outdat.write("15;Day 21 (2014/08/01);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
						outdat.close();
						
						File captureFileName = new File(dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
						if (captureFileName.exists()){
							try{
								PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
								captureFile.close();
								captureFileName.setReadable(true, false);
								captureFileName.setWritable(true, false);
								captureFileName.setExecutable(true, false);
							}catch(Exception e){}
						}
	
						
					}
				}*/
				
				/*if (tm.getDeviceId().equals("357800052537430")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						boolean flag = false;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 08") &&(line2.contains("2014/07/20"))){
								flag = true;
							}
						}
						br2.close();
					
						if (!flag){
							// do rewrite
							
							tempf.delete();
							for (int i =8; i < 15; i++){
								String day="";
								if (i < 10){
									day="0"+String.valueOf(day);
								}else{
									day=String.valueOf(day);
								}
								File tempf2 = new File(dir+tm.getDeviceId()+"_noise_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
								tempf2 = new File(dir+tm.getDeviceId()+"_accelerometer_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
								tempf2 = new File(dir+tm.getDeviceId()+"_magneticfield_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
								tempf2 = new File(dir+tm.getDeviceId()+"_wifi_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
							}
							BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
							outdat.write("Day 00 (2014/07/09);no lock;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 01 (2014/07/10);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 02 (2014/07/11);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 03 (2014/07/12);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 04 (2014/07/13);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 05 (2014/07/14);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 06 (2014/07/15);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 07 (2014/07/19);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/07/20);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/07/21);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/07/22);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/07/23);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/07/24);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/07/25);Location-sensitive + pattern;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/07/26);Location-sensitive + pattern;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/07/27);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/07/28);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/07/29);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/07/30);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/07/31);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/01);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/02);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/03);");outdat.newLine();
							outdat.write("Day 23 (2014/08/04);");outdat.newLine();
							outdat.write("Day 24 (2014/08/05);");outdat.newLine();
							outdat.close();
						
							// rewrite reminders as well
						
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/15);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/07/20);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/07/26);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/07/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/07/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/07/29);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/07/30);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/07/31);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/01);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/02);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
						}
					}
				}*/
				
				
				/*if (tm.getDeviceId().equals("358568057560165")){
					tempf = new File(dir+tm.getDeviceId()+"_tasklist.txt");
					if (tempf.exists()){				
						BufferedReader br2 = new BufferedReader(new FileReader(tempf));
						String line2;
						boolean flag = false;
						while ((line2 = br2.readLine()) != null) {
							if (line2.contains("Day 08") &&(line2.contains("2014/07/22"))){
								flag = true;
							}
						}
						br2.close();
					
						if (!flag){
							// do rewrite
							
							tempf.delete();
							for (int i =8; i < 15; i++){
								String day="";
								if (i < 10){
									day="0"+String.valueOf(day);
								}else{
									day=String.valueOf(day);
								}
								File tempf2 = new File(dir+tm.getDeviceId()+"_noise_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
								tempf2 = new File(dir+tm.getDeviceId()+"_accelerometer_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
								tempf2 = new File(dir+tm.getDeviceId()+"_magneticfield_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
								tempf2 = new File(dir+tm.getDeviceId()+"_wifi_captureDay "+day+".csv");
								if (tempf2.exists()){
									tempf2.delete();
								}
							}
							BufferedWriter outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_tasklist.txt"));
							outdat.write("Day 00 (2014/07/09);no lock;Consent Form, Pre-Study questionnaire;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 01 (2014/07/10);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 02 (2014/07/11);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 03 (2014/07/12);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 04 (2014/07/13);no lock;Select Home and Work networks;On this day, click on the select networks button and select your home and work Wi-Fi networks. Today, you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 05 (2014/07/14);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 06 (2014/07/15);no lock;Nothing;On this day you do not have to complete any questionnaires. Just use your phone normally.");outdat.newLine();
							outdat.write("Day 07 (2014/07/21);no lock;SUS & Perception Questionnaire;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 08 (2014/07/22);Location-sensitive + pin;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 09 (2014/07/23);Location-sensitive + pin;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 10 (2014/07/24);Location-sensitive + pin;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 11 (2014/07/25);Location-sensitive + pin;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 12 (2014/07/26);Location-sensitive + pin;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 13 (2014/07/27);Location-sensitive + pin;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 14 (2014/07/28);Location-sensitive + pin;SUS, Perception & Phase 2 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 2 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 15 (2014/07/29);User-defined scheme;SUS & Perception Questionnaires;In the morning select which locking scheme you want to use based on location. Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 16 (2014/07/30);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 17 (2014/07/31);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 18 (2014/08/01);User-defined scheme;SUS, Perception & Mid Phase 3 Questionnaires;In the morning you will be given the option to change the device protection schemes that you selected on Monday. Tonight complete the SUS, Perception & Mid Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 19 (2014/08/02);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 20 (2014/08/03);User-defined scheme;SUS & Perception Questionnaires;Tonight complete the System Usability Scale and Perception Questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 21 (2014/08/04);User-defined scheme;SUS, Perception & Phase 3 Questionnaires;Tonight complete the System Usability Scale, Perception & Phase 3 questionnaires which are provided in your booklet.");outdat.newLine();
							outdat.write("Day 22 (2014/08/05);");outdat.newLine();
							outdat.write("Day 23 (2014/08/06);");outdat.newLine();
							outdat.write("Day 24 (2014/08/07);");outdat.newLine();
							outdat.close();
						
							// rewrite reminders as well
						
							outdat = new BufferedWriter(new FileWriter(dir+tm.getDeviceId()+"_reminderslist.txt"));
							outdat.write("0;Day 03 (2014/07/11);Location-sensitive Reminder;Provide Feedback;Tomorrow select your home and work wifis.;on");outdat.newLine();
							outdat.write("1;Day 07 (2014/07/21);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("2;Day 08 (2014/07/22);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("3;Day 09 (2014/07/23);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires;on");outdat.newLine();
							outdat.write("4;Day 10 (2014/07/24);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("5;Day 11 (2014/07/25);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("6;Day 12 (2014/07/26);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("7;Day 13 (2014/07/27);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("8;Day 14 (2014/07/28);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 2 questionnaires. Use the app to select which device protection scheme you want to use based on your location.;on");outdat.newLine();
							outdat.write("9;Day 15 (2014/07/29);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("10;Day 16 (2014/07/30);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("11;Day 17 (2014/07/31);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires. Tomorrow you will have the option to change the device protection schemes that you defined 3 days ago.;on");outdat.newLine();
							outdat.write("12;Day 18 (2014/08/01);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.write("13;Day 19 (2014/08/02);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("14;Day 20 (2014/08/03);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale and Perception questionnaires.;on");outdat.newLine();
							outdat.write("15;Day 21 (2014/08/04);Location-sensitive Reminder;Fill in questionnairies;Please fill in the System Usability Scale, Perception and Phase 3 questionnaires.;on");outdat.newLine();
							outdat.close();
							
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

							File captureFileName = new File( dir+"/processedx", tm.getDeviceId()+"_profileflag.txt" );
							PrintWriter captureFile = new PrintWriter( new FileWriter( captureFileName, false ) );
							captureFile.println("processed2");
							captureFile.close();
							try{
								captureFileName.setReadable(true, false);
           	 					captureFileName.setWritable(true, false);
           	 					captureFileName.setExecutable(true, false);
							}catch(Exception e){}
							
						}	
					}
				}*/
			}

			TaskListItem curritem = null;
			int currposition =0;
			HashMap<String,TaskListItem> items = new HashMap<String,TaskListItem>();

			if (tempf.exists()){
				BufferedReader br2 = new BufferedReader(new FileReader(tempf));
				String line2;
				int c =0;
				while ((line2 = br2.readLine()) != null) {
					String [] tempbuff = line2.split(";");
					if (tempbuff.length> 2){
						int state=0;
						Date date = new Date();
						if (tempbuff[0].contains(dateFormat.format(date))){
							state = 1;
						}
						// improve this to appear only on current date
						if (tempbuff[0].contains("Day 04") && !homeandworkselected){
							state = 2;
						}
						
						if (tm.getDeviceId().equals("353771052780318")){
							if (tempbuff[0].contains("Day 09") && !configuredcontexts){
								state = 2;
							}
							if (tempbuff[0].contains("Day 11") && !configuredcontexts2){
								state = 2;
							}
						}else{
							if (tempbuff[0].contains("Day 15") && !configuredcontexts){
								state = 2;
							}
							if (tempbuff[0].contains("Day 18") && !configuredcontexts2){
								state = 2;
							}
						}
						
						
						TaskListItem item = new TaskListItem(tempbuff[0],tempbuff[1],tempbuff[2],tempbuff[3],state);
						if (tempbuff[0].contains(dateFormat.format(date))){
							curritem = item;
							currposition = c;
						}
						items.put(tempbuff[0], item);
						c++;
					}
				}
				br2.close();
			}

			List<TaskListItem> sortedItems2=new ArrayList(items.values());

			Display display = getWindowManager().getDefaultDisplay();
			int height = display.getHeight();

			ListView list = new ListView(this);
            list.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,((height*2)/5)));
	        list.setBackgroundResource(R.drawable.border_ui);
	        list.setDividerHeight(3);

	        Comparator<TaskListItem> myComparator = new Comparator<TaskListItem>() {
	            public int compare(TaskListItem obj1,TaskListItem obj2) {
	                return obj1.getDay().compareTo(obj2.getDay());
	            }
	        };

	        Collections.sort(sortedItems2, myComparator);
	        listAdapter = new TaskListAdapter( list.getContext(), sortedItems2);

	        list.setAdapter(  listAdapter );
	        list.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View view,
	                    int position, long id) {

	            	onListItemClick(parent,view,position,id);
	            	return;
	            }
	        });
	        list.setSelection(currposition);
	        list.setScrollbarFadingEnabled(false);
	        lLayout.addView(list);

            TextView tViewSpace3 = new TextView(this);
            tViewSpace3.setText("");
            tViewSpace3.setTextColor(Color.BLACK);
            tViewSpace3.setTextSize(2);
            tViewSpace3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            lLayout.addView(tViewSpace3);


            RelativeLayout relativeLayout1a = new RelativeLayout (this);
            RelativeLayout.LayoutParams relativeLayoutParams;

            btnTag = new Button(this);
            btnTag.setText("Select wifi details");
            btnTag.setTextSize(10);
            relativeLayoutParams = new RelativeLayout.LayoutParams(
            		LayoutParams.WRAP_CONTENT,
                   100);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutParams.setMargins(0, 0, 0, 0);
            relativeLayout1a.addView(btnTag, relativeLayoutParams);
            //lLayout.addView(relativeLayout1a);

            String filedir=getBaseContext().getFilesDir().getPath()+"/";
		    String currentday=getCurrentDay(filedir,tm.getDeviceId());

		    if (tm.getDeviceId().equals("357800052537794")){
	    		btnTag.setVisibility(View.VISIBLE);
	    	}else{
	    		if (currentday.contains("Day 04") && !homeandworkselected){
	    			btnTag.setVisibility(View.VISIBLE);
	    		}else{
	    			btnTag.setVisibility(View.INVISIBLE);
	    		}
	    	}
            OnClickListener myListener = new View.OnClickListener() {
        	    @Override
        	    public void onClick(View v) {
        	    	//Toast.makeText(getApplicationContext(),"Day 4 clicked", Toast.LENGTH_LONG).show();
        	    	//popUp.showAtLocation(lLayout, Gravity.BOTTOM, 10, 10);
                    //popUp.update(50, 50, 300, 80);
        	    	final Display display = getWindowManager().getDefaultDisplay();
        	    	popUp.showAtLocation(v, Gravity.TOP, 0, 80);
 	    		    popUp.update(0, 80, display.getWidth()-60, display.getHeight()/2);
        	    }
        	};
        	btnTag.setOnClickListener(myListener);

            btnConfigure = new Button(this);
            btnConfigure.setText("Configure locations");
            btnConfigure.setTextSize(10);
            //btnTag.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 30));
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                   100);
            relativeLayoutParams.addRule(RelativeLayout.RIGHT_OF,
            		btnTag.getId());
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
            		btnTag.getId()); // added top alignment rule

            relativeLayoutParams.setMargins(0, 0, 0, 0);
            relativeLayout1a.addView(btnConfigure, relativeLayoutParams);
            lLayout.addView(relativeLayout1a);

			if (tm.getDeviceId().equals("353771052780318")){
				if (currentday.contains("Day 09") && !configuredcontexts){
	            	btnConfigure.setVisibility(View.VISIBLE);
	            }else if (currentday.contains("Day 11") && !configuredcontexts2){
	            	btnConfigure.setVisibility(View.VISIBLE);
	            }else{
	            	btnConfigure.setVisibility(View.INVISIBLE);
	            }
			}else{
				//if (currentday.contains("Day 15") && !configuredcontexts){
	            //	btnConfigure.setVisibility(View.VISIBLE);
	            //}else if (currentday.contains("Day 18") && !configuredcontexts2){
	            //	btnConfigure.setVisibility(View.VISIBLE);
	            //}else{
	            //	btnConfigure.setVisibility(View.INVISIBLE);
	            //}
				
				if (currentday.contains("Day 15") || currentday.contains("Day 16") || currentday.contains("Day 17") ||currentday.contains("Day 18") ){
	            	btnConfigure.setVisibility(View.VISIBLE);
	            }else{
	            	btnConfigure.setVisibility(View.INVISIBLE);
	            }
			}
        	btnConfigure.setVisibility(View.VISIBLE);

            OnClickListener myListenerConf = new View.OnClickListener() {
        	    @Override
        	    public void onClick(View v) {
        	    	//Toast.makeText(getApplicationContext(),"Day 4 clicked", Toast.LENGTH_LONG).show();
        	    	//popUp.showAtLocation(lLayout, Gravity.BOTTOM, 10, 10);
                    //popUp.update(50, 50, 300, 80);
        	    	final Display display = getWindowManager().getDefaultDisplay();
        	    	popUpConf.showAtLocation(v, Gravity.TOP, 0, 80);
 	    		    popUpConf.update(0, 80, display.getWidth()-60, (display.getHeight()*3)/4);
        	    }
        	};
        	btnConfigure.setOnClickListener(myListenerConf);





            RelativeLayout relativeLayout1 = new RelativeLayout (this);
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutParams.setMargins(5, 0, 0, 0);
            TextView tViewFurther1 = new TextView(this);
            tViewFurther1.setText("Date: ");
            tViewFurther1.setTextSize(12);
            tViewFurther1.setTextColor(Color.BLACK);
            tViewFurther1.setId(1);
            relativeLayout1.addView(tViewFurther1, relativeLayoutParams);

            tViewFurtherDate = new TextView(this);
            tViewFurtherDate.setText(curritem.getDay());
            tViewFurtherDate.setTextSize(10);
            tViewFurtherDate.setTextColor(Color.BLACK);
            tViewFurtherDate.setTypeface(null, Typeface.BOLD);
            tViewFurtherDate.setId(2);
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.RIGHT_OF,
            		tViewFurther1.getId());
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
            		tViewFurther1.getId()); // added top alignment rule
            relativeLayout1.addView(tViewFurtherDate, relativeLayoutParams);
            lLayout.addView(relativeLayout1);






            //setup popup and all logic behind it

            LinearLayout layout = new LinearLayout(this);


            TextView tpopupHeader = new TextView(this);
            tpopupHeader.setText("Select your home and work Wi-Fi networks by clicking on the following buttons.");
            tpopupHeader.setTextSize(16);
            tpopupHeader.setTextColor(Color.BLACK);
            tpopupHeader.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams tpopupHeaderlps  = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            tpopupHeaderlps.setMargins(5, 0, 0, 0);
            tpopupHeader.setLayoutParams(tpopupHeaderlps);
            layout.addView(tpopupHeader);

            TextView tpopupSpace1 = new TextView(this);
            tpopupSpace1.setText("");
            tpopupSpace1.setTextColor(Color.BLACK);
            tpopupSpace1.setTextSize(6);
            tpopupSpace1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layout.addView(tpopupSpace1);

            //home
            final AlertDialog dialog1;

            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Select home wifi");

            builder1.setSingleChoiceItems(wifilist, 0, null);
            builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                       int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                       System.out.println(wifilist[selected]);

                       try{

                       File captureFileName = new File(getBaseContext().getFilesDir(),tm.getDeviceId()+"_locationbucket_list.dat" );
                       PrintWriter pw = new PrintWriter( new FileWriter( captureFileName, true ));
                       String value1 ="";
                       if (selected >=0){
                   			value1 = wifimap.get(selected).toString();
                   	   }else{
                   			value1 = wifimap.get(0).toString();
                   	   }
                       pw.println("");
					   pw.println("wifi_home="+value1);
					   pw.close();
                       }catch(Exception e){}
                       // Do something useful withe the position of the selected radio button
                   }
               });

              dialog1=builder1.create();

              Button btnSelectDD1 = new Button(this);
              btnSelectDD1.setText("Select Home Wifi");
              btnSelectDD1.setTextSize(10);
              btnSelectDD1.setId(1);
              btnSelectDD1.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                	  dialog1.show();
                  }
              });

              RelativeLayout relativeLayoutpopup1 = new RelativeLayout (this);
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutpopup1.addView(btnSelectDD1, relativeLayoutParams);
            layout.addView(relativeLayoutpopup1);


            TextView tpopupSpace2 = new TextView(this);
            tpopupSpace2.setText("");
            tpopupSpace2.setTextColor(Color.BLACK);
            tpopupSpace2.setTextSize(6);
            tpopupSpace2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layout.addView(tpopupSpace2);


            //work

            	final AlertDialog dialog2;

               AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
               builder2.setTitle("Select work wifi");

               builder2.setSingleChoiceItems(wifilist, 0, null);
               builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                       int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                       System.out.println(wifilist[selected]);

                       try{

                       File captureFileName = new File(getBaseContext().getFilesDir(),tm.getDeviceId()+"_locationbucket_list.dat" );
                       PrintWriter pw = new PrintWriter( new FileWriter( captureFileName, true ));
                       String value1 ="";
                       if (selected >=0){
                   			value1 = wifimap.get(selected).toString();
                   	   }else{
                   			value1 = wifimap.get(0).toString();
                   	   }
					   pw.println("wifi_work="+value1);
					   pw.close();
                       }catch(Exception e){}
                       // Do something useful withe the position of the selected radio button
                   }
               });

              dialog2=builder2.create();

              Button btnSelectDD2 = new Button(this);
              btnSelectDD2.setText("Select Work Wifi");
              btnSelectDD2.setTextSize(10);
              btnSelectDD2.setId(1);
              btnSelectDD2.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                	  dialog2.show();
                  }
              });

              RelativeLayout relativeLayoutpopup2 = new RelativeLayout (this);
              relativeLayoutParams = new RelativeLayout.LayoutParams(
                      RelativeLayout.LayoutParams.WRAP_CONTENT,
                      RelativeLayout.LayoutParams.WRAP_CONTENT);
              relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
              relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
              relativeLayoutpopup2.addView(btnSelectDD2, relativeLayoutParams);
              layout.addView(relativeLayoutpopup2);


            TextView tpopupSpace3 = new TextView(this);
            tpopupSpace3.setText("");
            tpopupSpace3.setTextColor(Color.BLACK);
            tpopupSpace3.setTextSize(6);
            tpopupSpace3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layout.addView(tpopupSpace3);


            RelativeLayout relativeLayoutpopup3 = new RelativeLayout (this);
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.setMargins(5, 0, 0, 0);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            Button btnPopup2 = new Button(this);
            btnPopup2.setText("Exit");
            btnPopup2.setTextSize(10);
            btnPopup2.setId(2);
            btnPopup2.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                	popUp.dismiss();
                }
            });
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutpopup3.addView(btnPopup2, relativeLayoutParams);
            layout.addView(relativeLayoutpopup3);


            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundResource(android.R.color.darker_gray);
            popUp.setContentView(layout);


            /*
             *
             *
             *
             * */

            //lLayout.addView(relativeLayout1);


            //setup popup and all logic behind it

            LinearLayout layoutConf = new LinearLayout(this);


            TextView tpopupHeaderConf = new TextView(this);
            tpopupHeaderConf.setText("Select which locking methods do you want to use in the following locations.");
            tpopupHeaderConf.setTextSize(16);
            tpopupHeaderConf.setTextColor(Color.BLACK);
            tpopupHeaderConf.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams tpopupHeaderlpsConf  = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            tpopupHeaderlpsConf.setMargins(5, 0, 0, 0);
            tpopupHeaderConf.setLayoutParams(tpopupHeaderlpsConf);
            layoutConf.addView(tpopupHeaderConf);

            TextView tpopupSpace1Conf = new TextView(this);
            tpopupSpace1Conf.setText("");
            tpopupSpace1Conf.setTextColor(Color.BLACK);
            tpopupSpace1Conf.setTextSize(6);
            tpopupSpace1Conf.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layoutConf.addView(tpopupSpace1Conf);



            final String[] unlock_options = new String[2];
            if (method1.contains("no lock")){
            	unlock_options[0]= "Location-sensitive";
            	unlock_options[1]= "No lock";
            }else{
            	unlock_options[0]= "Location-sensitive";
            	unlock_options[1]= method1;
            }

           // String unlock_options[] = {"Location-sensitive","PIN","Pattern","No Unlock"};

            //home
            RelativeLayout relativeLayoutpopup1Conf = new RelativeLayout (this);
            final AlertDialog dialogConf1;

            AlertDialog.Builder builderConf1 = new AlertDialog.Builder(this);
            builderConf1.setTitle("Select locking method for home:");

            builderConf1.setSingleChoiceItems(unlock_options, 0, null);
            builderConf1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                       int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                       System.out.println(unlock_options[selected]);

                       try{

               			File captureFileName = new File(getBaseContext().getFilesDir(),tm.getDeviceId()+"_context_selections.dat" );
                      	PrintWriter pw = new PrintWriter( new FileWriter( captureFileName, true ));
                      	Date date = new Date();
                    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    	pw.println("Date="+dateFormat.format(date));
						pw.println("home="+unlock_options[selected]);
						pw.close();

                		try{
        					  File fzip =new File(getBaseContext().getFilesDir()+"/"+tm.getDeviceId()+"_context_selections.dat");
        					  fzip.setReadable(true, false);
        					  fzip.setWritable(true, false);
        					  fzip.setExecutable(true, false);
        				}catch(Exception e){
        					  e.printStackTrace();
        				}


                       }catch(Exception e){}
                       // Do something useful withe the position of the selected radio button
                   }
               });

              dialogConf1=builderConf1.create();

              Button btnSelectConfDD1 = new Button(this);
              btnSelectConfDD1.setText("Select locking method for home");
              btnSelectConfDD1.setTextSize(10);
              btnSelectConfDD1.setId(1);
              btnSelectConfDD1.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                	  dialogConf1.show();
                  }
              });

            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutpopup1Conf.addView(btnSelectConfDD1, relativeLayoutParams);
            layoutConf.addView(relativeLayoutpopup1Conf);


            TextView tpopupSpace2Conf = new TextView(this);
            tpopupSpace2Conf.setText("");
            tpopupSpace2Conf.setTextColor(Color.BLACK);
            tpopupSpace2Conf.setTextSize(6);
            tpopupSpace2Conf.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layoutConf.addView(tpopupSpace2Conf);

            //work
            RelativeLayout relativeLayoutpopup2Conf = new RelativeLayout (this);

            final AlertDialog dialogConf2;

            AlertDialog.Builder builderConf2 = new AlertDialog.Builder(this);
            builderConf2.setTitle("Select locking method for Work:");

            builderConf2.setSingleChoiceItems(unlock_options, 0, null);
            builderConf2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                       int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                       System.out.println(unlock_options[selected]);

                       try{

               			File captureFileName = new File(getBaseContext().getFilesDir(),tm.getDeviceId()+"_context_selections.dat" );
                      	PrintWriter pw = new PrintWriter( new FileWriter( captureFileName, true ));
                    	pw.println("work="+unlock_options[selected]);
						pw.close();

                		try{
        					  File fzip =new File(getBaseContext().getFilesDir()+"/"+tm.getDeviceId()+"_context_selections.dat");
        					  fzip.setReadable(true, false);
        					  fzip.setWritable(true, false);
        					  fzip.setExecutable(true, false);
        				}catch(Exception e){
        					  e.printStackTrace();
        				}


                       }catch(Exception e){}
                       // Do something useful withe the position of the selected radio button
                   }
               });

              dialogConf2=builderConf2.create();

              Button btnSelectConfDD2 = new Button(this);
              btnSelectConfDD2.setText("Select locking method for work");
              btnSelectConfDD2.setTextSize(10);
              btnSelectConfDD2.setId(1);
              btnSelectConfDD2.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                	  dialogConf2.show();
                  }
              });

            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutpopup2Conf.addView(btnSelectConfDD2, relativeLayoutParams);
            layoutConf.addView(relativeLayoutpopup2Conf);


            TextView tpopupSpace3Conf = new TextView(this);
            tpopupSpace3Conf.setText("");
            tpopupSpace3Conf.setTextColor(Color.BLACK);
            tpopupSpace3Conf.setTextSize(6);
            tpopupSpace3Conf.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layoutConf.addView(tpopupSpace3Conf);


            //other places
            RelativeLayout relativeLayoutpopup3Conf = new RelativeLayout (this);

            final AlertDialog dialogConf3;

            AlertDialog.Builder builderConf3 = new AlertDialog.Builder(this);
            builderConf3.setTitle("Select locking method for other places:");

            builderConf3.setSingleChoiceItems(unlock_options, 0, null);
            builderConf3.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                       int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                       System.out.println(unlock_options[selected]);

                       try{

               			File captureFileName = new File(getBaseContext().getFilesDir(),tm.getDeviceId()+"_context_selections.dat" );
                      	PrintWriter pw = new PrintWriter( new FileWriter( captureFileName, true ));
                    	pw.println("other="+unlock_options[selected]);
						pw.close();

                		try{
        					  File fzip =new File(getBaseContext().getFilesDir()+"/"+tm.getDeviceId()+"_context_selections.dat");
        					  fzip.setReadable(true, false);
        					  fzip.setWritable(true, false);
        					  fzip.setExecutable(true, false);
        				}catch(Exception e){
        					  e.printStackTrace();
        				}


                       }catch(Exception e){}
                       // Do something useful withe the position of the selected radio button
                   }
               });

              dialogConf3=builderConf3.create();

              Button btnSelectConfDD3 = new Button(this);
              btnSelectConfDD3.setText("Select locking method for other places");
              btnSelectConfDD3.setTextSize(10);
              btnSelectConfDD3.setId(1);
              btnSelectConfDD3.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                	  dialogConf3.show();
                  }
              });

            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutpopup3Conf.addView(btnSelectConfDD3, relativeLayoutParams);
            layoutConf.addView(relativeLayoutpopup3Conf);


            TextView tpopupSpace4Conf = new TextView(this);
            tpopupSpace4Conf.setText("");
            tpopupSpace4Conf.setTextColor(Color.BLACK);
            tpopupSpace4Conf.setTextSize(6);
            tpopupSpace4Conf.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layoutConf.addView(tpopupSpace4Conf);

            //transitions
            RelativeLayout relativeLayoutpopup4Conf = new RelativeLayout (this);

            final AlertDialog dialogConf4;

            AlertDialog.Builder builderConf4 = new AlertDialog.Builder(this);
            builderConf4.setTitle("Select locking method for on the move:");

            builderConf4.setSingleChoiceItems(unlock_options, 0, null);
            builderConf4.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                       int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                       System.out.println(unlock_options[selected]);

                       try{

               			File captureFileName = new File(getBaseContext().getFilesDir(),tm.getDeviceId()+"_context_selections.dat" );
                      	PrintWriter pw = new PrintWriter( new FileWriter( captureFileName, true ));
						pw.println("transition="+unlock_options[selected]);
						pw.close();

						try{
        					  File fzip =new File(getBaseContext().getFilesDir()+"/"+tm.getDeviceId()+"_context_selections.dat");
        					  fzip.setReadable(true, false);
        					  fzip.setWritable(true, false);
        					  fzip.setExecutable(true, false);
        				}catch(Exception e){
        					  e.printStackTrace();
        				}

                       }catch(Exception e){}
                       // Do something useful withe the position of the selected radio button
                   }
               });

              dialogConf4=builderConf4.create();

              Button btnSelectConfDD4 = new Button(this);
              btnSelectConfDD4.setText("Select locking method for on the move");
              btnSelectConfDD4.setTextSize(10);
              btnSelectConfDD4.setId(1);
              btnSelectConfDD4.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                	  dialogConf4.show();
                  }
              });

            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutpopup4Conf.addView(btnSelectConfDD4, relativeLayoutParams);
            layoutConf.addView(relativeLayoutpopup4Conf);


            TextView tpopupSpace4bConf = new TextView(this);
            tpopupSpace4bConf.setText("");
            tpopupSpace4bConf.setTextColor(Color.BLACK);
            tpopupSpace4bConf.setTextSize(6);
            tpopupSpace4bConf.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layoutConf.addView(tpopupSpace4bConf);

            //transitions
            RelativeLayout relativeLayoutpopup4bConf = new RelativeLayout (this);

            final AlertDialog dialogConf4b;

            AlertDialog.Builder builderConf4b = new AlertDialog.Builder(this);
            builderConf4b.setTitle("Select locking method for new places:");

            builderConf4b.setSingleChoiceItems(unlock_options, 0, null);
            builderConf4b.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                       dialog.dismiss();
                       int selected = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                       System.out.println(unlock_options[selected]);

                       try{

               			File captureFileName = new File(getBaseContext().getFilesDir(),tm.getDeviceId()+"_context_selections.dat" );
                      	PrintWriter pw = new PrintWriter( new FileWriter( captureFileName, true ));
                    	pw.println("newplace="+unlock_options[selected]);
						pw.close();

                		try{
        					  File fzip =new File(getBaseContext().getFilesDir()+"/"+tm.getDeviceId()+"_context_selections.dat");
        					  fzip.setReadable(true, false);
        					  fzip.setWritable(true, false);
        					  fzip.setExecutable(true, false);
        				}catch(Exception e){
        					  e.printStackTrace();
        				}

                       }catch(Exception e){}
                       // Do something useful withe the position of the selected radio button
                   }
               });

              dialogConf4b=builderConf4b.create();

              Button btnSelectConfDD4b = new Button(this);
              btnSelectConfDD4b.setText("Select locking method for new places");
              btnSelectConfDD4b.setTextSize(10);
              btnSelectConfDD4b.setId(1);
              btnSelectConfDD4b.setOnClickListener(new OnClickListener() {
                  public void onClick(View v) {
                	  dialogConf4b.show();
                  }
              });

            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            relativeLayoutpopup4bConf.addView(btnSelectConfDD4b, relativeLayoutParams);
            layoutConf.addView(relativeLayoutpopup4bConf);


            TextView tpopupSpace5Conf = new TextView(this);
            tpopupSpace5Conf.setText("");
            tpopupSpace5Conf.setTextColor(Color.BLACK);
            tpopupSpace5Conf.setTextSize(6);
            tpopupSpace5Conf.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            layoutConf.addView(tpopupSpace5Conf);


            RelativeLayout relativeLayoutpopup5Conf = new RelativeLayout (this);
            Button btnPopup2Conf = new Button(this);
            btnPopup2Conf.setText("Exit");
            btnPopup2Conf.setTextSize(10);
            btnPopup2Conf.setId(2);
            btnPopup2Conf.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                	popUpConf.dismiss();
                }
            });
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutpopup5Conf.addView(btnPopup2Conf, relativeLayoutParams);
            layoutConf.addView(relativeLayoutpopup5Conf);


            LayoutParams paramsConf = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            layoutConf.setOrientation(LinearLayout.VERTICAL);
            layoutConf.setBackgroundResource(android.R.color.white);
            popUpConf.setContentView(layoutConf);

            /*
             *
             *
             *
             * */




            /*TextView tViewSpace4 = new TextView(this);
            tViewSpace4.setText("");
            tViewSpace4.setTextColor(Color.BLACK);
            tViewSpace4.setTextSize(4);
            tViewSpace4.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            lLayout.addView(tViewSpace4);*/

            RelativeLayout relativeLayout2 = new RelativeLayout (this);
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.setMargins(5, 0, 0, 0);
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
            relativeLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            TextView tViewFurther2 = new TextView(this);
            tViewFurther2.setText("Device Protection: ");
            tViewFurther2.setTextSize(12);
            tViewFurther2.setTextColor(Color.BLACK);
            tViewFurther2.setId(1);
            relativeLayout2.addView(tViewFurther2, relativeLayoutParams);

            tViewFurtherDevice = new TextView(this);
            tViewFurtherDevice.setText(curritem.getDescription());
            tViewFurtherDevice.setTextSize(10);
            tViewFurtherDevice.setTextColor(Color.BLACK);
            tViewFurtherDevice.setTypeface(null, Typeface.BOLD);
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams.addRule(RelativeLayout.RIGHT_OF,
            		tViewFurther2.getId());
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_TOP,
            		tViewFurther2.getId()); // added top alignment rule
            relativeLayout2.addView(tViewFurtherDevice, relativeLayoutParams);
            lLayout.addView(relativeLayout2);


            TextView tViewSpace5 = new TextView(this);
            tViewSpace5.setText("");
            tViewSpace5.setTextColor(Color.BLACK);
            tViewSpace5.setTextSize(4);
            tViewSpace5.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
            lLayout.addView(tViewSpace5);

            TextView tViewFurther3 = new TextView(this);
            tViewFurther3.setText("Task Description:");
            tViewFurther3.setTextSize(12);
            tViewFurther3.setTextColor(Color.BLACK);
            LinearLayout.LayoutParams tViewFurther3lps  = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            tViewFurther3lps.setMargins(5, 0, 0, 0);
            tViewFurther3.setLayoutParams(tViewFurther3lps);
            lLayout.addView(tViewFurther3);

            tViewFurtherDescription = new TextView(this);
            tViewFurtherDescription.setText(curritem.getDetailed());
            tViewFurtherDescription.setTextSize(12);
            tViewFurtherDescription.setTextColor(Color.BLACK);
            tViewFurtherDescription.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams tViewFurtherDescriptionlps  = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            tViewFurtherDescriptionlps.setMargins(5, 0, 0, 0);
            tViewFurtherDescription.setLayoutParams(tViewFurtherDescriptionlps);
            lLayout.addView(tViewFurtherDescription);





		}catch(Exception e){e.printStackTrace();}

		setContentView(lLayout);
	}

	protected void displayScreen(){
		try{
        	String dir = getBaseContext().getFilesDir().getPath();
        	while(dir.length() > 1){

        		File tempdir = new File(dir);
        		tempdir.setExecutable(true,false);
        		tempdir.setReadable(true, false);
        		tempdir.setWritable(true, false);

        		if (dir.lastIndexOf("/") == 0){
        			dir = "/";
        		}else{
        			dir = dir.substring(0, dir.lastIndexOf("/"));
        		}
        	}
        }catch(Exception e){}


		String filedir=getBaseContext().getFilesDir().getPath()+"/";

        try{
        	File tempfz = new File(filedir+"savedPass.txt");

        	if (tempfz.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
        		String line;

        		while ((line = br.readLine()) != null) {
        			try{
        				String [] tempbuff = line.split("=");
        				if (tempbuff[0].equals("pass")){
        					savedPattern = tempbuff[1].toCharArray();
        				}
        				if (tempbuff[0].equals("method1")){
        					method1=tempbuff[1].toString();
        				}
        				if (tempbuff[0].equals("method2")){
        					method2=tempbuff[1].toString();
        				}
        			}catch(Exception e){}
        		}
        		br.close();

        		//ToDo remove coment
        		// design a completely new screen
        		buildDefaultScreen();
        		
        		

        	}else{
        		LinearLayout lLayout = new LinearLayout(this);
                lLayout.setOrientation(LinearLayout.VERTICAL);
                lLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
                TextView tView = new TextView(this);
                tView.setText("Select your device protection scheme:");
                tView.setTextColor(Color.BLACK);
                tView.setTextSize(18);
                tView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
                lLayout.addView(tView);

                TextView tViewSpace = new TextView(this);
                tViewSpace.setText("");
                tViewSpace.setTextColor(Color.BLACK);
                tViewSpace.setTextSize(20);
                tViewSpace.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
                lLayout.addView(tViewSpace);

                Button btnTag1 = new Button(this);
                btnTag1.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                btnTag1.setText("No lock");
                btnTag1.setId(1);
                btnTag1.setOnClickListener(new OnClickListener() {

		               public void onClick(View v)
		               {
		            	   //show another page
		            	   method1="no lock";
		            	   LinearLayout lLayout = new LinearLayout(getBaseContext());
		                   lLayout.setOrientation(LinearLayout.VERTICAL);
		                   lLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
		                   TextView tView = new TextView(getBaseContext());
		                   tView.setText("Despite selecting the no unlock option you still need to select one of the following device protection schemes:");
		                   tView.setTextColor(Color.BLACK);
		                   tView.setTextSize(16);
		                   tView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		                   lLayout.addView(tView);

		                   TextView tViewSpace = new TextView(getBaseContext());
		                   tViewSpace.setText("");
		                   tViewSpace.setTextColor(Color.BLACK);
		                   tViewSpace.setTextSize(20);
		                   tViewSpace.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		                   lLayout.addView(tViewSpace);


		                   Button btnTag2 = new Button(getBaseContext());
		                   btnTag2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		                   btnTag2.setText("PIN Code");
		                   btnTag2.setId(2);
		                   btnTag2.setOnClickListener(new OnClickListener() {

		   		               public void onClick(View v)
		   		               {
				            	   method2="PIN";
		   		            	   //port the PIN code from external library
				            	   Intent intent = new Intent(getBaseContext(), PasscodePreferencesActivity.class);
						           startActivityForResult(intent, PasscodePreferencesActivity.ENABLE_PASSLOCK);

		   		               }
		                   });
		                   lLayout.addView(btnTag2);

		                   Button btnTag3 = new Button(getBaseContext());
		                   btnTag3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		                   btnTag3.setText("Pattern");
		                   btnTag3.setId(1);
		                   btnTag3.setOnClickListener(new OnClickListener() {

		   		               public void onClick(View v)
		   		               {
		   		            	   method2="pattern";
		   		            	   Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null,
		   		           		   getBaseContext(), LockPatternActivity.class);
		   		                   Settings.Security.setAutoSavePattern(getBaseContext(), true);
				                   Settings.Display.setMaxRetries(getBaseContext(), 1);
		   		                   startActivityForResult(intent, REQ_CREATE_PATTERN);

		   		               }
		   			    	});
		                   lLayout.addView(btnTag3);

		                   tViewSpace = new TextView(getBaseContext());
		                   tViewSpace.setText("");
		                   tViewSpace.setTextColor(Color.BLACK);
		                   tViewSpace.setTextSize(20);
		                   tViewSpace.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		                   lLayout.addView(tViewSpace);

		                   TextView tViewText = new TextView(getBaseContext());
		                   tViewText.setText("This will be the device protection scheme that will be prompted to you during the second and third phase of the study, when the system senses a significant change in the phone's surrounding environment. " +
		                   		"Remember to take note of your selection so that you do not forget it.");
		                   tViewText.setTextColor(Color.RED);
		                   tViewText.setTextSize(14);
		                   tViewText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		                   lLayout.addView(tViewText);

		                   setContentView(lLayout);
		               }
			    });
                lLayout.addView(btnTag1);

                Button btnTag2 = new Button(this);
                btnTag2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                btnTag2.setText("PIN Code");
                btnTag2.setId(2);
                btnTag2.setOnClickListener(new OnClickListener() {

		               public void onClick(View v)
		               {
		            	   method1="PIN";
		            	   method2="PIN";
		            	   //port the PIN code from external library

		            	   Intent intent = new Intent(getBaseContext(), PasscodePreferencesActivity.class);
				           startActivityForResult(intent, PasscodePreferencesActivity.ENABLE_PASSLOCK);

		               }
			    });
                lLayout.addView(btnTag2);

                Button btnTag3 = new Button(this);
                btnTag3.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                btnTag3.setText("Pattern");
                btnTag3.setId(1);
                btnTag3.setOnClickListener(new OnClickListener() {

		               public void onClick(View v)
		               {
		            	   method1="pattern";
		            	   method2="pattern";
		            	   Intent intent = new Intent(LockPatternActivity.ACTION_CREATE_PATTERN, null,
		           		   getBaseContext(), LockPatternActivity.class);
		                   Settings.Security.setAutoSavePattern(getBaseContext(), true);
		                   Settings.Display.setMaxRetries(getBaseContext(), 1);
		                   startActivityForResult(intent, REQ_CREATE_PATTERN);
		               }
			    });
                lLayout.addView(btnTag3);

                setContentView(lLayout);


        		/**/

        	}
        }catch(Exception e){System.out.println("Out from main exception: "+e.getMessage());e.printStackTrace();}
	}



	protected void onListItemClick(AdapterView<?> parent, View v, int position, long id) {

    	TaskListItem curritem = (TaskListItem)listAdapter.getItem( position );


    	tViewFurtherDate.setText(curritem.getDay());
    	tViewFurtherDevice.setText(curritem.getDescription());
    	tViewFurtherDescription.setText(curritem.getDetailed());

    	final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

    	Date date = new Date();
    	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
    	String tempbuff[] = curritem.getDay().split(" ");
    	if (tm.getDeviceId().equals("357800052537794")){
    		btnTag.setVisibility(View.VISIBLE);
    	}else{
    	
    	if (tempbuff[2].contains(dateFormat.format(date))){

    		if (curritem.getDay().contains("Day 04") && !homeandworkselected){
    			btnTag.setVisibility(View.VISIBLE);
    		}else{
    			btnTag.setVisibility(View.INVISIBLE);
    		}

    	}else{
    		// to change to 4
    		if ((Integer.parseInt(tempbuff[1]) >= 4)) {
				//btnTag.setVisibility(View.VISIBLE);

    			if (!homeandworkselected){
    				btnTag.setVisibility(View.VISIBLE);
    			}else{
        			btnTag.setVisibility(View.INVISIBLE);
        		}
    		}else{
    			btnTag.setVisibility(View.INVISIBLE);
    		}
    	}
    	}

    	String filedir=getBaseContext().getFilesDir().getPath()+"/";
    	
	    String currentday=getCurrentDay(filedir,tm.getDeviceId());

	    if (tm.getDeviceId().equals("353771052780318")){
			if ((currentday.contains("Day 08") || currentday.contains("Day 09")) && !configuredcontexts){
    			btnConfigure.setVisibility(View.VISIBLE);
			}else if (currentday.contains("Day 11") && !configuredcontexts2){
    			btnConfigure.setVisibility(View.VISIBLE);
			}else{
				btnConfigure.setVisibility(View.INVISIBLE);
			}
		}else{
			// improve this to appear only on current date
			if ((currentday.contains("Day 15") || currentday.contains("Day 14") || currentday.contains("Day 16") || currentday.contains("Day 17") || currentday.contains("Day 18")) && !configuredcontexts){
    			btnConfigure.setVisibility(View.VISIBLE);
			}else if (currentday.contains("Day 18") && !configuredcontexts2){
    			btnConfigure.setVisibility(View.VISIBLE);
			}else{
				btnConfigure.setVisibility(View.INVISIBLE);
			}
		}
	}


	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String filedir=getBaseContext().getFilesDir().getPath()+"/";

		switch (requestCode) {
			case PasscodePreferencesActivity.ENABLE_PASSLOCK:{
		           try{

	    				BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"savedPass.txt"));
	    				outdat.write("method1="+method1);
	    				outdat.newLine();
	    				outdat.write("method2="+method2);
	    				outdat.newLine();
	    				outdat.write("pass=");
	    				outdat.newLine();
	    				outdat.close();

	    				File tempdir = new File(filedir+"savedPass.txt");
	            		tempdir.setExecutable(true,false);
	            		tempdir.setReadable(true, false);
	            		tempdir.setWritable(true, false);

	            		buildDefaultScreen();

	    			}catch(Exception e){}

			}
	    	case REQ_CREATE_PATTERN: {
	    		if (resultCode == RESULT_OK) {
	    			char[] pattern = data.getCharArrayExtra(LockPatternActivity.EXTRA_PATTERN);
	    			System.out.println("result android pattern: "+pattern);
	    			try{
	    				BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"savedPass.txt"));
	    				outdat.write("method1="+method1);
	    				outdat.newLine();
	    				outdat.write("method2="+method2);
	    				outdat.newLine();
	    				outdat.write("pass="+pattern.toString());
	    				outdat.newLine();
	    				outdat.close();

	    				File tempdir = new File(filedir+"savedPass.txt");
	            		tempdir.setExecutable(true,false);
	            		tempdir.setReadable(true, false);
	            		tempdir.setWritable(true, false);

	            		buildDefaultScreen();

	    			}catch(Exception e){}
	    		}
	    		break;
	    	}// REQ_CREATE_PATTERN
	    }
	}

	private void startSamplingService() {
		//stopSamplingService(position);
		(new Thread(new Runnable() {
        	public void run() {
        		Intent i = new Intent();
        		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
        		startService( i );
        	}
        })).start();


		samplingServiceRunning = true;
		/*for (int j = 0; j < listAdapter.getCount(); j ++){
			SensorItem item = (SensorItem)listAdapter.getItem( j );
			sensorsList.put(item, String.valueOf(item.getCheckboxState()));
			if (item.getCheckboxState()){
				item.setSampling( true );
			}
		}
		listAdapter.notifyDataSetChanged();
		UpdateWidget(this);*/

		System.out.println("sampling rate is "+samplingServiceRate+" minutes");
		Intent iHeartBeatService = new Intent(this, HeartBeat.class);
		PendingIntent piHeartBeatService = PendingIntent.getService(this, 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piHeartBeatService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 300000, piHeartBeatService);

	    Intent iBatteryLevelService = new Intent(this, BatteryLevel.class);
		PendingIntent piBatteryLevelService = PendingIntent.getService(this, 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piBatteryLevelService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piBatteryLevelService);


	    Intent iWifiService = new Intent(this, Wifi.class);
	    PendingIntent piWifiService = PendingIntent.getService(this, 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piWifiService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piWifiService);

	    Intent iRunningApplicationsService = new Intent(this, RunningApplications.class);
		PendingIntent piRunningApplicationsService = PendingIntent.getService(this, 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piRunningApplicationsService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRunningApplicationsService);

	    Intent iMagneticFieldService = new Intent(this, MagneticField.class);
	    PendingIntent piMagneticFieldService = PendingIntent.getService(this, 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piMagneticFieldService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piMagneticFieldService);

	    Intent iLightService = new Intent(this, Lightv3.class);
	    PendingIntent piLightService = PendingIntent.getService(this, 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piLightService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piLightService);

	    Intent iAccelerometerService = new Intent(this,Accelerometer.class);
	    PendingIntent piAccelerometerService = PendingIntent.getService(this, 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piAccelerometerService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piAccelerometerService);

	    Intent iRotationService = new Intent(this,Rotation.class);
	    PendingIntent piRotationService = PendingIntent.getService(this, 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piRotationService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piRotationService);

	    /*Intent iGPSService = new Intent(this, GPS.class);
	    PendingIntent piGPSService = PendingIntent.getService(this, 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piGPSService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piGPSService);*/


	    Intent iNoiseService = new Intent(this, Sound.class);
	    PendingIntent piNoiseService = PendingIntent.getService(this, 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piNoiseService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piNoiseService);

	    /*
	    // 2am
	    // between mi and 3 run schedule to generate buckets and full view (do this for days 5,6,7 & 8)
	    Calendar twoamCalendar = Calendar.getInstance();
	    twoamCalendar.set(Calendar.HOUR_OF_DAY, 0);
	    twoamCalendar.set(Calendar.MINUTE, 0);
	    twoamCalendar.set(Calendar.SECOND, 0);
	    Intent iDailyBGService = new Intent(this, DailyBucketGenerator.class);
	    PendingIntent piDailyBGService = PendingIntent.getService(this, 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piDailyBGService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, twoamCalendar.getTimeInMillis() , 21600000, piDailyBGService);


	    // 3-5am
	    // this should only work between 3 and 5 am only (on days 5,6,7 & 8)
	    Calendar threeamCalendara = Calendar.getInstance();
	    threeamCalendara.set(Calendar.HOUR_OF_DAY, 2);
	    threeamCalendara.set(Calendar.MINUTE, 0);
	    threeamCalendara.set(Calendar.SECOND, 0);
	    Intent iDailyAnalysisService = new Intent(this, DailyAnalysis.class);
	    PendingIntent piDailyAnalysisService = PendingIntent.getService(this, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piDailyAnalysisService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendara.getTimeInMillis() , 21600000, piDailyAnalysisService);

	    // 3-7am
	    // this should run every 20 minutes until 7am (on day 8)
	    Calendar threeamCalendarb = Calendar.getInstance();
	    threeamCalendarb.set(Calendar.HOUR_OF_DAY, 4);
	    threeamCalendarb.set(Calendar.MINUTE, 0);
	    threeamCalendarb.set(Calendar.SECOND, 0);
	    Intent iCreateArffFilesService = new Intent(this, CreateArffFiles.class);
	    PendingIntent piCreateArffFilesService = PendingIntent.getService(this, 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piCreateArffFilesService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, threeamCalendarb.getTimeInMillis() , 20*60000, piCreateArffFilesService);
*/
	    //set to run from day 8th onwards
	    Intent iGatherLRService = new Intent(this, GatherLatestReadings.class);
	    PendingIntent piGatherLRService = PendingIntent.getService(this, 0, iGatherLRService, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piGatherLRService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), samplingServiceRate*60000, piGatherLRService);


	    // 5am
	    Calendar fiveamCalendar = Calendar.getInstance();
	    fiveamCalendar.set(Calendar.HOUR_OF_DAY, 6);
	    fiveamCalendar.set(Calendar.MINUTE, 0);
	    fiveamCalendar.set(Calendar.SECOND, 0);
	    Intent iFilesUploaderService = new Intent(this, FilesUploader.class);
		PendingIntent piFilesUploaderService = PendingIntent.getService(this, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(piFilesUploaderService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,fiveamCalendar.getTimeInMillis() , 43200000, piFilesUploaderService);


	    // set reminders to 8pm
	    Calendar eightpmCalendar = Calendar.getInstance();
	    eightpmCalendar.set(Calendar.HOUR_OF_DAY, 20);
	    eightpmCalendar.set(Calendar.MINUTE, 00);
	    eightpmCalendar.set(Calendar.SECOND, 0);
	    alarmManager = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
	    Intent intent = new Intent(this, ReminderService.class);
	    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager.cancel(pendingIntent);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,eightpmCalendar.getTimeInMillis() , 86400000, pendingIntent);



	    Calendar sevenamCalendar = Calendar.getInstance();
	    sevenamCalendar.set(Calendar.HOUR_OF_DAY, 7);
	    sevenamCalendar.set(Calendar.MINUTE, 00);
	    sevenamCalendar.set(Calendar.SECOND, 0);
	    alarmManager = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
	    Intent iStopStudyService = new Intent(this, StopStudy.class);
	    PendingIntent piStopStudyService = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager.cancel(piStopStudyService);
	    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,sevenamCalendar.getTimeInMillis() , 86400000, piStopStudyService);

	}

	private void stopSamplingService() {
		if( samplingServiceRunning ) {
        	Intent i = new Intent();
    		i.setClassName( "com.gcu.ambientunlocker","com.gcu.ambientunlocker.SamplingService" );
    	    stopService( i );

        	/*for (int j = 0; j < listAdapter.getCount(); j ++){
    			SensorItem item = (SensorItem)listAdapter.getItem( j );
    			item.setSampling( false );
    		}*/

        	samplingServiceRunning = false;

        	//listAdapter.notifyDataSetChanged();
			//UpdateWidget(this);

        	Intent iHeartBeatService = new Intent(this, HeartBeat.class);
			PendingIntent piHeartBeatService = PendingIntent.getService(this, 0, iHeartBeatService, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piHeartBeatService);

		    Intent iWifiService = new Intent(this, Wifi.class);
			PendingIntent piWifiService = PendingIntent.getService(this, 0, iWifiService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piWifiService);

		    Intent iNoiseService = new Intent(this, Sound.class);
			PendingIntent piNoiseService = PendingIntent.getService(this, 0, iNoiseService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piNoiseService);

		    /*Intent iGPSService = new Intent(this, GPS.class);
			PendingIntent piGPSService = PendingIntent.getService(this, 0, iGPSService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piGPSService);*/

		    Intent iBatteryLevelService = new Intent(this, BatteryLevel.class);
			PendingIntent piBatteryLevelService = PendingIntent.getService(this, 0, iBatteryLevelService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piBatteryLevelService);

		    Intent iRunningApplicationsService = new Intent(this, RunningApplications.class);
			PendingIntent piRunningApplicationsService = PendingIntent.getService(this, 0, iRunningApplicationsService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piRunningApplicationsService);

		    Intent iMagneticFieldService = new Intent(this, MagneticField.class);
			PendingIntent piMagneticFieldService = PendingIntent.getService(this, 0, iMagneticFieldService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piMagneticFieldService);

		    Intent iLightService = new Intent(this, Lightv3.class);
			PendingIntent piLightService = PendingIntent.getService(this, 0, iLightService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piLightService);

		    Intent iAccelerometerService = new Intent(this, Accelerometer.class);
			PendingIntent piAccelerometerService = PendingIntent.getService(this, 0, iAccelerometerService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piAccelerometerService);

		    Intent iRotationService = new Intent(this, Rotation.class);
			PendingIntent piRotationService = PendingIntent.getService(this, 0, iRotationService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piRotationService);

		    Intent iGatherLRService = new Intent(this, GatherLatestReadings.class);
			PendingIntent piGatherLRService = PendingIntent.getService(this, 0, iGatherLRService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piGatherLRService);

		    /*Intent iDailyBGService = new Intent(this, DailyBucketGenerator.class);
			PendingIntent piDailyBGService = PendingIntent.getService(this, 0, iDailyBGService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piDailyBGService);

		    Intent iDailyAnalysisService = new Intent(this, DailyAnalysis.class);
			PendingIntent piDailyAnalysisService = PendingIntent.getService(this, 0, iDailyAnalysisService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piDailyAnalysisService);


		    Intent iCreateArffFilesService = new Intent(this, CreateArffFiles.class);
		    PendingIntent piCreateArffFilesService = PendingIntent.getService(this, 0, iCreateArffFilesService, PendingIntent.FLAG_UPDATE_CURRENT);
		    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piCreateArffFilesService);*/


		    Intent iFilesUploaderService = new Intent(this, FilesUploader.class);
			PendingIntent piFilesUploaderService = PendingIntent.getService(this, 0, iFilesUploaderService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piFilesUploaderService);

		    Intent iReminderService = new Intent(this, ReminderService.class);
			PendingIntent piReminderService = PendingIntent.getService(this, 0, iReminderService, PendingIntent.FLAG_UPDATE_CURRENT);
			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		    alarmManager.cancel(piReminderService);

		}
	}

	protected void onPause(){
	     super.onPause();
	     //click = true;
	     /*SharedPreferences appPrefs = getSharedPreferences(PREF_FILE2,MODE_WORLD_WRITEABLE );
	     SharedPreferences.Editor ed = appPrefs.edit();
	     ed.putBoolean( SAMPLING_SERVICE_POSITION_KEY, samplingServiceRunning );
	     ed.commit();*/
	     try{
	    	 String filedir=getBaseContext().getFilesDir().getPath()+"/";
	    	 BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));
	    	 outdat.write("samplingServiceRunning,"+samplingServiceRunning);
	    	 outdat.newLine();
	    	 outdat.write("samplingServiceRate,"+samplingServiceRate);
	    	 outdat.newLine();
	    	 /*for (int j = 0; j < listAdapter.getCount(); j ++){
		 		SensorItem item = (SensorItem)listAdapter.getItem( j );
		 		outdat.write(item.getSensorName()+","+item.getCheckboxState());
	 		    outdat.newLine();
	    	 }*/
	    	 outdat.close();
	     }catch(Exception e){}

	     //UpdateWidget(this);
	}

	protected void onResume() {
		super.onResume();
		//click = true;
		/*SharedPreferences appPrefs = getSharedPreferences(PREF_FILE2,MODE_WORLD_WRITEABLE );
		samplingServiceRunning = appPrefs.getBoolean( SAMPLING_SERVICE_POSITION_KEY, false );*/

		try{

			/*SensorManager sensorManager =
	                (SensorManager)getSystemService( SENSOR_SERVICE  );
	        ArrayList<SensorItem> items = new ArrayList<SensorItem>();
	        List<Sensor> sensors = sensorManager.getSensorList( Sensor.TYPE_ALL );*/


			String filedir=getBaseContext().getFilesDir().getPath()+"/";

        	File tempfz = new File(filedir+"settings.txt");

        	if (tempfz.exists()){
        		BufferedReader br = new BufferedReader(new FileReader(tempfz));
        		String line;

        		while ((line = br.readLine()) != null) {

        			String [] tempbuff = line.split(",");
        			if (tempbuff[0].equals("samplingServiceRunning")){
        				samplingServiceRunning = Boolean.parseBoolean(tempbuff[1]);
        			}else if (tempbuff[0].equals("samplingServiceRate")){
        				samplingServiceRate = Integer.parseInt(tempbuff[1]);
        				System.out.println("onresume:"+samplingServiceRate);
        			}/*else{
        				sensorsList.put(tempbuff[0], tempbuff[1]);
        			}*/

        		}
        		br.close();
        	}else{
        		BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));
				outdat.write("samplingServiceRunning,false");
				outdat.newLine();
				outdat.write("samplingServiceRate,1");
				outdat.newLine();

				/*for( int i = 0 ; i < sensors.size() ; ++i ){
		        	SensorItem item = new SensorItem( sensors.get( i ).getName() );
		        	if (item.getSensorName().toLowerCase().contains("rgb") || item.getSensorName().toLowerCase().contains("light") || item.getSensorName().toLowerCase().contains("accelerometer")|| item.getSensorName().toLowerCase().contains("rotation") || item.getSensorName().toLowerCase().contains("orientation") || item.getSensorName().toLowerCase().contains("magnetic") || (item.getSensorName().toLowerCase().contains("acceleration") && !item.getSensorName().toLowerCase().contains("linear"))){
		        		sensorsList.put(item.getSensorName(), "true");
		        		outdat.write(item.getSensorName()+",true");
						outdat.newLine();
		        	}
		        }
				sensorsList.put("Wi-Fi sensor", ",true");
				outdat.write("Wi-Fi sensor"+",true");
				outdat.newLine();
				sensorsList.put("Microphone sensor", ",true");
				outdat.write("Microphone sensor"+",true");
				outdat.newLine();
				sensorsList.put("GPS sensor", ",true");
				outdat.write("GPS sensor"+",true");
				outdat.newLine();*/
				outdat.close();
        	}


        	if (isServiceRunning()){
    			samplingServiceRunning = true;
    		}else{
    			samplingServiceRunning = false;
    			startSamplingService();
    		}

    		/*for( int i = 0 ; i < sensors.size() ; ++i ){
            	SensorItem item = new SensorItem( sensors.get( i ) );
            	if (item.getSensorName().toLowerCase().contains("light") || item.getSensorName().toLowerCase().contains("accelerometer")|| item.getSensorName().toLowerCase().contains("rotation") || item.getSensorName().toLowerCase().contains("orientation") || item.getSensorName().toLowerCase().contains("magnetic") || (item.getSensorName().toLowerCase().contains("acceleration") && !item.getSensorName().toLowerCase().contains("linear"))){
            		if (sensorsList.containsKey(item.getSensorName())){
            			String state = sensorsList.get(item.getSensorName()).toString();
            			item.setCheckboxState(Boolean.parseBoolean(state));
            		}else{
            			item.setCheckboxState(false);
            		}
            		items.add( item );
            		if( samplingServiceRunning ) {
                		item.setSampling( true );
            		}
            	}
            }

    		ListView lv= (ListView) findViewById (R.id.list);
            listAdapter = new SensorListAdapter( lv.getContext(), items);
            lv.setAdapter(  listAdapter );*/

        }catch(Exception e){}

		displayScreen();

		//UpdateWidget(this);
	}

	protected void onDestroy() {
		super.onDestroy();

		/*SharedPreferences appPrefs = getSharedPreferences(PREF_FILE2,MODE_WORLD_WRITEABLE );
	    SharedPreferences.Editor ed = appPrefs.edit();
	    ed.putBoolean( SAMPLING_SERVICE_POSITION_KEY, samplingServiceRunning  );
	    ed.commit();*/

		try{
	    	 String filedir=getBaseContext().getFilesDir().getPath()+"/";
	    	 BufferedWriter outdat = new BufferedWriter(new FileWriter(filedir+"settings.txt"));
	    	 outdat.write("samplingServiceRunning,"+samplingServiceRunning);
	    	 outdat.newLine();
	    	 outdat.write("samplingServiceRate,"+samplingServiceRate);
	    	 outdat.newLine();
	    	 /*for (int j = 0; j < listAdapter.getCount(); j ++){
	 			SensorItem item = (SensorItem)listAdapter.getItem( j );
	 			outdat.write(item.getSensorName()+","+item.getCheckboxState());
 		    	outdat.newLine();
	 		 }*/
	    	 outdat.close();
	     }catch(Exception e){}

	    //UpdateWidget(this);

	}




}

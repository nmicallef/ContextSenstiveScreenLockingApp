package com.gcu.ambientunlocker;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class ReminderService extends IntentService {
    private static final int NOTIF_ID = 1;

    public ReminderService(){
        super("ReminderService");
    }

    @Override
      protected void onHandleIntent(Intent intent) {
    	System.out.println("Notification!!!");
    	Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        System.out.println("Today's date is "+dateFormat.format(cal.getTime()));

		String dir = getBaseContext().getFilesDir().getPath()+"/";
		final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
		
		try{
			
			File thefile =new File(dir+tm.getDeviceId()+"_reminderslist.txt");
			if (thefile.exists()  ){	
				BufferedReader br = new BufferedReader(new FileReader(thefile));
				String line;
				while ((line = br.readLine()) != null) {
					String [] tempbuff = line.split(";");
					if (tempbuff[1].contains(dateFormat.format(cal.getTime()))){
						NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(NOTIFICATION_SERVICE);
						Notification updateComplete = new Notification();
						updateComplete.icon = android.R.drawable.sym_def_app_icon;
						updateComplete.tickerText = tempbuff[4].toString();
						updateComplete.when = System.currentTimeMillis();
						updateComplete.flags = Notification.FLAG_ONLY_ALERT_ONCE|Notification.FLAG_AUTO_CANCEL;
						//updateComplete.flags =Notification.DEFAULT_ALL|Notification.FLAG_ONLY_ALERT_ONCE;
								
						Intent notificationIntent = new Intent(getBaseContext(),MainActivity.class);
						PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0,notificationIntent, 0);
						updateComplete.setLatestEventInfo(getBaseContext(), tempbuff[2].toString(),tempbuff[4].toString(), contentIntent);
						//updateComplete.vibrate = new long[]{100, 500};
						
						notificationManager.notify(0, updateComplete);
					}
				}
				br.close();
			}
		}catch(Exception e){}
    }

}

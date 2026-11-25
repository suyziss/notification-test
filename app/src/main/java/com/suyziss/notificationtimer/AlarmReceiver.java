package com.suyziss.notificationtimer;

import android.app.*;
import android.content.*;
import android.os.Build;
import org.json.*;
import java.util.*;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID="timer_alerts";
    @Override public void onReceive(Context context,Intent intent){
        int id=intent.getIntExtra("id",1001);String title=intent.getStringExtra("title");if(title==null||title.trim().isEmpty())title="Напоминание";
        NotificationManager manager=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=26&&manager.getNotificationChannel(CHANNEL_ID)==null){NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Напоминания",NotificationManager.IMPORTANCE_HIGH);channel.setDescription("Уведомления таймеров Suyziss");manager.createNotificationChannel(channel);}
        Intent open=new Intent(context,MainActivity.class);PendingIntent content=PendingIntent.getActivity(context,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,CHANNEL_ID):new Notification.Builder(context);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(title).setContentText("Время вышло").setAutoCancel(true).setContentIntent(content).setCategory(Notification.CATEGORY_ALARM);
        if(Build.VERSION.SDK_INT<26)b.setPriority(Notification.PRIORITY_HIGH);
        try{manager.notify(id,b.build());}catch(SecurityException ignored){}
        try{SharedPreferences p=context.getSharedPreferences("notification_history",Context.MODE_PRIVATE);JSONArray a=new JSONArray(p.getString("items","[]"));JSONObject o=new JSONObject();o.put("id",id);o.put("title",title);o.put("at",System.currentTimeMillis());a.put(o);while(a.length()>100){JSONArray n=new JSONArray();for(int i=1;i<a.length();i++)n.put(a.get(i));a=n;}p.edit().putString("items",a.toString()).apply();}catch(Exception ignored){}
        try{SharedPreferences p=context.getSharedPreferences("MainActivity",Context.MODE_PRIVATE);JSONArray old=new JSONArray(p.getString("active","[]"));JSONArray next=new JSONArray();for(int i=0;i<old.length();i++){JSONObject item=old.optJSONObject(i);if(item!=null&&item.optInt("id")!=id)next.put(item);}p.edit().putString("active",next.toString()).apply();}catch(Exception ignored){}
    }
}

package com.suyziss.notificationtimer;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final String PREF="suyziss_timer_store";
    private static final String ACTIVE="active";
    private static final int REQ_NOTIFICATIONS=42;
    private LinearLayout root, content, activeList;
    private EditText titleInput,hours,minutes,seconds;
    private TextView status, countLabel;
    private AlarmManager alarms;
    private int page=0;
    private int accent=Color.rgb(151,126,255);
    private JSONArray active=new JSONArray(), history=new JSONArray();

    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(10,11,17)); getWindow().setNavigationBarColor(Color.rgb(10,11,17)); alarms=(AlarmManager)getSystemService(ALARM_SERVICE); load(); buildShell(); showPage(0); askPermissions();}
    int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
    void load(){try{active=new JSONArray(getPreferences(0).getString(ACTIVE,"[]"));history=new JSONArray(getSharedPreferences("notification_history",0).getString("items","[]"));}catch(Exception e){active=new JSONArray();history=new JSONArray();}}
    void save(){getPreferences(0).edit().putString(ACTIVE,active.toString()).apply();}
    TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
    LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    TextView heading(String s){TextView t=text(s,23,Color.WHITE);t.setTypeface(null,Typeface.BOLD);t.setPadding(0,dp(8),0,dp(12));return t;}
    TextView small(String s){return text(s,12,Color.rgb(155,158,175));}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setTextSize(14);b.setAllCaps(false);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));return b;}
    void buildShell(){
        root=column();root.setBackgroundColor(Color.rgb(10,11,17));root.setPadding(dp(20),dp(18),dp(20),dp(12));
        LinearLayout brand=new LinearLayout(this);brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo=text("S",22,Color.WHITE);logo.setGravity(Gravity.CENTER);logo.setTypeface(null,Typeface.BOLD);logo.setBackground(bg(accent,13));brand.addView(logo,lp(42,42));
        LinearLayout names=column();names.setPadding(dp(12),0,0,0);TextView n=text("NOTIFICATION",13,Color.WHITE);n.setTypeface(null,Typeface.BOLD);TextView sub=small("TIMER  /  SUYZISS LAB");names.addView(n);names.addView(sub);brand.addView(names,new LinearLayout.LayoutParams(0,-2,1));
        TextView ver=small("v2.0");brand.addView(ver);root.addView(brand,lp(-1,54));
        content=column();ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(0,dp(8),0,0);
        String[] labels={"Таймер","Шаблоны","История","Настройки"};for(int i=0;i<4;i++){final int p=i;TextView tab=text(labels[i],12,Color.WHITE);tab.setGravity(Gravity.CENTER);tab.setPadding(dp(5),dp(12),dp(5),dp(12));tab.setOnClickListener(v->showPage(p));nav.addView(tab,new LinearLayout.LayoutParams(0,dp(48),1));}
        root.addView(nav);setContentView(root);
    }
    void showPage(int p){page=p;content.removeAllViews();if(p==0)timerPage();else if(p==1)presetsPage();else if(p==2)historyPage();else settingsPage();}
    void card(LinearLayout parent,View child){LinearLayout box=column();box.setPadding(dp(15),dp(14),dp(15),dp(14));box.setBackground(bg(Color.rgb(24,26,37),18));box.addView(child);LinearLayout.LayoutParams q=lp(-1,-2);q.bottomMargin=dp(12);parent.addView(box,q);}
    EditText input(String hint,String value,int size){EditText e=new EditText(this);e.setSingleLine(true);e.setHint(hint);e.setText(value);e.setTextColor(Color.WHITE);e.setHintTextColor(Color.GRAY);e.setTextSize(size);e.setGravity(Gravity.CENTER);e.setInputType(2);e.setSelectAllOnFocus(true);e.setBackground(bg(Color.rgb(37,39,53),12));return e;}
    void timerPage(){
        content.addView(heading("Таймеры"));
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);countLabel=small(active.length()+" активных таймеров");top.addView(countLabel,new LinearLayout.LayoutParams(0,-2,1));TextView tag=text("MULTI-ALARM",10,accent);top.addView(tag);content.addView(top,lp(-1,28));
        LinearLayout form=column();form.setPadding(dp(15),dp(14),dp(15),dp(15));form.setBackground(bg(Color.rgb(24,26,37),18));
        form.addView(small("НОВОЕ УВЕДОМЛЕНИЕ"));titleInput=input("Название напоминания","Напоминание",16);titleInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);LinearLayout.LayoutParams tp=lp(-1,50);tp.topMargin=dp(10);form.addView(titleInput,tp);
        LinearLayout fields=new LinearLayout(this);fields.setGravity(Gravity.CENTER);fields.setPadding(0,dp(12),0,dp(10));hours=input("00","00",25);minutes=input("05","05",25);seconds=input("00","00",25);fields.addView(hours,new LinearLayout.LayoutParams(0,dp(62),1));fields.addView(text(":",25,Color.WHITE));fields.addView(minutes,new LinearLayout.LayoutParams(0,dp(62),1));fields.addView(text(":",25,Color.WHITE));fields.addView(seconds,new LinearLayout.LayoutParams(0,dp(62),1));form.addView(fields);
        LinearLayout units=new LinearLayout(this);String[] u={"ЧАСЫ","МИНУТЫ","СЕКУНДЫ"};for(String x:u){TextView t=small(x);t.setGravity(Gravity.CENTER);units.addView(t,new LinearLayout.LayoutParams(0,-2,1));}form.addView(units);
        Button add=button("＋  Добавить таймер");add.setOnClickListener(v->schedule());LinearLayout.LayoutParams ap=lp(-1,52);ap.topMargin=dp(14);form.addView(add,ap);content.addView(form,lp(-1,-2));
        TextView activeTitle=heading("Запланировано");activeTitle.setTextSize(19);activeTitle.setPadding(0,dp(20),0,dp(10));content.addView(activeTitle);
        activeList=column();content.addView(activeList);renderActive();
        TextView note=small("Таймеры сохраняются после закрытия приложения. Для точного срабатывания Android может запросить специальное разрешение.");note.setPadding(0,dp(8),0,dp(16));content.addView(note);
    }
    void schedule(){
        long h=num(hours),m=num(minutes),s=num(seconds),total=h*3600+m*60+s;
        if(total<=0||total>315360000L){Toast.makeText(this,"Укажите длительность от 1 секунды до 10 лет",0).show();return;}
        if(Build.VERSION.SDK_INT>=31&&!alarms.canScheduleExactAlarms()){startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));Toast.makeText(this,"Разрешите точные будильники и повторите",1).show();return;}
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=0){requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIFICATIONS);return;}
        int id=(int)(System.currentTimeMillis()&0x7fffffff);String title=titleInput.getText().toString().trim();if(title.isEmpty())title="Напоминание";
        long at=System.currentTimeMillis()+total*1000L;
        try{JSONObject item=new JSONObject();item.put("id",id);item.put("title",title);item.put("at",at);item.put("created",System.currentTimeMillis());active.put(item);save();Intent i=new Intent(this,AlarmReceiver.class).putExtra("id",id).putExtra("title",title);PendingIntent pi=PendingIntent.getBroadcast(this,id,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,pi);hours.setText("00");minutes.setText("05");seconds.setText("00");titleInput.setText("Напоминание");((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(titleInput.getWindowToken(),0);showPage(0);Toast.makeText(this,"Таймер добавлен",0).show();}catch(Exception e){Toast.makeText(this,"Не удалось создать таймер",0).show();}
    }
    long num(EditText e){try{return Math.max(0,Long.parseLong(e.getText().toString()));}catch(Exception x){return 0;}}
    void renderActive(){activeList.removeAllViews();countLabel.setText(active.length()+" активных таймеров");if(active.length()==0){TextView empty=small("Пока пусто. Создайте первый таймер выше.");empty.setPadding(0,dp(8),0,dp(18));activeList.addView(empty);return;}
        ArrayList<JSONObject> items=new ArrayList<>();for(int i=0;i<active.length();i++)items.add(active.optJSONObject(i));Collections.sort(items,(a,b)->Long.compare(a.optLong("at"),b.optLong("at")));
        for(JSONObject o:items){if(o==null)continue;LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);LinearLayout info=column();TextView name=text(o.optString("title"),15,Color.WHITE);name.setTypeface(null,Typeface.BOLD);long left=Math.max(0,(o.optLong("at")-System.currentTimeMillis())/1000);TextView time=small(String.format(Locale.getDefault(),"Через %02d:%02d:%02d",left/3600,(left%3600)/60,left%60));info.addView(name);info.addView(time);row.addView(info,new LinearLayout.LayoutParams(0,-2,1));TextView cancel=text("ОТМЕНА",10,Color.rgb(255,130,150));cancel.setPadding(dp(8),dp(12),0,dp(12));int id=o.optInt("id");cancel.setOnClickListener(v->cancel(id));row.addView(cancel);card(activeList,row);}
    }
    void cancel(int id){Intent i=new Intent(this,AlarmReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(this,id,i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(pi!=null){alarms.cancel(pi);pi.cancel();}JSONArray next=new JSONArray();for(int j=0;j<active.length();j++){JSONObject o=active.optJSONObject(j);if(o!=null&&o.optInt("id")!=id)next.put(o);}active=next;save();showPage(0);}
    void presetsPage(){content.addView(heading("Быстрые шаблоны"));content.addView(small("ОДНО КАСАНИЕ — И ТАЙМЕР УЖЕ ЗАПЛАНИРОВАН"));String[][] data={{"1 минута","00:01:00"},{"5 минут","00:05:00"},{"10 минут","00:10:00"},{"15 минут","00:15:00"},{"25 минут · Focus","00:25:00"},{"30 минут","00:30:00"},{"45 минут","00:45:00"},{"1 час","01:00:00"},{"2 часа","02:00:00"},{"Завтра · 12 часов","12:00:00"}};for(String[] d:data){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);TextView name=text(d[0],16,Color.WHITE);row.addView(name,new LinearLayout.LayoutParams(0,-2,1));Button b=button("Старт");b.setOnClickListener(v->{String[] t=d[1].split(":");hours.setText(t[0]);minutes.setText(t[1]);seconds.setText(t[2]);titleInput.setText(d[0]);showPage(0);schedule();});row.addView(b,lp(92,44));card(content,row);}}
    void historyPage(){content.addView(heading("История"));content.addView(small("Последние сработавшие уведомления"));if(history.length()==0){content.addView(small("История появится после первого уведомления."));return;}for(int i=history.length()-1;i>=0;i--){JSONObject o=history.optJSONObject(i);if(o==null)continue;LinearLayout row=column();row.addView(text(o.optString("title"),15,Color.WHITE));row.addView(small(new SimpleDateFormat("dd.MM.yyyy  HH:mm",Locale.getDefault()).format(new Date(o.optLong("at")))));card(content,row);}Button clear=button("Очистить историю");clear.setOnClickListener(v->{history=new JSONArray();save();showPage(2);});content.addView(clear,lp(-1,50));}
    void settingsPage(){content.addView(heading("Настройки"));String[] opts={"Внешний вид","Тёмная AMOLED-тема активна","Версия приложения · 2.0","Минимальная версия Android · 10 (SDK 29+)","Уведомления и точные будильники","Данные хранятся локально на устройстве","SUYZISS LAB"};for(String s:opts){TextView t=text(s,15,Color.WHITE);t.setPadding(dp(14),dp(17),dp(14),dp(17));card(content,t);}Button clear=button("Отменить все таймеры");clear.setOnClickListener(v->{JSONArray old=active;for(int i=0;i<old.length();i++){JSONObject o=old.optJSONObject(i);if(o!=null)cancelAlarmOnly(o.optInt("id"));}active=new JSONArray();save();showPage(3);});content.addView(clear,lp(-1,50));}
    void cancelAlarmOnly(int id){Intent i=new Intent(this,AlarmReceiver.class);PendingIntent p=PendingIntent.getBroadcast(this,id,i,PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);if(p!=null){alarms.cancel(p);p.cancel();}}
    void askPermissions(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=0)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIFICATIONS);}
    @Override protected void onResume(){super.onResume();if(content!=null&&page==0)showPage(0);}
}

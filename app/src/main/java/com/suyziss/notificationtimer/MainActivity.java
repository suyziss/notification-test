package com.suyziss.notificationtimer;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends android.app.Activity {
    private EditText hours, minutes, seconds;
    private TextView status;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private static final int REQUEST_NOTIFICATIONS = 42;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 77, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        buildUi();
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView label(String text, int size) {
        TextView t = new TextView(this); t.setText(text); t.setTextColor(Color.WHITE); t.setTextSize(size); return t;
    }
    private void buildUi() {
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER_HORIZONTAL); root.setPadding(dp(24), dp(36), dp(24), dp(24)); root.setBackgroundColor(Color.rgb(12,14,20));
        TextView brand = label("SUYZISS  /  TOOLS", 13); brand.setTextColor(Color.rgb(164,151,255)); brand.setLetterSpacing(.18f);
        root.addView(brand, new LinearLayout.LayoutParams(-1, dp(32)));
        TextView title = label("Таймер", 34); title.setTypeface(Typeface.DEFAULT, Typeface.BOLD); title.setPadding(0, dp(12), 0, dp(4)); root.addView(title);
        TextView subtitle = label("Установите время — уведомление придёт автоматически", 14); subtitle.setTextColor(Color.LTGRAY); root.addView(subtitle);
        View spacer = new View(this); root.addView(spacer, new LinearLayout.LayoutParams(1, dp(48)));
        TextView prompt = label("ДЛИТЕЛЬНОСТЬ", 12); prompt.setTextColor(Color.rgb(170,170,185)); root.addView(prompt, new LinearLayout.LayoutParams(-1, dp(28)));
        LinearLayout fields = new LinearLayout(this); fields.setGravity(Gravity.CENTER); fields.setOrientation(LinearLayout.HORIZONTAL);
        hours = field("00"); minutes = field("05"); seconds = field("00");
        fields.addView(hours, weight()); fields.addView(separator(":")); fields.addView(minutes, weight()); fields.addView(separator(":")); fields.addView(seconds, weight());
        root.addView(fields, new LinearLayout.LayoutParams(-1, dp(76)));
        TextView units = label("ЧАСЫ                         МИНУТЫ                       СЕКУНДЫ", 10); units.setTextColor(Color.GRAY); units.setGravity(Gravity.CENTER); root.addView(units, new LinearLayout.LayoutParams(-1, dp(30)));
        status = label("Готов к запуску", 14); status.setTextColor(Color.rgb(180,180,195)); status.setGravity(Gravity.CENTER); status.setPadding(0, dp(28), 0, dp(24)); root.addView(status, new LinearLayout.LayoutParams(-1, dp(76)));
        Button start = button("Запустить таймер", Color.rgb(133,112,255)); start.setOnClickListener(v -> startTimer()); root.addView(start, new LinearLayout.LayoutParams(-1, dp(56)));
        Button cancel = button("Отменить", Color.rgb(42,44,56)); cancel.setOnClickListener(v -> cancelTimer()); LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(52)); cp.topMargin = dp(12); root.addView(cancel, cp);
        TextView foot = label("Работает в фоне • Android 10 и новее", 12); foot.setTextColor(Color.GRAY); foot.setGravity(Gravity.CENTER); LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(60)); fp.topMargin = dp(26); root.addView(foot, fp);
        setContentView(root);
    }
    private LinearLayout.LayoutParams weight() { return new LinearLayout.LayoutParams(0, dp(68), 1); }
    private EditText field(String hint) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setText(hint); e.setTextColor(Color.WHITE); e.setTextSize(28); e.setGravity(Gravity.CENTER); e.setSelectAllOnFocus(true); e.setInputType(2); e.setBackgroundColor(Color.rgb(30,32,43)); return e;
    }
    private TextView separator(String s) { TextView t=label(s,28); t.setGravity(Gravity.CENTER); return t; }
    private Button button(String text, int color) { Button b=new Button(this); b.setText(text); b.setTextColor(Color.WHITE); b.setTextSize(15); b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color)); return b; }
    private long read(EditText e) { try { return Math.max(0, Long.parseLong(e.getText().toString().trim())); } catch(Exception ex) { return 0; } }
    private void startTimer() {
        long total = read(hours)*3600L + read(minutes)*60L + read(seconds);
        if (total <= 0) { Toast.makeText(this, "Укажите время больше нуля", Toast.LENGTH_SHORT).show(); return; }
        if (total > 315360000L) { Toast.makeText(this, "Слишком большая длительность", Toast.LENGTH_SHORT).show(); return; }
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            Intent settings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()));
            startActivity(settings); Toast.makeText(this, "Разрешите точные будильники и нажмите запуск ещё раз", Toast.LENGTH_LONG).show(); return;
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS); return;
        }
        long trigger = System.currentTimeMillis() + total*1000L;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, alarmIntent);
        status.setText("Уведомление запланировано через " + String.format(Locale.getDefault(), "%02d:%02d:%02d", total/3600, (total%3600)/60, total%60));
        Toast.makeText(this, "Таймер запущен", Toast.LENGTH_SHORT).show();
    }
    private void cancelTimer() { alarmManager.cancel(alarmIntent); status.setText("Таймер отменён"); Toast.makeText(this, "Таймер отменён", Toast.LENGTH_SHORT).show(); }
}

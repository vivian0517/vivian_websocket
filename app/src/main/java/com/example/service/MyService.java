package com.example.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("tt", "oncreate");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("service", "xxx", manager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(this,"ChanelId")
                    .setContentTitle("截图小助手")
                    .setContentText("点击进入")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.camera)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.camera))
                    .setContentIntent(pi)
                    .setChannelId("service")
                    .build();

            startForeground(1, notification);
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("tt","onstartcommand");
        new Thread(new Runnable() {
            @Override
            public void run() {
               //处理逻辑

            }
        }).start();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("tt","ondestroy");
    }
}
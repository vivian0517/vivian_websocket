package com.example.service;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.java_websocket.WebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final static int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_CODE_FLOAT = 2;
    private MyWebSocket socket;
    private TextView ip_address;
    private WifiManager wifiManager;
    private String address;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;
    private Button startService;
    private Button stopService;
    private Button permissionService;
    private Button testsend;

    //监听网络wifi变化
    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {// 这个监听wifi的打开与关闭，与wifi的连接无关
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                Log.e("hhh", "wifiState" + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        ip_address.setText("请打开wifi");
                        startService.setEnabled(false);
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //do something
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                address = intIP2String(wifiInfo.getIpAddress());
                                ip_address.setText(address);
                                startService.setEnabled(true);
                            }
                        }, 2000);

                        break;

                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取当前wifi名称
                    Log.i("websocket", "连接到网络 " + wifiInfo.getIpAddress());
                    address = intIP2String(wifiInfo.getIpAddress());
                    ip_address.setText(address);
                    if (socket != null) {
                        try {
                            socket.stop();
                            startService.setEnabled(true);
                            stopService.setEnabled(false);
                            permissionService.setEnabled(false);
                            //将连接上的客户端hashset传递给服务
                            FloatWindowsService.setClientServer((HashSet<WebSocket>) socket.connections());

                            Log.e("websocket", "断开链接");
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("websocket", "->出现异常：" + e);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Log.e("websocket", "->出现异常：" + e);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService = (Button) findViewById(R.id.button);
        stopService = (Button) findViewById(R.id.button2);
        permissionService = (Button) findViewById(R.id.button3);
        testsend = (Button)findViewById(R.id.button4);
        ip_address = (TextView) findViewById(R.id.ip_address);
        startService.setOnClickListener(this);
        stopService.setOnClickListener(this);
        permissionService.setOnClickListener(this);
        testsend.setOnClickListener(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //初始将两个按钮都禁用，条件符合再启用
        startService.setEnabled(false);
        stopService.setEnabled(false);
        permissionService.setEnabled(false);
        //监听网络wifi变化
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);

        if (!isTaskRoot()) {
            Intent intent = getIntent();
            String action = intent.getAction();
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
                return;
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                //Intent startIntent = new Intent(this,MyService.class);
                //startService(startIntent);
                startMyWebSocket();
                break;
            case R.id.button2:
                //Intent stopIntent = new Intent(this,MyService.class);
                //stopService(stopIntent);
                try {
                    socket.stop();
                    //将连接上的客户端hashset传递给服务
                    FloatWindowsService.setClientServer((HashSet<WebSocket>) socket.connections());

                    Log.e("websocket", "断开链接");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("websocket", "->出现异常：" + e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("websocket", "->出现异常：" + e);
                }
                startService.setEnabled(true);
                stopService.setEnabled(false);
                permissionService.setEnabled(false);
                break;
            case R.id.button3:
                requestCapturePermission();
                break;
            //case R.id.button4:
                //List<WebSocket> webSocketCollection = new ArrayList<>();
                //webSocketCollection.addAll(socket.connections());
                //FloatWindowsService.setClientServer((HashSet<WebSocket>) socket.connections());
                //break;

            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startMyWebSocket() {

        if (wifiManager.isWifiEnabled() && address != "") {
            InetSocketAddress myHost = new InetSocketAddress(address, 9090);
            socket = new MyWebSocket(myHost);
            socket.start();
            startService.setEnabled(false);
            stopService.setEnabled(true);
            permissionService.setEnabled(true);
        } else {
            Toast.makeText(this, "请打开wifi", Toast.LENGTH_LONG).show();
        }


    }


    private String intIP2String(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }


    private void requestCapturePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(getApplicationContext().MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }

    private void requestFloatWindowPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(MainActivity.this)) {

                Toast.makeText(MainActivity.this, "已开启悬浮窗", Toast.LENGTH_LONG).show();
                // FloatWindowsService.setResultData(data);
                //startService(new Intent(this, FloatWindowsService.class));
                // Intent startIntent = new Intent(this,MyService.class);
                //将连接上的客户端hashset传递给服务
                FloatWindowsService.setClientServer((HashSet<WebSocket>) socket.connections());
                Log.e("websocket", socket.connections().toString());
                startService(new Intent(getApplicationContext(), FloatWindowsService.class));
            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                Toast.makeText(MainActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_LONG).show();
                startActivityForResult(intent, REQUEST_CODE_FLOAT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    FloatWindowsService.setResultData(data);
                    requestFloatWindowPermission();
                }
                break;
            case REQUEST_CODE_FLOAT:
                requestFloatWindowPermission();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }
}
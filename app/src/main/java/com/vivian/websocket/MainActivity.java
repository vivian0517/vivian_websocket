package com.vivian.websocket;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

public class MainActivity extends AppCompatActivity {
    private MyWebSocket client;
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 192.168.1.101为安卓服务端，需要连接wifi后 高级选项ip设置为静态,输入自定义地址
        // 方便客户端 找 服务端,不需要用getHostAddress等，可能连接不上
        // 9090为端口
        InetSocketAddress myHost = new InetSocketAddress("10.249.255.204", 9090);
        client = new MyWebSocket(myHost);
        client.start();
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    client.stop();
                    Log.e("websocket", "断开链接");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("websocket", "->出现异常："+e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("websocket", "->出现异常："+e);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            client.stop();
            Log.e("websocket", "断开链接");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("websocket", "->出现异常："+e);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("websocket", "->出现异常："+e);
        }
    }
}
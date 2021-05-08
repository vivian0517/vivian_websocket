package com.vivian.websocket;


import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;

public class MyWebSocket extends WebSocketServer {
    MyWebSocket(InetSocketAddress host){
        super(host);
    }
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Log.d("websocket", "onOpen()一个客户端连接成功："+conn.getRemoteSocketAddress());
    }
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d("websocket", "onClose()服务器关闭");
    }
    @Override
    public void onMessage(WebSocket conn, String message) {
        // 这里在网页测试端发过来的是文本数据 测试网页 http://www.websocket-test.com/
        // 需要保证android 和 加载网页的设备(我这边是电脑) 在同一个网段内，连同一个wifi即可
        Log.d("websocket", "onMessage()网页端来的消息->"+message);
    }
    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        // 接收到的是Byte数据，需要转成文本数据，根据你的客户端要求
        // 看看是string还是byte，工具类在下面贴出
        Log.d("websocket", "onMessage()接收到ByteBuffer的数据->"+ByteUtil.byteBufferToString(message));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // 异常  经常调试的话会有缓存，导致下一次调试启动时，端口未关闭,多等待一会儿
        // 可以在这里回调处理，关闭连接，开个线程重新连接调用startMyWebsocketServer()
        Log.e("websocket", "->onError()出现异常："+ex);


    }
    @Override
    public void onStart() {
        Log.d("websocket", "onStart()，WebSocket服务端启动成功");
    }
}


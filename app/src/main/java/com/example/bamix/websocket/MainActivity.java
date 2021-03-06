package com.example.bamix.websocket;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;
import de.tavendo.autobahn.WebSocketOptions;

public class MainActivity extends AppCompatActivity {
    private String url = "ws://bamixsocket.azurewebsites.net/Controllers/Handler2.ashx";
    private TextView ipInfo;
    private EditText serverUrl;
    private EditText messageView;
    private Button connectButton;
    private Button sendButton;
    private boolean isConnected = false;
    private WebSocketConnection mConnection;
    private Server server;
    private final String UrlKey = "ServerUrl";
    private Timer mTimer;
    private MyTimerTask mMyTimerTask;
    private final String keepActive = "keep active";
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageView = (EditText) findViewById(R.id.message);
        serverUrl = (EditText) findViewById(R.id.server);
        ipInfo = (TextView) findViewById(R.id.ip);
        ListView listView = (ListView) findViewById(R.id.listView);
        connectButton = (Button) findViewById(R.id.connect);
        sendButton = (Button) findViewById(R.id.send);

        server = new Server(this);
        ipInfo.setText(server.getIpAddress()+" : "+ server.getPort());
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        if(listView!=null) listView.setAdapter(adapter);
        LoadUrlFromPreferences();
        Connect();
    }

    private String LoadServerAddress(){
        String address = serverUrl.getText().toString();
        if(!address.equals(url)){
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(UrlKey, address);
            editor.apply();
        }
        return  address;
    }

    private void SaveServerAddress(){
        String address = serverUrl.getText().toString();
        if(!address.equals(url)){
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(UrlKey, address);
            editor.apply();
        }
    }

    private void LoadUrlFromPreferences(){
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        url = sharedPref.getString(UrlKey,url);
        serverUrl.setText(url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SaveServerAddress();
        server.onDestroy();
    }

    public void onConnect(View v) {
        if(isConnected) Disconnect(); else Connect();
   }

    private void Connect(){
        if(mTimer!=null){
            mTimer.cancel();
        }
        mTimer = new Timer();
        mMyTimerTask = new MyTimerTask();
        mTimer.schedule(mMyTimerTask, 5000,5000);
        try {
            mConnection = new WebSocketConnection();
            WebSocketOptions options = new WebSocketOptions();
            options.setSocketConnectTimeout(1000000000);
            options.setSocketReceiveTimeout(1000000000);
            mConnection.connect(LoadServerAddress(), new WebSocketHandler() {
                @Override
                public void onOpen() {
                    adapter.insert("Connect",0);
                    connectButton.setText("Disconnect");
                    sendButton.setEnabled(true);
                    isConnected=true;
                }

                @Override
                public void onTextMessage(String message) {
                    server.send(message);
                    adapter.insert("Host: " + message.substring(0,message.length()<10?message.length():10),0);
                }

                @Override
                public void onClose(int code, String reason) {
                    adapter.insert("Disconnect",0);
                    connectButton.setText("Connect");
                    isConnected=false;
                    Connect();
                }
            },options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Disconnect() {
        mConnection.disconnect();
    }


    public void SendToServer(final String message){
        try {
            if( mConnection==null || !mConnection.isConnected()) Connect();
            if(message.isEmpty() )return;
            mConnection.sendTextMessage(message);
            if(!message.equals(keepActive)){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.insert("Client: " + message.substring(0,message.length()<10?message.length():10),0);
                    }
                });
           }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void onSend(View v){
        SendToServer(messageView.getText().toString());
        messageView.setText("");

    }

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            SendToServer(keepActive);
        }
    }
}

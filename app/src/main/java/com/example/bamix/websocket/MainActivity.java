package com.example.bamix.websocket;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;

public class MainActivity extends AppCompatActivity {
    private String url = "ws://alexignatyy-001-site1.ftempurl.com/Controllers/Handler1.ashx";
    private EditText messageView;
    private Button connectButton;
    private Button sendButton;
    private boolean isConnected = false;
    private WebSocketConnection mConnection;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageView = (EditText) findViewById(R.id.message);
        ListView listView = (ListView) findViewById(R.id.listView);
        connectButton = (Button) findViewById(R.id.connect);
        sendButton = (Button) findViewById(R.id.send);

        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        if(listView!=null) listView.setAdapter(adapter);
    }

    public void onConnect(View v) {
        if(isConnected) Disconnect(); else Connect();
   }

    private void Connect()
    {
        try {
            mConnection = new WebSocketConnection();
            mConnection.connect(url, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    adapter.add("Connect");
                    connectButton.setText("Disconnect");
                    sendButton.setEnabled(true);
                    isConnected=true;
                }

                @Override
                public void onTextMessage(String message) {
                    adapter.add(message);
                }

                @Override
                public void onClose(int code, String reason) {
                    adapter.add("Disconnect");
                    connectButton.setText("Connect");
                    sendButton.setEnabled(false);
                    isConnected=false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Disconnect() {
        mConnection.disconnect();
    }

    public void onSend(View v){
        try {
            String message = messageView.getText().toString();
            if(message.isEmpty())return;
            mConnection.sendTextMessage(message);
            messageView.setText("");
            adapter.add("ME: "+message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}

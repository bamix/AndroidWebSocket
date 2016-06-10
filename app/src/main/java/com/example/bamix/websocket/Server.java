package com.example.bamix.websocket;

/**
 * Created by bamix on 10.06.16.
 */

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class Server {
    MainActivity activity;
    ServerSocket serverSocket;
    ArrayList<Socket> sockets = new ArrayList<>();
    public boolean isAlive = true;
    static final int socketServerPORT = 8080;

    public Server(MainActivity activity) {
        this.activity = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    public int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void Send(String message)
    {
        for (Socket socket : sockets ) {
            SocketServerReplyThread replyThread = new SocketServerReplyThread(socket,message);
            replyThread.run();
        }
    }

    private class SocketServerThread extends Thread {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(socketServerPORT);

                while (isAlive) {
                    final Socket socket = serverSocket.accept();
                    sockets.add(socket);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activity.adapter.add("Connected: "+socket.getInetAddress().toString());
                        }
                    });
                    SocketServerReadThread readThread = new SocketServerReadThread(socket);
                    readThread.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketServerReadThread extends Thread {
        private Socket socket;

        SocketServerReadThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            char[] buffer = new char[1024];
            while (socket.isConnected())
            {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader( socket.getInputStream());
                    int c =inputStreamReader.read(buffer);
                    if(c==-1)
                    {
                        socket.close();
                        sockets.remove(socket);
                        Log.d("myTag","disconnect ");
                        break;
                    }
                    Log.d("myTag","read: "+ new String(buffer));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SocketServerReplyThread extends Thread {
        private Socket socket;
        private String message;

        SocketServerReplyThread(Socket socket, String message) {
            this.socket = socket;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                if(!socket.isConnected())return;
                PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
                out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "+ inetAddress.getHostAddress()+"\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
}


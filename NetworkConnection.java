package com.example.connectfourgame;

import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkConnection {
    private static NetworkConnection instance; //singleton
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private Thread listenThread;
    private boolean flagRun = false;
    private Handler mainHandler= new Handler(Looper.getMainLooper());
    private NetworkCallback callback;

    public interface NetworkCallback{
        void onPlayersListReceived(String[] players);
        void onGameStarted(String role, String rivalName);
        void onMoveReceived(int column);
        void onRivalLeft();
        void onRestartReceived();
    }

    private NetworkConnection(){}

    public static synchronized NetworkConnection getInstance(){
        if (instance == null){
            instance = new NetworkConnection();
        }
        return instance;
    }

    public void setCallback(NetworkCallback callback){
        this.callback = callback;
    }

    public void connect(final String ipAdress, final int port, final String userName){
        if (flagRun) return;

        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    socket = new Socket(ipAdress, port);
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                    pw.println(userName);
                    pw.flush();
                    flagRun = true;
                    startListening();
                }catch(IOException problem){
                    problem.printStackTrace();
                }
            }
        }).start();
    }

    public void sendMessage(final String message){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    if (pw != null){
                        pw.println(message);
                        pw.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startListening() {
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (flagRun) {
                    try {
                        final String line = br.readLine();
                        if (line != null) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        handleServerMessage(line);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            break;
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
                disconnect();
            }
        });
        listenThread.start();
    }

    private void handleServerMessage(String message){
        if (callback == null || message == null) return;

        message = message.trim();

        if (message.startsWith("PLAYER_LIST:")) {
            String listContent = message.replace("PLAYER_LIST:", "");
            if (listContent.isEmpty()) return;
            String[] players= listContent.split(",");
            callback.onPlayersListReceived(players);
        }
        else if (message.startsWith("START_MESSAGE;")){
            String[] tokeni = message.split(";");
            if(tokeni.length >= 3){
                callback.onGameStarted(tokeni[1], tokeni[2]);
            }
//            String playerRole = tokeni[1];
//            String rivalName = tokeni[2];
//            callback.onGameStarted(playerRole, rivalName);
        }
        else if (message.startsWith("TURN;")){
            String[] tokeni = message.split(";");
            if (tokeni.length >= 2){
                callback.onMoveReceived(Integer.parseInt(tokeni[1]));
            }
//            int column = Integer.parseInt(tokeni[1]);
//            callback.onMoveReceived(column);
        }
        else if (message.equals("PLAYER_LEFT")){
            callback.onRivalLeft();
        }
        else if (message.equals("RESTART")){
            callback.onRestartReceived();
        }
    }

    public void disconnect(){
        flagRun = false;
        try{
            if (socket != null) socket.close();
            if (br != null) br.close();
            if (pw != null) pw.close();
        }catch (IOException problem){
            problem.printStackTrace();
        }
    }

}



















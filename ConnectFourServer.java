/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.connectfourserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectFourServer {
    
    private ServerSocket ssocket;
    private int port;
    private ArrayList<ConnectedPlayerClient> clients;

    public ServerSocket getSsocket(){
        return ssocket;
    }
    
    public void setSsocket(ServerSocket ssocket){
        this.ssocket = ssocket;
    }
    
    public int getPort(){
        return port;
    }
    
    public void setPort(int port){
        this.port = port;
    }
    
    public ArrayList<ConnectedPlayerClient> getClients(){
        return clients;
    }
    
    public void acceptClients(){
        Socket client = null;
        Thread thrd;
        
        while(true){
            try{
                System.out.println("Server čeka nove igrače...");
                client = this.ssocket.accept();
            } catch(IOException problem){
                Logger.getLogger(ConnectFourServer.class.getName()).log(Level.SEVERE, null, problem);
            }
            
            if (client != null){
                ConnectedPlayerClient clnt = new ConnectedPlayerClient(client, clients);
                clients.add(clnt);
                thrd = new Thread(clnt);
                thrd.start();
            } else{
                break;
            }
        }
    }
    
    public ConnectFourServer(int port){
        this.clients = new ArrayList<>();
        try {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        } catch(IOException problem){
            Logger.getLogger(ConnectFourServer.class.getName()).log(Level.SEVERE, null, problem);
        }
    }
    
    public static void sendListOfAvailablePlayers(ArrayList<ConnectedPlayerClient> allThreads) {
        StringBuilder availableList = new StringBuilder("PLAYER_LIST:");
        
        for (ConnectedPlayerClient c : allThreads) {
            if (c.isAvailable()) {
                availableList.append(c.getUserName()).append(",");
            }
        }

        for (ConnectedPlayerClient c : allThreads) {
            if (c.isAvailable()) {
                c.sendMessage(availableList.toString());
            }
        }
    }
    
    public static void main(String[] args) {
        ConnectFourServer server = new ConnectFourServer(4925);
        System.out.println("Server za Connect Four je pokrenut, slušam na portu 4925...");
        server.acceptClients();
    }
}
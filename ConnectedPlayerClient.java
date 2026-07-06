/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.connectfourserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectedPlayerClient implements Runnable {
    
    private Socket socket;
    private String userName;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectedPlayerClient> allClients;
    private boolean available; 
    private ConnectedPlayerClient rival; 
    
    public String getUserName(){
        return userName;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    public void setAvailable(boolean available) {
        this.available = available;
    }
    
    public ConnectedPlayerClient getRival() {
        return rival;
    }
    
    public void setRival(ConnectedPlayerClient rival) {
        this.rival = rival;
    }

    public void sendMessage(String message) {
        this.pw.println(message);
    }
  
    public ConnectedPlayerClient(Socket socket, ArrayList<ConnectedPlayerClient> allClients){
        this.socket = socket;
        this.userName = "";
        this.allClients = allClients;
        this.available = true;
        this.rival = null;
        
        try{
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"), true);
        }catch(IOException problem){
            Logger.getLogger(ConnectedPlayerClient.class.getName()).log(Level.SEVERE, null, problem);
        }
    }
    
    @Override
    public void run(){
        while(true){
            try{
                if(this.userName.equals("")){
                    this.userName = this.br.readLine();
                    if(this.userName != null){
                        this.userName = this.userName.trim();
                        System.out.println("Konektovan igrač: " + this.userName);
                        ConnectFourServer.sendListOfAvailablePlayers(allClients);
                    } else {
                        closeConnection();
                        break;
                    }
                } else {
                    System.out.println("Server čeka na " + this.userName + " zahtev...");
                    String line = this.br.readLine();
                    
                    if(line != null){
                        System.out.println("Stigao je zahtev od " + this.userName + ": " + line);
                        if (line.startsWith("INVITATION")) {
                            String[] tokeni = line.split(";");
                            String selectedPlayer = tokeni[1].trim();
                            
                            ConnectedPlayerClient peerFound = null;
                            for(ConnectedPlayerClient cl : allClients) {
                                if(cl.getUserName().equalsIgnoreCase(selectedPlayer) && cl.isAvailable()) {
                                    peerFound = cl;
                                    break;
                                }
                            }
                            
                            if(peerFound != null) {
                                this.available = false;
                                peerFound.setAvailable(false);
                                
                                this.rival = peerFound;
                                peerFound.setRival(this);
                                this.sendMessage("START_MESSAGE;PLAYER1;" + peerFound.getUserName());
                                peerFound.sendMessage("START_MESSAGE;PLAYER2;" + this.userName);
                                
                                ConnectFourServer.sendListOfAvailablePlayers(allClients);
                            } else {
                                this.sendMessage("ERROR;Igrač je nedostupan.");
                            }
                        }
                        else if (line.startsWith("TURN")) {
                            if(this.rival != null) {
                                this.rival.sendMessage(line);
                            }
                        }
                        else if (line.startsWith("RESTART")) {
                            if(this.rival != null) {
                                this.rival.sendMessage("RESTART");
                            }
                        }
                        
                    } else {
                        closeConnection();
                        break;
                    }
                }
            } catch(IOException problem){
                System.out.println("Disconnected player: " + this.userName);
                closeConnection();
                return;
            }
        }
    }

    private void closeConnection() {
    
        if(this.rival != null) {
            this.rival.sendMessage("PLAYER_LEFT");
            this.rival.setAvailable(true);
            this.rival.setRival(null);
        }
        
        synchronized(this.allClients){
            Iterator<ConnectedPlayerClient> it = this.allClients.iterator();
            while(it.hasNext()){
                if(it.next().getUserName().equals(this.userName)){
                    it.remove();
                    break;
                }
            }
        }

        ConnectFourServer.sendListOfAvailablePlayers(allClients);
        
        try {
            this.socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ConnectedPlayerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
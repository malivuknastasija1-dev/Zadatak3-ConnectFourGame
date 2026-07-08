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
                        this.userName = this.userName.trim().replace("\r", "");
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
                            String selectedPlayer = tokeni[1].trim().replace("\r", "");
                            System.out.println("[SERVER] " + this.userName + " šalje pozivnicu za: '" + selectedPlayer + "'");                            
                            ConnectedPlayerClient peerFound = null;
                            for(ConnectedPlayerClient cl : allClients) {
                                if(cl.getUserName().equalsIgnoreCase(selectedPlayer) && cl.isAvailable()) {
                                    peerFound = cl;
                                    break;
                                }
                            }
                            
                            if(peerFound != null) {
                                peerFound.sendMessage("STIGAO_IZAZOV;" + this.userName);
                                System.out.println("[SERVER] Pozivnica uspešno prosleđena igraču " + selectedPlayer);
                            } else {
                                System.out.println("[SERVER] GRESKA: Igrac '" + selectedPlayer + "' nije pronadjen ili nije dostupan!");
                                this.sendMessage("ERROR;Igrač je nedostupan.");
                            }
                        }
                            else if (line.startsWith("RESPONSE")){
                            String[] tokeni = line.split(";");
                            String response = tokeni[1].trim().replace("\r", "");
                            String rivalName = tokeni[2].trim().replace("\r", "");
                            ConnectedPlayerClient rivalClient = null;
                            for(ConnectedPlayerClient cl : allClients) {
                                if(cl.getUserName().equalsIgnoreCase(rivalName)) {
                                    rivalClient = cl;
                                    break;
                                }
                            }
                            if (rivalClient != null){
                                if (response.equals("ACCEPT")){
                                    boolean canPlay = (this.rival == rivalClient) || (rivalClient.isAvailable() && this.available);
                                    
                                    if (canPlay){
                                        this.available = false;
                                    rivalClient.setAvailable(false);
                                    this.rival = rivalClient;
                                    rivalClient.setRival(this);
                                    rivalClient.sendMessage("START_MESSAGE;RED_PLAYER;" + this.userName);
                                    this.sendMessage("START_MESSAGE;BLUE_PLAYER;" + rivalClient.getUserName());
                                    ConnectFourServer.sendListOfAvailablePlayers(allClients);
                                    System.out.println("[SERVER] Partija uspesno sklopljena izmedju " + rivalName + " i " + this.userName);
                                    } else{
                                        rivalClient.sendMessage("INVITATION_REJECTED;" + this.userName);
                                        System.out.println("[SERVER] Igrač je nedostupan u lobiju za novu partiju.");
                                    }
                                } else{
                                    this.available = true;
                                    rivalClient.setAvailable(true);
                                    this.rival = null;
                                    rivalClient.setRival(null);
                                    rivalClient.sendMessage("INVITATION_REJECTED;" + this.userName);
                                    ConnectFourServer.sendListOfAvailablePlayers(allClients);
                                    System.out.println("[SERVER] Igrac " + this.userName + " je odbio izazov.");
                                }
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

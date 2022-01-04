package com.networkP2P;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.json.JSONObject;

public class Manager {
    public static void main(String[] args) throws Exception {


        Thread a = new Thread() {
            public void run() {
                try {
                    InetAddress ip = InetAddress.getByName("localhost");
                    Peer peer = new Peer(ip, 2222, "p");
                    peer.connectedTo.put("127.0.0.1", 2223);
              
  
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };


       Thread b =  new Thread() {
            public void run() {
                try {
                    InetAddress ip = InetAddress.getByName("localhost");
                    Peer peer = new Peer(ip, 2223, "p1");
                    peer.connectedTo.put("127.0.0.1", 2222);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        b.start();
        a.start();
       
        a.join();
        b.join();

    }
}

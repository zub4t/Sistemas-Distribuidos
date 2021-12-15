package com.networkP2P;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.Math;

public class Peer {
    private Map<String, JSONArray> dictionary = new TreeMap<>();
    ServerSocketChannel serverSocketChannel;
    Map<String, Integer> connectedTo = new TreeMap<>();
    Server server;
    String name;

    public Peer(InetAddress addr, int port, String name) {
        this.name = name;
        try {
            JSONArray words = new JSONArray(
                    HttpsClient.doHttpsRequest("https://random-word-api.herokuapp.com/word?number=10"));
            for (int i = 0; i < 3; i++) {
                String word = words.get(generateNumber(0, 9)).toString();
                JSONArray meaning = new JSONArray(
                        HttpsClient.doHttpsRequest("https://api.dictionaryapi.dev/api/v2/entries/en/word"));
                // System.out.println(meaning.toString());
                dictionary.put(word, meaning);
            }
            System.out.println(name + " está iniciando servidor");
            server = new Server(name);
            final InetAddress addr_aux = addr;
            final int port_aux = port;
            new Thread() {
                public void run() {
                    try {
                        server.init(addr_aux, port_aux);
                    } catch (Exception e) {
                    }
                }
            }.start();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) throws Exception {
        for (Map.Entry<String, Integer> entry : connectedTo.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

            server.sendMessage(msg, entry.getKey(), entry.getValue());
        }

    }

    public static int generateNumber(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;
    }
}

class Server {
    Selector selector;
    ServerSocketChannel serverChannel;
    SocketChannel sc;
    String name;

    Server(String name) {
        this.name = name;

    }

    public void init(InetAddress addr, int port) throws IOException {
        selector = Selector.open();
/*
        sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
*/
        serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(new InetSocketAddress(addr, port));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        run();
    }

    public void sendMessage(String msg, String dst, int port) throws Exception {

        sc.connect(new InetSocketAddress(dst, port));
        System.out.println(sc.isConnected());
        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        msg = name + " -> " + msg;
        buf.put(msg.getBytes());
        buf.flip();

        while (buf.hasRemaining()) {
            sc.write(buf);
        }

    }

    public void run() throws IOException {

        try {
            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    } else if (key.isReadable()) {
                        System.out.println("HOST " + name + " isReadable for ");
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        channel.read(buffer);
                        String mgs = new String(buffer.array(), "ASCII");
                        System.out.println(mgs);
                        key.interestOps(SelectionKey.OP_WRITE);

                    } else if (key.isWritable()) {
                        System.out.println("HOST " + name + " isWritable for ");
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        buffer.flip();

                        channel.write(buffer);
                        if (buffer.hasRemaining()) {
                            buffer.compact();
                        } else {
                            buffer.clear();
                        }
                        key.interestOps(SelectionKey.OP_READ);
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
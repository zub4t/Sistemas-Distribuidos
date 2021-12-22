package com.networkP2P;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import org.json.JSONArray;

import java.lang.Math;

public class Peer {
    ServerSocketChannel serverSocketChannel;
    Map<String, Integer> connectedTo = new TreeMap<>();
    Server server;
    String name;
    SocketChannel sc;
    Selector selector;

    public static void main(String[] args) throws Exception {
        int port = 2222;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        InetAddress ip = InetAddress.getByName("192.168.0.156");
        Peer peer = new Peer(ip, port, "p");

        Scanner scanner = new Scanner(System.in);
        System.out.println("0 - register");
        System.out.println("1 - push");
        System.out.println("2 - pull");
        System.out.println("3 - pushpull");
        Message m;
        ByteBuffer bb;
        while (true) {
            String c = scanner.nextLine();
            String command[] = c.split(" ");

            switch (command[0]) {
                case "register":
                    System.out.println("Type the IP followed by the port ");
                    c = scanner.nextLine();
                    command = c.split(" ");

                    if (peer.register(command[0], Integer.parseInt(command[1]))) {
                        System.out.println("Connection success!!!");
                        peer.connectedTo.put(command[0], Integer.parseInt(command[1]));
                    }
                    break;
                case "push":
                    System.out.println("Type the IP followed by the port ");
                    c = scanner.nextLine();
                    command = c.split(" ");
                    m = new Message(1, peer.server.dictionary);
                    bb = Server.serialize(m);
                    peer.sendMessage(bb, command[0], Integer.parseInt(command[1]));
                    break;
                case "pull":
                    System.out.println("Type the IP followed by the port ");
                    c = scanner.nextLine();
                    command = c.split(" ");
                    m = new Message(2, null);
                    bb = Server.serialize(m);
                    peer.sendMessage(bb, command[0], Integer.parseInt(command[1]));

                    break;
                case "pushpull":
                    System.out.println("Type the IP followed by the port ");
                    c = scanner.nextLine();
                    command = c.split(" ");
                    m = new Message(3, peer.server.dictionary);
                    bb = Server.serialize(m);
                    peer.sendMessage(bb, command[0], Integer.parseInt(command[1]));
                    break;

                case "listConnectedTo":
                    System.out.println("List of connected peer");
                    for (Map.Entry<String, Integer> entry : peer.connectedTo.entrySet()) {
                        System.out.println("IP = " + entry.getKey() + ", Port = " +
                                entry.getValue());
                    }

                case "listDictionary":
                    System.out.println("List of connected peer");
                    for (Map.Entry<String, String> entry : peer.server.dictionary.entrySet()) {
                        System.out.println("word = " + entry.getKey() + ", meaning = " +
                                entry.getValue());
                    }
                    break;

            }

        }

    }

    public Peer(final InetAddress addr, final int port, final String name) {
        this.name = name;

        server = new Server(name);

        new Thread() {
            public void run() {
                try {
                    System.out.println(name + " está iniciando servidor");
                    server.init(addr, port);
                } catch (Exception e) {
                }
            }
        }.start();

        try {
            // HttpsClient.doHttpsRequest("https://random-word-api.herokuapp.com/word?number=10")
            JSONArray words = new JSONArray("[car,ball,blue,bird,tiger,house,assignment,work,world,word,test]");
            for (int i = 0; i < 3; i++) {
                String word = words.get(generateNumber(0, 9)).toString();
                JSONArray meaning = new JSONArray(
                        HttpsClient.doHttpsRequest("https://api.dictionaryapi.dev/api/v2/entries/en/word"));
                // System.out.println(meaning.toString());
                this.server.dictionary.put(word, meaning.toString());
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public boolean register(String ip, int port) {
        try {
            sc = SocketChannel.open();
            sc.connect(new InetSocketAddress(ip, port));
            return sc.isConnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public void sendMessage(ByteBuffer bb, String ip, int port) throws Exception {
        selector = Selector.open();
        sc = SocketChannel.open();

        sc.connect(new InetSocketAddress(ip, port));
        while (bb.hasRemaining()) {
            sc.write(bb);
        }
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(100024));
        selector.select();
        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            System.out.println("Waiting response");
            SelectionKey key = keys.next();
            keys.remove();
            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer buffer = (ByteBuffer) key.attachment();
                channel.read(buffer);
                Message msg = Server.deserialize(buffer);

                switch (msg.getType()) {

                    case 2:
                    case 3:
                        server.dictionary.putAll(msg.dictionaryContentReciver);
                        System.out.println("Me: Receiving dictionary update");
                        break;

                }
                System.out.println("Friend: " + msg.comment);
                sc.close();
                return;

            }

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
    String name;
    public Map<String, String> dictionary = new HashMap<>();

    Server(String name) {
        this.name = name;

    }

    public void init(InetAddress addr, int port) throws IOException {
        selector = Selector.open();

        serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(new InetSocketAddress(addr, port));
        System.out.println("Escutando em " + addr.getHostAddress() + ":" + port);

        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        run();
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
                        System.out.println("Accepting connection");
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(100024));
                    } else if (key.isReadable()) {
                        System.out.println("Receving message");
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        channel.read(buffer);
                        Message msg = deserialize(buffer);

                        switch (msg.getType()) {
                            case 1:
                            case 3:
                                dictionary.putAll(msg.dictionaryContentSender);
                                System.out.println("Receiving dictionary update");
                                break;
                            case 2:
                                System.out.println("Preparing to seding my dictionary");

                                break;

                            default:
                                break;
                        }

                        key.interestOps(SelectionKey.OP_WRITE);

                    } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        Message msg = deserialize(buffer);

                        switch (msg.getType()) {
                            case 1:
                                System.out.println("Sending updated dictionary confirmation message");
                                if (msg.dictionaryContentReciver != null)
                                    msg.dictionaryContentReciver.clear();
                                if (msg.dictionaryContentSender != null)
                                    msg.dictionaryContentSender.clear();
                                msg.comment = "Receive your dictionary message ty ;D";

                                buffer = serialize(msg);
                                channel.write(buffer);
                                if (buffer.hasRemaining()) {
                                    buffer.compact();
                                } else {
                                    buffer.clear();
                                }
                                break;
                            case 2:
                            case 3:
                                System.out.println("Sending my dictionary ");

                                msg.dictionaryContentReciver = this.dictionary;
                                msg.comment = "Sending my dictionary :P"
                                        + (msg.getType() == 3 ? " and confirming the update of my dictionary " : "");
                                buffer = serialize(msg);
                                channel.write(buffer);
                                if (buffer.hasRemaining()) {
                                    buffer.compact();
                                } else {
                                    buffer.clear();
                                }
                                break;

                            default:
                                break;
                        }

                        channel.close();
                        // key.interestOps(SelectionKey.OP_READ);
                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Message deserialize(ByteBuffer buffer) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(buffer.array());
            ObjectInputStream stream = new ObjectInputStream(bis);
            Message msg = (Message) stream.readObject();
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static ByteBuffer serialize(Message msg) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(msg);
            oos.flush();
            byte[] b = bos.toByteArray();
            ByteBuffer bb = ByteBuffer.wrap(b);
            return bb;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
}
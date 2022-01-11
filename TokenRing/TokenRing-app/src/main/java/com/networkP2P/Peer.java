package com.networkP2P;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import org.json.JSONArray;

import java.lang.Math;

public class Peer {
    ServerSocketChannel serverSocketChannel;
    Map<String, Tuple> connectedTo = new TreeMap<>();
    Server server;
    String name;
    SocketChannel sc;
    Selector selector;
    boolean isReading;
    boolean isUpdating;

    public static void main(String[] args) throws Exception {

        InetAddress ip = InetAddress.getByName((args[0]));
        int port = Integer.parseInt(args[1]);
        System.out.println("Listening in " + ip.getHostAddress() + ":" + port);
        Peer peer = new Peer(ip, port);

        Scanner scanner = new Scanner(System.in);
        System.out.println("0 - register");
        System.out.println("1 - push");
        System.out.println("2 - pull");
        System.out.println("3 - pushpull");
        System.out.println("4 - listConnectedTo");
        System.out.println("5 - listDictionary");

        Message m;
        ByteBuffer bb;
        while (true) {
            String c = scanner.nextLine();
            String command[] = c.split(" ");
            try {

                switch (command[0]) {
                    case "register":
                        System.out.println("Type the IP followed by the port ");
                        c = scanner.nextLine();
                        command = c.split(" ");
                        m = new Message(0, null);
                        m.setComment(command[3] + "--" + args[0] + "--" + port);
                        bb = Server.serialize(m);
                        try {
                            peer.sendMessage(bb, command[1], Integer.parseInt(command[2]));
                            System.out.println("Connection success!!!");
                            peer.connectedTo.put(command[0], new Tuple(command[1], Integer.parseInt(command[2])));

                        } catch (Exception e) {
                            System.out.println("Connection Fail!!!");
                            e.printStackTrace();

                        }

                        break;
                    case "push":
                        System.out.println("Type the IP followed by the port ");
                        c = scanner.nextLine();
                        command = c.split(" ");
                        m = new Message(1, peer.server.dictionary);
                        bb = Server.serialize(m);
                        if (peer.connectedTo.get(command[0]) != null) {
                            peer.sendMessage(bb, peer.connectedTo.get(command[0]).ip,
                                    peer.connectedTo.get(command[0]).port);

                        } else {
                            peer.sendMessage(bb, command[0], Integer.parseInt(command[1]));

                        }
                        break;
                    case "pull":
                        System.out.println("Type the IP followed by the port ");
                        c = scanner.nextLine();
                        command = c.split(" ");
                        m = new Message(2, null);
                        bb = Server.serialize(m);

                        if (peer.connectedTo.get(command[0]) != null) {
                            peer.sendMessage(bb, peer.connectedTo.get(command[0]).ip,
                                    peer.connectedTo.get(command[0]).port);

                        } else {
                            peer.sendMessage(bb, command[0], Integer.parseInt(command[1]));

                        }
                        break;
                    case "pushpull":
                        System.out.println("Type the IP followed by the port ");
                        c = scanner.nextLine();
                        command = c.split(" ");
                        m = new Message(3, peer.server.dictionary);
                        bb = Server.serialize(m);
                        if (peer.connectedTo.get(command[0]) != null) {
                            peer.sendMessage(bb, peer.connectedTo.get(command[0]).ip,
                                    peer.connectedTo.get(command[0]).port);

                        } else {
                            peer.sendMessage(bb, command[0], Integer.parseInt(command[1]));

                        }
                        break;

                    case "listConnectedTo":
                        System.out.println("List of connected peer");
                        for (Map.Entry<String, Tuple> entry : peer.connectedTo.entrySet()) {
                            System.out.println("Alias = " + entry.getKey() + ", IP = " +
                                    entry.getValue().ip + " PORT=" + entry.getValue().port);
                        }
                        break;
                    case "listDictionary":
                        while (true) {
                            if (!peer.isUpdating) {
                                peer.isReading = true;
                                System.out.println("List of connected peer");
                                for (Map.Entry<String, String> entry : peer.server.dictionary.entrySet()) {
                                    System.out.println("word = " + entry.getKey() + ", meaning = " +
                                            entry.getValue());
                                }
                                peer.isReading = false;
                                break;
                            }

                        }

                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("0 - register");
                System.out.println("1 - push");
                System.out.println("2 - pull");
                System.out.println("3 - pushpull");
                System.out.println("4 - listConnectedTo");
                System.out.println("5 - listDictionary");
            }
        }

    }

    public Peer(final InetAddress addr, final int port) {

        server = new Server(this);

        new Thread() {
            public void run() {
                try {
                    System.out.println(" Está iniciando servidor");
                    server.init(addr, port);
                } catch (Exception e) {
                }
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    while (true) {
                        if (!isReading) {
                            isUpdating = true;
                            JSONArray words = new JSONArray(
                                    HttpsClient.doHttpsRequest("https://random-word-api.herokuapp.com/word?number=1"));

                            for (int i = 0; i < words.length(); i++) {
                                String word = words.get(i).toString();

                                server.dictionary.put(word, "....");
                            }
                            isUpdating = false;
                            Thread.sleep(Peer.generateNumber(10, 20) * 1000);

                        }

                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }.start();

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
    public Map<String, String> dictionary = new TreeMap<>();
    Peer peer;

    Server(Peer peer) {
        this.peer = peer;

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
                            case 0:

                                String addr = msg.getComment().split("--")[1];
                                int port = Integer.parseInt(msg.getComment().split("--")[2]);

                                peer.connectedTo.put(msg.getComment().split(
                                        "--")[0],
                                        new Tuple(addr, port));
                                System.out.println(
                                        "Registering " + msg.getComment().split("--")[0] + " to " + addr + ":" + port);
                                break;
                            case 1:
                            case 3:
                                dictionary.putAll(msg.dictionaryContentSender);
                                System.out.println("Receiving dictionary update");
                                break;
                            case 2:
                                System.out.println("Preparing to send my dictionary");

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
                            case 0:
                                System.out.println("Sending registered confirmation message");
                                if (msg.dictionaryContentReciver != null)
                                    msg.dictionaryContentReciver.clear();
                                if (msg.dictionaryContentSender != null)
                                    msg.dictionaryContentSender.clear();
                                msg.comment = "Registered your Alias Friend ";

                                buffer = serialize(msg);
                                channel.write(buffer);
                                if (buffer.hasRemaining()) {
                                    buffer.compact();
                                } else {
                                    buffer.clear();
                                }
                                break;
                            case 1:
                                System.out.println("Sending updated dictionary confirmation message");
                                if (msg.dictionaryContentReciver != null)
                                    msg.dictionaryContentReciver.clear();
                                if (msg.dictionaryContentSender != null)
                                    msg.dictionaryContentSender.clear();
                                msg.comment = "Received your dictionary message ty ;D";

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

class Tuple {
    public Tuple(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    String ip;
    int port;
}
package com.chat;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import java.lang.Math;

public class Peer {
    ServerSocketChannel serverSocketChannel;
    List<String[]> connectedTo = new ArrayList<>();
    Server server;
    String name;

    public static void main(String[] args) throws Exception {
        int port = 2222;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);
        InetAddress ip = InetAddress.getByName("localhost");
        final Peer peer = new Peer(ip, port, "p");

        Scanner scanner = new Scanner(System.in);
        System.out.println("Type the host followed by the port, when done type '.done' ");
        do {
            String c = scanner.nextLine();
            if (!c.equals(".done")) {
                String command[] = c.split(" ");
                try {
                    peer.connectedTo.add(new String[] { command[0], command[1] });
                } catch (Exception e) {
                    System.out.println("Not possible to add this host");
                }
            } else {
                break;
            }

        } while (true);
        System.out.println("Open Application Chat");

        new Thread() {
            public void run() {
                while (true) {
                    boolean[] checkPeersMessage = new boolean[peer.connectedTo.size()];
                    if (peer.server.messages.size() > 0) {
                        while (Peer.areAllTrue(checkPeersMessage)) {

                        }
                        if (!peer.server.messages.get(0).isBleat) {
                            System.out.println(peer.server.messages.get(0).getLamportClock() + ": "
                                    + peer.server.messages.get(0).getComment());

                        }
                        peer.server.messages.remove(0);
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        while (true) {
            Message m;
            ByteBuffer bb;
            String c = scanner.nextLine();
            m = new Message(peer.server.myLamportClock, c, false);
            peer.server.messages.add(m);
            // Collections.sort(peer.server.messages);
            bb = Server.serialize(m);
            peer.sendMessage(bb);

        }

    }

    public static boolean areAllTrue(boolean[] array) {
        for (boolean b : array)
            if (!b)
                return false;
        return true;
    }

    public Peer(final InetAddress addr, final int port, final String name) {
        this.name = name;

        server = new Server(name, this);

        new Thread() {
            public void run() {
                try {
                    System.out.println(name + " está iniciando servidor");
                    server.init(addr, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void sendMessage(ByteBuffer bb) throws Exception {

        for (String[] entry : connectedTo) {
            bb.rewind();
            SocketChannel sc = SocketChannel.open();
            String ip = entry[0];
            int port = Integer.parseInt(entry[1]);
            sc.connect(new InetSocketAddress(ip, port));
            while (bb.hasRemaining()) {
                sc.write(bb);
            }

            sc.close();

        }
        this.server.myLamportClock++;

    }

}

class Server {
    Selector selector;
    ServerSocketChannel serverChannel;
    String name;
    int myLamportClock;
    Peer myPeer;
    List<Message> messages = new ArrayList<>();

    Server(String name, Peer myPeer) {
        this.name = name;
        this.myPeer = myPeer;
    }

    public void init(InetAddress addr, int port) throws IOException {
        selector = Selector.open();

        serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.bind(new InetSocketAddress(addr, port));
        System.out.println("listening  " + addr.getHostAddress() + ":" + port);

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
                        // System.out.println("Accepting connection");
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel channel = server.accept();
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(100024));
                    } else if (key.isReadable()) {

                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        channel.read(buffer);
                        Message msg = deserialize(buffer);
                        // System.out.println("Receving message " + msg.getLamportClock());
                        this.myLamportClock = Math.max(myLamportClock, msg.getLamportClock()) + 1;
                        if (msg.isBleat) {
                            Message bleat = new Message(this.myLamportClock, "comment", true);
                            ByteBuffer bb = Server.serialize(bleat);
                            myPeer.sendMessage(bb);
                        } else {
                            messages.add(msg);
                            Collections.sort(messages);

                        }

                        if (buffer.hasRemaining()) {
                            buffer.compact();
                        } else {
                            buffer.clear();
                        }
                        key.cancel();
                        channel.close();
                        /*
                         * Message bleat = new Message(0, "");
                         * bleat.isBleat = true;
                         * myPeer.sendMessage(Server.serialize(bleat));
                         */

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
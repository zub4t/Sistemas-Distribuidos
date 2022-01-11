package com.tokenRing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.Random;
import java.util.Scanner;

public class Peer {
    public final static int MAX_PACKET_SIZE = 200;// 65507;
    DatagramChannel channeLeft;
    DatagramChannel channeRight;
    Selector selector;
    String ADDR_LEFT = "";
    String ADDR_RIGHT = "";
    int PORT_OUT_LEFT;
    int PORT_OUT_RIGHT;
    SelectionKey left, right;
    String name;
    Random rand = new Random();
    boolean hasToken;
    boolean isLock;

    public static void main(String[] args) {
        final Peer peer;
        try {

            int PORT_IN_LEFT = Integer.parseInt(args[0]);
            int PORT_IN_RIGHT = Integer.parseInt(args[1]);

            int PORT_OUT_LEFT = Integer.parseInt(args[2]);
            int PORT_OUT_RIGHT = Integer.parseInt(args[3]);

            String ADDR_LEFT = (args[4]);
            String ADDR_RIGHT = (args[5]);
            String nme = (args[6]);
            peer = new Peer(PORT_IN_LEFT, PORT_IN_RIGHT, PORT_OUT_LEFT, PORT_OUT_RIGHT, ADDR_LEFT, ADDR_RIGHT, nme);
            System.out.println("this peer has the token ?");
            Scanner scanner = new Scanner(System.in);
            peer.hasToken = scanner.nextInt() == 1 ? true : false;
            System.out.println(peer.hasToken);
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        peer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

            if (peer.hasToken) {
                System.out.println("The ring will running to the left or to the right ?");
                System.out.println("Type 1 for left");
                if (scanner.nextInt() == 1) {
                    peer.ignite(PORT_OUT_LEFT, ADDR_LEFT);
                } else {
                    peer.ignite(PORT_OUT_RIGHT, ADDR_RIGHT);

                }

            }
            new Thread() {
                @Override
                public void run() {
                    try {
                        Scanner sc = new Scanner(System.in);
                        while (true) {
                            String cmd = sc.nextLine();
                            if (cmd.equals("lock")) {
                                peer.isLock = true;
                            }
                            if (cmd.equals("unlock")) {
                                peer.isLock = false;
                            }

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            thread.join();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public Peer(int PORT_IN_LEFT, int PORT_IN_RIGHT, int PORT_OUT_LEFT, int PORT_OUT_RIGHT, String ADDR_LEFT,
            String ADDR_RIGHT, String name) throws IOException {

        this.selector = Selector.open();
        this.ADDR_LEFT = ADDR_LEFT;
        this.ADDR_RIGHT = ADDR_RIGHT;
        this.PORT_OUT_LEFT = PORT_OUT_LEFT;
        this.PORT_OUT_RIGHT = PORT_OUT_RIGHT;
        this.name = name;

        channeLeft = DatagramChannel.open();
        channeLeft.socket().bind(new InetSocketAddress(PORT_IN_LEFT));
        channeLeft.configureBlocking(false);
        left = channeLeft.register(selector, SelectionKey.OP_READ);

        channeRight = DatagramChannel.open();
        channeRight.socket().bind(new InetSocketAddress(PORT_IN_RIGHT));
        channeRight.configureBlocking(false);
        right = channeRight.register(selector, SelectionKey.OP_READ);

    }

    public void start() throws IOException, InterruptedException, ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            int readyChannels = selector.selectNow();
            if (readyChannels == 0)
                continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (key.isReadable()) {
                    DatagramChannel in = (DatagramChannel) key.channel();
                    ByteBuffer bb = ByteBuffer.allocate(MAX_PACKET_SIZE);
                    in.receive(bb);
                    bb.flip();
                    /*
                     * while (bb.hasRemaining()) { System.out.print(bb.get()); }
                     */
                    // Token token = (Token) SerializationUtils.deserialize(bb.array());
                    ByteArrayInputStream bis = new ByteArrayInputStream(bb.array());
                    ObjectInputStream stream = new ObjectInputStream(bis);
                    Token token = (Token) stream.readObject();
                    System.out.println(token);
                    token.setID(token.getID() + 1);
                    token.setMessage("The last peer to touch on this token was " + name);

                    while (true) {
                        Thread.sleep(100);
                        if (!isLock) {

                            bb.clear();
                            // bb.put(SerializationUtils.serialize(token));

                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            ObjectOutputStream oos = new ObjectOutputStream(bos);
                            oos.writeObject(token);
                            oos.flush();
                            bb.put(bos.toByteArray());

                            bb.flip();

                            DatagramChannel disposableDatagramChannel = DatagramChannel.open();
                            if (key == left) {
                                disposableDatagramChannel.send(bb, new InetSocketAddress(ADDR_LEFT, PORT_OUT_LEFT));

                            } else {
                                disposableDatagramChannel.send(bb, new InetSocketAddress(ADDR_RIGHT, PORT_OUT_RIGHT));

                            }
                            disposableDatagramChannel.close();
                            break;
                        }

                    }

                }
                iter.remove();
            }
        }

    }

    public void ignite(int PORT_OUT, String ADDR) throws IOException {
        System.out.println("iniciando ignite at port " + PORT_OUT);
        DatagramChannel disposableDatagramChannel = DatagramChannel.open();
        ByteBuffer bb = ByteBuffer.allocate(MAX_PACKET_SIZE);
        bb.clear();
        // bb.put(SerializationUtils.serialize(new Token("initial message, starting in
        // Thread " + name, 0, family)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Token("initial message, starting in Thread " + name, 0));
        oos.flush();
        bb.put(bos.toByteArray());

        bb.flip();

        disposableDatagramChannel.send(bb, new InetSocketAddress(ADDR, PORT_OUT));
        disposableDatagramChannel.close();

    }

}

class Token implements Serializable {
    private String message;
    private long ID;
    private int family;

    public Token(String message, long ID) {
        this.message = message;
        this.ID = ID;
    }

    public String getMessage() {
        return message;
    }

    public long getID() {
        return ID;
    }

    public int getFamily() {
        return family;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public void setFamily(int family) {
        this.family = family;
    }

    @Override
    public String toString() {
        return "ID " + this.ID + " -- family" + this.family + "\n" + this.message;

    }
}

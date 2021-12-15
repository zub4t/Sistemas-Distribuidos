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

public class Peer {
    public final static int MAX_PACKET_SIZE = 200;// 65507;
    DatagramChannel channeLeft;
    DatagramChannel channeRight;
    Selector selector;
    String ADDR = "";
    int PORT_OUT_LEFT;
    int PORT_OUT_RIGHT;
    SelectionKey left, right;
    String name;
    Random rand = new Random();

    public void init(int PORT_IN_LEFT, int PORT_IN_RIGHT, int PORT_OUT_LEFT, int PORT_OUT_RIGHT, String dst,
            String name) throws IOException {

        this.selector = Selector.open();
        this.ADDR = dst;
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
                    System.out.println("TOKEN Is \t" + token);
                    token.setID(token.getID() + 1);
                    token.setMessage("The last thead to touch on this token was " + name);
                    Thread.sleep(rand.nextInt(10000));

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
                        disposableDatagramChannel.send(bb, new InetSocketAddress(ADDR, PORT_OUT_LEFT));

                    } else {
                        disposableDatagramChannel.send(bb, new InetSocketAddress(ADDR, PORT_OUT_RIGHT));

                    }
                    disposableDatagramChannel.close();
                }
                iter.remove();
            }
        }

    }

    public void ignite(int PORT_OUT, int family) throws IOException {
        System.out.println("iniciando ignite at port " + PORT_OUT);
        DatagramChannel disposableDatagramChannel = DatagramChannel.open();
        ByteBuffer bb = ByteBuffer.allocate(MAX_PACKET_SIZE);
        bb.clear();
        // bb.put(SerializationUtils.serialize(new Token("initial message, starting in
        // Thread " + name, 0, family)));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new Token("initial message, starting in Thread " + name, 0, family));
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

    public Token(String message, long ID, int family) {
        this.message = message;
        this.ID = ID;
        this.family = family;
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

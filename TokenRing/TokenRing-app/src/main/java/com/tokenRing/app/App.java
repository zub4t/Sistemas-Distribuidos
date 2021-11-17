package com.tokenRing.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App {
    DatagramChannel channelInput;
    DatagramChannel channelOutput;
    ByteBuffer bb = ByteBuffer.allocate(128);
    Selector selector;
    String ADDR = "";
    int PORT_OUT = 0;

    public void init(int PORT_IN, int out, String dst) throws IOException {
        selector = Selector.open();
        ADDR = dst;
        PORT_OUT = out;

        channelInput = DatagramChannel.open();
        channelOutput = DatagramChannel.open();
        channelInput.socket().bind(new InetSocketAddress(PORT_IN));

        channelOutput.configureBlocking(false);
        channelInput.configureBlocking(false);
        channelInput.register(selector, SelectionKey.OP_READ);

    }

    public void start() throws IOException {

        while (true) {
            int readyChannels = selector.selectNow();
            // System.out.println("Canais ready " + readyChannels);
            if (readyChannels == 0)
                continue;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                int interestOps = key.interestOps();
                System.out.println(interestOps);
                if (key.isReadable()) {
                    DatagramChannel in = (DatagramChannel) key.channel();
                    in.read(bb);
                    bb.flip();
                    while (bb.hasRemaining()) {
                        System.out.print((char) bb.get());
                    }
                    System.out.println();
                    bb.rewind();
                    channelOutput.send(bb, new InetSocketAddress(ADDR, PORT_OUT));
                    bb.clear();
                }
                iter.remove();
            }
        }

    }

    public void sendToken(ByteBuffer bb) throws IOException {
        System.out.println(" envinado Inicial");
        channelOutput.send(bb, new InetSocketAddress(ADDR, PORT_OUT));

    }

}

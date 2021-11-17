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
    ByteBuffer incoming = ByteBuffer.allocate(16);
    ByteBuffer outcoming = ByteBuffer.allocate(16);
    Selector selector;
    SelectionKey keyToRead;
    SelectionKey keyToWrite;
    String ADDR = "";
    int PORT_OUT = 0;

    public void init(int PORT_IN, int out, String dst) throws IOException {
        ADDR = dst;
        PORT_OUT = out;

        channelInput = DatagramChannel.open();
        channelOutput = DatagramChannel.open();

        channelInput.socket().bind(new InetSocketAddress(PORT_IN));

        channelOutput.configureBlocking(false);
        channelInput.configureBlocking(false);

        selector = Selector.open();

        keyToRead = channelInput.register(selector, SelectionKey.OP_READ);
        keyToWrite = channelOutput.register(selector, SelectionKey.OP_WRITE);
    }

    public void start() throws IOException {
       
        Set<SelectionKey> selectedKeys = selector.selectedKeys();

        Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

        while (true) {
            int readyChannels = selector.selectNow();
            // System.out.println("Canais ready " + readyChannels);
            if (readyChannels == 0)
                continue;

            while (keyIterator.hasNext()) {

                SelectionKey key = keyIterator.next();
                if (key.isReadable()) {

                    channelInput.read(outcoming);

                    byte[] tst = new byte[16];
                    outcoming.get(tst);
                    System.out.println("Recebi algo  na thread:" + Thread.currentThread().getName() + " e foi  "
                            + (new String(tst, StandardCharsets.UTF_8)));
                } else if (key.isWritable()) {

                }

                keyIterator.remove();
            }
        }

    }

    public void sendToken(ByteBuffer bb) throws IOException {
        System.out.println(" envinado Inicial");
        channelOutput.send(bb, new InetSocketAddress(ADDR, PORT_OUT));
     
    }

}

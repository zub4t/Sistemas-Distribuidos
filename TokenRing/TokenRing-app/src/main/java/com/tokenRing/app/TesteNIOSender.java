package com.tokenRing.app;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class TesteNIOSender {
    public static void main(String[] args) throws Exception {
        String newData = "New String to write to file..." + System.currentTimeMillis();
        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());
        buf.flip();
        DatagramChannel channel = DatagramChannel.open();
        channel.send(buf, new InetSocketAddress("localhost", 8084));
        

    }

}
package com.tokenRing.app;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;

public class TesteNIOReceiver {
    public static void main(String[] args) throws Exception {

        System.out.println("Listening ");
        DatagramChannel channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(9999));
        ByteBuffer buf = ByteBuffer.allocate(48);
        // buf.clear();
        // 
        // System.out.println(StandardCharsets.UTF_8.decode(buf).toString());
        channel.receive(buf);
        buf.flip();
        while (buf.hasRemaining()) {
            System.out.print((char) buf.get());
        }
        System.out.println();
    }
}

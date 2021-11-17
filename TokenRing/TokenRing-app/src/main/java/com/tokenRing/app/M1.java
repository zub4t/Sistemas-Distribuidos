package com.tokenRing.app;

import java.io.IOException;
import java.nio.ByteBuffer;

public class M1 {

    public static void main(String[] args) {
        final App app1 = new App();
        final App app2 = new App();
        try {
            app1.init(8084, 8085, "localhost");
            app2.init(8085, 8084, "localhost");
            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.put("hello".getBytes());
            new Thread() {
                @Override
                public synchronized void start() {
                    try {
                        app2.start();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };

            app1.sendToken(bb);

            app1.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

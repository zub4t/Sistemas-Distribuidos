package com.tokenRing.app;

import java.io.IOException;
import java.nio.ByteBuffer;

public class M1 {

    public static void main(String[] args) {
        final App app1 = new App();
        final App app2 = new App();
        try {
            app1.init(8084, 8085, "localhost");
            app1.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

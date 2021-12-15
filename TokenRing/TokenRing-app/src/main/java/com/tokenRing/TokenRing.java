package com.tokenRing;

public class TokenRing {

    public static void main(String[] args) {
        TokenRing m1 = new TokenRing();
        m1.setUp();
    }

    public void setUp() {
        final Peer app1 = new Peer();
        final Peer app2 = new Peer();
        final Peer app3 = new Peer();

        try {
            // in_LEFT, in_RIGHT, out_LEFT, out_RIGHT
            Thread t1 = new Thread() {
                @Override
                public void run() {
                    try {
                        app1.init(8084, 8085, 8086, 8087, "localhost", "Miguel");
                        app1.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t1.start();
            Thread t2 = new Thread() {
                @Override
                public void run() {
                    try {
                        app2.init(8086, 8087, 8088, 8089, "localhost", "Marco");
                        app2.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t2.start();

            Thread t3 = new Thread() {
                @Override
                public void run() {
                    try {
                        app3.init(8088, 8089, 8084, 8085, "localhost", "Naruto");
                        app3.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            t3.start();

            System.out.println(" START app2");

            Thread.sleep(1000);
            app1.ignite(8086, 1);
            // app2.ignite(8084, 2);

            // app1.ignite(8087, 2);
            // app2.ignite(8085, 2);
            t1.join();
            t2.join();
            t3.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

package com.ducnguyen;

public class Main {

    public static void main(String[] args) {
        DemoSynchronization demoSynchronization = new DemoSynchronization();
        demoSynchronization.prepareDemo(10);
        demoSynchronization.startDemo();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        demoSynchronization.stopDemo();
    }
}

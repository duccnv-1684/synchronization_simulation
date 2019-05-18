package com.ducnguyen;

public class Main {

    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            System.out.println("==================================================================================================================");
            System.out.println("The "+(i+1)+"th testing");
            DemoSynchronization demoSynchronization = new DemoSynchronization();
            demoSynchronization.prepareDemo(50);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(">>>>>>>>>>>>>>>>>>>>");
            demoSynchronization.startDemo();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(">>>>>>>>>>>>>>>>>>>>");
            demoSynchronization.findCoordinator();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            demoSynchronization.getCoordinator();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            demoSynchronization.stopDemo();
        }
    }
}

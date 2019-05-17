package com.ducnguyen;

import java.util.*;

class DemoSynchronization {
    private Set<Host> mHosts = new HashSet<>();

    void prepareDemo(int numberClients) {
        for (int i = 0; i < numberClients; i++) {
            Host host = new Host();
            host.setName(i+"");
            mHosts.add(host);
        }
        for (Host host : mHosts) {
            host.setHosts(mHosts);
        }
    }


    void startDemo() {
        for (Host host : mHosts) {
            host.start();
        }
//        List<Host> hostList = new ArrayList<>(mHosts);
//        for (int i = 0; i < 5; i++) {
//            String message = "Message from " + hostList.get(i).getName() + "to " + hostList.get(i + 3).getName();
//            hostList.get(i).sendMessage(message, hostList.get(i + 3));
//        }
    }
    void initRing(){
        for (Host host : mHosts) {
            host.initRing();
        }
    }

    void stopDemo() {
        for (Host host : mHosts) {
            host.interrupt();
        }
    }
}

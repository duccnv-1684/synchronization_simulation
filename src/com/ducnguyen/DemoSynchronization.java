package com.ducnguyen;

import java.util.*;

class DemoSynchronization {
    private Set<Host> hosts = new HashSet<>();
    private List<String> id;
    private String trueCoordinator;

    void prepareDemo(int numberClients) {
        id = new ArrayList<>();
        for (int i = 0; i < numberClients; i++) {
            Host host = new Host();
            String hostId = UUID.randomUUID().toString();
//            String hostId = i + "";
            host.setName(hostId);
            id.add(hostId);
            System.out.println("Host " + host.getName() + " is init");
            hosts.add(host);
        }
        Collections.sort(id);
        trueCoordinator = id.get(id.size() - 1);
        for (Host host : hosts) {
            host.setHosts(hosts);
            host.trueCoordinator = this.trueCoordinator;
        }
    }


    void startDemo() {
        for (Host host : hosts) {
            host.start();
        }
        System.out.println("True coordinator is " + trueCoordinator);
    }

    void stopDemo() {
        for (Host host : hosts) {
            host.interrupt();
        }
    }

    void findCoordinator() {
        for (Host host : hosts) {
            host.findCoordinator();
        }
    }

    void getCoordinator() {
        for (Host host : hosts) {
            host.getCoordinator();
        }
    }
}

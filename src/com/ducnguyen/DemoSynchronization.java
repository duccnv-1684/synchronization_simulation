package com.ducnguyen;

import java.util.*;

class DemoSynchronization {
    private Set<Peer> peers = new HashSet<>();

    void prepareDemo(int numberClients) {
        for (int i = 0; i < numberClients; i++) {
            Peer peer = new Peer();
            peer.setName(UUID.randomUUID().toString());
            peers.add(peer);
        }
        for (Peer peer : peers) {
            peer.setPeers(peers);
        }
    }


    void startDemo() {
        for (Peer peer : peers) {
            peer.start();
        }
        List<Peer> peerList = new ArrayList<>(peers);
        for (int i = 0; i < 5; i++) {
            String message = "Message from " + peerList.get(i).getName() + "to " + peerList.get(i + 3).getName();
            peerList.get(i).sendMessage(message, peerList.get(i + 3));
        }
    }

    void stopDemo() {
        for (Peer peer : peers) {
            peer.interrupt();
        }
    }
}

package com.ducnguyen;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Peer extends Thread {
    private BlockingQueue<String> messages = new LinkedBlockingDeque<>();
    private Set<Peer> peers = new HashSet<>();
    private boolean isRunning;

    @Override
    public synchronized void start() {
        isRunning = true;
        super.start();
    }

    @Override
    public void run() {
        String message;
        while (isRunning)
            while ((message = messages.poll()) != null) handleMessage(message);
    }

    @Override
    public void interrupt() {
        isRunning = false;
        super.interrupt();
    }

    private void handleMessage(String message) {
        System.out.println(message);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Peer)) return false;
        else return ((Peer) obj).getName().equals(getName());
    }

    private void receiveMessage(String message) {
        try {
            this.messages.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void setPeers(Set<Peer> peers) {
        this.peers.retainAll(peers);
        this.peers.addAll(peers);
        this.peers.remove(this);
    }

    void sendMessage(String message, Peer peer) {
        peer.receiveMessage(message);
    }
}

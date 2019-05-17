package com.ducnguyen;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Host extends Thread {
    private BlockingQueue<String> mMessages = new LinkedBlockingDeque<>();
    private Set<Host> mHosts = new HashSet<>();
    private boolean mIsRunning;


    private Host mPreviousHost;
    private Host mNextHost;
    private boolean mIsRequesting;
    private boolean mIsFinding;
    private boolean mIsNeedGiveToken;

    @Override
    public synchronized void start() {
        mIsRunning = true;
        super.start();
    }

    @Override
    public void run() {
        String message;
        while (mIsRunning)
            while ((message = mMessages.poll()) != null) handleMessage(message);
    }

    @Override
    public void interrupt() {
        mIsRunning = false;
        super.interrupt();
    }

    private void handleMessage(String message) {
        String prefix = TokenRingMessage.getMessagePrefix(message);
        Host sender = getSender(message);
        switch (prefix) {
            case TokenRingMessage.MESSAGE_REQUEST_BECOME_NEXT_HOST_PREFIX:
                if (mPreviousHost != null)
                    sendMessage(TokenRingMessage.messageRequestUpdateNextHost(getName()), mPreviousHost);
                mPreviousHost = sender;
                System.out.println("Previous of " + getName() + " is " + sender.getName());
                sendMessage(TokenRingMessage.messageReplyBecomeNextHost(getName()), sender);
                break;
            case TokenRingMessage.MESSAGE_REPLY_BECOME_NEXT_HOST_PREFIX:
                mIsFinding = false;
                mNextHost = sender;
                break;
            case TokenRingMessage.MESSAGE_REQUEST_UPDATE_NEXT_HOST_PREFIX:
                mNextHost = null;
                sendMessage(TokenRingMessage.messageReplyUpdateNextHost(getName()), sender);
                findNextHost();
                break;
            case TokenRingMessage.MESSAGE_REPLY_UPDATE_NEXT_HOST_PREFIX:
                break;
            case TokenRingMessage.MESSAGE_GIVE_TOKEN_PREFIX:
                if (mIsNeedGiveToken) onAccept();
                else sendMessage(TokenRingMessage.messageGiveToken(getName()), mNextHost);
        }
    }
    public void initRing(){
        if (mNextHost==null && !mIsFinding) findNextHost();
    }

    void onAccept() {

    }

    private void findNextHost() {
        if (mHosts.size() == 0) return;
        mIsFinding = true;
        List<Host> hosts = new ArrayList<>(mHosts);
        List<String> hostIds = new ArrayList<>();
        for (Host host : hosts) hostIds.add(host.getName());
        hostIds.add(getName());
        Collections.sort(hostIds);
        int hostIndex = hostIds.indexOf(getName());
        int nextHostIndex = (++hostIndex == hostIds.size()) ? 0 : hostIndex;
        String nextHostId = hostIds.get(nextHostIndex);
        Host nextHost = null;
        for (Host host : hosts) {
            if (host.getName().equals(nextHostId)) {
                nextHost = host;
                break;
            }
        }
        sendMessage(TokenRingMessage.messageRequestBecomeNextHost(getName()), nextHost);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Host)) return false;
        else return ((Host) obj).getName().equals(getName());
    }

    private void receiveMessage(String message) {
        try {
            this.mMessages.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void setHosts(Set<Host> hosts) {
        this.mHosts.retainAll(hosts);
        this.mHosts.addAll(hosts);
        this.mHosts.remove(this);
    }

    void sendMessage(String message, Host host) {
        host.receiveMessage(message);
    }

    Host getSender(String message) {
        String senderId = TokenRingMessage.getMessageContent(message);
        for (Host host : mHosts) {
            if (host.getName().equals(senderId)) return host;
        }
        return null;
    }
}

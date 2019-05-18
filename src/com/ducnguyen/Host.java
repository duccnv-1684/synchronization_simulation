package com.ducnguyen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class Host extends Thread implements CentralizedRequestQueue.OnQueueChangeListener {
    private BlockingQueue<String> messages = new LinkedBlockingDeque<>();
    private Set<Host> hosts = new HashSet<>();
    private boolean isRunning;

    private Host mCoordinator;
    private String mCoordinatorId;
    private int mCoordinatorHopeCount = 0;
    private int mElectionHopeCount = 0;
    private boolean mIsFindingCoordinator;
    private boolean mIsCoordinatorFound;
    private boolean mIsConnectedToCoordinator;
    private boolean mIsElecting;
    private boolean mIsDenied;
    private List<String> mRequestQueue;
    private int mPreviousHopeCount;
    String trueCoordinator;

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
        switch (CentralizedMessage.getMessagePrefix(message)) {
            case CentralizedMessage.MESSAGE_REQUEST_COORDINATOR_PREFIX:
                if (mIsCoordinatorFound)
                    sendMessage(CentralizedMessage.messageReplyCoordinatorFound(mCoordinatorId), getSender(message));
                else
                    sendMessage(CentralizedMessage.messageReplyCoordinatorNotFound(getName()), getSender(message));
                break;

            case CentralizedMessage.MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX:
                String coordinatorReply = CentralizedMessage.getMessageContent(message);
                if (mCoordinatorId==null || mCoordinatorId.compareTo(coordinatorReply) < 0) {
                    mIsFindingCoordinator = false;
                    mIsCoordinatorFound = true;
                    mCoordinatorId = CentralizedMessage.getMessageContent(message);
                    connectToCoordinator();
                }
                break;

            case CentralizedMessage.MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX:
                mCoordinatorHopeCount--;
                if (mCoordinatorHopeCount == 0) {
                    mIsFindingCoordinator = false;
                    if (mPreviousHopeCount != hosts.size()) {
                        findCoordinator();
                    } else {
                        startCoordinatorElection();
                    }
                }
                break;

            case CentralizedMessage.MESSAGE_REQUEST_ELECTION_PREFIX:
                if (getName().compareTo(getSender(message).getName()) > 0) {
                    sendMessage(CentralizedMessage.messageReplyElectionDenyElection(getName()), getSender(message));
                    if (!mIsElecting && !mIsDenied) startCoordinatorElection();
                } else {
                    mIsDenied = true;
                    sendMessage(CentralizedMessage.messageReplyElectionAccept(getName()), getSender(message));
                }
                break;

            case CentralizedMessage.MESSAGE_REPLY_ELECTION_ACCEPT_PREFIX:
                mElectionHopeCount--;
                if (mElectionHopeCount == 0) {
                    mIsElecting = false;
                    mIsDenied = false;
                    setAsCoordinator();
                }
                break;
            case CentralizedMessage.MESSAGE_REPLY_ELECTION_DENY_PREFIX:
                mIsElecting = false;
                mIsDenied = true;
                break;

            case CentralizedMessage.MESSAGE_REQUEST_ENQUEUE_PREFIX:
                mRequestQueue.add(CentralizedMessage.getMessageContent(message));
                sendMessage(CentralizedMessage.messageReplyEnqueue(getName()), getSender(message));
                break;

            case CentralizedMessage.MESSAGE_REPLY_ENQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REQUEST_DEQUEUE_PREFIX:
                mRequestQueue.remove(CentralizedMessage.getMessageContent(message));
                sendMessage(CentralizedMessage.messageReplyDequeue(getName()), getSender(message));
                break;

            case CentralizedMessage.MESSAGE_REPLY_DEQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REPLY_GIVE_ACCESS_PREFIX:
                onAccepted();
                break;

            default:
                break;

        }
    }

    private void startCoordinatorElection() {
        mIsCoordinatorFound = false;
        mIsConnectedToCoordinator = false;
        mIsElecting = true;
        mIsDenied = false;
        List<Host> hosts = new ArrayList<>(this.hosts);
        mElectionHopeCount = hosts.size();
        for (Host host : hosts) {
            if (mIsDenied) return;
            sendMessage(CentralizedMessage.messageRequestElection(getName()), host);
        }
    }

    private void setAsCoordinator() {
        mIsDenied = false;
        mIsElecting = false;
        mIsCoordinatorFound = true;
        mIsConnectedToCoordinator = true;
        mCoordinatorId = getName();
        mRequestQueue = new CentralizedRequestQueue<>(this);
        List<Host> hosts = new ArrayList<>(this.hosts);
        for (Host host : hosts)
            sendMessage(CentralizedMessage.messageReplyCoordinatorFound(mCoordinatorId), host);
    }

    void getCoordinator() {
        if (mCoordinatorId.equals(trueCoordinator))
            System.out.println(getName() + "'s coordinator is true");
        else
            System.out.println(getName() + "'s coordinator is false, currently is " + mCoordinatorId);
    }

    void findCoordinator() {
        if (mIsConnectedToCoordinator || mIsFindingCoordinator || mIsElecting) return;
        if (mIsCoordinatorFound) {
            connectToCoordinator();
            return;
        }
        mIsFindingCoordinator = true;
        List<Host> hosts = new ArrayList<>(this.hosts);
        mCoordinatorHopeCount = hosts.size();
        mPreviousHopeCount = mCoordinatorHopeCount;
        for (Host host : hosts)
            sendMessage(CentralizedMessage.messageRequestCoordinator(getName()), host);
    }

    private void connectToCoordinator() {
        for (Host host : new ArrayList<>(hosts)) {
            if (host.getName().equals(mCoordinatorId)) {
                mCoordinator = host;
                mIsConnectedToCoordinator = true;
                return;
            }
        }
        connectToCoordinator();
    }


    private void onAccepted() {
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Host)) return false;
        else return ((Host) obj).getName().equals(getName());
    }

    private void receiveMessage(String message) {
        try {
            this.messages.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void setHosts(Set<Host> hosts) {
        this.hosts.retainAll(hosts);
        this.hosts.addAll(hosts);
        this.hosts.remove(this);
    }

    void sendMessage(String message, Host host) {
        String content = "";
        switch (CentralizedMessage.getMessagePrefix(message)) {
            case CentralizedMessage.MESSAGE_REQUEST_COORDINATOR_PREFIX:
                content = "request coordinator";
                break;

            case CentralizedMessage.MESSAGE_REPLY_COORDINATOR_FOUND_PREFIX:
                content = "coordinator found";
                break;

            case CentralizedMessage.MESSAGE_REPLY_COORDINATOR_NOT_FOUND_PREFIX:
                content = "coordinator not found";
                break;

            case CentralizedMessage.MESSAGE_REQUEST_ELECTION_PREFIX:
                content = "request election";
                break;

            case CentralizedMessage.MESSAGE_REPLY_ELECTION_ACCEPT_PREFIX:
                content = "election accept";
                break;
            case CentralizedMessage.MESSAGE_REPLY_ELECTION_DENY_PREFIX:
                content = "election deny";
                break;

            case CentralizedMessage.MESSAGE_REQUEST_ENQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REPLY_ENQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REQUEST_DEQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REPLY_DEQUEUE_PREFIX:
                break;

            case CentralizedMessage.MESSAGE_REPLY_GIVE_ACCESS_PREFIX:
                break;

            default:
                break;

        }
//        System.out.println(getName() + " send message \"" + content + "\" to " + host.getName());
        host.receiveMessage(message);
    }

    Host getSender(String message) {
        String senderId = CentralizedMessage.getMessageContent(message);
        for (Host host : hosts) {
            if (host.getName().equals(senderId)) return host;
        }
        return null;
    }

    @Override
    public void onDataChanged() {
        String accessId = mRequestQueue.get(0);
        for (Host host : hosts) {
            if (host.getName().equals(accessId)) {
                sendMessage(CentralizedMessage.messageReplyGiveAccess(getName()), host);
                break;
            }
        }
    }
}

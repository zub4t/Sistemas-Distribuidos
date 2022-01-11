package com.chat;

import java.io.Serializable;

public class Message implements Serializable, Comparable<Message> {
    private int lamportClock;
    private String owner;
    private String comment;
    public Boolean isBleat;

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Message(int lamportClock, String comment, Boolean isBleat) {
        this.lamportClock = lamportClock;
        this.comment = comment;
        this.isBleat = isBleat;
    }

    public int getLamportClock() {
        return this.lamportClock;
    }

    public void setLamportClock(int lamportClock) {
        this.lamportClock = lamportClock;
    }

    @Override
    public int compareTo(Message o) {
        Integer myClock = new Integer(this.getLamportClock());
        return myClock.compareTo(new Integer(o.getLamportClock()));
    }

}

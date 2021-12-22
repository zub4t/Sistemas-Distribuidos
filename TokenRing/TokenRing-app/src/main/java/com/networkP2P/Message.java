package com.networkP2P;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONArray;

/**
 * tipo 0 - register tipo 1 - push tipo 2 - pull tipo 3 - pushpull
 */
public class Message implements Serializable {
    private int type;
    public Map<String, String> dictionaryContentSender;
    public Map<String, String> dictionaryContentReciver;
    public String comment;

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Message(int type, Map<String, String> dictionarySender) {
        this.type = type;
        this.dictionaryContentSender = dictionarySender;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

}

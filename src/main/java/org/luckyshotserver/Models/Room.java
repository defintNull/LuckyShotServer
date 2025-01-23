package org.luckyshotserver.Models;

import org.java_websocket.WebSocket;

import java.util.ArrayList;

public class Room {
    private WebSocket owner;
    private ArrayList<WebSocket> members; // doesn't contain owner

    public Room(WebSocket owner) {
        this.owner = owner;
        this.members = new ArrayList<>();
        members.add(owner);
    }

    public void removeMember(WebSocket member) {
        this.members.remove(member);
    }

    public void addMember(WebSocket member) {
        members.add(member);
    }

    public WebSocket getOwner() {
        return owner;
    }

    public ArrayList<WebSocket> getMembers() {
        return members;
    }
}

package org.luckyshotserver.Facades.Services;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends WebSocketServer {
    private final static int port = 8456;
    private static Server instance = null;
    private ConcurrentHashMap<WebSocket, String> loggedUser = new ConcurrentHashMap<>();

    private Server() {
        super(new InetSocketAddress(port));
    }

    public static Server getInstance() {
        if(instance == null) {
            instance = new Server();
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        //Show Login Menu
        webSocket.send("Ciao");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        webSocket.send("addio");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Connection Start");
    }
}

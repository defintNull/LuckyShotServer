package org.luckyshotserver.Facades.Services;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.luckyshotserver.Facades.LoginFacade;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.UserFacade;
import org.luckyshotserver.Models.User;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Server extends WebSocketServer {
    private final static int port = 8456;
    private static Server instance = null;
    private ConcurrentHashMap<WebSocket, User> loggedUser = new ConcurrentHashMap<>();

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
        System.out.println("Client connected");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        loggedUser.remove(webSocket);
        System.out.println("Client disconnected");
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        ArrayList<String> message = new ArrayList<>(Arrays.asList(s.split(":")));
        String command = message.removeFirst();
        String params = String.join(":", message);
        if(command.equals("LOGIN")) {
            String username = params.split("&")[0];
            String password = params.split("&")[1];
            LoginFacade loginFacade = new LoginFacade();
            User user = loginFacade.login(webSocket, username, password);
            if(user == null) {
                sendError(webSocket, "NOT_FOUND");
                return;
            }
            if(loggedUser.containsValue(user)) {
                sendError(webSocket, "ALREADY_LOGGED");
                return;
            }
            ObjectConverter converter = new ObjectConverter();
            String userJSON = converter.userToJson(user);
            sendOk(webSocket, userJSON);
            loggedUser.put(webSocket, user);
        }
        else if (command.equals("REGISTER")) {
            String username = params.split("&")[0];
            String password = params.split("&")[1];
            LoginFacade loginFacade = new LoginFacade();
            loginFacade.register(webSocket, username, password);
        }
        else if (command.equals("UPDATE_USER")) {
            UserFacade userFacade = new UserFacade();
            User user = loggedUser.get(webSocket);

            if (!userFacade.updateUser(user, params)) {
                sendError(webSocket, "FATAL");
            } else {
                sendOk(webSocket, "OK");
            }
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(e.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Connection Start");
    }

    public void sendError(WebSocket webSocket, String message) {
        sendMessage(webSocket, "ERROR:" + message);
    }

    public void sendOk(WebSocket webSocket, String message) {
        sendMessage(webSocket, "OK:" + message);
    }

    public void sendMessage(WebSocket webSocket, String message) {
        webSocket.send("START");
        webSocket.send(message);
        webSocket.send("STOP");
    }
}

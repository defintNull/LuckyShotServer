package org.luckyshotserver.Facades.Services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Connection {
    private static Connection instance;
    private ServerSocket socket;
    private int portNumber;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private ArrayList<Socket> clientSockets;

    private Connection(int portNumber) {
        try {
            socket = new ServerSocket(portNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

        clientSockets = new ArrayList<>();
        this.portNumber = portNumber;
    }

    public static Connection getInstance(int portNumber) {
        if (instance == null) {
            instance = new Connection(portNumber);
        }
        return instance;
    }

    public boolean accept() {
        try {
            clientSockets.add(socket.accept());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void send(String message) {
        try {
            if (out == null) {
                out = new DataOutputStream(clientSockets.get(0).getOutputStream());
            }
            out.writeUTF(message); // Invia il messaggio come stringa UTF
            out.flush(); // Garantisce che il messaggio venga inviato immediatamente
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String receive() {
        try {
            if (in == null) {
                in = new DataInputStream(clientSockets.get(0).getInputStream());
            }
            return in.readUTF(); // Legge un messaggio UTF dal flusso di input
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Ritorna null in caso di errore
        }
    }

    public void close() {
        try{
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

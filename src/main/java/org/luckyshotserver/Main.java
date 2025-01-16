package org.luckyshotserver;

import org.luckyshotserver.Facades.Services.Server;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.start();
    }
}
package com.ntnt.httpserver;

import com.ntnt.httpserver.servers.HttpServer;

public class Main {

    public static void main(String[] args) {
        // write your code here
        HttpServer server = new HttpServer(5000, "com.ntnt.httpserver.controllers");
        server.start();

    }
}


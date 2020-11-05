package com.ntnt.httpserver.servers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class RequestQueueHandler extends Thread {
    private Queue<Socket> requestQueue;
    private Map<String, Map<Class, Method>> methodsHandleRequest;
    private int lenOfQueue;

    public RequestQueueHandler(Map<String, Map<Class, Method>> methodsHandleRequest) {
        this.requestQueue = new ArrayDeque<>();
        this.lenOfQueue = 0;
        this.methodsHandleRequest = methodsHandleRequest;
    }

    public boolean enQueue(Socket socket) {
        try {
            if(lenOfQueue < 2){
                requestQueue.add(socket);
                lenOfQueue++;
            }else{
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        while(true){
            try{

                if(requestQueue.size() == 0){
                    continue;
                }

                Socket socket = requestQueue.poll();

                InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                InetAddress inetAddress = inetSocketAddress.getAddress();
                System.out.println("[Request]    From: " + inetAddress.getHostName() + " -- Size in queue: " + lenOfQueue);

                SocketHandler socketHandler = new SocketHandler(socket, ()-> this.lenOfQueue--);

                //
                Class socketHandlerClass = socketHandler.getClass();
                Field methodsHandleRequestField = socketHandlerClass.getDeclaredField("methodsHandleRequest");
                methodsHandleRequestField.setAccessible(true);
                methodsHandleRequestField.set(socketHandler, this.methodsHandleRequest);

                socketHandler.start();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

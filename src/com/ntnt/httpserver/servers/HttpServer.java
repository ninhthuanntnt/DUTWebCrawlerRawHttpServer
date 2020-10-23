package com.ntnt.httpserver.servers;

import com.ntnt.httpserver.annotations.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer extends Thread {

    private ServerSocket server;
    private Map<String, Map<Class, Method>> methodsHandleRequest;
    private String controllerPackage;
    private int port;

    public HttpServer(int port, String controllerPackage) {
        this.port = port;
        this.methodsHandleRequest = new HashMap<>();
        this.controllerPackage = controllerPackage;
    }

    public HttpServer(String controllerPackage) throws IOException {
        server = new ServerSocket(8080);
        this.methodsHandleRequest = new HashMap<>();
        this.controllerPackage = controllerPackage;
    }

    @Override
    public void run() {
        try {
            this.server = new ServerSocket(this.port);
            initMethodsHandleRequest(this.controllerPackage);

            System.out.println("[HttpServer] HttpServer started on port " + port);
            while (true) {
                Socket socket = this.server.accept();
                SocketHandler socketHandler = new SocketHandler(socket);

                //
                Class socketHandlerClass = socketHandler.getClass();
                Field methodsHandleRequestField = socketHandlerClass.getDeclaredField("methodsHandleRequest");
                methodsHandleRequestField.setAccessible(true);
                methodsHandleRequestField.set(socketHandler, this.methodsHandleRequest);

                socketHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initMethodsHandleRequest(String packageName) {
        try {
            // get a list of classes needing to scan
            List<Class> classes = new ArrayList<>();
            File current = new File(".");
            String absolutePath = current.getAbsolutePath().substring(0, current.getAbsolutePath().length() - 1);

            String pathToPackage = packageName.replace(".", "\\");
            File scannedPackage = new File(absolutePath + "src\\" + pathToPackage);
            File[] listClassFiles = scannedPackage.listFiles();
            if (listClassFiles != null && listClassFiles.length > 0) {
                for (File classFile : listClassFiles) {
                    classes.add(Class.forName(packageName + "." + classFile.getName().split(".java")[0]));
                }
            }

            // start scan in every class
            for (Class scannedClass : classes) {
                String url;
                RequestMapping classAnnotation = (RequestMapping) scannedClass.getAnnotation(RequestMapping.class);

                if (classAnnotation == null)
                    url = "/";
                else
                    url = classAnnotation.value();

                Method[] methods = scannedClass.getMethods();
                for (Method method : methods) {
                    RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                    if (methodAnnotation != null) {
                        Map<Class, Method> classMethodMap = new HashMap<>();
                        classMethodMap.put(scannedClass, method);
                        this.methodsHandleRequest.put(methodAnnotation.method() + " " + url + methodAnnotation.value(), classMethodMap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

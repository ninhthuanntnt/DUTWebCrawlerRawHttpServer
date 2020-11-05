package com.ntnt.httpserver.servers;

import com.ntnt.httpserver.controllers.NTControllerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class SocketHandler extends Thread {
    private final Socket socket;
    private final BufferedWriter writer;
    private final InputStream reader;

    private Map<String, Map<Class, Method>> methodsHandleRequest;
    private Callable callBack;

    public SocketHandler(Socket socket, Callable callBack) throws IOException {
        this.callBack = callBack;
        this.socket = socket;
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        this.reader = socket.getInputStream();
    }

    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        char c;
        while ((c = (char) reader.read()) >= 0) {
            if (c == '\n' || c == '\uFFFF') break;
            if (c == '\r') {
                c = (char) reader.read();
                if ((c < 0) || (c == '\n')) break;
                sb.append('\r');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            String line;
            HttpRequest httpRequest = new HttpRequest();
            HttpResponse httpResponse = new HttpResponse();
            int contentLength = 0;

            // get request line information
            String requestLine = readLine();
            httpRequest.fetchRequestLineInfo(requestLine);

            // read header infos
            while ((line = readLine()) != null) {
                if (line.contains("Content-Length:")) {
                    contentLength = Integer.parseInt(line.replace("Content-Length:", "").trim());
                }
                if (line.isEmpty()) {
                    break;
                }
                httpRequest.fetchHeaderInfo(line);
            }

            if(httpRequest.getMethod().equalsIgnoreCase("OPTIONS")){

                httpResponse.setHeaders("Access-Control-Allow-Headers", httpRequest.getHeaders("Access-Control-Request-Headers"));

            }else{
                // Start read data from body
                StringBuilder bodyInfo = new StringBuilder();
                for (int i = 0; i < contentLength; i++) {
                    int c = this.reader.read();
                    bodyInfo.append((char) c);
                }

                httpRequest.fetchBodyInfo(bodyInfo.toString());

                // make response
//            if (bodyInfo.length() > 0) {

                String method = httpRequest.getMethod();
                String path = httpRequest.getPath();
                // execute the specified function is declared in MainController
                this.methodsHandleRequest.get(method + " " + path).forEach(
                        (key, value) -> {
                            try {
                                value.invoke(NTControllerFactory.getController(key), httpRequest, httpResponse);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                );
            }
            this.writer.write(httpResponse.toString());

        } catch (Exception e) {
            HttpResponse notFoundResponse = new HttpResponse();
            notFoundResponse.setStatus(404);
            try {
                this.writer.write(notFoundResponse.toString());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            e.printStackTrace();
        }

        try {
            this.writer.close();
            this.reader.close();
            this.socket.close();

            callBack.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package org.example;

import org.apache.http.NameValuePair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private static final int THREAD_POOL_SIZE = 64;

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }

    public static void handleConnection(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {

                return;
            }

            final var pathAndQuery = parts[1];
            final var path = pathAndQuery.split("\\?")[0]; // Extract path without query
            final var request = new Request(pathAndQuery, path, parts[2]);

            if (!validPaths.contains(path)) {
                sendNotFoundResponse(out);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                handleClassicHtmlRequest(filePath, out, mimeType, request);
            } else {
                handleRegularRequest(filePath, out, mimeType);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendNotFoundResponse(OutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static void handleClassicHtmlRequest(Path filePath, OutputStream out, String mimeType, Request request) throws IOException {
        try {
            final var template = Files.readString(filePath);
            final var content = template.replace("{time}", LocalDateTime.now().toString() + getQueryParamsString(request)).getBytes();
            sendOkResponse(out, mimeType, content.length);
            out.write(content);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRegularRequest(Path filePath, OutputStream out, String mimeType) throws IOException {
        try {
            final var length = Files.size(filePath);
            sendOkResponse(out, mimeType, length);
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendOkResponse(OutputStream out, String mimeType, long contentLength) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + contentLength + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }

    private static String getQueryParamsString(Request request) {
        StringBuilder queryParamsString = new StringBuilder();
        List<NameValuePair> queryParams = request.getQueryParams();
        for (NameValuePair param : queryParams) {
            queryParamsString.append("&").append(param.getName()).append("=").append(param.getValue());
        }
        return queryParamsString.toString();
    }
}
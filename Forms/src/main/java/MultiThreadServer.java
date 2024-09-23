import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;


class Server extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    private HashMap<String, Map<String, Handler1>> handlers;


    public Server(Socket s) throws IOException {
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                .getOutputStream())), true);

        start();
    }

    public void run() {
        try {
            while (true) {
                String requestLine = in.readLine();
                String[] parts = requestLine.split(" ");
                if (requestLine.equals("END"))
                    break;
                System.out.println("Echoing: " + requestLine);
                out.println(requestLine);

                if (parts.length != 3) {
                    // just close socket
                    continue;
                }

                final String path = parts[1];
                if (!validPaths.contains(path)) {
                    out.write(String.valueOf((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes()));
                    out.flush();
                    continue;
                }

                final Path filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);
                // special case for classic
                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write(String.valueOf((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes()));
                    out.write(String.valueOf(content));
                    out.flush();
                    continue;
                }
                final var length = Files.size(filePath);
                out.write(String.valueOf((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + Files.size(filePath) + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes()));
                Files.copy(filePath, Path.of(String.valueOf(out)));
                out.flush();
            }
            System.out.println("closing...");
        } catch (IOException e) {
            System.err.println("IO Exception");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Socket not closed");
            }
        }
    }

    public void addHandler(String method, String path, Handler1 handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);
    }

    public HashMap<String, Map<String, Handler1>> getHandlers() {
        return handlers;
    }
}

public class MultiThreadServer {
    static final int PORT = 8080;
    static ExecutorService executeIt = Executors.newFixedThreadPool(64);

    public static void main(String[] args) throws IOException {
        try (ServerSocket s = new ServerSocket(PORT)) {
            System.out.println("Server Started");
            try {
                while (true) {
                    Socket socket = s.accept();
                    try {
                        executeIt.execute(new Server(socket));
                    } catch (IOException e) {
                        socket.close();
                    }
                }
            } finally {
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


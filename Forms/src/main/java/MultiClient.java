import java.net.*;

import java.io.*;

class ClientThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static int counter = 0;
    private int id = counter++;
    private static int threadcount = 0;

    public static int threadCount() {
        return threadcount;
    }

    public ClientThread(InetAddress addr) throws IOException {
        System.out.println("Making client " + id);
        threadcount++;

        try (Socket socket = new Socket(addr, MultiThreadServer.PORT);
             var in = new BufferedReader(new InputStreamReader(socket
                     .getInputStream()));
             var out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                     socket.getOutputStream())), true);) {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void run() {
        try {
            //for (int i = 0; i < 25; i++) {
            out.println("Client " + id);
            String str = in.readLine();
            System.out.println(str);
            //}
            out.println("END");
        } catch (IOException e) {
            System.err.println("IO Exception");
        } finally {

            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Socket not closed");
            }
            threadcount--;
        }
    }
}

public class MultiClient {
    //static final int MAX_THREADS = 4;

    public static void main(String[] args) throws IOException,
            InterruptedException {
        InetAddress addr = InetAddress.getByName("localhost");
        while (true) {
            new ClientThread(addr);
            Thread.currentThread().sleep(100);
        }
    }
}

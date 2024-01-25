import java.io.*;
import java.net.*;

public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    private void start() {
        while(!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("SERVER: W3lc0m3!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            } catch(IOException e) {

            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        Server server = new Server(serverSocket);
        server.start();
    }
}

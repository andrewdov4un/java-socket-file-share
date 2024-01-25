import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    private static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        addClientHandler();
        String userCommand;
        String[] words;
        File file = new File("src/data");
        File[] files = initialiseFiles(file);

        while(socket.isConnected()) {
            try {
                userCommand = bufferedReader.readLine(); // waits???
                words = userCommand.split(" ");
                if (words.length == 2 && words[0].equals("FILE") && words[1].equals("LIST")) {
                    if(file.isDirectory()) {
                        for(File fileToCheck : files) {
                            if(getFileExtension(fileToCheck).equals("txt")) {
                                writeToBufferedWriter(bufferedWriter, fileToCheck.getName());
                            }
                        }
                    }
                } else if (words[0].equals("GET")) {
                    String filename = userCommand.substring(4);
                    file = new File("src/data/" + filename);
                    if(isInFiles(file, files)) {
                        try (BufferedReader br = new BufferedReader(new FileReader("src/data/" + words[1]))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                                // im going to send data to each client individually
                                writeToBufferedWriter(bufferedWriter, line);
                            }
                        }
                    } else {
                        writeToBufferedWriter(bufferedWriter, "NO SUCH FILE!");
                    }
                } else {
                    writeToBufferedWriter(bufferedWriter, "NO SUCH COMMAND!");
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (socket != null)
                socket.close();
            if (bufferedReader != null)
                bufferedReader.close();
            if (bufferedWriter != null)
                bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean isInFiles(File file, File[] files) {
        for(File f : files) {
            if (f.equals(file)) return true;
        }
        return false;
    }
    private void writeToBufferedWriter(BufferedWriter bufferedWriter, String message) {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private String getFileExtension(File file) {
        int dotIndex = file.getName().lastIndexOf('.');
        return file.getName().substring(dotIndex + 1);
    }

    private File[] initialiseFiles(File path) {
        File[] files = path.listFiles();
        return files;
    }

    private void removeClientHandler() {
        clientHandlers.remove(this);
    }
    private void addClientHandler() {
        clientHandlers.add(this);
    }
}

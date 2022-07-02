package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerTask implements Runnable {
    private final Socket socket;
    private final Server server;
    private final DB db;
    ServerTask(Socket socket, Server server, DB db) {
        this.socket = socket;
        this.server = server;
        this.db = db;
    }

    @Override
    public void run() {
        try (DataInputStream inputStream = new DataInputStream(this.socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(this.socket.getOutputStream())) {

            String input = inputStream.readUTF();
            //System.out.println(input);

            if ("EXIT".equals(input)) {
                this.server.stop();
                return;
            }

            Pattern storePattern = Pattern.compile("PUT( [^ ]+)?");
            Matcher storeMatcher = storePattern.matcher(input);

            if (storeMatcher.find()) {
                this.createFile(storeMatcher, inputStream, outputStream);
                return;
            }

            Pattern pattern = Pattern.compile("(GET|DELETE) (BY_ID|BY_NAME) ([^ ]+)");
            Matcher matcher = pattern.matcher(input);


            if (matcher.find()) {
                if ("GET".equals(matcher.group(1))) {
                    this.getFile(matcher, outputStream);
                    return;
                }

                this.deleteFile(matcher, outputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createFile(Matcher matcher, DataInputStream inputStream, DataOutputStream outputStream) throws IOException {
        String fileName = null;
        StringBuilder response = new StringBuilder("200");

        if ("PUT".equals(matcher.group().trim())) {
            fileName = Thread.currentThread().getId() + "_" + System.currentTimeMillis();
        }

        if (fileName == null) {
            fileName = matcher.group(1).trim();
        }

        int length = inputStream.readInt();
        byte[] data = new byte[length];
        inputStream.readFully(data, 0, data.length);

        FileManager fileManager = new FileManager(fileName, this.db);

        try {
            response.append(" ").append(fileManager.create(data));
        } catch (Exception e) {
            response.setLength(0);
            response.append("403");
        } finally {
            outputStream.writeUTF(response.toString());
            //System.out.println(response);
        }
    }

    private void getFile(Matcher matcher, DataOutputStream outputStream) throws IOException {
        try {
            String fileName = matcher.group(3);

            if ("BY_ID".equals(matcher.group(2))) {
                fileName = this.db.getFileName(Integer.parseInt(matcher.group(3), 10));
            }

            FileManager fileManager = new FileManager(fileName, this.db);

            byte[] data = fileManager.getContent();

            outputStream.writeUTF("200");
            outputStream.writeInt(data.length);
            outputStream.write(data);
        } catch (Exception e) {
            outputStream.writeUTF("404");
        }
    }

    private void deleteFile(Matcher matcher, DataOutputStream outputStream) throws IOException {
        try {
            String fileName = matcher.group(3);

            if ("BY_ID".equals(matcher.group(2))) {
                fileName = this.db.getFileName(Integer.parseInt(matcher.group(3), 10));
            }

            FileManager fileManager = new FileManager(fileName, this.db);

            fileManager.delete();

            outputStream.writeUTF("200");
        } catch (Exception e) {
            outputStream.writeUTF("404");
        }
    }

}

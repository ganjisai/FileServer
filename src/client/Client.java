package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    static final String SOURCE_PATH = System.getProperty("user.dir") + "/src/client/data";
    private final String ipAddress;
    private final int port;
    private final Scanner scanner;
    private Method method;
    private String sourceFileName;
    private String targetFileName;
    private int fileId;

    Client(String ipAddress, int port, Scanner scanner) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.scanner = scanner;
    }

    void send() {
        try (Socket socket = new Socket(InetAddress.getByName(this.ipAddress), this.port);
             DataInputStream inputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream())) {

            StringBuilder request = new StringBuilder(this.method.name());

            if (this.method == Method.PUT && this.targetFileName != null) {
                request.append(" ").append(this.targetFileName);
            }

            if (this.method == Method.GET || this.method == Method.DELETE) {
                if (this.targetFileName != null) {
                    request.append(" BY_NAME ").append(this.targetFileName);
                } else {
                    request.append(" BY_ID ").append(this.fileId);
                }
            }

            outputStream.writeUTF(request.toString());

            if (method == Method.PUT) {
                File file = new File(SOURCE_PATH + "/" + this.sourceFileName);
                outputStream.writeInt((int) file.length());
                outputStream.write(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            }

            System.out.println("The request was sent.");

            if (this.method == Method.EXIT) {
                return;
            }

            Response response = new Response(inputStream.readUTF());

            if (response.isNotFound()) {
                System.out.println("The response says that this file is not found!");
                return;
            }

            if (response.isForbidden()) {
                System.out.println("The response says that creating the file was forbidden!");
                return;
            }

            if (method == Method.GET) {
                System.out.print("The file was downloaded! Specify a name for it: ");
                String fileName = this.scanner.nextLine();

                try (
                        FileOutputStream fileOutputStream = new FileOutputStream(SOURCE_PATH + "/" + fileName);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)
                ) {
                    int length = inputStream.readInt();
                    byte[] data = new byte[length];
                    inputStream.readFully(data, 0, data.length);

                    bufferedOutputStream.write(data);
                }

                System.out.println("File saved on the hard drive!");
                return;
            }

            if (method == Method.PUT) {
                System.out.println("Response says that file is saved! ID = " + response.getData());
                return;
            }

            System.out.println("The response says that the file was successfully deleted!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void setMethod(Method method) {
        this.method = method;
    }

    void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    void setFileId(int id) {
        this.fileId = id;
    }
}
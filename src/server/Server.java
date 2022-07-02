package server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final static String SERIALIZED_DB_PATH = System.getProperty("user.dir") + "/src/server/db";
    private final static String SERIALIZED_DB_FILE_PATH = SERIALIZED_DB_PATH + "/serializedDb.ser";
    private final String ipAddress;
    private final int port;
    private final DB db;
    private final ExecutorService pool;
    private boolean run = false;
    private ServerSocket serverSocket;

    Server(String ipAddress, int port) throws IOException, ClassNotFoundException {
        this.ipAddress = ipAddress;
        this.port = port;
        this.db = deserializeDb();
        this.pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    void start() throws IOException {
        if (!new File(SERIALIZED_DB_PATH).exists()) {
            Files.createDirectories(Paths.get(SERIALIZED_DB_PATH));
        }

        this.run = true;
        this.serverSocket = new ServerSocket(this.port, 50, InetAddress.getByName(this.ipAddress));

        System.out.println("Server started!");

        while (this.run) {
            try {
                Socket socket = serverSocket.accept();
                this.pool.submit(new ServerTask(socket, this, this.db));
            } catch (SocketException ignored) {}
        }
    }

    void stop() throws IOException {
        this.run = false;

        this.pool.shutdownNow();
        this.serverSocket.close();
    }

    synchronized static void serializeDb(DB db) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(SERIALIZED_DB_FILE_PATH);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)) {

            objectOutputStream.writeObject(db);
        }
    }

    private static DB deserializeDb() throws IOException, ClassNotFoundException {
        if (!new File(SERIALIZED_DB_FILE_PATH).exists()) {
            return new DB();
        }

        Object db;

        try (FileInputStream inputStream = new FileInputStream(SERIALIZED_DB_FILE_PATH);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream)) {

            db = objectInputStream.readObject();
        } catch (InvalidClassException e) {
            return new DB();
        }

        return (DB) db;
    }
}

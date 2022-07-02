package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File dbPath = new File(FileManager.FILE_PATH);
        if (!dbPath.exists()) {
            Files.createDirectories(Paths.get(FileManager.FILE_PATH));
        }

        Server server = new Server("127.0.0.1", 23456);
        server.start();
    }
}

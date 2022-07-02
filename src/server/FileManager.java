package server;

import java.io.*;

public class FileManager {
    final static String FILE_PATH = System.getProperty("user.dir") + "/src/server/data";
    private final File file;
    private final DB db;

    FileManager(String fileName, DB db) {
        this.file = new File(FILE_PATH + "/" + fileName);
        this.db = db;
    }

    byte[] getContent() throws IOException {
        if (!this.file.exists()) {
            throw new FileNotFoundException();
        }

        byte[] data;

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(this.file))) {
            data = inputStream.readAllBytes();
        }

        return data;
    }

    synchronized void delete() throws Exception {
        if (!this.file.exists()) {
            throw new FileNotFoundException();
        }

        if (this.file.delete()) {
            this.db.remove(this.db.getId(this.file.getName()));
            Server.serializeDb(this.db);
        }
    }

    synchronized int create(byte[] data) throws Exception {
        if (this.file.exists()) {
            throw new Exception("File has already exists.");
        }
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.file))) {
            bufferedOutputStream.write(data);
        }

        int fileId = this.db.storeFile(this.file);
        //System.out.println(fileId);
        Server.serializeDb(this.db);

        return fileId;
    }

}

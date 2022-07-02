package server;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DB implements Serializable {

    private final Map<Integer, String> store = new HashMap<>();

    private int autoIncrement = 1;

    synchronized int storeFile(File file) {
        this.store.put(this.autoIncrement, file.getName());

        return this.autoIncrement++;
    }

    String getFileName(int id) throws Exception {
        if (!this.store.containsKey(id)) {
            throw new Exception("ID does not exist");
        }

        return this.store.get(id);
    }

    int getId(String fileName) throws  Exception {
        if (!this.store.containsValue(fileName)) {
            throw new Exception("File name does not exist");
        }

        return this.store.entrySet().stream()
                .filter(entry -> fileName.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .reduce((first, second) -> first)
                .orElseThrow();
    }

    synchronized void remove(int id) throws Exception {
        if (!this.store.containsKey(id)) {
            throw new Exception("ID does not exist");
        }

        this.store.remove(id);
    }

    void showAll() {
        for (Map.Entry<Integer, String> file : store.entrySet()) {
            System.out.println(file.getKey() + ":" + file.getValue());
        }
    }
}

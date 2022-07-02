package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        if (!new File(Client.SOURCE_PATH).exists()) {
            Files.createDirectories(Paths.get(Client.SOURCE_PATH));
        }

        Client client = new Client("127.0.0.1", 23456, scanner);

        System.out.print("Enter action (1 - get a file, 2 - save a file, 3 - delete a file): ");

        String input = scanner.nextLine();
        Method method = Method.getByAction("exit".equals(input) ? 4 : Integer.parseInt(input, 10));

        if (method == null) {
            return;
        }

        client.setMethod(method);

        if (method == Method.EXIT) {
            client.send();
            return;
        }

        if (method == Method.PUT) {
            System.out.print("Enter name of the file: ");
            client.setSourceFileName(scanner.nextLine());

            System.out.print("Enter name of the file to be saved on server: ");
            String targetFile = scanner.nextLine().trim();

            if (!targetFile.isEmpty()) {
                client.setTargetFileName(targetFile);
            }

            client.send();
            return;
        }

        if (method == Method.GET || method == Method.DELETE) {

            System.out.print("Do you want to " + (method == Method.GET ? "get" : "delete") + " the file by name or by id (1 - name, 2 - id): ");

            input = scanner.nextLine();

            if ("1".equals(input)) {
                System.out.print("Enter the name of the file: ");
                client.setTargetFileName(scanner.nextLine());
            }

            if ("2".equals(input)) {
                System.out.print("Enter id: ");
                client.setFileId(Integer.parseInt(scanner.nextLine(), 10));
            }
        }
        client.send();
    }
}

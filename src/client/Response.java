package client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Response {
    private int statusCode;
    private String data;

    Response(String responseString) {
        this.parse(responseString);
    }

    boolean isSuccessful() {
        return this.statusCode == 200;
    }

    boolean isForbidden() {
        return this.statusCode == 403;
    }

    boolean isNotFound() {
        return this.statusCode == 404;
    }

    String getData() {
        return this.data;
    }

    private void parse(String responseString) {
        Pattern pattern = Pattern.compile("(\\d{3})(.*)");
        Matcher matcher = pattern.matcher(responseString);

        if (!matcher.find()) {
            System.out.println("Invalid response");
            return;
        }

        this.statusCode = Integer.parseInt(matcher.group(1), 10);
        this.data = matcher.group(2).trim();
    }
}

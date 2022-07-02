package client;

public enum Method {
    GET(1),
    PUT(2),
    DELETE(3),
    EXIT(4);

    private final int action;

    static Method getByAction(int action) {
        for (Method method : Method.values()) {
            if (action == method.getAction()) {
                return method;
            }
        }

        return null;
    }

    Method(int action) {
        this.action = action;
    }

    int getAction() {
        return this.action;
    }
}

package org.itmo.testing.lab2;

import io.javalin.Javalin;
import org.itmo.testing.lab2.controller.UserAnalyticsController;

public class Main {

    public static void main(String[] args) {
        int port = 1234;
        Javalin app = UserAnalyticsController.createApp();
        app.start(port);
    }
}

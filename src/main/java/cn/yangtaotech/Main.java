package cn.yangtaotech;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        run();
    }

    private static void run() {
        Client client = new Client();
        try {
            client.run();
        } catch (IOException e) {
            System.out.println("client read data err!");
            e.printStackTrace();
        }
    }
}

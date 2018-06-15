package com.example.shaniherskowitz.slide;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection extends Thread{

    private Socket socket;
    private Scanner scanner;

    public ClientConnection() throws Exception {


    }

    public void run() {
        try {
            this.socket = new Socket("10.0.2.2", 9000);
            this.scanner = new Scanner(System.in);
            System.out.println("\r\nConnected to Server: " + "10.0.2.2");
            start1();
        } catch(Exception e) {

        }

    }

    public void start1() throws IOException {
        String input;
        while (true) {
            input = scanner.nextLine();
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(input);
            out.flush();
        }
    }

}

package com.example.shaniherskowitz.slide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientConnection extends Thread {

    public Socket socket;
    private Scanner scanner;
    DataOutputStream dOut;

    /**
     * Creates the client connection
     */
    public ClientConnection() {


    }

    /**
     * Runs the client
     */
    public void run() {
        try {
            //creates a socket for the client
            this.socket = new Socket("10.0.2.2", 9000);
            this.scanner = new Scanner(System.in);
            System.out.println("\r\nConnected to Server: " + "10.0.2.2");
            //transfer images to the server
            startTransfer();
        } catch(Exception e) {
        }
    }

    /**
     *
     * @throws IOException
     */
    public void start1() throws IOException {
        String input;
        while (true) {
            input = scanner.nextLine();
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            out.println(input);
            out.flush();
        }
    }

    /**
     * Gets the pictures to transfer to the server
     */
    public void startTransfer() {
        getPics();
    }


    /**
     * Send images to the server
     * @param imgbyte - the image want to send
     * @param pic - file want to send
     */
    public void connect(byte[] imgbyte, File pic) {
        try {
            try {

//                OutputStream output = socket.getOutputStream();
//                FileInputStream fis = new FileInputStream(pic);
//
//                String encodedImage = Base64.encodeToString(imgbyte , Base64.DEFAULT);
//                PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
//                out.println(encodedImage);
//                out.flush();



                dOut.writeInt(imgbyte.length); // write length of the message
                dOut.write(imgbyte);
                //output.write(imgbyte);
                //output.flush();

            } catch (Exception e) {
                Log.e("TCP", "S:Error", e);
            } finally {
                //socket.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }
    }


    /**
     * Convert the image to bytes
     * @param pic - the picture want to convet
     * @return - image converted to bytes
     */
    public byte[] picToByte(File pic) {
        try {
            FileInputStream fis = new FileInputStream(pic);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            return getBytesFromBitmap(bm);
        } catch(Exception e) {
            Log.e("TCP", "C: Error", e);
        }
        return null;
    }

    /**
     * Gets teh bytes from bitmap
     * @param bitmap - the bitmap
     * @return an array of bytes
     */
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
        return stream.toByteArray();
    }

    /**
     * Gets the pictures from the dcim file
     */
    public void getPics() {
        // Getting the Camera Folder
        File dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        if(dcim == null) return;
        try {
            File[] pics = dcim.listFiles();
            int count = 0;
            dOut = new DataOutputStream(socket.getOutputStream());
            dOut.writeInt(pics.length); // write length of the message
            if (pics != null) {
                File pictures = pics[0];
                for (File pic : pics) {
                    byte[] byte_pic = picToByte(pic);
                    //calls connect with the picture in bytes and the file
                    connect(byte_pic, pic);
                }
            }
        } catch(Exception e) {
            Log.e("TCP", "C: Error", e);
        } finally {
            try {
                socket.close();
            } catch (Exception d) {

            }

        }
    }
}

package com.suas.uxdual;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import static android.content.ContentValues.TAG;

public class TcpClient {

    public static final String SERVER_IP = "192.168.2.220";//"10.0.2.2"; //your computer IP address
    public static final int SERVER_PORT = 2018;//4444;
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    //private PrintWriter mBufferOut;
    public PrintWriter mBufferOut;
    //public PrintStream mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;
    public int where = 0;
    boolean SocketClosed = false;
    boolean ClientStopped = false;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    /*public void sendMessage(String message) {
        //if (mBufferOut != null && !mBufferOut.checkError()) {
        mBufferOut.println(message);
        mBufferOut.flush();
        //}
    }*/

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    void sendMessage(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    //Log.d(TAG, "Sending: " + message);
                    mBufferOut.print(message);
                    mBufferOut.flush();
                }
            }
        }).start();
    }


    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send message that we are closing the connection
        //sendMessage(Constants.CLOSED_CONNECTION + "VuIR");

        mRun = false;

        if (mBufferOut != null) {
            //mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
        SocketClosed = true;
        ClientStopped = true;
    }

    public void run() {

        mRun = true;
        Socket socket;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            //InetAddress serverAddr = InetAddress.getByAddress(SERVER_IP);

            //Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, SERVER_PORT);
            socket.setTcpNoDelay(true);

            try {

                //sends the message to the server
                //mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferOut = new PrintWriter(socket.getOutputStream());


                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // send login name
                //sendMessage(Constants.LOGIN_NAME);
                int counter = 0;

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                        //counter++;
                        //sendMessage(new String("Here it is " + counter));
                    }
                }
                SocketClosed = false;

                //Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                //Log.e("TCP", "S: onProgressUpdate Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.\
                Log.i(TAG, "run: onProgressUpdate socket closed");
                socket.close();
                SocketClosed = true;
            }

        } catch (Exception e) {
            //Log.e("TCP", "C: Error onProgressUpdate", e);
        }
    }

    //Declare the interface. The method messageReceived(String message) will have to be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}


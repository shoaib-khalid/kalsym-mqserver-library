package com.kalsym.mqServer;

import java.io.UnsupportedEncodingException;
import org.jeromq.ZMQ;

/**
 *
 * @author Ali Khan
 */
public class JeraServer {

    //  final private static org.slf4j.Logger LogProperties.log = LoggerFactory.getLogger(JeraServer.class.getName());
    final private String responseIP;
    final private int responsePort;
    final private int recievePort;

    /**
     * parametrised constructor: sets ip, response port and receive port of jera
     * server
     *
     * @param responseIP
     * @param responsePort
     * @param recievePort
     */
    public JeraServer(String responseIP, int responsePort, int recievePort) {
        this.responseIP = responseIP;
        this.responsePort = responsePort;
        this.recievePort = recievePort;
    }

    /**
     * Starts server at provide port
     *
     * @param listenPort
     */
    public static void startServer(int listenPort) {
        try {
            ZMQ.Context context = ZMQ.context(1);

            ZMQ.Socket responder = context.socket(ZMQ.DEALER);
            responder.bind("tcp://*:" + listenPort);

            //LogProperties.WriteLog("[JeraServer] Started on port: " + listenPort);
            while (!Thread.currentThread().isInterrupted()) {
                // Wait for next request from the client
                byte[] request = responder.recv(0);

                try {
                    final String strReq = new String(request, "UTF-8");
                    Thread reqThread = new Thread(new Runnable() {
                        public void run() {
                            //LogProperties.WriteLog("Request recieved: " + strReq);
                            RequestFactory rf = new RequestFactory(strReq);
                            rf.run();
                        }
                    });
                    reqThread.start();
                } catch (UnsupportedEncodingException ex) {
                    //LogProperties.WriteLog("Error Identifying request" + ex);
                }

                try {
                    // Do some 'work'
                    Thread.sleep(500);
                    //  responder.close();
                    // Send reply back to client
                    // String reply = "World";
                    // responder.send(reply.getBytes(), 0);
                } catch (InterruptedException ex) {
                    //LogProperties.WriteLog("Error Identifying request" + ex);
                }
            }
            responder.close();
            context.term();
            //LogProperties.WriteLog("[JeraServer] Stopped");
        } catch (Exception exp) {
            //LogProperties.WriteLog("Exception in starting server" + exp);
        }
    }

    /**
     * sends message to provided ip and port
     *
     * @param message
     * @param ip
     * @param port
     */
    public static void send(String message, String ip, int port) {
        ZMQ.Context context = ZMQ.context(1);

        // Socket to talk to server
        System.out.println("Connecting to server…");
        //LogProperties.WriteLog("Sending response: " + message);
        ZMQ.Socket requester = context.socket(ZMQ.DEALER);
        //requester.connect("tcp://localhost:5555");
        String connectStr = "tcp://" + ip + ":" + port;
        //requester.connect("tcp://localhost:5555");
        requester.connect(connectStr);
        requester.send(message.getBytes(), 0);

//        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {
//            String request = "Hello " + requestNbr;
//            System.out.println("Sending Hello " + requestNbr);
//            requester.send(request.getBytes(), 0);
//        }
        requester.close();
        context.term();
    }

    /**
     * start server
     */
    public void startServer() {
        startServer(this.recievePort);
    }

    /**
     * Sends message to response ip and port set in while initializing
     *
     * @param RefID
     * @param message
     */
    public void send(String RefID, String message) {
        try {
            ZMQ.Context context = ZMQ.context(1);
            System.out.println("Connecting to server…");
            //LogProperties.WriteLog("[" + RefID + "] Sending: " + message);
            ZMQ.Socket requester = context.socket(ZMQ.DEALER);
            String connectStr = "tcp://" + this.responseIP + ":" + this.responsePort;
            requester.connect(connectStr);
            requester.send(message.getBytes(), 0);
            requester.close();
            context.term();
        } catch (Exception exp) {
            //LogProperties.WriteLog("Error in SendResponse" + exp);
        }
    }
}

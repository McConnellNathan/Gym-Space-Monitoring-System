package main.java.utility;


import main.java.protocol.Envelope;
import main.java.protocol.Msg;
import main.java.protocol.ReplyChannel;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SocketListener is a class we used to encapsulate listing on an input stream
 * and then adding the message from that stream to a blocking queue to bottleneck
 * the flow of messages to be handled by a singular thread. This class by default
 * will correlate the queue to the static queue in processor class, but
 * there is a constructor that allows for an optional bring your own queue to the
 * party functionality.
 *
 * Author: Nathan McConnell
 */


public class Listener implements Runnable, ReplyChannel {


    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private String name;

    private LinkedBlockingQueue<Envelope> queue = null;

    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * The SocketListener constructor takes in a input stream and connects
     * its blocking queue to Processor's static blocking queue, it also
     * adds itself to processors list of listeners.
     * @param socket expects a open socket
     */
    public Listener(Socket socket, Server receiver) throws IOException {
        //this is the queue from the interface, all listeners will be
        //adding to the same queue from the interface for the processor
        this.socket = socket;
        this.queue = receiver.getMailBox();
        // CRITICAL: output first + flush to avoid handshake deadlock
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();

        this.in = new ObjectInputStream(socket.getInputStream());


    }


    /**
     * The SocketListeners implementation of run first labels the current thread
     * as a SocketListener thread, and then loops infinitely blocking trying
     * to read a message and then add the message form the input stream to the
     * blocking queue. It also if reading a disconnect message disconnects itself
     */
    public void run() {
        name = "Listener-" + socket.getRemoteSocketAddress();
        Thread.currentThread().setName(name);

        try {
            while (running.get()) {

                    //this will block when trying to read
                    Object obj = in.readObject();

                    if (!(obj instanceof Msg)) {
                             throw new IllegalStateException("Not a valid message");
                        }

                    Msg msg = (Msg)obj;
                    queueMessage(msg);


//                    todo make this a shudown message not disconnecting
                    if (msg instanceof Msg.DisconnectMsg disconnectMsg) {
                        //put a sleep here so this can still be called but for programs
                        //that need it but for ones that don't
                        Thread.sleep(500);
                        System.out.println("Disconnected From: \n\t" + disconnectMsg.disconnectMsg());
                        close();
                        break;
                    }

            }
        } catch (EOFException | SocketException e) {
            System.out.println("Client disconnected");
        }
            catch (Exception e) {
            e.printStackTrace();
        }
            finally {
            close();
        }

        System.out.println(Thread.currentThread().getName() + " Finished");
    }


    /**
     * queueMessage allows for child classes to add additionally preprocessing to a message
     * if needed before sending it to the main processor.
     * @param message
     */
    protected void queueMessage(Msg message) {
        try {
            queue.put(new Envelope(message, this));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a message back to the connected client.
     *
     * This method is synchronized to prevent multiple threads from writing
     * to the same ObjectOutputStream at the same time, which would corrupt
     * the stream.
     *
     * The stream is flushed to ensure the message is immediately transmitted.
     * reset() is called to prevent Java serialization from reusing cached
     * object references when sending multiple messages.
     *
     * @param msg the message to send to the client
     * @throws IOException if the connection is no longer valid
     */
    @Override
    public synchronized void send(Msg msg) throws IOException {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset(); // prevents object reference caching surprises
        }  catch (IOException e) {
        System.err.println("Error writing to output Listener Stream ");
        throw new RuntimeException(e);
            }
    }

    /**
     * Closes this connection and stops the listener.
     *
     * Setting running to false ends the read loop, and closing the socket
     * safely terminates all underlying streams and unblocks any pending
     * read operations.
     */
    @Override
    public void close() {
        try {
            send(new Msg.DisconnectMsg(name + "Disconnected"));
        } catch (IOException ignored) {}
        running.set(false);
        try {
            socket.close();   // closes EVERYTHING safely
        } catch (IOException ignored) {}
    }

}

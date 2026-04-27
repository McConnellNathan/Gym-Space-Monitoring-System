package main.java.utility;

import main.java.protocol.Msg;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Small object-stream client for talking to GSMS servers over sockets.
 */
public class RemoteMessageClient implements Closeable {

    private final String host;
    private final int port;

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public RemoteMessageClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(host, port);
        this.socket.setTcpNoDelay(true);

        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    public RemoteMessageClient(String host, int port, Msg initialMessage) throws IOException {
        this(host, port);
        if (initialMessage != null) {
            send(initialMessage);
        }
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public synchronized void send(Msg msg) throws IOException {
        out.writeObject(msg);
        out.flush();
        out.reset();
    }

//    Todo probably want to reconsider this reading capablilites perhaps making it a thread that is listting on the inpurt
//      stream for new messages and then processing them somehow?
//      Perhaps a process message function could be passed in as a paramerter on how it handles messages?
    public synchronized Msg read() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if (!(obj instanceof Msg msg)) {
            throw new IOException("Received non-protocol object from remote server");
        }
        return msg;
    }

    public synchronized Msg sendAndRead(Msg msg) throws IOException, ClassNotFoundException {
        send(msg);
        return read();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}

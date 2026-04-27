package utility;

import protocol.Envelope;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processor is an abstract class designed to encapsulate a common pattern where
 * a thread will read from a blocking queue and then process the message. This
 * class is designed to work hand in hand with the SocketListener class that
 * listens on an input stream and adds the message received to a processors
 * blocking queue. It expects with extension for the process message method
 * to be implemented. Additionally, it allows for getMessage to be overwritten in
 * case a class decides it wants to use a different queue than the one built in.
 *
 * Author: Nathan McConnell
 *
 */
public abstract class Server implements Runnable, AutoCloseable {

    //only one class in each program should implement this interface because
    //that will be the blocking queue is static so that the socketListener can
    //see it automatically, also it is implicitly static from the interface
    //if classes wish to have an instance of a blocking queue then they must
    //Overwrite GetMessage()
    private LinkedBlockingQueue<Envelope> mailBox = new LinkedBlockingQueue<>();

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final CountDownLatch terminated = new CountDownLatch(1);

    private ServerSocket server = null;

//    this is the exact task for the accepting loop
    private Future<?> acceptTask;

//    switched to a thread safe list
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();


    //    This executor will handle all thread tasking from the processor
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * getMailBox returns the instance of the processor class's queue
     * @return the static mailBox
     */
    public LinkedBlockingQueue<Envelope> getMailBox() {
        return mailBox;
    }


    /**
     * processMessage is a abstract method that the main thread calls passing
     * each message it receives from its queue to. The implementation of this
     * method is expected to parse messages in whatever way fits the situation.
     * Example:
     * processMessage(Envelope env)
     *  Msg msg = env.msg();
     *
     *     if (msg instanceof Msg.Ping cmd) {
     *         try {
     *             env.replyTo().send(new Msg.Pong());
     *         } catch (IOException e) {
     *             env.replyTo().close();
     *         }
     *     }
     * @param env is an envelope record that contains env.msg but allows env.replyTo().send(Msg) to respond
     */
    public abstract void processMessage(Envelope env);


    /**
     * Create the inital set up of the server for the Processor waiting to be started
     * @param port give server listening port
     */
    public Server(int port) {
        try {
            this.server = new ServerSocket(port);
            this.server.setReuseAddress(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * After creating a Processor and calling start it will begin running itself and managing the
     * message handling thread along with starting the server to be listened on
     */
    public void start() {
        executor.submit(this);
        startAccepting();
    }

    /**
     * Processors implementation of runnable run method loops infinitely until
     * stopRunning() is called. Within this loop it waits till it gets a message
     * and then once a message has been received it will process the message
     */
    public void run() {
        while (running.get()) {
            try {
                Envelope env = getMessage();
                if (env != null) processMessage(env); // this was called twice so I removed one/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(Thread.currentThread().getName() + " Finished");
    }

    /**
     * getMessage waits to get a message from the queue for 1 second
     * @return a message if received or null if not received anything for a
     * second
     * @throws InterruptedException
     */
    public Envelope getMessage() throws InterruptedException {
        return mailBox.poll(1, TimeUnit.SECONDS);
    }

    /** Kick off the accept loop. Consider calling this from outside instead of constructor. */
    protected void startAccepting() {
        // prevent double-start
        if (acceptTask != null) return;

        acceptTask = executor.submit(() -> {
            System.out.printf("Waiting for connections...");
            while (running.get()) {
                try {
                    Socket socket = server.accept(); // blocks
                    socket.setTcpNoDelay(true);

                   createListener(socket);

                } catch (SocketException se) {
                    // server.close() during shutdown causes this: that's the exit signal
                    if (running.get()) System.err.println("Accept loop socket error: " + se.getMessage());
                    break;
                } catch (IOException ioe) {
                    if (running.get()) System.err.println("Accept loop IO error: " + ioe.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println(e.getMessage());
                } catch (RuntimeException re) {
                    System.err.println("Accept loop crashed: " + re);
                }

            }
        });
    }


    /**
     * addListener adds a Socket Listener to the static listener list. Ideally
     * this socket listener is a listener adding information to the static
     * mailBox's queue
     * @param socket is the socket stream to be listened on by the listener
     */
    protected void createListener(Socket socket) throws IOException {
        addListener(new Listener(socket , this));
    }

    /**
     * addListener adds a listener to the processor list and then
     * starts the listener thread via submitting it to the executor.
     * @param listener
     */
    protected void addListener(Listener listener) {
        listeners.add(listener);
        submit(listener);
    }

    /**
     * Stop running will make it so when the queue times out if not given a final
     * message it will stop running the loop and the thread will finish. Additionally,
     * it will track down all socket listeners and close their sockets/ allow
     * their respective threads to also finish.
     */
    public void stopRunning() {
        running.set(false);
        for (Listener listener : listeners) {
            listener.close();
        }
        close();
    }

    /**
     * Kills all Thread Tasks behind handled by the
     * executor.
     */
    public void close() {
        // Unblock accept()
        try { server.close(); } catch (IOException ignored) {}

        if (acceptTask != null) acceptTask.cancel(true);

        // Virtual threads are cheap; shutdownNow is basically ctr + c to all threads.
        executor.shutdownNow();

        terminated.countDown();
    }

    /**
     * submit starts a task on the executor and instantly returns a future
     * object which can be used to get any return statements from the task via f.get().
     * Note f.get() is equivalent to thread.join() and will block until task is finished.
     * @param task a runnable task (aka our listeners) to be added to the executor
     * @return Future<?></?>
     */
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    /**
     * setRunning allows for the while loop variable in the main run method to
     * be set to false.
     * @param run true means continue loop, false means stop.
     */
    public void setRunning(boolean run) {
        running.set(run);
    }

    public void awaitShutdown() throws InterruptedException {
        terminated.await();
    }

    public boolean isRunning() {
        return running.get();
    }


}

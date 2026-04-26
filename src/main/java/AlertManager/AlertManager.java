package main.java.AlertManager;

import main.java.protocol.Envelope;
import main.java.utility.Server;


public class AlertManager extends Server {


    /**
     * Create the inital set up of the server for the Processor waiting to be started
     *
     * @param port give server listening port
     */
    public AlertManager(int port) {
        super(port);
    }

    @Override
    public void processMessage(Envelope env) {

    }
}
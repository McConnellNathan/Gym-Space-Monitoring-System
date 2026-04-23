package protocol;

import java.io.IOException;

/**
 * ReplyChannel represents a way for the Server to send a message back to
 * the client that originally sent a request.
 *
 * Instead of exposing sockets or output streams to the Server, each
 * connection provides a ReplyChannel implementation that knows how to send
 * data back across that specific connection.
 *
 * When a message is received, the Listener attaches its ReplyChannel to the
 * message (via an Envelope) before placing it into the Server's mailbox.
 * This allows the Server to easily respond without needing to track
 * connections or manage networking details.
 *
 * In short:
 *      Sever handles logic
 *      ReplyChannel handles communication
 *
 * Typical usage:
 *      env.replyTo().send(responseMsg);
 *
 * Author: Nathan McConnell
 */

public interface ReplyChannel {
    void send(Msg msg) throws IOException;
    void close();
}
package protocol;

/**
 * Envelope is a small wrapper that pairs an incoming message with the
 * connection it came from.
 *
 * When a Listener receives a message from a client, it wraps the message
 * together with its ReplyChannel and places the Envelope into the
 * Processor's mailbox queue.
 *
 * This allows the Processor to:
 *   - process messages in one central place
 *   - know who sent the message
 *   - easily send a response back if needed
 *
 * Flow of data looks roughly like:
 *
 *      Client -> Listener -> Envelope -> Processor
 *                                         |
 *                                         v
 *                                   ReplyChannel
 *                                         |
 *                                         v
 *                                       Client
 *
 * This avoids needing global maps of clients or sockets and keeps networking
 * separate from application logic.
 *
 * Author: Nathan McConnell
 */

public record Envelope(Msg msg, ReplyChannel replyTo) { }
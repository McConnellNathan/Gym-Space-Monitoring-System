package main.java.protocol;

import java.io.Serializable;

/**
 * The Message class will have enums to send messages between the different lays of a system.
 * The messages need to be serializable objects so ideally only contain ints and strings.
 * Message information should be organized in the following form for different messages
 *
 * creating a message:
 *  Msg.FloorInformation info =
 *         new Msg.FloorInformation(10, 20, new int[]{1, 2, 3});
 * Msg msg =
 *         new Msg.FloorUpdateMsg(5, 42, info);
 *
 * Sending Messages:
 * ObjectOutPutStream.writeObject(msg)
 * ObjectOutPutStream.flush()
 *
 * Reading Messages:
 *
 * Object obj = in.readObject()
 * if (!(obj instanceof Message msg)) {
 *     throw new IllegalStateException("Not a valid message");
 * }
 *
 * Msg msg = (msg) obj
 *
 * switch(msg){
 *     case Msg.FloorUpdateMsg m -> {
 *         System.out.println("Update for id " + m.id());
 *     }
 *     case Msg.ErrorMsg e -> {
 *         System.out.println("Error: " + e.message());
 *     }
 * }
 *
 * Author: Nathan McConnell
 */




public interface Msg extends Serializable {

    public final int FREE_INT = 0;

    public final int OCCUPIED_INT = 1;

    public final boolean FREE_BOOL = false;

    public final boolean OCCUPIED_BOOL = true;

    public final int LIGHT_ERROR_CODE = -2;

    public final int SENSOR_ERROR_CODE = -1;

    record Ping() implements Msg {}

    record Pong() implements Msg {}

    /*
     *  Need Alert message with alert type (crictial, warning, informational), alert descriptions 
    * Messege for SoundMonitor, Ocupancy, Injury, Fall detection, Agression, tripHazard
    * 
    * GUI alert manager send update message to gui
    * 
    * Log store read/write request 
     */

    record FloorInformation(int id, int maxCapacity, int available, int[] floorData) implements Serializable {}

    record InitialFloorPing(FloorInformation info) implements Msg {}

    record FloorUpdateMsg(int change, int id, FloorInformation info) implements Msg {}

    record ParkingSpotUpdateMsg(int id, boolean isOccupied) implements Msg {}

    record ErrorMsg(int code, int spotID,String message) implements Msg {}

//    if the sensor driver has an error it sends the parking spot this message
    record SensorErrorMsg() implements Msg {}

    record SensorUpdateMsg(boolean isOccupied) implements Msg {}

    record ToggleLightMsg(boolean enabled) implements Msg {}

//    this disconnected message is for disconnecting sockets/streamn
//    NOT for light or sensor errors
    record DisconnectMsg(String disconnectMsg) implements Msg {}
}

package VEXnetDriver.utils;

import VEXnetDriver.VEXnetPacket;

/**
 * Code for quick building packets using the VEX
 * 
 * @author ScottWegley
 * @version 1.0
 */
public class PacketBuilder {
    public static VEXnetPacket FULL_STOP = VEXnetPacket.compileControllerPacket(
            (byte) (127), (byte) (127), (byte) (127), (byte) (127),
            false, false,
            false, false,
            false, false, false, false,
            false, false, false, false,
            (byte) 127, (byte) 127, (byte) 127);

    public static VEXnetPacket FULL_FORWARD = drive(255, 0);

    public static VEXnetPacket FULL_REVERSE = drive(0, 255);

    public static VEXnetPacket FULL_OPEN_CLAW = claw(255);

    public static VEXnetPacket FULL_CLOSE_CLAW = claw(0);

    /** Function used to construct packets that send only driving signals. */
    public static VEXnetPacket drive(int i1, int i2) {
        if (i1 <= 255 && i1 >= 0 && i2 <= 255 && i2 >= 0) {
            return VEXnetPacket.compileControllerPacket(
                    (byte) (i1), (byte) (i2), (byte) (127), (byte) (127),
                    false, false,
                    false, false,
                    false, false, false, false,
                    false, false, false, false,
                    (byte) 127, (byte) 127, (byte) 127);
        } else
            return FULL_STOP;
    }

    /** Function used to construct packets that send only arm control signals. */
    public static VEXnetPacket arm(int i1) {
        if (i1 <= 255 && i1 >= 0) {
            return VEXnetPacket.compileControllerPacket(
                    (byte) (127), (byte) (127), (byte) (i1), (byte) (127),
                    false, false,
                    false, false,
                    false, false, false, false,
                    false, false, false, false,
                    (byte) 127, (byte) 127, (byte) 127);
        } else
            return FULL_STOP;
    }

    /** Function used to construct packets that send only claw control signals. */
    public static VEXnetPacket claw(int i1) {
        if (i1 <= 255 && i1 >= 0) {
            return VEXnetPacket.compileControllerPacket(
                    (byte) (127), (byte) (127), (byte) (127), (byte) (i1),
                    false, false,
                    false, false,
                    false, false, false, false,
                    false, false, false, false,
                    (byte) 127, (byte) 127, (byte) 127);
        } else
            return FULL_STOP;
    }

    /**
     * Function used to construct packets that send signals to all current motors.
     */
    public static VEXnetPacket fullControl(int i1, int i2, int i3, int i4) {
        if (i1 <= 255 && i1 >= 0 && i2 <= 255 && i2 >= 0 && i3 <= 255 && i3 >= 0 && i4 <= 255 && i4 >= 0) {
            return VEXnetPacket.compileControllerPacket(
                    (byte) (i1), (byte) (i2), (byte) (i3), (byte) (i4),
                    false, false,
                    false, false,
                    false, false, false, false,
                    false, false, false, false,
                    (byte) 127, (byte) 127, (byte) 127);
        } else
            return FULL_STOP;
    }

    /**
     * Packet Parameters for
     * @see VEXnetPacket#compileControllerPacket(byte Ch1, byte Ch2, byte Ch3, byte
     *      Ch4, byte Ch5, byte Ch6,
     *      byte AccelY, byte AccelX, byte AccelZ)
     **/
    public enum PacketData {
        Ch1,
        Ch2,
        Ch3,
        Ch4,
        Btn5D,
        Btn5U,
        Btn6D,
        Btn6U,
        Btn7D,
        Btn7L,
        Btn7R,
        Btn7U,
        Btn8D,
        Btn8L,
        Btn8R,
        Btn8U,
        AccelY,
        AccelX,
        AccelZ
    }
}

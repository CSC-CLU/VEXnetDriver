/**
 * Code for communicating using the VEXnet
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 */
public class VEXnetPacket {

    /** Known packet types
     *
     * @see VEXnetPacket#VEXnetPacket(PacketType, byte... data)
     * @see VEXnetPacket#VEXnetPacket(byte type, byte size, boolean checksum, byte... data)
     * **/
    public enum PacketType {
        LCD_UPDATE(0x1E, 17),
        LCD_UPDATE_RESPONSE(0x16, 1),

        JOY_STATUS_REQUEST(0x3B, 0),
        JOY_STATUS_REQUEST_RESPONSE(0x39, 9),

        JOY_VERSION_REQUEST(0x3A, 0),
        JOY_VERSION_REQUEST_RESPONSE(0x3B, 2, false);

        /** the byte denoting the packet type.
         *
         * @see #get(byte type)
         * @see VEXnetPacket#type
         **/
        final byte type;

        /** the amount of bytes is sent with this packet type **/
        final byte size;

        /**
         * whether a checksum is expected to be provided at the end of some transmissions.
         *
         * @see VEXnetPacket#includeChecksum
         **/
        final boolean checksum;

        PacketType(int type, int size, boolean checksum) {
            this.type = (byte)type;
            this.size = (byte)size;
            this.checksum = checksum;
        }

        PacketType(int type, int size) { this(type, size, true); }

        /**
         * get packet type based on its {@link #type}
         * @param type Packet type
         * @return Packet type
         */
        public static PacketType get(byte type) {
            return get(type, (byte)0);
        }

        /** {@link #get(byte type) }, but excludes any types that cannot contain {@code size} **/
        static PacketType get(byte type, byte size) {
            for(PacketType t : values()) {
                if(t.type == type && t.size >= size) return t;
            }
            return null;
        }

        /** get the type corresponding to the packet **/
        static PacketType get(VEXnetPacket packet) {
            return get(packet.type, packet.size);
        }
    }

    /**
     * The byte denoting this packet's attributes. It is received right after the two sink bytes.
     *
     * @see PacketType#type **/
    byte type;
    /** the amount of bytes that are expected to be contained by this packet **/
    byte size;
    /** the contents of the packet **/
    byte[] data;
    boolean includeChecksum = true;

    /**
     * @see #VEXnetPacket(byte type, byte size, byte... data)
     * @param type Packet type
     */
    VEXnetPacket(byte type) { this(type, (byte) 0); }

    /**
     * Calls {@link #VEXnetPacket(byte type, byte size, boolean checksum, byte... data)} with information derived from {@code type}
     * @param type Packet type
     * @param data Packet data
     */
    public VEXnetPacket(PacketType type, byte... data) {
        this(type.type, type.size, type.checksum, data);
    }

    /**
     * @see #VEXnetPacket(byte, byte, boolean, byte...)
     * @param type Packet type
     * @param size Packet size
     * @param data Packet data
     */
    VEXnetPacket(byte type, byte size, byte... data)
    {this(type, size, true, data);}

    /**
     * Create a VEXnetPacket with specific type, size, data, and includeChecksum
     * @param type the byte corresponding to the type of the packet
     * @param size the maximum size of the packet
     * @param includeChecksum whether a checksum is expected when sending or receiving the packet.
     * @param data the bytes held by the packet, up to at most {@code size}.
     */
    VEXnetPacket(byte type, byte size, boolean includeChecksum, byte... data) {
        this.type = type;
        this.size = size;
        this.data = data.length > 0 ? data : new byte[size];
        this.includeChecksum = includeChecksum;
    }

    /**
     * Create a copy of a packet
     * @param packet Packet to copy
     */
    VEXnetPacket(VEXnetPacket packet) {
        this(packet.type, packet.size, packet.includeChecksum);
        System.arraycopy(this.data, 0, packet.data, 0, this.size);
    }

    /**
     * Create a string representation of the packet
     * @return String representation
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Packet: ");
        PacketType pt = PacketType.get(this);
        str.append(pt != null ? pt : "UNKNOWN_PACKET_TYPE")
           .append('\n')
           .append(String.format("Type: 0x%02X", this.type))
           .append("\n")
           .append("Size: ")
           .append(size)
           .append("\n");
        if (data.length > 0) {
            str.append("Data: ");
            for(byte b : data) str.append(String.format("%02X ", b));
        }
        return str.toString();
    }

    /**
     * Constructs a packet imitating a VEXNet partner controller to be sent to the main controller
     * @param joystick_1 Value for joystick 1 (0-255)
     * @param joystick_2 Value for joystick 2 (0-255)
     * @param joystick_3 Value for joystick 3 (0-255)
     * @param joystick_4 Value for joystick 4 (0-255)
     * @param _5D Is 5D pressed
     * @param _5U Is 5U pressed
     * @param _6D Is 6D pressed
     * @param _6U Is 6U pressed
     * @param _7D Is 7D pressed
     * @param _7L Is 7L pressed
     * @param _7U Is 7U pressed
     * @param _7R Is 7R pressed
     * @param _8D Is 8D pressed
     * @param _8L Is 8L pressed
     * @param _8U Is 8U pressed
     * @param _8R Is 8R pressed
     * @param accel_Y Value for accelerometer Y (0-255)
     * @param accel_X Value for accelerometer X (0-255)
     * @param accel_Z Value for accelerometer Z (0-255)
     * @return Partner controller packet
     */
    public static VEXnetPacket compileControllerPacket(byte joystick_1,
                                                       byte joystick_2,
                                                       byte joystick_3,
                                                       byte joystick_4,
                                                       boolean _5D, boolean _5U,
                                                       boolean _6D, boolean _6U,
                                                       boolean _7D, boolean _7L, boolean _7U, boolean _7R,
                                                       boolean _8D, boolean _8L, boolean _8U, boolean _8R,
                                                       byte accel_Y,
                                                       byte accel_X,
                                                       byte accel_Z) {
        byte[] data = {
            joystick_1, joystick_2, joystick_3, joystick_4,
            (byte) ((_5D ? (char) 0x01 : 0) | (_5U ? (char) 0x02 : 0) | (_6D ? (char) 0x04 : 0) | (_6U ? (char) 0x08 : 0)),
            (byte) ((_7D ? (char) 0x01 : 0) | (_7L ? (char) 0x02 : 0) | (_7U ? (char) 0x04 : 0) | (_7R ? (char) 0x08 : 0) |
                    (_8D ? (char) 0x10 : 0) | (_8L ? (char) 0x20 : 0) | (_8U ? (char) 0x40 : 0) | (_8R ? (char) 0x80 : 0)),
            accel_Y, accel_X, accel_Z
        };
        return new VEXnetPacket(PacketType.JOY_STATUS_REQUEST_RESPONSE, data);
    };
}

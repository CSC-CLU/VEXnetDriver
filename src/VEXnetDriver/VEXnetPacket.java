package VEXnetDriver;

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
     * Create a VEXnetDriver.VEXnetPacket with specific type, size, data, and includeChecksum
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
     * helper method that packs bits into a byte, starting from the least significant bit.
     */
    private static byte asByte(Boolean... bits) {
        if(bits.length > Byte.SIZE) throw new IllegalArgumentException("This method supports up to 8 bits, but got " + bits.length + "bits");
        byte packed = 0;
        // set the bits starting at least significant bit.
        for(int i = 0; i < bits.length; i++) if(bits[i]) packed |= 1 << i+1;
        return packed;
    }

    /**
     * Constructs a packet imitating a VEXNet partner controller to be sent to the main controller
     * @param Ch1 Value for joystick 1 (0-255)
     * @param Ch2 Value for joystick 2 (0-255)
     * @param Ch3 Value for joystick 3 (0-255)
     * @param Ch4 Value for joystick 4 (0-255)
     * @param Btn5D Is 5D pressed
     * @param Btn5U Is 5U pressed
     * @param Btn6D Is 6D pressed
     * @param Btn6U Is 6U pressed
     * @param Btn7D Is 7D pressed
     * @param Btn7L Is 7L pressed
     * @param Btn7U Is 7U pressed
     * @param Btn7R Is 7R pressed
     * @param Btn8D Is 8D pressed
     * @param Btn8L Is 8L pressed
     * @param Btn8U Is 8U pressed
     * @param Btn8R Is 8R pressed
     * @param AccelY Value for accelerometer Y (0-255)
     * @param AccelX Value for accelerometer X (0-255)
     * @param AccelZ Value for accelerometer Z (0-255)
     * @return Partner controller packet
     */
    public static VEXnetPacket compileControllerPacket(byte Ch1,
                                                       byte Ch2,
                                                       byte Ch3,
                                                       byte Ch4,
                                                       boolean Btn5D, boolean Btn5U, boolean Btn6D, boolean Btn6U,
                                                       boolean Btn7D, boolean Btn7L, boolean Btn7U, boolean Btn7R,
                                                       boolean Btn8D, boolean Btn8L, boolean Btn8U, boolean Btn8R,
                                                       byte AccelY,
                                                       byte AccelX,
                                                       byte AccelZ) {
        return compileControllerPacket(
                Ch1, Ch2, Ch3, Ch4,
                // Ch5
                asByte(Btn5D, Btn5U, Btn6D, Btn6U/* [+4 unused] */),
                // Ch6
                asByte(Btn7D, Btn7L, Btn7U, Btn7R, Btn8D,  Btn8L, Btn8U, Btn8R),
                AccelY, AccelX, AccelZ

        );
    };

    /** Constructs a packet imitating a VEXNet partner controller to be sent to the main controller.
     * Button inputs are combined into {@code Ch5} and {@code Ch6}, allowing an extra 4 bits to be used.
     *
     * @see #compileControllerPacket(byte, byte, byte, byte, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean, byte, byte, byte)
     **/
    public static VEXnetPacket compileControllerPacket(byte Ch1, byte Ch2, byte Ch3, byte Ch4, byte Ch5, byte Ch6,
                                                       byte AccelY, byte AccelX, byte AccelZ) {
        return new VEXnetPacket(PacketType.JOY_STATUS_REQUEST_RESPONSE,
                Ch1, Ch2, Ch3, Ch4, Ch5, Ch6,
                AccelY, AccelX, AccelZ);
    }


}

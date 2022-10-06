/**
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 * @date October 5, 2022
 * @brief Code for communicating using the VEXnet
 */

public class VEXnetPacket {
    public enum PacketType {
        // type, size, includeChecksum
        LCD_UPDATE(0x1E, 17),
        LCD_UPDATE_RESPONSE(0x16, 1),

        JOY_STATUS_REQUEST(0x3B, 0),
        JOY_STATUS_REQUEST_RESPONSE(0x39, 9),

        JOY_VERSION_REQUEST(0x3A, 0),
        JOY_VERSION_REQUEST_RESPONSE(0x3B, 2, false);

        final byte type, size;
        final boolean checksum;
        PacketType(int type, int size, boolean checksum) {
            this.type = (byte)type;
            this.size = (byte)size;
            this.checksum = checksum;
        }

        PacketType(int type, int size) { this(type, size, true); }

        // get packet type based on its char attribute
        public static PacketType get(byte type) {
            return get(type, (byte)0);
        }

        static PacketType get(byte type, byte size) {
            for(PacketType t : values()) {
                if(t.type == type && t.size >= size) return t;
            }
            return null;
        }

        // get the type corresponding to the packet
        static PacketType get(VEXnetPacket packet) {
            return get(packet.type, packet.size);
        }
    }

    byte type, size;
    byte[] data;
    boolean includeChecksum = true;

    public VEXnetPacket() { }

    public VEXnetPacket(PacketType type, byte... data) {
        this(type.type, type.size, type.checksum, data);
    }

    VEXnetPacket(byte type) { this(type, (byte) 0); }
    VEXnetPacket(byte type, byte size, byte... data)
    {this(type, size, true, data);}

    VEXnetPacket(byte type, byte size, boolean includeChecksum, byte... data) {
        this.type = type;
        this.size = size;
        this.data = data.length > 0 ? data : new byte[size];
        this.includeChecksum = includeChecksum;
    }

    VEXnetPacket(VEXnetPacket packet) {
        this(packet.type, packet.size, packet.includeChecksum);
        System.arraycopy(this.data, 0, packet.data, 0, this.size);
    }

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
        str.append("Data: ");
        if (data.length > 0) for(byte b : data) str.append(String.format("%02X ", b));
        else str.append("None");
        return str.toString();
    }

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

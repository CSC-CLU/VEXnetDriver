/**
 * @file VEXnetPacketjava
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
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

        final char type, size;
        final boolean checksum;
        PacketType(int type, int size, boolean checksum) {
            this.type = (char)type;
            this.size = (char)size;
            this.checksum = checksum;
        }

        PacketType(int type, int size) { this(type, size, true); }

        // get packet type based on its char attribute
        public static PacketType get(char type) {
            return get(type,(char) 0);
        }

        static PacketType get(char type, char size) {
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

    char type = 0, size = 0;
    char[] data = null;
    boolean includeChecksum = true;

    public VEXnetPacket() { }

    public VEXnetPacket(PacketType type)
    { this(type, null); }

    public VEXnetPacket(PacketType type, char data[]) {
        this(type.type, type.size, data, type.checksum);
    }

    VEXnetPacket(char type, char size)
    {this(type, size, null, true);}

    VEXnetPacket(char type, char size, char data[])
    {this(type, size, data, true);}

    VEXnetPacket(char type, char size, char data[], boolean includeChecksum) {
        this.type = type;
        this.size = size;
        this.data = data != null ? data : new char[size];
        this.includeChecksum = includeChecksum;
    }

    VEXnetPacket(VEXnetPacket packet) {
        this(packet.type, packet.size, null, packet.includeChecksum);
        System.arraycopy(this.data, 0, packet.data, 0, this.size);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Packet: ");
        str.append(PacketType.get(this));
        PacketType pt = PacketType.get(this);
        str.append(pt != null ? pt : "UNKNOWN_PACKET_TYPE")
           .append('\n')
           .append(String.format("Type: 0x%02X", (int)this.type))
           .append("\n")
           .append("Size: ")
           .append(Integer.toString(this.size))
           .append("\n");
        str.append("Data: ");
        if (data != null) for(char c : data) str.append(String.format("%02X ", (int)c));
        else str.append("None");
        return str.toString();
    }

    public static VEXnetPacket compileControllerPacket(char joystick_1,
                                                       char joystick_2,
                                                       char joystick_3,
                                                       char joystick_4,
                                                       boolean _5D, boolean _5U,
                                                       boolean _6D, boolean _6U,
                                                       boolean _7D, boolean _7L, boolean _7U, boolean _7R,
                                                       boolean _8D, boolean _8L, boolean _8U, boolean _8R,
                                                       char accel_Y,
                                                       char accel_X,
                                                       char accel_Z) {
        char[] data = {
            joystick_1, joystick_2, joystick_3, joystick_4,
            (char) ((_5D ? (char) 0x01 : 0) | (_5U ? (char) 0x02 : 0) | (_6D ? (char) 0x04 : 0) | (_6U ? (char) 0x08 : 0)),
            (char) ((_7D ? (char) 0x01 : 0) | (_7L ? (char) 0x02 : 0) | (_7U ? (char) 0x04 : 0) | (_7R ? (char) 0x08 : 0) |
                    (_8D ? (char) 0x10 : 0) | (_8L ? (char) 0x20 : 0) | (_8U ? (char) 0x40 : 0) | (_8R ? (char) 0x80 : 0)),
            accel_Y, accel_X, accel_Z
        };
        return new VEXnetPacket(PacketType.JOY_STATUS_REQUEST_RESPONSE, data);
    };
}

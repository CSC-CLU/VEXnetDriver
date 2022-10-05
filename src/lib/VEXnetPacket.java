package lib;

import static lib.VEXnetPacket.PacketType.*;

public class VEXnetPacket {
    public enum PacketType {
        // type, size, includeChecksum
        LCD_UPDATE,                     // 0x1E, 17, true
        LCD_UPDATE_RESPONSE,            // 0x16, 1, true

        JOY_STATUS_REQUEST,             // 0x3B, 0
        JOY_STATUS_REQUEST_RESPONSE,    // 0x39, 9, true

        JOY_VERSION_REQUEST,            // 3A, 0
        JOY_VERSION_REQUEST_RESPONSE    // 3B, 2, false
    }

    char type = 0, size = 0;
    char[] data = null;
    boolean includeChecksum = true;

    public VEXnetPacket() { }

    public VEXnetPacket(PacketType type)
    {this(type, null);}

    public VEXnetPacket(PacketType type, char data[]) {
        switch (type) {
            case LCD_UPDATE -> {
                this.type = 0x1E;
                this.size = 17;
            }
            case LCD_UPDATE_RESPONSE -> {
                this.type = 0x16;
                this.size = 1;
            }
            case JOY_STATUS_REQUEST -> {
                this.type = 0x3B;
                this.size = 0;
            }
            case JOY_STATUS_REQUEST_RESPONSE -> {
                this.type = 0x39;
                this.size = 9;
            }
            case JOY_VERSION_REQUEST -> {
                this.type = 0x3A;
                this.size = 0;
            }
            case JOY_VERSION_REQUEST_RESPONSE -> {
                this.type = 0x3B;
                this.size = 2;
                this.includeChecksum = false;
            }
            default -> System.out.println("Error: Invalid packet type specified. Using defaults.");
        }
        this.data = data != null ? data : new char[this.size];
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
        this.type = packet.type;
        this.size = packet.size;
        this.data = new char[this.size];
        System.arraycopy(this.data, 0, packet.data, 0, this.size);
        this.includeChecksum = packet.includeChecksum;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Packet: ");
        switch (this.type) {
            case 0x1E:
                str.append("LCD_UPDATE\n");
                break;
            case 0x16:
                str.append("LCD_UPDATE_RESPONSE\n");
                break;
            case 0x3B:
                if (this.data == null)
                    str.append("JOY_STATUS_REQUEST\n");
                else
                    str.append("JOY_VERSION_REQUEST_RESPONSE\n");
                break;
            case 0x39:
                str.append("JOY_STATUS_REQUEST_RESPONSE\n");
                break;
            case 0x3A:
                str.append("JOY_VERSION_REQUEST\n");
                break;
            default:
                str.append("UNKNOWN_PACKET_TYPE\n");
        }
        str.append("Type: 0x").append(String.format("%02X", (int)this.type)).append("\n");
        str.append("Size: ").append(Integer.toString(this.size)).append("\n");
        str.append("Data: ");
        if (this.data != null) {
            for (int i = 0; i < this.size; i++) {
                str.append(String.format("%02X", (int)this.data[i])).append(' ');
            }
        } else {
            str.append("None");
        }
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
        char[] data = new char[9];
        data[0] = joystick_1;
        data[1] = joystick_2;
        data[2] = joystick_3;
        data[3] = joystick_4;
        data[4] = (char)((_5D ? (char)0x01 : 0) | (_5U ? (char)0x02 : 0) | (_6D ? (char)0x04 : 0)| (_6U ? (char)0x08 : 0));
        data[5] = (char)((_7D ? (char)0x01 : 0) | (_7L ? (char)0x02 : 0) | (_7U ? (char)0x04 : 0)| (_7R ? (char)0x08 : 0) |
                         (_8D ? (char)0x10 : 0) | (_8L ? (char)0x20 : 0) | (_8U ? (char)0x40 : 0)| (_8R ? (char)0x80 : 0));
        data[6] = accel_Y;
        data[7] = accel_X;
        data[8] = accel_Z;
        return new VEXnetPacket(JOY_STATUS_REQUEST_RESPONSE, data);
    };
}

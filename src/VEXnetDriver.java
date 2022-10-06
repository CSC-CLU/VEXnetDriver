/**
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 * @date October 5, 2022
 * @brief Code for communicating using the VEXnet
 */

// https://github.com/Fazecast/jSerialComm

import com.fazecast.jSerialComm.SerialPort;

import static com.fazecast.jSerialComm.SerialPort.NO_PARITY;
import static com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
import static java.util.Objects.requireNonNull;

public class VEXnetDriver {
    public enum DeviceType {
        LCD(19200),
        VEXnet_Joystick_Partner_Port(115200);

        final int bauds;
        DeviceType(int bauds) {
            this.bauds = bauds;
        }
    }

    SerialPort serial;
    String serial_port = null;
    boolean showSuccess = false;
    byte[] buffer = new byte[10];
    int bufferSize = 0;
    int bufferPosition = 0;

    public VEXnetDriver() {
    }

    public VEXnetDriver(SerialPort device, DeviceType deviceType, boolean showSuccess) {
        serial = device;
        this.showSuccess = showSuccess;
        serial_port = serial.getSystemPortName();
        requireNonNull(deviceType);
        openDevice(serial, deviceType.bauds, 8, NO_PARITY, ONE_STOP_BIT);
    }
    public VEXnetDriver(SerialPort device, DeviceType deviceType) {
        this(device, deviceType, false);
    }


    VEXnetDriver(SerialPort device, int bauds, int dataBits, int parity, int stopBits) {
        this(device, bauds, dataBits, parity, stopBits, false);
    }

    VEXnetDriver(SerialPort device, int bauds, int dataBits, int parity, int stopBits, boolean showSuccess) {
        this.serial = device;
        this.showSuccess = showSuccess;

        openDevice(this.serial, bauds, dataBits, parity, stopBits);
    }

    boolean isDeviceOpen() {
        return serial.isOpen();
    }

    @Override
    protected void finalize() {
        serial.closePort();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            SendVexProtocolPacket                                           //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void SendVexProtocolPacket(VEXnetPacket packet) {
        if (!isDeviceOpen()) { // If the serial device is not open, return.
            System.out.println("Error: Serial device is not open");
            return;
        }

        writeBytes(
                (byte)0xAA, // Sync 1
                (byte)0x55, // Sync 2
                packet.type
        );

        if (packet.size != 0) {
            byte Checksum = 0;

            writeBytes(packet.includeChecksum ? (byte) (packet.size + 1) : // +1 for Checksum
                    packet.size);  // +0 for no Checksum

            for (int i = 0; i < packet.size; i++) {
                byte Byte = packet.data[i];
                writeBytes(Byte);
                Checksum -= Byte;
            }

            if (packet.includeChecksum)
                writeBytes(Checksum);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          ReceiveVexProtocolPacket                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public VEXnetPacket ReceiveVexProtocolPacket() {
        if (!isDeviceOpen()) { // If the serial device is not open, return.
            System.out.println("Error: Serial device is not open");
            return null;
        }

        if (bytesAvailable() == 0) return null; // If there is nothing in the serial buffer, return.

        char Checksum = 0;

//        System.out.printf("0x%02X%n", peekByte());
        if (readByte() != (byte)0xAA) // Sync 1
            return null; // Expect Sync 1

//        System.out.printf("0x%02X%n", peekByte());
        if (readByte() != 0x55) // Sync 2
            return null; // Expect Sync 2

//        System.out.printf("0x%02X%n", peekByte());
        byte chr = readByte(); // Packet type
        VEXnetPacket.PacketType type = VEXnetPacket.PacketType.get(chr);
        VEXnetPacket packet =
                type != null ? new VEXnetPacket(type) :
                        new VEXnetPacket(chr, (byte) 0);

        // Packet size
//        System.out.printf("0x%02X%n", peekByte());
        if ((peekByte() == (byte)0xAA || peekByte() == (byte)0x01) && packet.size == 0) {
            // If no more data available and the packet is still empty just return
            return packet;
        } else {
            if (packet.size == 0) { // If packet size is zero
//                System.out.printf("0x%02X%n", peekByte());
                chr = readByte();
                packet.size = packet.includeChecksum ? (byte)(chr - 1) : chr;
                packet.data = new byte[packet.size];
            }
            // If packet size does not match expected size
            if ((chr != packet.size + 1 && packet.includeChecksum) || chr != packet.size) {
                System.out.println("Error: packet size is not correct");
                return null;
            }
        }

        for (int i = 0; i < packet.size; i++) {
            // Payload byte
            Checksum += packet.data[i] = readByte();
        }

        if (Checksum == 0 || !packet.includeChecksum) // If checksum is correct
            return packet;
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                   Supporting functions for the serial library to interpret response codes                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean openDevice(SerialPort Device, int Bauds, int Databits, int Parity, int Stopbits) {
        serial.setComPortParameters(Bauds, Databits, Stopbits, Parity);

        boolean status = serial.openPort();

        if (status)
            System.out.println("Successful connection to " + serial_port);
        else
            System.out.println("Error: unable to open serial device");

        flushReceiver();
        return status;
    }

    boolean writeBytes(byte... bytes) {
        int code = serial.writeBytes(bytes, bytes.length);
        if(code != -1) {
            if(showSuccess) System.out.println("Bytes written successfully");
            return true;
        }
        System.out.println("Error: error while writing data");
        return false;
    }

    byte readByte() {
        if (bufferPosition != bufferSize) {
            return buffer[bufferPosition++];
        }
        int code = serial.readBytes(buffer, buffer.length);

        if (code != -1) {
            bufferSize = code;
            bufferPosition = 0;
            if (showSuccess)
                System.out.println("Bytes read successfully");
            return readByte();
        }
        System.out.println("Error: error while reading the byte");
        return 0;
    }

    byte peekByte() {
        if (bufferPosition != bufferSize) {
            return buffer[bufferPosition];
        }
        byte Byte = readByte();
        bufferPosition--;
        return Byte;
    }

    boolean flushReceiver() {
        boolean status = serial.flushIOBuffers();
        if (status) {
            if (showSuccess) {
                System.out.println("Receiver successfully flushed");
            }
        } else {
            System.out.println("Error: receiver not flushed successfully");
        }
        return status;
    }

    int bytesAvailable() {
        if (bufferPosition != bufferSize)
            return bufferSize - bufferPosition + serial.bytesAvailable();
        else
            return serial.bytesAvailable();
    }
}

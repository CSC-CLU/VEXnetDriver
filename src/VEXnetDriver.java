/**
 * @file VEXnetDriver.java
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
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
    char[] buffer = new char[10];
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

        writeChar((char) 0xAA); // Sync 1
        writeChar((char) 0x55); // Sync 2
        writeChar(packet.type);

        if (packet.size != 0) {
            char Checksum = 0;

            writeChar(packet.includeChecksum ? (char) (packet.size + 1) : // +1 for Checksum
                    packet.size);  // +0 for no Checksum

            for (int i = 0; i < packet.size; i++) {
                char Byte = packet.data[i];
                writeChar(Byte);
                Checksum -= Byte;
            }

            if (packet.includeChecksum)
                writeChar(Checksum);
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

        if (readChar() != 0xaa) // Sync 1
            return null; // Expect Sync 1

        if (readChar() != 0x55) // Sync 2
            return null; // Expect Sync 2

        char chr = readChar(); // Packet type
        VEXnetPacket.PacketType type = VEXnetPacket.PacketType.get(chr);
        VEXnetPacket packet =
                type != null ? new VEXnetPacket(type) :
                        new VEXnetPacket(chr, (char) 0);

        // Packet size
        if (peekChar() == 0 && packet.size == 0) { // If no more data available and data size is zero
            return packet;
        } else {
            if (packet.size == 0) { // If packet size is zero
                chr = readChar();
                packet.size = (char) (chr - 1);
                packet.data = new char[packet.size];
            }
            // If packet size does not match expected size
            if ((chr != packet.size + 1 && packet.includeChecksum) || chr != packet.size) {
                System.out.println("Error: packet size is not correct");
                return null;
            }
        }

        for (int i = 0; i < packet.size; i++) {
            chr = readChar(); // Payload byte
            packet.data[i] = chr;
            Checksum += chr;
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

    boolean writeChar(char Byte) {
        int code = serial.writeBytes(new byte[]{(byte) Byte}, 1);

        if (code != -1) {
            if (showSuccess)
                System.out.println("Bytes written successfully");
            return true;
        }
        System.out.println("Error: error while writing data");
        return false;
    }

    char readChar() {
        if (bufferPosition != bufferSize) {
            char Byte = buffer[bufferPosition];
            bufferPosition++;
            return Byte;
        }
        byte[] buffer = new byte[this.buffer.length];
        int code = serial.readBytes(buffer, buffer.length);

        if (code != -1) {
            if (showSuccess)
                System.out.println("Bytes read successfully");
            bufferPosition = 0;
            bufferSize = code;
            for (int i = 0; i < bufferSize; i++) {
                this.buffer[i] = (char) buffer[i];
            }
            return readChar();
        }
        System.out.println("Error: error while reading the byte");
        return 0;
    }

    char peekChar() {
        if (bufferPosition != bufferSize) {
            return buffer[bufferPosition];
        }
        char Byte = readChar();
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

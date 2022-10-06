/**
 * @file VEXnetDriver.java
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
 * @date October 5, 2022
 * @brief Code for communicating using the VEXnet
 */

package lib;

// https://github.com/Fazecast/jSerialComm
import com.fazecast.jSerialComm.SerialPort;

import static lib.VEXnetPacket.PacketType.*;

import static com.fazecast.jSerialComm.SerialPort.NO_PARITY;
import static com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;

public class VEXnetDriver {
    public enum DeviceType {
        VEX_LCD_Display,
        VEXnet_Joystick_Partner_Port
    }

    SerialPort serial;
    String serial_port = null;
    boolean showSuccess = false;
    char[] buffer = new char[10];
    int bufferSize = 0;
    int bufferPosition = 0;

    public VEXnetDriver() {}

    public VEXnetDriver(SerialPort device, DeviceType deviceType)
    {this(device, deviceType, false);}

    public VEXnetDriver(SerialPort device, DeviceType deviceType, boolean showSuccess) {
        this.serial = device;
        this.showSuccess = showSuccess;
        this.serial_port = this.serial.getSystemPortName();
        int bauds;
        switch (deviceType) {
            case VEX_LCD_Display -> bauds = 19200;
            case VEXnet_Joystick_Partner_Port -> bauds = 115200;
            default -> {
                System.out.println("Error: Invalid device type specified.");
                return;
            }
        }

        openDevice(this.serial, bauds, 8, NO_PARITY, ONE_STOP_BIT);
    }

    VEXnetDriver(SerialPort device, int bauds, int dataBits, int parity, int stopBits)
    {this(device, bauds, dataBits, parity, stopBits, false);}

    VEXnetDriver(SerialPort device, int bauds, int dataBits, int parity, int stopBits, boolean showSuccess) {
        this.serial = device;
        this.showSuccess = showSuccess;

        openDevice(this.serial, bauds, dataBits, parity, stopBits);
    }

    boolean isDeviceOpen() { return serial.isOpen(); }

    @Override
    protected void finalize() {
        this.serial.closePort();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            SendVexProtocolPacket                                           //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void SendVexProtocolPacket(VEXnetPacket packet) {
        if (!isDeviceOpen()) { // If the serial device is not open, return.
            System.out.println("Error: Serial device is not open");
            return;
        }

        writeChar((char)0xAA); // Sync 1
        writeChar((char)0x55); // Sync 2
        writeChar(packet.type);

        if (packet.size != 0) {
            char Checksum = 0;

            writeChar(packet.includeChecksum ? (char)(packet.size+1) : // +1 for Checksum
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

        if (readChar() != (char)0xaa) // Sync 1
            return null; // Expect Sync 1

        if (readChar() != 0x55) // Sync 2
            return null; // Expect Sync 2

        char chr = readChar(); // Packet type
        VEXnetPacket packet = null;
        switch (chr) {
            case 0x1E -> packet = new VEXnetPacket(LCD_UPDATE);
            case 0x16 -> packet = new VEXnetPacket(LCD_UPDATE_RESPONSE);
            case 0x3B -> packet = new VEXnetPacket(JOY_STATUS_REQUEST);
            case 0x39 -> packet = new VEXnetPacket(JOY_STATUS_REQUEST_RESPONSE);
            case 0x3A -> {
                packet = new VEXnetPacket(JOY_VERSION_REQUEST);
                return packet;
            }
            default -> {
                packet = new VEXnetPacket();
                packet.type = chr;
            }
        }

        // Packet size
        if (peekChar() == 0 && packet.size == 0) { // If no more data available and data size is zero
            return packet;
        } else {
            if (packet.size == 0) { // If packet size is zero
                chr = readChar();
                packet.size = (char)(chr - 1);
                packet.data = new char[packet.size];
            }
            // If packet size does not match expected size
            if ((chr != packet.size+1 && packet.includeChecksum) || chr != packet.size) {
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
        this.serial.setComPortParameters(Bauds, Databits, Stopbits, Parity);

        boolean status = this.serial.openPort();

        if (status)
            System.out.println("Successful connection to " + this.serial_port);
        else
            System.out.println("Error: unable to open serial device");

        flushReceiver();
        return status;
    }

    boolean writeChar(char Byte) {
        int code = this.serial.writeBytes(new byte[]{(byte)Byte}, 1);

        if (code != -1) {
            if (showSuccess)
                System.out.println("Bytes written successfully");
            return true;
        }
        System.out.println("Error: error while writing data");
        return false;
    }

    char readChar() {
        if (this.bufferPosition != this.bufferSize) {
            char Byte = this.buffer[this.bufferPosition];
            this.bufferPosition++;
            return Byte;
        }
        byte[] buffer = new byte[this.buffer.length];
        int code = this.serial.readBytes(buffer, this.buffer.length);

        if (code != -1) {
            if (showSuccess)
                System.out.println("Bytes read successfully");
            this.bufferPosition = 0;
            this.bufferSize = code;
            for (int i = 0; i < this.bufferSize; i++) {
                this.buffer[i] = (char)buffer[i];
            }
            return readChar();
        }
        System.out.println("Error: error while reading the byte");
        return 0;
    }

    char peekChar() {
        if (this.bufferPosition != this.bufferSize) {
            return this.buffer[this.bufferPosition];
        }
        char Byte = readChar();
        this.bufferPosition--;
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
        if (this.bufferPosition != this.bufferSize)
            return this.bufferSize - this.bufferPosition + this.serial.bytesAvailable();
        else
            return this.serial.bytesAvailable();
    }
}

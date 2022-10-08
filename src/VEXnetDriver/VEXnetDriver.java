package VEXnetDriver;// https://github.com/Fazecast/jSerialComm
import com.fazecast.jSerialComm.SerialPort;

import java.security.InvalidParameterException;

import static com.fazecast.jSerialComm.SerialPort.NO_PARITY;
import static com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
import static java.util.Objects.requireNonNull;

/**
 * Code for communicating using the VEXnet
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 */
public class VEXnetDriver {
    /** Known device types
     * @see VEXnetDriver#VEXnetDriver(String, DeviceType, boolean)
     * @see VEXnetDriver#VEXnetDriver(String, int, int, int, int, boolean)
     */
    public enum DeviceType {
        LCD(19200),
        VEXnet_Joystick_Partner_Port(115200);

        final int bauds;
        DeviceType(int bauds) {
            this.bauds = bauds;
        }
    }

    /**
     * Serial port device
     */
    SerialPort serial;

    /**
     * String name of the serial port
     */
    String serial_port = null;

    /**
     * Determines whether success messages should be shown on serial library calls
     */
    boolean showSuccess = false;

    /**
     * Buffer to be used when reading serial data
     */
    byte[] buffer = new byte[10];

    /**
     * Size of the contents of the buffer (not necessarily the same as buffer.length)
     */
    int bufferSize = 0;

    /**
     * Position of the next byte in the buffer to be read
     */
    int bufferPosition = 0;

    /**
     * Create an instance of the driver using device type
     * @param device Serial port device
     * @param deviceType They type of the device
     */
    public VEXnetDriver(String device, DeviceType deviceType) {
        this(device, deviceType, false);
    }

    /**
     * Create an instance of the driver using device type
     * @param device Serial port device
     * @param deviceType They type of the device
     * @param showSuccess Show successful serial communication messages
     */
    public VEXnetDriver(String device, DeviceType deviceType, boolean showSuccess) {
        serial = findSerialPort(device);
        if (serial == null)
            throw new InvalidParameterException("Com port \"" + device + "\" does not exist");
        this.showSuccess = showSuccess;
        serial_port = serial.getSystemPortName();
        requireNonNull(deviceType);
        openDevice(serial, deviceType.bauds, 8, NO_PARITY, ONE_STOP_BIT);
    }

    /**
     * Create an instance of the driver using exact specifications
     * @param device Serial port device
     * @param bauds Baud rate of serial port
     * @param dataBits Data bits
     * @param parity Parity type
     * @param stopBits Stop bits
     */
    VEXnetDriver(String device, int bauds, int dataBits, int parity, int stopBits) {
        this(device, bauds, dataBits, parity, stopBits, false);
    }

    /**
     * Create an instance of the driver using exact specifications
     * @param device Serial port device
     * @param bauds Baud rate of serial port
     * @param dataBits Data bits
     * @param parity Parity type
     * @param stopBits Stop bits
     * @param showSuccess Show successful serial communication messages
     */
    VEXnetDriver(String device, int bauds, int dataBits, int parity, int stopBits, boolean showSuccess) {
        serial = findSerialPort(device);
        if (serial == null)
            throw new InvalidParameterException("Com port \"" + device + "\" does not exist");
        this.showSuccess = showSuccess;

        openDevice(this.serial, bauds, dataBits, parity, stopBits);
    }

    /**
     * Check if the serial device is open
     * @return Is serial port open
     */
    boolean isDeviceOpen() {
        return serial.isOpen();
    }

    /**
     * Close the serial device when the driver object is destroyed
     */
    @Override
    protected void finalize() {
        serial.closePort();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            SendVexProtocolPacket                                           //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Send a VEXnet packet to the serial device
     * @param packet VEXnet packet
     */
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

    /**
     * Receive a VEXnet packet from serial device if one is present
     * @return VEXnet packet if available, otherwise null
     */
    public VEXnetPacket ReceiveVexProtocolPacket() {
        if (!isDeviceOpen()) { // If the serial device is not open, return.
            System.out.println("Error: Serial device is not open");
            return null;
        }

        if (bytesAvailable() == 0) return null; // If there is nothing in the serial buffer, return.

        char Checksum = 0;

        if (readByte() != (byte)0xAA) // Sync 1
            return null; // Expect Sync 1

        if (readByte() != 0x55) // Sync 2
            return null; // Expect Sync 2

        byte chr = readByte(); // Packet type
        VEXnetPacket.PacketType type = VEXnetPacket.PacketType.get(chr);
        VEXnetPacket packet =
                type != null ? new VEXnetPacket(type) :
                        new VEXnetPacket(chr, (byte) 0);

        // Packet size
        if ((peekByte() == (byte)0xAA || peekByte() == (byte)0x01) && packet.size == 0) {
            // If no more data available and the packet is still empty just return
            return packet;
        } else {
            if (packet.size == 0) { // If packet size is zero
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

    /**
     * Open a serial port device
     * @param Device Serial port
     * @param Bauds Baud rate
     * @param Databits Data bits
     * @param Parity Parity mode
     * @param Stopbits Stop bits
     * @return Success status
     */
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

    /**
     * Write a byte to the serial device
     * @param bytes Byte to be sent
     * @return Success status
     */
    boolean writeBytes(byte... bytes) {
        int code = serial.writeBytes(bytes, bytes.length);
        if(code != -1) {
            if(showSuccess) System.out.println("Bytes written successfully");
            return true;
        }
        System.out.println("Error: error while writing data");
        return false;
    }

    /**
     * Return the next byte from the buffer
     * If the buffer is empty, refill it
     * @return Byte read
     */
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

    /**
     * Get the next byte in the buffer without moving on to the next byte
     * @return Byte read
     */
    byte peekByte() {
        if (bufferPosition != bufferSize) {
            return buffer[bufferPosition];
        }
        byte Byte = readByte();
        bufferPosition--;
        return Byte;
    }

    /**
     * Clear all contents of the sending and receiving buffers
     * @return Success status
     */
    boolean flushReceiver() {
        boolean status = serial.flushIOBuffers();
        this.buffer = new byte[1];
        this.bufferSize = this.buffer.length;
        this.bufferPosition = this.bufferSize;
        if (status) {
            if (showSuccess) {
                System.out.println("Receiver successfully flushed");
            }
        } else {
            System.out.println("Error: receiver not flushed successfully");
        }
        return status;
    }

    /**
     * Check the number of bytes available in the serial buffers
     * @return Bytes available in buffers
     */
    int bytesAvailable() {
        if (bufferPosition != bufferSize)
            return bufferSize - bufferPosition + serial.bytesAvailable();
        else
            return serial.bytesAvailable();
    }

    /**
     * Get a list of com ports available on the computer
     * @return Com pot list
     */
    public static String[] availableComPorts() {
        SerialPort[] comPorts = SerialPort.getCommPorts();
        String[] ports = new String[comPorts.length];
        for (int i = 0; i < comPorts.length; i++) {
            ports[i] = comPorts[i].getSystemPortName();
        }
        return ports;
    }

    /**
     * Get a descriptive list of com ports available on the computer
     * @return Descriptive com port list
     */
    public static String[] availableComPortsDescriptive() {
        SerialPort[] comPorts = SerialPort.getCommPorts();
        String[] ports = new String[comPorts.length];
        for (int i = 0; i < comPorts.length; i++) {
            ports[i] = comPorts[i].getDescriptivePortName();
        }
        return ports;
    }

    /**
     * Find the com port with the same name as the one provided
     * @param comPort Name of com port
     * @return SerialPort if port was found, otherwise null
     */
    private SerialPort findSerialPort(String comPort) {
        for (SerialPort port : SerialPort.getCommPorts()) {
            if (port.getSystemPortName().equals(comPort) || port.getDescriptivePortName().equals(comPort)) {
                return port;
            }
        }
        return null;
    }
}

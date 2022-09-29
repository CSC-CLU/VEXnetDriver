/**
 * @file VEXnetDriver.cpp
 * @author Eric Heinke
 * @version 0.0
 * @date September 28 2022
 * @brief Code for communicating using the VEXnet
 */

#include "VEXnetDriver.h"

VEXnetDriver::VEXnetDriver(const char *Device, bool showSuccess = false):
serial(),
statusCodes(Device, showSuccess)
{
    statusCodes.openDevice(this->serial.openDevice(Device, 115200, SERIAL_DATABITS_8, SERIAL_PARITY_NONE, SERIAL_STOPBITS_1));
    this->buffer = new char[100];
}

VEXnetDriver::~VEXnetDriver() { }

bool VEXnetDriver::isDeviceOpen()
{
    return this->serial.isDeviceOpen();
}

void VEXnetDriver::SendVexProtocolPacket(unsigned char PacketType,
                                         unsigned char PayloadSize,
                                         unsigned char *DataBytes)
{
    if (!this->serial.isDeviceOpen()) { // If the serial device is not open, return.
        cout<<"Error: Serial device is not open"<<endl;
        return;
    }

    if (!PayloadSize) return;

    this->buffer[0] = 0xAA; // Sync 1
    this->buffer[1] = 0x55; // Sync 2
    this->buffer[2] = PacketType;
    this->buffer[3] = PayloadSize+1; // +1 for Checksum

    unsigned char Checksum = 0;

    int i = 4;
    while (PayloadSize--) {
        unsigned char Byte = *DataBytes++;
        this->buffer[i] = Byte;
        Checksum -= Byte;
        i++;
    }

    this->buffer[i] = Checksum;

    this->serial.writeBytes(this->buffer, i);
}

bool VEXnetDriver::ReceiveVexProtocolPacket(unsigned char *PacketType,
                                            unsigned char *PayloadSize,
                                            unsigned char *DataBytes)
{
    if (!this->serial.isDeviceOpen()) { // If the serial device is not open, return.
        cout<<"Error: Serial device is not open"<<endl;
        return;
    }

    if (serial.available()) return false; // If there is nothing in the serial buffer, return.

    unsigned char Checksum = 0;

    char *chr;

    statusCodes.readChar(serial.readChar(chr));
    if (*chr != (char)0xaa)
        return false; // Expect Sync 1

    statusCodes.readChar(serial.readChar(chr));
    if (*chr != 0x55)
        return false; // Expect Sync 2

    statusCodes.readChar(serial.readChar(chr));
    *PacketType = *chr;

    if (PayloadSize) { // We are expecting data (PayloadSize != null)
        statusCodes.readChar(serial.readChar(chr));
        unsigned char Bytes = *chr;

        *PayloadSize = Bytes-1;

        while (Bytes--) {
            statusCodes.readChar(serial.readChar(chr));
            unsigned char Byte = *chr;
            *DataBytes++ = Byte;
            Checksum += Byte;
        }
    }

    return (Checksum==0);
}

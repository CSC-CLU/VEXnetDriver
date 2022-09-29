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
    if (!this->serial.isDeviceOpen()) {
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

// char readChar() {
//     char* tmpChar;
//     serial.readChar(tmpChar);
//     return *tmpChar;
// }

// bool ReceiveVexProtocolPacket(unsigned char *PacketType,
//                               unsigned char *PayloadSize,
//                               unsigned char *DataBytes)
// {
//     if (!serial.isDeviceOpen()) return false; // If the serial device is not open, return.
//     if (serial.available()) return false; // If there is nothing in the serial buffer, return.

//     unsigned char Checksum=0;

//     if (readChar() != 0xaa)
//         return false; // Expect Sync 1

//     if (readChar() != 0x55)
//         return false; // Expect Sync 2

//     *PacketType = readChar();

//     if (PayloadSize) { // We are expecting data
//         unsigned char Bytes = readChar();

//         *PayloadSize = Bytes-1;

//         while (Bytes--) {
//             unsigned char Byte = readChar();
//             *DataBytes++ = Byte;
//             Checksum += Byte;
//         }
//     }

//     return (Checksum==0);
// }

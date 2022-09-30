/**
 * @file VEXnetDriver.cpp
 * @author Eric Heinke
 * @version 0.0
 * @date September 29 2022
 * @brief Code for communicating using the VEXnet
 */

#include "VEXnetDriver.h"

#include <iostream>

VEXnetDriver::VEXnetDriver(const char *device, DeviceType deviceType, bool showSuccess) :
        statusCodes(device, showSuccess) {
    unsigned int bauds;
    switch (deviceType) {
        case VEX_LCD_Display:
            bauds = 19200;
            break;
        case VEXnet_Joystick_Partner_Port:
            bauds = 115200;
            break;
        default:
            std::cout << "Error: Invalid device type specified." << std::endl;
            return;
    };
    statusCodes.openDevice(serial.openDevice(device, bauds));
}

void VEXnetDriver::SendVexProtocolPacket(VEXnetPacket packet) {

}

VEXnetPacket *VEXnetDriver::ReceiveVexProtocolPacket() {
    return nullptr;
}

// void VEXnetDriver::SendVexProtocolPacket(unsigned char PacketType,
//                                          unsigned char PayloadSize,
//                                          unsigned char *DataBytes,
//                                          bool includeChecksum = true)
// {
//     if (!this->serial.isDeviceOpen()) { // If the serial device is not open, return.
//         cout<<"Error: Serial device is not open"<<endl;
//         return;
//     }

//     statusCodes.writeChar(serial.writeChar(0xAA)); // Sync 1
//     statusCodes.writeChar(serial.writeChar(0x55)); // Sync 2
//     statusCodes.writeChar(serial.writeChar(PacketType));

//     if (PayloadSize) {

//         unsigned char Checksum = 0;

//         statusCodes.writeChar(serial.writeChar(PayloadSize+1)); // +1 for Checksum

//         while (PayloadSize--) {
//             unsigned char Byte = *DataBytes++;
//             statusCodes.writeChar(serial.writeChar(Byte));
//             Checksum -= Byte;
//         }

//         if (includeChecksum)
//             statusCodes.writeChar(serial.writeChar(Checksum));
//     }
// }

// bool VEXnetDriver::ReceiveVexProtocolPacket(unsigned char *PacketType,
//                                             unsigned char *PayloadSize,
//                                             unsigned char *DataBytes)
// {
//     if (!this->serial.isDeviceOpen()) { // If the serial device is not open, return.
//         cout<<"Error: Serial device is not open"<<endl;
//         return false;
//     }

//     if (serial.available()) return false; // If there is nothing in the serial buffer, return.

//     unsigned char Checksum = 0;

//     char *chr;

//     statusCodes.readChar(serial.readChar(chr));
//     if (*chr != (char)0xaa)
//         return false; // Expect Sync 1

//     statusCodes.readChar(serial.readChar(chr));
//     if (*chr != 0x55)
//         return false; // Expect Sync 2

//     statusCodes.readChar(serial.readChar(chr));
//     *PacketType = *chr;

//     if (PayloadSize) { // We are expecting data (PayloadSize != null)
//         statusCodes.readChar(serial.readChar(chr));
//         unsigned char Bytes = *chr;

//         *PayloadSize = Bytes-1;

//         while (Bytes--) {
//             statusCodes.readChar(serial.readChar(chr));
//             unsigned char Byte = *chr;
//             *DataBytes++ = Byte;
//             Checksum += Byte;
//         }
//     }

//     return (Checksum==0);
// }
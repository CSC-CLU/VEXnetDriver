/**
 * @file VEXnetDriver.cpp
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
 * @date September 30 2022
 * @brief Code for communicating using the VEXnet
 */

#include "VEXnetDriver.h"

#include <iostream>

VEXnetDriver::VEXnetDriver(const char *device, DeviceType deviceType, bool showSuccess) :
    serial_port(device),
    showSuccess(showSuccess)
    {
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
    openDevice(device, bauds);
}

VEXnetDriver::VEXnetDriver(const char *device, unsigned int bauds, SerialDataBits dataBits, 
                           SerialParity parity, SerialStopBits stopBits, bool showSuccess) :
    serial_port(device),
    showSuccess(showSuccess)
    {
    openDevice(device, bauds, dataBits, parity, stopBits);
}

void VEXnetDriver::SendVexProtocolPacket(const VEXnetPacket &packet) {
    if (!serial.isDeviceOpen()) { // If the serial device is not open, return.
        std::cout<<"Error: Serial device is not open"<<std::endl;
        return;
    }

    writeChar(0xAA); // Sync 1
    writeChar(0x55); // Sync 2
    writeChar(packet.type);

    if (packet.size) {
        unsigned char Checksum = 0;

        writeChar(packet.includeChecksum ? packet.size+1 : // +1 for Checksum
                                           packet.size);  // +0 for no Checksum
        
        unsigned char *ptr = packet.data;
        for (int i = 0; i < packet.size; i++) {
            unsigned char Byte = *ptr++;
            writeChar(Byte);
            Checksum -= Byte;
        }

        if (packet.includeChecksum)
            writeChar(Checksum);
    }
}

VEXnetPacket *VEXnetDriver::ReceiveVexProtocolPacket() {
    return nullptr;
}

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

bool VEXnetDriver::openDevice(const char *Device, const unsigned int Bauds,
                    SerialDataBits Databits, SerialParity Parity, SerialStopBits Stopbits) {
    int code = serial.openDevice(Device, Bauds, Databits, Parity, Stopbits);
    switch (code) {
        case 1:
            if (showSuccess > 1)
                printf("Successful connection to %s\n",this->serial_port);
            return true;
        case -1:
            printf("Error: device not found %s\n",this->serial_port);
            return false;
        case -2:
            printf("Error: error while opening the device %s\n",this->serial_port);
            return false;
        case -3:
            printf("Error: error while getting port parameters %s\n",this->serial_port);
            return false;
        case -4:
            printf("Error: Speed (Bauds) not recognized %s\n",this->serial_port);
            return false;
        case -5:
            printf("Error: error while writing port parameters %s\n",this->serial_port);
            return false;
        case -6:
            printf("Error: error while writing timeout parameters %s\n",this->serial_port);
            return false;
        case -7:
            printf("Error: Databits not recognized %s\n",this->serial_port);
            return false;
        case -8:
            printf("Error: Stopbits not recognized %s\n",this->serial_port);
            return false;
        case -9:
            printf("Error: Parity not recognized %s\n",this->serial_port);
            return false;
        default:
            printf("Error: an unknown error has occured\n");
            return false;  
    };
}

bool VEXnetDriver::writeChar(const char Byte) {
    int code = serial.writeChar(Byte);
    switch (code) {
        case 1:
            if (showSuccess)
                std::cout<<"Character written succesfully"<<std::endl;
            return true;
        case -1:
            std::cout<<"Error: error while writting data"<<std::endl;
            return false;
        default:
            std::cout<<"Error: an unknown error has occured"<<std::endl;
            return false;
    };
}

bool VEXnetDriver::readChar(char *pByte,const unsigned int timeOut_ms = 0) {
    int code = serial.readChar(pByte, timeOut_ms);
    switch (code) {
        case 1:
            if (showSuccess) 
                std::cout<<"Character read succesfully"<<std::endl;
            return true;
        case 0:
            std::cout<<"Error: Timeout reached"<<std::endl;
            return false;
        case -1:
            std::cout<<"Error: error while setting the Timeout"<<std::endl;
            return false;
        case -2:
            std::cout<<"Error: error while reading the byte"<<std::endl;
            return false;
        default:
            std::cout<<"Error: an unknown error has occured"<<std::endl;
            return false;
    };
}

bool VEXnetDriver::writeString(const char *String) {
    int code = serial.writeString(String);
    switch (code) {
        case 1:
            if (showSuccess)
                std::cout<<"String written succesfully"<<std::endl;
            return true;
        case -1:
            std::cout<<"Error: error while writting data"<<std::endl;
            return false;
        default:
            std::cout<<"Error: an unknown error has occured"<<std::endl;
            return false;
    };
}

bool VEXnetDriver::readString(char *receivedString,
                              char finalChar,
                              unsigned int maxNbBytes,
                              const unsigned int timeOut_ms = 0) {
    int code = serial.readString(receivedString, finalChar, maxNbBytes, timeOut_ms);
    if (code >   0) {
        if (showSuccess) 
            std::cout<<"String read succesfully"<<std::endl;
        return true;
    } else if (code ==  0) {
        std::cout<<"Error: timeout reached"<<std::endl;
        return false;
    } else if (code == -1) {
        std::cout<<"Error: error while setting the Timeout"<<std::endl;
        return false;
    } else if (code == -2) {
        std::cout<<"Error: error while reading the byte"<<std::endl;
        return false;
    } else if (code == -3) {
        std::cout<<"Error: MaxNbBytes is reached"<<std::endl;
        return false;
    } else {
        std::cout<<"Error: an unknown error has occured"<<std::endl;
        return false;
    }
}

bool VEXnetDriver::writeBytes(const void *Buffer, const unsigned int NbBytes) {
    int code = serial.writeBytes(Buffer, NbBytes);
    switch (code) {
        case 1:
            if (showSuccess)
                std::cout<<"Bytes written succesfully"<<std::endl;
            return true;
        case -1:
            std::cout<<"Error: error while writting data"<<std::endl;
            return false;
        default:
            std::cout<<"Error: an unknown error has occured"<<std::endl;
            return false;
    };
}

bool VEXnetDriver::readBytes(void *buffer, unsigned int maxNbBytes, const unsigned int timeOut_ms = 0, 
                             unsigned int sleepDuration_us = 100) {
    int code = readBytes(buffer, maxNbBytes, timeOut_ms, sleepDuration_us);
    if (code > -1) {
        if (showSuccess) {
            std::cout<<"Bytes read succesfully"<<std::endl;
        }
        return true;
    } else if (code == -1) {
        std::cout<<"Error: error while setting the Timeout"<<std::endl;
        return false;
    } else if (code == -2) {
        std::cout<<"Error: error while reading the byte"<<std::endl;
        return false;
    } else {
        std::cout<<"Error: an unknown error has occured"<<std::endl;
        return false;
    }
}

bool VEXnetDriver::flushReceiver() {
    int code = serial.flushReceiver();
    // If code != 0
    if (code) {
        if (showSuccess) {
            std::cout<<"Receiver succesfully flushed"<<std::endl;
        }
        return true;
    } else {
        std::cout<<"Error: reciever not flushed succesfully"<<std::endl;
        return false;
    }
}

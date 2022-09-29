/**
 * @file VEXnetDriver.h
 * @author Eric Heinke
 * @version 0.0
 * @date September 28 2022
 * @brief Code for communicating using the VEXnet
 */

#ifndef VEXNETDRIVER_H
#define VEXNETDRIVER_H

#include "serialib.h"
#include "DecodeStatusCodes.h"

struct VEXnetDriver {
private:
    serialib serial;
    DecodeStatusCodes statusCodes;
    char *buffer;
public:
    VEXnetDriver(const char *Device, bool showSuccess);
    ~VEXnetDriver();
    bool isDeviceOpen();
    void SendVexProtocolPacket(unsigned char PacketType,
                               unsigned char PayloadSize,
                               unsigned char *DataBytes);
    bool ReceiveVexProtocolPacket(unsigned char *PacketType,
                                  unsigned char *PayloadSize,
                                  unsigned char *DataBytes);
};

#endif // VEXNETDRIVER_H
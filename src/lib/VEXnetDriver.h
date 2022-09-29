/**
 * @file VEXnetDriver.h
 * @author Eric Heinke
 * @version 0.0
 * @date September 29 2022
 * @brief Code for communicating using the VEXnet
 */

#ifndef VEXNETDRIVER_H
#define VEXNETDRIVER_H

#include "serialib.h"
#include "DecodeStatusCodes.h"

static const unsigned int VEXnet_Joystick_Partner_Port = 115200;
static const unsigned int VEX_LCD_Display = 19200;

struct VEXnetDriver {
private:
    serialib serial;
    DecodeStatusCodes statusCodes;
    char *buffer;
public:
    VEXnetDriver(const char *Device, bool showSuccess, unsigned int Bauds);
    ~VEXnetDriver();
    bool isDeviceOpen();
    void SendVexProtocolPacket(unsigned char PacketType,
                               unsigned char PayloadSize,
                               unsigned char *DataBytes,
                               bool includeChecksum);
    bool ReceiveVexProtocolPacket(unsigned char *PacketType,
                                  unsigned char *PayloadSize,
                                  unsigned char *DataBytes);
    void PartnerJoystick_RespondToStatusRequest(unsigned char *DataBytes);
};

#endif // VEXNETDRIVER_H
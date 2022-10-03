/**
 * @file VEXnetDriver.h
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
 * @date October 3 2022
 * @brief Code for communicating using the VEXnet
 */

#ifndef VEXNETDRIVER_H
#define VEXNETDRIVER_H

#include "serialib.h"
#include "VEXnetPacket.h"

struct VEXnetDriver {
    enum DeviceType {
        VEX_LCD_Display,
        VEXnet_Joystick_Partner_Port
    };

    VEXnetDriver() {};
    VEXnetDriver(const char *device, DeviceType deviceType, bool showSuccess = false);
    VEXnetDriver(const char *device, unsigned int bauds, SerialDataBits dataBits, 
                 SerialParity parity, SerialStopBits stopBits, bool showSuccess = false);
    ~VEXnetDriver() {serial.closeDevice(); delete[] serial_port;};

    bool isDeviceOpen() { return serial.isDeviceOpen(); }
    void SendVexProtocolPacket(const VEXnetPacket &packet);
    VEXnetPacket* ReceiveVexProtocolPacket();

private:
    serialib serial;
    const char *serial_port = nullptr;
    bool showSuccess = false;
    char *buffer;
    bool bufferContents = false;
private:
    bool openDevice(const char *device, const unsigned int bauds, SerialDataBits dataBits = SERIAL_DATABITS_8, 
                    SerialParity parity = SERIAL_PARITY_NONE, SerialStopBits stopBits = SERIAL_STOPBITS_1);
    bool writeChar(const char Byte);
    bool readChar(char *pByte,const unsigned int timeOut_ms = 0);
    bool peakChar(char *pByte, unsigned int timeOut_ms = 0);
    bool writeString(const char *String);
    bool readString(char *receivedString, char finalChar, unsigned int maxNbBytes, 
                    const unsigned int timeOut_ms = 0);
    bool writeBytes(const void *Buffer, const unsigned int NbBytes);
    bool readBytes(void *buffer, unsigned int maxNbBytes, const unsigned int timeOut_ms = 0, 
                   unsigned int sleepDuration_us = 100);
    bool flushReceiver();
};

#endif // VEXNETDRIVER_H
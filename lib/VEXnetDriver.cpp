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
}

VEXnetDriver::~VEXnetDriver() { }

bool VEXnetDriver::isDeviceOpen()
{
    return this->serial.isDeviceOpen();
}

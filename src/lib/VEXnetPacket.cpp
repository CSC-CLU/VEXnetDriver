/**
 * @file VEXnetPacket.cpp
 * @author Eric Heinke
 * @version 0.0
 * @date September 29 2022
 * @brief 
 */

#include "VEXnetPacket.h"

#include <iostream>

VEXnetPacket::VEXnetPacket(PacketType type, unsigned char data[]) {
    switch (type) {
        case LCD_UPDATE:
            this->type = 0x1E;
            this->size = 17;
            this->includeChecksum = true;
            break;
        case LCD_UPDATE_RESPONSE:
            this->type = 0x16;
            this->size = 1;
            this->includeChecksum = true;
            break;
        case JOY_STATUS_REQUEST:
            this->type = 0x3B;
            this->size = 0;
            break;
        case JOY_STATUS_REQUEST_RESPONSE:
            this->type = 0x39;
            this->size = 9;
            this->includeChecksum = true;
            break;
        case JOY_VERSION_REQUEST:
            this->type = 0x3A;
            this->size = 0;
            break;
        case JOY_VERSION_REQUEST_RESPONSE:
            this->type = 0x3B;
            this->size = 2;
            this->includeChecksum = false;
            break;
        default:
            std::cout << "Error: Invalid packet type specified. Using defaults." << std::endl;
    };
    this->data = data ? data : new unsigned char[this->size];
}

constexpr VEXnetPacket::VEXnetPacket(unsigned char type,
                                     unsigned char size,
                                     unsigned char data[],
                                     bool includeChecksum)
                                     : type(type)
                                     , size(size)
                                     , data(data ? data : new unsigned char[size])
                                     , includeChecksum(includeChecksum)
                                     {}

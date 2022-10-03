/**
 * @file VEXnetPacket.cpp
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
 * @date October 3 2022
 * @brief 
 */

#include "VEXnetPacket.h"

#include <iostream>
#include <sstream>
#include <iomanip>

using namespace std;

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
            cout << "Error: Invalid packet type specified. Using defaults." << endl;
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

VEXnetPacket::VEXnetPacket(const VEXnetPacket &packet):
type(packet.type),
size(packet.size),
includeChecksum(packet.includeChecksum)
{
    this->data = new unsigned char[this->size];
    memcpy(this->data, packet.data, this->size);
}

std::string VEXnetPacket::toString() {
    stringstream sstream;
    sstream<<"Packet: ";
    switch (this->type) {
        case 0x1E:
            sstream<<"LCD_UPDATE"<<endl;
            break;
        case 0x16:
            sstream<<"LCD_UPDATE_RESPONSE"<<endl;
            break;
        case 0x3B:
            if (!this->data)
                sstream<<"JOY_STATUS_REQUEST"<<endl;
            else
                sstream<<"JOY_VERSION_REQUEST_RESPONSE"<<endl;
            break;
        case 0x39:
            sstream<<"JOY_STATUS_REQUEST_RESPONSE"<<endl;
            break;
        case 0x3A:
            sstream<<"JOY_VERSION_REQUEST"<<endl;
            break;
        default:
            sstream<<"UNKNOWN_PACKET_TYPE"<<endl;
    };
    sstream<<"Type: 0x"<<hex<<uppercase<<setw(2)<<setfill('0')<<(int)(this->type)<<endl;
    sstream<<"Size: "<<to_string(this->size)<<endl;
    sstream<<"Data: ";
    if (this->data) {
        for (int i = 0; i < this->size; i++) {
            sstream<<hex<<uppercase<<setw(2)<<setfill('0')<<(int)(this->data[i])<<' ';
        }
    } else {
        sstream<<"None";
    }
    sstream<<endl;
    return sstream.str();
}
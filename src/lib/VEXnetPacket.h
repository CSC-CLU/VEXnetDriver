//
// Created by Zrp200 on 9/29/2022.
//

#ifndef VEXNETDRIVER_VEXNETPACKET_H
#define VEXNETDRIVER_VEXNETPACKET_H

struct VEXnetPacket {
    enum PacketType {
        // type, size, includeChecksum
        LCD_UPDATE, // 0x1e, 17, true
        LCD_UPDATE_RESPONSE, // 0x16, 1, true

        JOY_STATUS_REQUEST, // 0x3b, 0
        JOY_STATUS_REQUEST_RESPONSE, // 0x39, 9, true

        JOY_VERSION_REQUEST, // 3a, 0
        JOY_VERSION_REQUEST_RESPONSE // 3b, 2, false
    };
    unsigned char type=0, size=0, *data = nullptr;
    bool includeChecksum=true;
};

#endif //VEXNETDRIVER_VEXNETPACKET_H

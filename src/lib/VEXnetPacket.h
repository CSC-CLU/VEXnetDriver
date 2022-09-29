/**
 * @file VEXnetPacket.cpp
 * @author Eric Heinke, Zrp200
 * @version 0.0
 * @date September 29 2022
 * @brief 
 */

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
    unsigned char type = 0, size = 0, *data = nullptr;
    bool includeChecksum = true;

    VEXnetPacket() {};

    VEXnetPacket(PacketType type, unsigned char data[] = nullptr);

    constexpr VEXnetPacket(unsigned char type, unsigned char size,
                           unsigned char data[] = nullptr, bool includeChecksum = true);

    ~VEXnetPacket();
};

#endif //VEXNETDRIVER_VEXNETPACKET_H

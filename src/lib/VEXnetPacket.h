/**
 * @file VEXnetPacket.h
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
 * @date September 30 2022
 * @brief 
 */

#ifndef VEXNETPACKET_H
#define VEXNETPACKET_H

struct VEXnetPacket {
    enum PacketType {
        // type, size, includeChecksum
        LCD_UPDATE,                     // 0x1E, 17, true
        LCD_UPDATE_RESPONSE,            // 0x16, 1, true

        JOY_STATUS_REQUEST,             // 0x3B, 0
        JOY_STATUS_REQUEST_RESPONSE,    // 0x39, 9, true

        JOY_VERSION_REQUEST,            // 3A, 0
        JOY_VERSION_REQUEST_RESPONSE    // 3B, 2, false
    };
    unsigned char type = 0, size = 0, *data = nullptr;
    bool includeChecksum = true;

    VEXnetPacket() {};

    VEXnetPacket(PacketType type, unsigned char data[] = nullptr);

    constexpr VEXnetPacket(unsigned char type, unsigned char size,
                           unsigned char data[] = nullptr, bool includeChecksum = true);

    VEXnetPacket(const VEXnetPacket &packet);

    ~VEXnetPacket() { delete[] this->data; };

    static VEXnetPacket* compileControllerPacket(unsigned char joystick_1,
                                 unsigned char joystick_2,
                                 unsigned char joystick_3,
                                 unsigned char joystick_4,
                                 bool _5D, bool _5U,
                                 bool _6D, bool _6U,
                                 bool _7D, bool _7L, bool _7U, bool _7R,
                                 bool _8D, bool _8L, bool _8U, bool _8R,
                                 unsigned char accel_Y,
                                 unsigned char accel_X,
                                 unsigned char accel_Z) {
        unsigned char *data = new unsigned char[9];
        data[0] = joystick_1;
        data[1] = joystick_2;
        data[2] = joystick_3;
        data[3] = joystick_4;
        data[4] = (0x01 * _5D) | (0x02 * _5U) | (0x04 * _6D) | (0x08 * _6U);
        data[5] = (0x01 * _7D) | (0x02 * _7L) | (0x04 * _7U) | (0x08 * _7R) | 
                  (0x10 * _8D) | (0x20 * _8L) | (0x40 * _8U) | (0x80 * _8R);
        data[6] = accel_Y;
        data[7] = accel_X;
        data[8] = accel_Z;
        return new VEXnetPacket(JOY_STATUS_REQUEST_RESPONSE, data);
    };

};

#endif //VEXNETPACKET_H

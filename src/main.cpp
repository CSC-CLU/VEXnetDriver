/**
 * @file main.cpp
 * @author Eric Heinke (sudo-Eric)
 * @version 0.0
 * @date October 3 2022
 * @brief Code for testing communications with the VEXnet using the VEXnetDriver
 */

#include "lib/VEXnetDriver.h"
#include "lib/VEXnetPacket.h"

#include <iostream>
// #include <bitset>
#include <string>
// #include <sstream>

// Serial library
#include "lib/serialib.h"

#if defined (_WIN32) || defined(_WIN64)
    #include<windows.h>
#endif
#if defined (__linux__) || defined(__APPLE__)
    #include<unistd.h>
#endif

#if defined (_WIN32) || defined(_WIN64)
    //for serial ports above "COM9", we must use this extended syntax of "\\.\COMx".
    //also works for COM0 to COM9.
    #define SERIAL_PORT "\\\\.\\COM5"
#endif
#if defined (__linux__) || defined(__APPLE__)
    #define SERIAL_PORT "/dev/ttyACM0"
#endif

#include <thread>
#include <chrono>
using namespace std;

// Serial object
serialib serial;

void delay(int delay) {
    Sleep(delay);
    // this_thread::sleep_for(chrono::milliseconds(delay));

}

int main(int argc, char *argv[])
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Serial Communication with VEX controller
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    VEXnetDriver vexDriver = VEXnetDriver(SERIAL_PORT, VEXnetDriver::VEXnet_Joystick_Partner_Port, false);
    if (!vexDriver.isDeviceOpen()) {return -1;}

    while (true) {
        VEXnetPacket *packet = vexDriver.ReceiveVexProtocolPacket();
        if (packet)
            cout<<packet->toString()<<endl;
        delete packet;

        packet = VEXnetPacket::compileControllerPacket(
            0x7F, 0x7F, 0x7F, 0x7F, false, false, false, true, false, 
            false, false, false, false, false, false, false, 0x7f, 0x7f, 0x7f);
        cout<<"Sending controller packet"<<endl;
        vexDriver.SendVexProtocolPacket(*packet);
        delete packet;

        delay(1000);
    }

    return 0 ;
}

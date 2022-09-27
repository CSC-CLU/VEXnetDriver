/**
 * @file VEXnetDriver.cpp
 * @author Eric Heinke
 * @date September 27 2022
 * @brief Code for communicating using the VEXnet
 */

#include <iostream>
#include <bitset>
#include <string>
#include <sstream>

// Serial library
#include "lib/serialib.h"

// Serial library status code decoding
#include "DecodeStatusCodes.h"

#if defined (_WIN32) || defined(_WIN64)
    #include<windows.h>
#endif
#if defined (__linux__) || defined(__APPLE__)
    #include<unistd.h>
#endif

#if defined (_WIN32) || defined(_WIN64)
    //for serial ports above "COM9", we must use this extended syntax of "\\.\COMx".
    //also works for COM0 to COM9.
    #define SERIAL_PORT "\\\\.\\COM4"
#endif
#if defined (__linux__) || defined(__APPLE__)
    #define SERIAL_PORT "/dev/ttyACM0"
#endif

using namespace std;

// Define function headder information
void writeChar(char);
void SendVexProtocolPacket(unsigned char, unsigned char, unsigned char*);
char readChar();
bool ReceiveVexProtocolPacket(unsigned char*, unsigned char*, unsigned char*);




// Serial object
serialib serial;
DecodeStatusCodes statusCodes(false);

int main(int argc, char *argv[])
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Serial Communication
    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Serial object
    // serialib serial; // Moved above
    // DecodeStatusCodes statusCodes(false); // Moved above

    // Connection to serial port
    char statusCode = serial.openDevice(SERIAL_PORT, 115200, SERIAL_DATABITS_8, SERIAL_PARITY_NONE, SERIAL_STOPBITS_1);

    // If connection fails, display error message and exit. Otherwise, display a success message
    if (statusCodes.openDevice(statusCode) == false) return 1;

    statusCode = serial.flushReceiver();

    // Display status code message
    statusCodes.flushReceiver(statusCode);

    // cout<<"Select operation to perform:"<<endl;
    // cout<<"\t1: Get controller status (x1)"<<endl;
    // cout<<"\t2: Get controller status (x10)"<<endl;
    // cout<<"\t3: Get controller status (infinite)"<<endl;
    // cout<<"\t4: Play tower light animation"<<endl;
    // cout<<"\t5: Play flatline sound effect"<<endl;
    // cout<<"\t6: Nunchuck Controller Settings"<<endl;
    // cout<<"\t7: Set button color"<<endl;
    // cout<<"> ";
    // int mode;
    // cin>>mode;
    // // mode = 2;



    // Close the serial device
    serial.closeDevice();

    return 0 ;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Code to decode and encode (read and write) VEX packet data
////////////////////////////////////////////////////////////////////////////////////////////////////

void writeChar(char c) {
    serial.writeChar(c);
}

void SendVexProtocolPacket(unsigned char PacketType,
                           unsigned char PayloadSize,
                           unsigned char *DataBytes)
{
    if (!serial.isDeviceOpen()) return; // If the serial device is not open, return.

    writeChar(0xaa); // Sync 1
    writeChar(0x55); // Sync 2

    writeChar(PacketType);

    if (PayloadSize) {
        unsigned char Checksum=0;

        writeChar(PayloadSize+1);  // +1 for Checksum

        while(PayloadSize--) {
            unsigned char Byte = *DataBytes++;
            writeChar(Byte);
            Checksum -= Byte;
        }

        writeChar(Checksum);
    }
}

char readChar() {
    char* tmpChar;
    serial.readChar(tmpChar);
    return *tmpChar;
}

bool ReceiveVexProtocolPacket(unsigned char *PacketType,
                              unsigned char *PayloadSize,
                              unsigned char *DataBytes)
{
    if (!serial.isDeviceOpen()) return false; // If the serial device is not open, return.
    if (serial.available()) return false; // If there is nothing in the serial buffer, return.

    unsigned char Checksum=0;

    if (readChar() != 0xaa)
        return false; // Expect Sync 1

    if (readChar() != 0x55)
        return false; // Expect Sync 2

    *PacketType = readChar();

    if (PayloadSize) { // We are expecting data
        unsigned char Bytes = readChar();

        *PayloadSize = Bytes-1;

        while (Bytes--) {
            unsigned char Byte = readChar();
            *DataBytes++ = Byte;
            Checksum += Byte;
        }
    }

    return (Checksum==0);
}
/**
 * @file DecodeStatusCodes.h
 * @author Eric Heinke
 * @version 1.0
 * @date September 27 2022
 * @brief File contains functions for displaying status message from the return status of Philippe Lucidarme's serial library.
 */

#ifndef DECODESTATUSCODES_H
#define DECODESTATUSCODES_H

#include <iostream>

using namespace std;

class DecodeStatusCodes
{
public:
    DecodeStatusCodes();
    DecodeStatusCodes(const char *Device, bool showSuccess);
    ~DecodeStatusCodes();

    bool flushReceiver(char code);
    bool openDevice(char code);
    bool readBytes(int code);
    bool readChar(char code);
    bool readString(int code);
    bool writeBytes(char code);
    bool writeChar(char code);
    bool writeString(char code);
private:
    const char *serial_port = nullptr;
    bool showSuccess = false;
};

inline DecodeStatusCodes::DecodeStatusCodes(const char *Device, bool showSuccess = false):
serial_port(Device),
showSuccess(showSuccess)
// mode true: Display success and failure messages
// mode false: Display only failure messages
{ }
inline DecodeStatusCodes::~DecodeStatusCodes() {
    delete[] this->serial_port;
}

inline bool DecodeStatusCodes::flushReceiver(char code)
{
    // If code != 0
    if (code) {
        if (showSuccess) {
            cout<<"Receiver succesfully flushed"<<endl;
        }
        return true;
    } else {
        cout<<"Error: reciever not flushed succesfully"<<endl;
        return false;
    }
}

inline bool DecodeStatusCodes::openDevice(char code)
{
    switch (code) {
        case 1:
            if (true)
                printf("Successful connection to %s\n",this->serial_port);
            return true;
        case -1:
            printf("Error: device not found %s\n",this->serial_port);
            return false;
        case -2:
            printf("Error: error while opening the device %s\n",this->serial_port);
            return false;
        case -3:
            printf("Error: error while getting port parameters %s\n",this->serial_port);
            return false;
        case -4:
            printf("Error: Speed (Bauds) not recognized %s\n",this->serial_port);
            return false;
        case -5:
            printf("Error: error while writing port parameters %s\n",this->serial_port);
            return false;
        case -6:
            printf("Error: error while writing timeout parameters %s\n",this->serial_port);
            return false;
        case -7:
            printf("Error: Databits not recognized %s\n",this->serial_port);
            return false;
        case -8:
            printf("Error: Stopbits not recognized %s\n",this->serial_port);
            return false;
        case -9:
            printf("Error: Parity not recognized %s\n",this->serial_port);
            return false;
        default:
            printf("Error: an unknown error has occured\n");
            return false;  
    };
}

inline bool DecodeStatusCodes::readBytes(int code)
{
    if (code > -1) {
        if (showSuccess) {
            cout<<"Bytes read succesfully"<<endl;
        }
        return true;
    } else if (code == -1) {
        cout<<"Error: error while setting the Timeout"<<endl;
        return false;
    } else if (code == -2) {
        cout<<"Error: error while reading the byte"<<endl;
        return false;
    } else {
        cout<<"Error: an unknown error has occured"<<endl;
        return false;
    }
}

inline bool DecodeStatusCodes::readChar(char code)
{
    switch (code) {
        case 1:
            if (showSuccess) 
                cout<<"Character read succesfully"<<endl;
            return true;
        case 0:
            cout<<"Error: Timeout reached"<<endl;
            return false;
        case -1:
            cout<<"Error: error while setting the Timeout"<<endl;
            return false;
        case -2:
            cout<<"Error: error while reading the byte"<<endl;
            return false;
        default:
            cout<<"Error: an unknown error has occured"<<endl;
            return false;
    };
}

inline bool DecodeStatusCodes::readString(int code)
{
    if (code >   0) {
        if (showSuccess) 
            cout<<"String read succesfully"<<endl;
        return true;
    } else if (code ==  0) {
        cout<<"Error: timeout reached"<<endl;
        return false;
    } else if (code == -1) {
        cout<<"Error: error while setting the Timeout"<<endl;
        return false;
    } else if (code == -2) {
        cout<<"Error: error while reading the byte"<<endl;
        return false;
    } else if (code == -3) {
        cout<<"Error: MaxNbBytes is reached"<<endl;
        return false;
    } else {
        cout<<"Error: an unknown error has occured"<<endl;
        return false;
    }
}

inline bool DecodeStatusCodes::writeBytes(char code)
{
    switch (code) {
        case 1:
            if (showSuccess)
                cout<<"Bytes written succesfully"<<endl;
            return true;
        case -1:
            cout<<"Error: error while writting data"<<endl;
            return false;
        default:
            cout<<"Error: an unknown error has occured"<<endl;
            return false;
    };
}

inline bool DecodeStatusCodes::writeChar(char code)
{
    switch (code) {
        case 1:
            if (showSuccess)
                cout<<"Character written succesfully"<<endl;
            return true;
        case -1:
            cout<<"Error: error while writting data"<<endl;
            return false;
        default:
            cout<<"Error: an unknown error has occured"<<endl;
            return false;
    };
}

inline bool DecodeStatusCodes::writeString(char code)
{
    switch (code) {
        case 1:
            if (showSuccess)
                cout<<"String written succesfully"<<endl;
            return true;
        case -1:
            cout<<"Error: error while writting data"<<endl;
            return false;
        default:
            cout<<"Error: an unknown error has occured"<<endl;
            return false;
    };
}

#endif // DECODESTATUSCODES_H
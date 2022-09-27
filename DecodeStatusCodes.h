/**
 * @file /DecodeStatusCodes.h
 * @author Eric Heinke
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
    DecodeStatusCodes(const char*, bool mode);
    ~DecodeStatusCodes();

    bool flushReceiver(const char code);
    bool openDevice(const char code);
    bool readBytes(const int code);
    bool readChar(const char code);
    bool readString(const int code);
    bool writeBytes(const char code);
    bool writeChar(const char code);
    bool writeString(const char code);
private:
    const char *serial_port;
    bool showSuccess;
};

DecodeStatusCodes::DecodeStatusCodes(const char *serial_port, bool showSuccess):
serial_port(serial_port),
showSuccess(showSuccess)
// mode true: Display success and failure messages
// mode false: Display only failure messages
{ }
DecodeStatusCodes::~DecodeStatusCodes() {
    delete[] this->serial_port;
}

bool DecodeStatusCodes::flushReceiver(const char code)
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

bool DecodeStatusCodes::openDevice(const char code)
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

bool DecodeStatusCodes::readBytes(const int code)
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

bool DecodeStatusCodes::readChar(const char code)
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

bool DecodeStatusCodes::readString(const int code)
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

bool DecodeStatusCodes::writeBytes(const char code)
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

bool DecodeStatusCodes::writeChar(const char code)
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

bool DecodeStatusCodes::writeString(const char code)
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
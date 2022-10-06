/**
 * @file main.java
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 0.5a
 * @date October 5, 2022
 * @brief Code for communicating using the VEXnet
 */

// https://github.com/Fazecast/jSerialComm
import com.fazecast.jSerialComm.SerialPort;

public class main {
    public static void main(String[] args) {

        SerialPort[] ports = SerialPort.getCommPorts();

        if (ports.length == 0) {
            System.out.println("There are no com ports available.");
            return;
        }

        System.out.println("Available com ports:");
        for (SerialPort port: ports) {
            System.out.println(port.getSystemPortName());
//            System.out.println(port.getDescriptivePortName());
        }

        VEXnetPacket packet = VEXnetPacket.compileControllerPacket(
                (char)127, (char)127, (char)127, (char)127,
                false, false,
                true, false,
                false, false, false, false,
                false, false, false, false,
                (char)127, (char)127, (char)127);
//        System.out.println(packet);

        SerialPort comPort = ports[0];

//        System.out.println("Using: " + comPort.getSystemPortName());
        System.out.println("Using: " + comPort.getDescriptivePortName());

        VEXnetDriver driver = new VEXnetDriver(comPort, VEXnetDriver.DeviceType.VEXnet_Joystick_Partner_Port);

        for(int i = 0; i < 100; i++)
            driver.SendVexProtocolPacket(packet);
    }
}
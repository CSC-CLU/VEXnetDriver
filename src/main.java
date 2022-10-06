/**
 * @file main.java
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 * @date October 5, 2022
 * @brief Code for communicating using the VEXnet
 */

// https://github.com/Fazecast/jSerialComm
import com.fazecast.jSerialComm.SerialPort;

public class main {
    public static void main(String[] args) throws InterruptedException {

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

        VEXnetPacket packet1 = VEXnetPacket.compileControllerPacket(
                (byte)127, (byte)127, (byte)127, (byte)127,
                false, false,
                true, false,
                false, false, false, false,
                false, false, false, false,
                (byte)127, (byte)127, (byte)127);
        VEXnetPacket packet2 = VEXnetPacket.compileControllerPacket(
                (byte)127, (byte)127, (byte)127, (byte)127,
                false, false,
                false, true,
                false, false, false, false,
                false, false, false, false,
                (byte)127, (byte)127, (byte)127);
//        System.out.println(packet);

        SerialPort comPort = ports[0];

//        System.out.println("Using: " + comPort.getSystemPortName());
        System.out.println("Using: " + comPort.getDescriptivePortName());

        VEXnetDriver driver = new VEXnetDriver(comPort, VEXnetDriver.DeviceType.VEXnet_Joystick_Partner_Port);

        while (true) {
            Thread.sleep(100);
            for (int i = 0; i < 200; i++) {
                driver.SendVexProtocolPacket(packet1);
                Thread.sleep(1);
            }
            VEXnetPacket packe3 = driver.ReceiveVexProtocolPacket();
            if (packe3 != null)
                System.out.println(packe3);

            Thread.sleep(100);
            for (int i = 0; i < 80; i++) {
                driver.SendVexProtocolPacket(packet2);
                Thread.sleep(1);
            }
            packe3 = driver.ReceiveVexProtocolPacket();
            if (packe3 != null)
                System.out.println(packe3);

        }
    }
}
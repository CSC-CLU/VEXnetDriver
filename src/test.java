/**
 * Code for communicating using the VEXnet
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 */

// https://github.com/Fazecast/jSerialComm

/**
 * Class for testing VEXnet driver
 */
public class test {
    /**
     * Main function for testing VEXnet driver
     * @param args No arguments are expected
     * @throws InterruptedException Thread.sleep
     */
    public static void main(String[] args) throws InterruptedException {

        String[] comPorts = VEXnetDriver.availableComPorts();
        String[] comPortsDescriptive = VEXnetDriver.availableComPortsDescriptive();
        String comPort = null;

        if (args.length == 0) {
            if (comPorts.length == 0) {
                System.out.println("There are no com ports available.");
                return;
            }
            comPort = comPorts[0];
            System.out.println("Using: " + comPort);
        } else {
            if (args[0].equals("--list")) {
                if (comPorts.length == 0) {
                    System.out.println("There are no com ports available.");
                } else {
                    System.out.println("Available com ports:");
                    for (int i = 0; i < comPorts.length; i++) {
                        System.out.println(comPorts[i] + " | " + comPortsDescriptive[i]);
                    }
                }
                return;
            } else if (args[0].equals("-p") || args[0].equals("--port")) {
                if (args.length < 2) {
                    System.out.println("Error: A port must be specified");
                    return;
                }
                for (String port : comPorts) {
                    if (port.equals(args[1])) {
                        comPort = port;
                        break;
                    }
                }
                if (comPort == null) {
                    System.out.println("Error: No com port named \"" + args[1] + "\"");
                    return;
                } else {
                    System.out.println("Using: " + comPort);
                }
            } else {
                System.out.println("Error: Unknown argument \"" + args[0] + "\"");
                return;
            }
        }

        VEXnetPacket packet1 = VEXnetPacket.compileControllerPacket(
                (byte)(127), (byte)(127), (byte)(127), (byte)(127),
                false, false,
                false, false,
                false, false, false, false,
                false, false, false, false,
                (byte)127, (byte)127, (byte)127);
        VEXnetPacket packet2 = VEXnetPacket.compileControllerPacket(
                (byte)127, (byte)127, (byte)127, (byte)127,
                false, false,
                false, false,
                false, false, false, false,
                false, false, false, false,
                (byte)127, (byte)127, (byte)127);
//        System.out.println(packet);

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
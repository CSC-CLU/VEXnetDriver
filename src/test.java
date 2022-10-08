/**
 * Code for communicating using the VEXnet
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 */

import java.util.concurrent.TimeUnit;

/**
 * Class for testing VEXnet driver
 */
public class test {
    private static VEXnetDriver driver = null;
    private static final int packetDelay = 20;

    /**
     * Main function for testing VEXnet driver
     * @param args No arguments are expected
     * @throws InterruptedException TimeUnit.MILLISECONDS.sleep()
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

        driver = new VEXnetDriver(comPort, VEXnetDriver.DeviceType.VEXnet_Joystick_Partner_Port);

        VEXnetPacket packet_send = VEXnetPacket.compileControllerPacket(
                (byte)(127), (byte)(127), (byte)(127), (byte)(127),
                false, false,
                false, false,
                false, false, false, false,
                false, false, false, false,
                (byte)127, (byte)127, (byte)127);

        VEXnetPacket packet_receive = null;

        TimeUnit.MILLISECONDS.sleep(100);
        packet_receive = driver.ReceiveVexProtocolPacket();
        if (packet_receive != null)
            System.out.println(packet_receive);

        // Move forward
        robotDrive(255, 1000);
        delay(1000);

        // Pick up item
        robotClaw(1, 500);
        delay(1000);
        robotDrive(200, 250);
        delay(1000);
        robotClaw(-1, 250);
        delay(1000);
        robotArm(1, 1200);

        // Turn around
        robotTurn(255, 1800);
        delay(1000);

        // Drive back
        robotDrive(255, 1000);
        delay(1000);

        // Set item down
        robotArm(-1, 200);
        delay(1000);
        robotClaw(1, 200);
        delay(1000);
        robotDrive(0, 50);
        delay(1000);
        robotClaw(-1, 300);
    }

    private static void robotControl(int leftSpeed, int rightSpeed, int armSpeed, int clawSpeed, int time) throws InterruptedException {
        VEXnetPacket packet = driver.ReceiveVexProtocolPacket();
        if (packet != null)
            System.out.println(packet);
        packet = VEXnetPacket.compileControllerPacket(
                (byte)(127), (byte)(255-rightSpeed), (byte)(255-leftSpeed), (byte)(127),
                clawSpeed > 0, clawSpeed < 0,
                armSpeed < 0, armSpeed > 0,
                false, false, false, false,
                false, false, false, false,
                (byte)127, (byte)127, (byte)127);
        int reps = time / packetDelay;
        for (int i = 0; i < reps; i++) {
            driver.SendVexProtocolPacket(packet);
            TimeUnit.MILLISECONDS.sleep(packetDelay);
        }
    }

    private static void delay(int time) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(time);
    }

    private static void robotDrive(int speed, int time) throws InterruptedException {
        robotControl(speed, speed, 0, 0, time);
    }

    private static void robotTurn(int speed, int time) throws InterruptedException {
        robotControl(speed, -speed, 0, 0, time);
    }

    private static void robotArm(int speed, int time) throws InterruptedException {
        robotControl(127, 127, speed, 0, time);
    }

    private static void robotClaw(int speed, int time) throws InterruptedException {
        robotControl(127, 127, 0, speed, time);
    }
}
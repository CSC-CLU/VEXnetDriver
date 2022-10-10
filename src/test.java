/**
 * Code for communicating using the VEXnet
 * @author Eric Heinke (sudo-Eric), Zrp200
 * @version 1.0
 */

import VEXnetDriver.VEXnetDriver;
import VEXnetDriver.VEXnetPacket;

import java.util.Timer;
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

    public static volatile int packets = 0;
    public static volatile long time = 0;
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
        robotDrive(Byte.MAX_VALUE,900);
        // Pick up item
        robotClaw(false, 500);
        delay(1000);
        robotDrive(50, 250);
        delay(1000);
        robotClaw(true, 250);
        delay(1000);
        robotArm(40, 1200);

        // Turn around
        robotTurn(-Byte.MAX_VALUE, 1800);
        delay(1000);

        // Drive back
        robotDrive(Byte.MAX_VALUE, 1000);
        delay(1000);

        // Set item down
        robotArm(-40, 200);
        delay(1000);
        robotClaw(false, 200);
        delay(1000);
        robotDrive(-100, 50);
        delay(1000);
        robotClaw(true, 300);
    }

    private static void robotControl(int leftSpeed, int rightSpeed, int armSpeed, int clawSpeed, int time) throws InterruptedException {
        VEXnetPacket packet = driver.ReceiveVexProtocolPacket();
        if (packet != null)
            System.out.println(packet);
        packet = VEXnetPacket.compileControllerPacket(
                (byte)(127+leftSpeed), (byte)(127-rightSpeed),
                (byte)(127+armSpeed), (byte)(127-clawSpeed),
                false, false, false, false,
                false, false, false, false, false, false, false, false,
                (byte)127, (byte)127, (byte)127);
        int reps = time / packetDelay;
        System.out.println(packet);
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
        robotControl(0, 0, speed, 0, time);
    }

    private static void robotClaw(int speed, int time) throws InterruptedException {
        robotControl(0, 0, 0, speed, time);
    }

    private static void robotClaw(boolean close, int time) throws InterruptedException {
        robotClaw(close ? 40 : -40, time);
    }
}
import java.net.*;
import java.io.*;
import java.util.*;

public class PastryClient {
    public static void main(String args[]) {
        Map<Integer, Integer> hopsCount = new HashMap<>();
        for (int i = 1; i < 1001; i++) {
            boolean fail = false;
            String id = String.format("%04d", Integer.parseInt(Integer.toString(new Random().nextInt(256), 4)));
            String[] reply = visit(id, args[0]);
            if (reply == null) {
                while (reply != null) {
                    id = String.format("%04d", Integer.parseInt(Integer.toString(new Random().nextInt(256), 4)));
                    reply = visit(id, args[0]);
                }
            }
            else {
                while (reply.length != 2) {
                    id = String.format("%04d", Integer.parseInt(Integer.toString(new Random().nextInt(256), 4)));
                    reply = visit(id, args[0]);
                    if (reply == null) {
                        while (reply != null) {
                            id = String.format("%04d", Integer.parseInt(Integer.toString(new Random().nextInt(256), 4)));
                            reply = visit(id, args[0]);
                        }
                    }
                }
            }
            System.out.println("We want: " + id);
            System.out.println("[" + i + "] Visiting: " + args[0]);
            int hop = 1;
            while (reply[0] != id) {
                try {
                    System.out.println("Currently on ID: " + reply[0]);
                }
                catch (Exception e) {
                    i--;
                    fail = true;
                    break;
                }
                if (reply[0].equals(id)) {
                    break;
                }
                hop++;
                System.out.println("Visiting: " + reply[1] + "; Hop#: " + hop);
                reply = visit(id, reply[1]);
                if (reply == null || hop > 6 || reply.length != 2 || reply[1].length() < 7) {
                    i--;
                    fail = true;
                    if (reply == null) {
                        System.out.println("Reply was null...");
                    }
                    else if (hop > 6) {
                        System.out.println("Hop limit exceeded...");
                    }
                    else if (reply.length != 2) {
                        System.out.println("Reply is not length 2...");
                    }
                    else if (reply[1].length() < 7) {
                        System.out.println("Reply[1] is less than 7...");
                    }
                    break;
                }
            }
            if (!fail) {
                System.out.println("Iteration: " + i + "; Hops to target ID " + id + ": " + hop);
                if (!hopsCount.containsKey(hop)) {
                    hopsCount.put(hop, 1);
                }
                else {
                    hopsCount.put(hop, hopsCount.get(hop) + 1);
                }
            }
            else {
                System.out.println("Fail!");
            }
        }
        for (int i = 0; i < 7; i++) {
            System.out.println(i + ": " + (hopsCount.get(i) == null ? 0 : hopsCount.get(i)));
        }
    }

    public static String[] visit(String id, String ip) {
        String[] replySplit = null;
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            aSocket.setSoTimeout(1500);
            InetAddress aHost = InetAddress.getByName(ip);
            byte[] m = id.getBytes();
            int serverPort = 32710;
            DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
            aSocket.send(request);
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            String replyString = new String(reply.getData()).trim();
            replySplit = replyString.split(":");
        }
        catch (SocketException e) {
            // System.out.println("Socket: " + e.getMessage());
        }
        catch (IOException e) {
            // System.out.println("IO: " + e.getMessage());
        }
        finally {
            if(aSocket != null) aSocket.close();
        }
        return replySplit;
    }
}
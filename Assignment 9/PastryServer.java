import java.io.*;
import java.net.*;
import java.util.*;

public class PastryServer {
    public static DatagramSocket socket;
    public static Map<String, String> leafSet;
    public static Map<String, String> routingTable;
    public static Map<String, String> combined;
    public static byte[] buffer;
    public static byte[] answer;
    public static String userQuery;
    public static String originalUserQuery;

    public static void main(String[] args) {
        leafSet = new HashMap<>();
        leafSet.put("2331", "18.220.68.55");
        leafSet.put("3001", "13.57.5.20");
        leafSet.put("3013", "13.56.191.118");
        leafSet.put("3020", "13.58.105.45");

        routingTable = new HashMap<>();
        routingTable.put("0123", "18.222.59.189"); // Row 1
        routingTable.put("1123", "13.57.66.133");
        routingTable.put("2102", "54.219.136.134");
        routingTable.put("3011", "47.156.67.63");
        routingTable.put("3011", "47.156.67.63"); // Row 2
        routingTable.put("3111", "18.217.11.51");
        routingTable.put("3222", "18.188.79.150");
        routingTable.put("3310", "54.177.53.121");
        routingTable.put("3001", "13.57.5.20"); // Row 3
        routingTable.put("3011", "47.156.67.63");
        routingTable.put("3022", "13.56.207.150");
        routingTable.put("3031", "null");
        routingTable.put("3010", "null"); // Row 4
        routingTable.put("3011", "47.156.67.63");
        routingTable.put("3012", "null");
        routingTable.put("3013", "13.56.191.118");

        try {
            socket = new DatagramSocket(32710);
            buffer = new byte[1000];
            answer = null;
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                answer = null;
                userQuery = new String(request.getData()).trim();
                originalUserQuery = new String(request.getData()).trim();
                if (userQuery.length() > 4) {
                    answer = new String("INVALID REQUEST").getBytes();
                }
                String[] leafSetSet = leafSet.keySet().toArray(new String[leafSet.size()]);
                String[] routingTableSet = routingTable.keySet().toArray(new String[leafSet.size()]);
                if (answer == null) {
                    for (String key : leafSetSet) {
                        if (key.startsWith(userQuery)) {
                            String ip = leafSet.get(key);
                            if (ip.equals("null")) {
                                answer = new String(ip).getBytes();
                            }
                            else {
                                answer = new String(key + ":" + ip).getBytes();
                            }
                            break;
                        }
                    }
                }
                if (answer == null) {
                    for (String key : routingTableSet) {
                        if (key.startsWith(userQuery)) {
                            String ip = routingTable.get(key);
                            if (ip.equals("null")) {
                                answer = new String(ip).getBytes();
                            }
                            else {
                                answer = new String(key + ":" + ip).getBytes();
                            }
                            break;
                        }
                    }
                }
                if (answer == null) {
                    while ((userQuery = userQuery.substring(0, userQuery.length() - 1)).length() > 0) {
                        for (String key : leafSet.keySet()) {
                            if (key.startsWith(userQuery)) {
                                String ip = leafSet.get(key);
                                if (ip.equals("null")) {
                                    answer = new String(ip).getBytes();
                                }
                                else {
                                    answer = new String(key + ":" + ip).getBytes();
                                }
                                break;
                            }
                        }
                        if (answer != null) {
                            break;
                        }
                    }
                }
                userQuery = originalUserQuery;
                if (answer == null) {
                    while ((userQuery = userQuery.substring(0, userQuery.length() - 1)).length() > 0) {
                        for (String key : routingTable.keySet()) {
                            if (key.startsWith(userQuery)) {
                                String ip = routingTable.get(key);
                                if (ip.equals("null")) {
                                    answer = new String(ip).getBytes();
                                }
                                else {
                                    answer = new String(key + ":" + ip).getBytes();
                                }
                                break;
                            }
                        }
                        if (answer != null) {
                            break;
                        }
                    }
                }
                if (answer == null) {
                    answer = new String("INVALID REQUEST").getBytes();
                }
                DatagramPacket reply = new DatagramPacket(answer, answer.length, request.getAddress(), request.getPort());
                socket.send(reply);
                buffer = new byte[1000];
            }
        }
        catch (SocketException se) {
            se.printStackTrace();
        }
        catch (IOException io) {
            io.printStackTrace();
        }
        finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
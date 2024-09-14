package cn.ccc212.utils;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NetworkUtil {

    public static void connectToWifi(String ssid) {
        try {
            String command = String.format("netsh wlan connect name=\"%s\"", ssid);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new RuntimeException("连接到:" + ssid + "失败");
        }
    }

    @SneakyThrows
    public static boolean isWifiConnected() {
        Process process = Runtime.getRuntime().exec("ping -n 1 www.bilibili.com");
        // 返回0表示Ping成功，网络可用
        int returnVal = process.waitFor();
        return (returnVal == 0);
    }

    @SneakyThrows
    public static String checkWifiStatus() {
        Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String result = new String();
        while ((line = reader.readLine()) != null) {
            result += line;
            result += "\n";
//            if (line.contains("SSID")) {
//               result += line;
//               break;
//            }
        }
        return result.toString();
    }

    public static String getSSID(String result) {
        return result == null ? result.split("SSID")[1].replaceAll(":", "").split("\n")[0].trim() : "";
    }

    @SneakyThrows
    public static String getIPv4() {
        Process process = Runtime.getRuntime().exec("ipconfig");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String ipv4Address = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("IPv4")) {
                ipv4Address = line.split(":")[1].trim();
                if (ipv4Address.startsWith("10")) {
                    break;
                }
            }
        }
        return ipv4Address;
    }

    @SneakyThrows
    public static String getGateway() {
        Process process = Runtime.getRuntime().exec("route print");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String gateway = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("0.0.0.0")) {
                gateway = line.split("\\s+")[3];
                break;
            }
        }
        return gateway;
    }
}

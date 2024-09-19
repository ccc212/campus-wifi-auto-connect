package cn.ccc212.utils;

import cn.ccc212.exception.BizException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class NetworkUtil {

    public static void connectToWifi(String ssid) {
        try {
            String command = String.format("netsh wlan connect name=\"%s\"", ssid);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new BizException("连接到:" + ssid + "失败");
        }
    }

    @SneakyThrows
    public static boolean isWifiConnected() {
        Process process = Runtime.getRuntime().exec("ping -n 1 www.bilibili.com");
        // 返回0表示Ping成功，网络可用
        int result = 0;
        try {
            result = process.waitFor();
        } catch (InterruptedException e) {
            log.info("停止");
        }
        return result == 0;
    }

    @SneakyThrows
    public static String checkWifiStatus() {
        Process process = Runtime.getRuntime().exec("netsh wlan show interfaces");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
        String line;
        StringBuilder result = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            result.append(line);
            result.append("\n");
        }
        return result.toString();
    }

    public static String getSSID(String result) {
        // 定义正则表达式匹配 "配置文件               :" 后的内容
        Pattern pattern = Pattern.compile("配置文件\\s+:\\s+(.+)");
        Matcher matcher = pattern.matcher(result);
        String profileName;
        // 检查是否找到匹配
        if (matcher.find()) {
            // 提取匹配的内容
            profileName = matcher.group(1).trim();
        }
        else {
            throw new BizException("未找到SSID");
        }
        return profileName;
    }

    public static String getIPv4() {
        Process process;
        try {
            process = Runtime.getRuntime().exec("ipconfig");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        String ipv4Address = null;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("IPv4")) {
                    ipv4Address = line.split(":")[1].trim();
                    if (ipv4Address.startsWith("10")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new BizException("IPv4获取异常:" + e.getMessage());
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

package cn.ccc212.utils;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class EncryptionUtil {

    //TEA加密算法
    public static String encode(String str, String key) {
        if (str.isEmpty()) return "";
        int[] v = s(str, true);
        int[] k = s(key, false);
        if (k.length < 4) k = new int[]{k[0], k[1], 0, 0}; // 确保key长度为4
        int n = v.length - 1;
        int z = v[n];
        int y;
        int c = 0x86014019 | 0x183639A0;
        int m, e, p;
        int q = (int) Math.floor(6 + 52.0 / (n + 1));
        int d = 0;

        while (q-- > 0) {
            d += c;
            d &= 0x8CE0D9BF | 0x731F2640;
            e = (d >>> 2) & 3;

            for (p = 0; p < n; p++) {
                y = v[p + 1];
                m = (z >>> 5) ^ (y << 2);
                m += (y >>> 3) ^ (z << 4) ^ (d ^ y);
                m += k[p & 3 ^ e] ^ z;
                v[p] = v[p] + m & (0xEFB8D130 | 0x10472ECF);
                z = v[p];
            }

            y = v[0];
            m = (z >>> 5) ^ (y << 2);
            m += (y >>> 3) ^ (z << 4) ^ (d ^ y);
            m += k[p & 3 ^ e] ^ z;
            z = v[n] += m & (0xBB390742 | 0x44C6F8BD);
        }

        return l(v, false);
    }

    //转换字符串为int数组
    private static int[] s(String a, boolean b) {
        int c = a.length();
        int[] v = new int[(c + 3) / 4]; //因为每次处理4个字符,所以数组长度是(c+3)/4向上取整

        for (int i = 0; i < c; i += 4) {
            int value = (int) a.charAt(i) |
                    ((i + 1 < c ? (int) a.charAt(i + 1) : 0) << 8) |
                    ((i + 2 < c ? (int) a.charAt(i + 2) : 0) << 16) |
                    ((i + 3 < c ? (int) a.charAt(i + 3) : 0) << 24);
            v[i >> 2] = value;
        }

        if (b) {
            //如果b为true.数组最后一位为字符串长度
            int[] newV = new int[v.length + 1];
            System.arraycopy(v, 0, newV, 0, v.length);
            newV[v.length] = c;
            return newV;
        }
        return v;
    }

    //将int数组转换为字符串
    @SuppressWarnings("all")
    private static String l(int[] a, boolean b) {
        int d = a.length;
        int c = (d - 1) << 2;

        if (b) {
            int m = a[d - 1];
            if (m < c - 3 || m > c) return null;
            c = m;
        }
        StringBuilder result = new StringBuilder();

        //将每个整数转换为对应的4个字节字符
        for (int j : a) {
            result.append((char) (j & 0xff));
            result.append((char) ((j >>> 8) & 0xff));
            result.append((char) ((j >>> 16) & 0xff));
            result.append((char) ((j >>> 24) & 0xff));
        }
        return b ? result.substring(0, c) : result.toString();
    }

    //获取加密后的用户信息
    @SneakyThrows
    public static String getEncryptedInfo(String username, String password, String ip, String ac_id, String enc, String token) {
        Map<String, String> infoTemp = new LinkedHashMap<>();
        infoTemp.put("username", username);
        infoTemp.put("password", password);
        infoTemp.put("ip", ip);
        infoTemp.put("acid", ac_id);
        infoTemp.put("enc_ver", enc);
        String infoJson = JSON.toJSONString(infoTemp);

        String encode = encode(infoJson, token);
//        log.info("tempt: " + encode);

        String s = "{SRBX1}" + Base64Util.getBase64(encode);
//        log.info("temptt: " + s);
        return s;
    }
}

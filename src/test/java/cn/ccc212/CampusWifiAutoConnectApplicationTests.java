package cn.ccc212;

import lombok.SneakyThrows;
import org.apache.tomcat.util.buf.HexUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.MessageDigest;

@SpringBootTest
class CampusWifiAutoConnectApplicationTests {

    @Value("${wifi.name}")
    private String wifiName;

    @Autowired
    private Login login;

    @Test
    public void testCheck() {
        Long start = System.currentTimeMillis();

        String result = NetworkUtil.checkWifiStatus();
        System.out.println(result);
        System.out.println("ssid:"+ NetworkUtil.getSSID(result));

        System.out.println("耗时："+(System.currentTimeMillis()-start)+"ms");
        start = System.currentTimeMillis();

        String status = NetworkUtil.isWifiConnected() ? "已连接wifi" : "未连接wifi";
        System.out.println(status);

        System.out.println("耗时："+(System.currentTimeMillis()-start)+"ms");

    }

    @Test
    public void testConnect() {
        NetworkUtil.connectToWifi(wifiName);
    }

    @Test
    public void testHmacMd5() {
        String md5Str = Login.hmacMd5("123", "450e266568ed711ee6c6679b76971b371131a330f36b99801dfee22ffa68a41e");
        System.out.println(md5Str);
    }

    @Test
    public void testParse() {
        System.out.println("jQuery11240875962819453646_1726037840650({\"challenge\":\"450e266568ed711ee6c6679b76971b371131a330f36b99801dfee22ffa68a41e\",\"client_ip\":\"10.155.213.137\",\"ecode\":0,\"error\":\"ok\",\"error_msg\":\"\",\"expire\":\"60\",\"online_ip\":\"10.155.213.137\",\"res\":\"ok\",\"srun_ver\":\"SRunCGIAuthIntfSvr V1.18 B20240108\",\"st\":1726038340})"
                .replaceAll(".*\"challenge\":\"([^\"]+)\".*", "$1"));
    }

    @Test
    @SneakyThrows
    public void testSha1() {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA"); // 此处的sha代表sha1
        byte[] cipherBytes = messageDigest.digest("123".getBytes());
        String hexString = HexUtils.toHexString(cipherBytes);
        System.out.println(hexString);
    }

    @Test
    public void testLogin() {
//        NetworkUtil.connectToWifi(wifiName);
        System.out.println(login.login());
    }

    @Test
    public void testGetInfo() {
        EncryptionUtil.getEncryptedInfo("123", "123", "10.155.213.137", "25", "srun_bx1",
                "00d51a172cfc2de6dfe2fdef3f257b5df088b4f8dd3554a10d25b762a450e695");
    }

    @Test
    public void testGetIPv4() {
        System.out.println(NetworkUtil.getIPv4());
    }

    @Test
    public void testGetGateway() {
        System.out.println(NetworkUtil.getGateway());
    }

}

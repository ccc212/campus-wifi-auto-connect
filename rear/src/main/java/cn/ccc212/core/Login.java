package cn.ccc212.core;

import cn.ccc212.pojo.BaseDTO;
import cn.ccc212.pojo.GlobalState;
import cn.ccc212.utils.EncryptionUtil;
import cn.ccc212.utils.NetworkUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.tomcat.util.buf.HexUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.function.Supplier;

@Component
@Slf4j
public class Login {
    private String callbackPrefix;
    private String challenge;
    private final String action = "login";
    private String hmacMd5password;
    private String chksum;
    private String info;
    private String acId;
    private final String n = "200";
    private final String type = "1";
    private final String enc = "srun_bx1";
    private String srcIp;
    private String dstIp;

    @Value("${wifi.username}")
    private String username;
    @Value("${wifi.password}")
    private String password;

    private OkHttpClient client = new OkHttpClient().newBuilder().build();

    public void getDefaultIp() {
        srcIp = GlobalState.useRouter ? getRouterIp() : NetworkUtil.getIPv4();
        setDstIpAndAcId();
    }

    private String getValueOrDefault(String input, Supplier<String> defaultValueSupplier) {
        return StringUtil.isBlank(input) ? defaultValueSupplier.get() : input;
    }

    public void setBase(BaseDTO baseDTO) {
        String dstIp = baseDTO.getDstIp();
        String acId = baseDTO.getAcId();

        username = getValueOrDefault(baseDTO.getUsername(), () -> username);
        password = getValueOrDefault(baseDTO.getPassword(), () -> password);
        this.dstIp = getValueOrDefault(dstIp, () -> this.dstIp);

        if (!StringUtil.isBlank(acId)) {
            this.acId = acId;
        }
        else if (StringUtil.isBlank(acId)) {
            setDstIpAndAcId();
        }

        GlobalState.useRouter = !StringUtil.isBlank(dstIp) || !StringUtil.isBlank(acId);
        srcIp = GlobalState.useRouter ? getRouterIp() : NetworkUtil.getIPv4();
    }

    private String getRouterIp() {
        srcIp = NetworkUtil.getIPv4();
        String result = login();
        return StringUtil.isBlank(result) ? result : result.split("client_ip\":\"")[1].split("\"")[0];
    }

    @SneakyThrows
    private void init() {
        if (!GlobalState.useRouter) {
            String ipv4 = NetworkUtil.getIPv4();
            if (!srcIp.equals(ipv4) && StringUtil.isBlank(ipv4)) {
                srcIp = ipv4;
            }
        }
        log.info("srcIp = " + srcIp);
        log.info("dstIp = " + dstIp);
        log.info("acId = " + acId);
        callbackPrefix = "jQuery112405644064296283513";
        challenge = getChallenge();
        info = getInfo();
        hmacMd5password = hmacMd5("", challenge);
        chksum = getChksum();
    }

    private void setDstIpAndAcId() {
        String gatewayUrl = "http://" + NetworkUtil.getGateway();
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(gatewayUrl).openConnection();
        } catch (IOException e) {
            log.error(e.getMessage());
            return;
        }
        //设置重定向跟随行为
        connection.setInstanceFollowRedirects(false);
        try {
            connection.connect();
        } catch (Exception e) {
            log.error("无法连接网关，可能还未连接到校园网，请关闭并重新启动自动登录;或是使用路由器却未设置acId，异常信息为：" + e.getMessage());
            return;
        }
        //获取跳转后的URL
        String redirectHtml = connection.getHeaderField("Location");
//        log.info("redirectHtml = " + redirectHtml);
        connection.disconnect();

        if (redirectHtml != null) {
            try {
                dstIp = new URL(redirectHtml).getHost();
            } catch (MalformedURLException e) {
                log.error(e.getMessage());
                return;
            }
            setAcId(redirectHtml);
        } else {
            log.info("无重定向");
        }
    }

    @SneakyThrows
    private void setAcId(String redirectHtml) {
        String[] split = redirectHtml.split("/");
        for (String s : split) {
            if (s.startsWith("index_")) {
                acId = s.split("\\.")[0].replaceAll("index_", "");
                return;
            }
        }
    }


    //获取challenge值
    private String getChallenge() {
        String challengeUrl = UriComponentsBuilder.fromHttpUrl("http://" + dstIp + "/cgi-bin/get_challenge")
                .queryParam("callback", callbackPrefix)
                .queryParam("username", username)
                .queryParam("ip", srcIp)
                .toUriString();
//        log.info("challengeUrl = " + challengeUrl);

        Request request = new Request.Builder()
                .url(challengeUrl)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0")
                .build();
        Response response;
        String challengeResponse;
        try {
            response = client.newCall(request).execute();
            challengeResponse = response.body().string();
        } catch (Exception e) {
            log.error("无法连到校园网，请检查wifi是否连接校园网 或 在配置里开启自动选择校园网，异常信息为：" + e.getMessage());
            return "";
        }
//        log.info("challengeResponse = " + challengeResponse);

        return challengeResponse.replaceAll(".*\"challenge\":\"([^\"]+)\".*", "$1");
    }

    //获取chksum值
    @SneakyThrows
    private String getChksum() {
        String chksum = challenge + username;
        chksum += challenge + hmacMd5password;
        chksum += challenge + acId;
        chksum += challenge + srcIp;
        chksum += challenge + n;
        chksum += challenge + type;
        chksum += challenge + info;
//        log.info("chksum = " + chksum);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA"); // 此处的sha代表sha1
        byte[] cipherBytes = messageDigest.digest(chksum.getBytes());
        String hexString = HexUtils.toHexString(cipherBytes);
        return hexString;
    }

    private String getInfo() {
        return EncryptionUtil.getEncryptedInfo(username, password, srcIp, acId, enc, challenge);
    }

    //生成加密后的密码
    @SneakyThrows
    public static String hmacMd5(String password, String challenge) {
        if (StringUtil.isBlank(challenge)) {
            return "";
        }
        SecretKeySpec keySpec = new SecretKeySpec(challenge.getBytes(), "HmacMD5");

        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(keySpec);

        byte[] hmac = mac.doFinal(password.getBytes());
        String sb = new String();
        for (byte b : hmac) {
            sb += String.format("%02x", b);
        }

        return sb;
    }

    //发送登录请求
    @SneakyThrows
    public String login() {
        init();

        String loginUrl = UriComponentsBuilder.fromHttpUrl("http://" + dstIp + "/cgi-bin/srun_portal")
                .queryParam("callback", callbackPrefix)
                .queryParam("action", action)
                .queryParam("username", username)
                .queryParam("password", "{MD5}" + hmacMd5password)
                .queryParam("ac_id", acId)
                .queryParam("ip", srcIp)
                .queryParam("chksum", chksum)
                .queryParam("n", n)
                .queryParam("type", type)
                .toUriString() + "&info=" + info.replace("+", "%2B").replace("/", "%2F");
//        log.info("loginUrl = " + loginUrl);
//
//        log.info("callbackPrefix = " + callbackPrefix);
//        log.info("challenge = " + challenge);
//        log.info("action = " + action);
//        log.info("hmacMd5password = " + "{MD5}" + hmacMd5password);
//        log.info("chksum = " + chksum);
//        log.info("info = " + info);
//        log.info("acId = " + acId);
//        log.info("n = " + n);
//        log.info("type = " + type);
//        log.info("enc = " + enc);
//        log.info("srcIp = " + srcIp);
//        log.info("dstIp = " + dstIp);

        Request request = new Request.Builder()
                .url(loginUrl)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.error("请求失败，异常信息为：" + e.getMessage());
            return "";
        }
        return response.body().string();
    }
}

package cn.ccc212.core;

import cn.ccc212.pojo.BaseDTO;
import cn.ccc212.utils.EncryptionUtil;
import cn.ccc212.utils.NetworkUtil;
import lombok.SneakyThrows;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

@Component
public class Login {
    private String loginPage;
    private String callbackPrefix;
    private String challenge;
    private final String action = "login";
    private String hmacMd5password;
    private String chksum;
    private String info;
    private String ac_id;
    private final String n = "200";
    private final String type = "1";
    private String enc = "srun_bx1";
    private String srcIp;
    private String dstIp;

    @Value("${wifi.username}")
    private String username;
    @Value("${wifi.password}")
    private String password;

    private OkHttpClient client = new OkHttpClient().newBuilder().build();

    {
        srcIp = NetworkUtil.getIPv4();
        setDstIpAndAcId();
    }

    @SneakyThrows
    private void init(BaseDTO baseDTO) {
        if (!StringUtil.isBlank(baseDTO.getUsername())) {
            username = baseDTO.getUsername();
        }
        if (!StringUtil.isBlank(baseDTO.getPassword())) {
            password = baseDTO.getPassword();
        }

        if (!StringUtil.isBlank(baseDTO.getSrcIp())) {
            srcIp = baseDTO.getSrcIp();
        }
        else if (StringUtil.isBlank(srcIp)) {
            srcIp = NetworkUtil.getIPv4();
        }

//        if (!StringUtil.isBlank(baseDTO.getDstIp())) {
//            dstIp = baseDTO.getDstIp();
//            String gatewayUrl = "http://" + dstIp;
//            HttpURLConnection connection = (HttpURLConnection) new URL(gatewayUrl).openConnection();
//            connection.setInstanceFollowRedirects(false);
//            connection.connect();
//            setAcId(connection.getHeaderField("Location"));
//        }
//        else if (StringUtil.isBlank(dstIp)) {
//            dstIp = NetworkUtil.getGateway();
//        }

        System.out.println("srcIp = " + srcIp);
        System.out.println("dstIp = " + dstIp);
        System.out.println("ac_id = " + ac_id);
        callbackPrefix = "jQuery112405644064296283513";
        loginPage = "http://" + dstIp + "/srun_portal_pc?ac_id=" + ac_id + "&theme=bit";
        challenge = getChallenge();
        info = getInfo();
        hmacMd5password = hmacMd5("", challenge);
        chksum = getChksum();
    }

    @SneakyThrows
    private void setDstIpAndAcId() {
        String gatewayUrl = "http://" + NetworkUtil.getGateway();
        HttpURLConnection connection = (HttpURLConnection) new URL(gatewayUrl).openConnection();
        //设置重定向跟随行为
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        //获取跳转后的URL
        String redirectHtml = connection.getHeaderField("Location");
        System.out.println("redirectHtml = " + redirectHtml);
        connection.disconnect();

        if (redirectHtml != null) {
            dstIp = new URL(redirectHtml).getHost();
            setAcId(redirectHtml);
        } else {
            System.out.println("无重定向");
        }
    }

    @SneakyThrows
    private void setAcId(String redirectHtml) {
//        //访问页面并获取HTML内容
//        Document doc = Jsoup.connect(redirectHtml).get();
//        //查找页面中的跳转链接
//        Element metaRefresh = doc.selectFirst("meta[http-equiv=refresh]");
//        if (metaRefresh != null) {
//            //提取URL
//            String content = metaRefresh.attr("content");
//            String redirectUrl = content.split("url=")[1];
//
//            String[] queryParams = redirectUrl.split("\\?")[1].split("&");
//            for (String queryParam : queryParams) {
//                if (queryParam.startsWith("ac_id=")) {
//                    ac_id = queryParam.split("=")[1];
//                    break;
//                }
//            }
//        }
        String[] split = redirectHtml.split("/");
        for (String s : split) {
            if (s.startsWith("index_")) {
                ac_id = s.split("\\.")[0].replaceAll("index_", "");
                return;
            }
        }
    }


    //获取challenge值
    @SneakyThrows
    private String getChallenge() {
        String challengeUrl = UriComponentsBuilder.fromHttpUrl("http://" + dstIp + "/cgi-bin/get_challenge")
                .queryParam("callback", callbackPrefix)
                .queryParam("username", username)
                .queryParam("ip", srcIp)
                .toUriString();
        System.out.println("challengeUrl = " + challengeUrl);

        Request request = new Request.Builder()
                .url(challengeUrl)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0")
                .build();
        Response response = client.newCall(request).execute();
        String challengeResponse = response.body().string();
        System.out.println("challengeResponse = " + challengeResponse);

        return challengeResponse.replaceAll(".*\"challenge\":\"([^\"]+)\".*", "$1");
    }

    //获取chksum值
    @SneakyThrows
    private String getChksum() {
        String chksum = challenge + username;
        chksum += challenge + hmacMd5password;
        chksum += challenge + ac_id;
        chksum += challenge + srcIp;
        chksum += challenge + n;
        chksum += challenge + type;
        chksum += challenge + info;
        System.out.println("chksum = " + chksum);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA"); // 此处的sha代表sha1
        byte[] cipherBytes = messageDigest.digest(chksum.getBytes());
        String hexString = HexUtils.toHexString(cipherBytes);
        return hexString;
    }

    private String getInfo() {
        return EncryptionUtil.getEncryptedInfo(username, password, srcIp, ac_id, enc, challenge);
    }

    //生成加密后的密码
    @SneakyThrows
    public static String hmacMd5(String password, String challenge) {
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
    public String login(BaseDTO baseDTO) {
        init(baseDTO);

        String loginUrl = UriComponentsBuilder.fromHttpUrl("http://" + dstIp + "/cgi-bin/srun_portal")
                .queryParam("callback", callbackPrefix)
                .queryParam("action", action)
                .queryParam("username", username)
                .queryParam("password", "{MD5}" + hmacMd5password)
                .queryParam("ac_id", ac_id)
                .queryParam("ip", srcIp)
                .queryParam("chksum", chksum)
                .queryParam("n", n)
                .queryParam("type", type)
                .toUriString() + "&info=" + info.replace("+", "%2B").replace("/", "%2F");
        System.out.println("loginUrl = " + loginUrl);

        System.out.println("callbackPrefix = " + callbackPrefix);
        System.out.println("challenge = " + challenge);
        System.out.println("action = " + action);
        System.out.println("hmacMd5password = " + "{MD5}" + hmacMd5password);
        System.out.println("chksum = " + chksum);
        System.out.println("info = " + info);
        System.out.println("ac_id = " + ac_id);
        System.out.println("n = " + n);
        System.out.println("type = " + type);
        System.out.println("enc = " + enc);
        System.out.println("srcIp = " + srcIp);
        System.out.println("dstIp = " + dstIp);

        Request request = new Request.Builder()
                .url(loginUrl)
                .get()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}

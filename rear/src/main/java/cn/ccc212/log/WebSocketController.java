package cn.ccc212.log;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@ServerEndpoint("/log")
@RestController
@Slf4j
public class WebSocketController {

    private Process process;
    private InputStream inputStream;

    @OnOpen
    public void onOpen(Session session) {
        try {
            //获取项目根目录
            String projectDir = System.getProperty("user.dir");
            String logFilePath = projectDir + "\\cwac\\application.log";
            process = Runtime.getRuntime().exec("powershell.exe Get-Content " + logFilePath + " -Wait");
            inputStream = process.getInputStream();
            TailfLogThread thread = new TailfLogThread(inputStream, session);
            thread.start();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @OnClose
    public void onClose() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if (process != null) {
            process.destroy();
        }
    }

    @OnError
    public void onError(Throwable thr) {
        log.error(thr.getMessage());
    }
}
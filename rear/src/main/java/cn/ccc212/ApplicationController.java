package cn.ccc212;

import cn.ccc212.core.Check;
import cn.ccc212.core.Login;
import cn.ccc212.pojo.BaseDTO;
import cn.ccc212.pojo.ConfigDTO;
import cn.ccc212.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@Tag(name = "接口")
@Slf4j
public class ApplicationController {

    @Autowired
    private Login login;
    @Autowired
    private Check check;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @PostMapping("/start")
    @Operation(summary = "启动")
    public Result start(@RequestBody BaseDTO baseDTO) {
        login.setBase(baseDTO);
        check.setNetworkName(baseDTO.getNetworkName());
        check.startTask();
        log.info("启动成功");
        return Result.success("启动成功");
    }

    @PostMapping("/stop")
    @Operation(summary = "暂停")
    public Result stop() {
        check.stopTask();
        return Result.success("暂停成功");
    }

    @PostMapping("/config")
    @Operation(summary = "配置")
    public Result config(@RequestBody ConfigDTO configDTO) {
        check.setConfig(configDTO);
        log.info("配置成功");
        return Result.success("配置成功");
    }

    @GetMapping(value = "/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        SseEmitter emitter = new SseEmitter();

        executorService.submit(() -> {
            long lastKnownPosition = 0;
            File logFile = new File("application.log");

            while (true) {
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    // 如果文件的大小大于上次读取的位置，说明有新的内容写入
                    if (logFile.length() > lastKnownPosition) {
                        reader.skip(lastKnownPosition);

                        String line;
                        while ((line = reader.readLine()) != null) {
                            emitter.send(SseEmitter.event().data(line));
                        }

                        // 更新最后已知的文件位置
                        lastKnownPosition = logFile.length();
                    }

                    // 等待一段时间后再检查
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    emitter.completeWithError(e);
                    break;
                }
            }

            emitter.complete();
        });

        return emitter;
    }

}

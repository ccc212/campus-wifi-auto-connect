package cn.ccc212.core;

import cn.ccc212.pojo.ConfigDTO;
import cn.ccc212.pojo.GlobalState;
import cn.ccc212.utils.NetworkUtil;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class Check {

    @Setter
    @Value("${wifi.name}")
    private String networkName;

    @Autowired
    private Login login;
    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> checkTask;
    private ScheduledFuture<?> reconnectTask;

    private LocalTime startTime = LocalTime.of(7, 0); //默认七点开始执行
    private LocalTime endTime = LocalTime.of(23, 30); //默认晚上十一点半关闭
    private long interval = 10 * 1000; //默认任务执行间隔10秒
    private boolean autoSelect = true; //默认开启自动选择校园网

    public String setConfig(ConfigDTO configDTO) {
        long interval = Long.parseLong(configDTO.getInterval());

        if (!StringUtil.isBlank((configDTO.getInterval()))) {
            if (interval < 5) {
                log.error("执行间隔不能小于5");
                return "执行间隔不能小于5";
            }
            this.interval = interval * 1000;
        }
        if (!StringUtil.isBlank((configDTO.getStartTime()))) startTime = LocalTime.parse(configDTO.getStartTime());
        if (!StringUtil.isBlank((configDTO.getEndTime()))) endTime = LocalTime.parse(configDTO.getEndTime());
        if (!StringUtil.isBlank((configDTO.getAutoSelect()))) autoSelect = Boolean.parseBoolean(configDTO.getAutoSelect());

        if (checkTask != null && !checkTask.isCancelled()) {
            stopTask();
            startTask();
        }

        log.info("配置成功");
        return "配置成功";
    }

    //每天凌晨3点清理一次日志
    @Scheduled(cron = "0 0 3 * * ?")
    public void clearLog() {
        File file = new File("./cwac/application.log");
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("\n");
            log.info("每天定时清理");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    //定时监测是否需要开启或关闭任务（每分钟检查一次）
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void monitorTask() {
        if (GlobalState.isStop) return;
        LocalTime now = LocalTime.now();
        if (startTime == null || endTime == null) {
            return;
        }
        if (startTime.isBefore(endTime)) {
            if (now.isAfter(startTime) && now.isBefore(endTime)) {
                if (checkTask != null && !checkTask.isCancelled()) {
                    return;
                }
                startTask();
            } else {
                stopTask();
            }
        }
        else {
            if (now.isAfter(startTime) || now.isBefore(endTime)) {
                if (checkTask != null && !checkTask.isCancelled()) {
                    return;
                }
                startTask();
            } else {
                stopTask();
            }
        }
    }

    @SneakyThrows
    public String startTask() {
        String result = "启动成功";

        if (startTime.equals(endTime)) {
            GlobalState.isStop = false;
            log.info("自动连接全天执行");
            if (checkTask != null && !checkTask.isCancelled()) {
                return result;
            }
            checkTask = taskScheduler.scheduleAtFixedRate(this::check,
                    new Date(),
                    interval);
            return result;
        }

        LocalDate today = LocalDate.now();

        LocalDateTime startDateTime = LocalDateTime.of(today, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(today, endTime);

        if (endTime.isBefore(startTime)) {
            endDateTime = endDateTime.plusDays(1);
        }

        LocalDateTime now = LocalDateTime.now();

        long startDelay = ChronoUnit.MILLIS.between(now, startDateTime);
        long endDelay = ChronoUnit.MILLIS.between(now, endDateTime);

        if (startDelay < 0 && endDelay > 0) {
            GlobalState.isStop = false;
            log.info("自动连接在 " + startTime + " 启动");
            log.info("自动连接在 " + endTime + " 停止");
            if (checkTask != null && !checkTask.isCancelled()) {
                return result;
            }
            checkTask = taskScheduler.scheduleAtFixedRate(this::check,
                    new Date(System.currentTimeMillis() + startDelay),
                    interval);
        }

        if (endDelay < 0 || startDelay > 0) {
            result = "不在执行的时间范围内";
            stopTask();
        }

        return result;
    }

    public void stopTask() {
        GlobalState.isStop = true;
        if (checkTask != null && !checkTask.isCancelled()) {
            checkTask.cancel(true);
        }
        if (reconnectTask != null && !reconnectTask.isCancelled()) {
            reconnectTask.cancel(true);
        }
        log.info("连接已停止");
    }

    private void check() {
        if (GlobalState.isStop) return;
        String ssid = NetworkUtil.getSSID(NetworkUtil.checkWifiStatus());
        if (StringUtil.isBlank(ssid)) return;
        boolean wifiConnected = NetworkUtil.isWifiConnected();
        log.info("ssid = " + ssid);
        log.info("wifiConnected = " + wifiConnected);
        if (ssid.equals(networkName) && !wifiConnected) {
            log.info(login.login());
        }
        else if (!ssid.equals(networkName) && autoSelect) {
            stopTask();
            log.info("正在尝试重新连接");
            NetworkUtil.connectToWifi(networkName);
            reconnectTask = taskScheduler.schedule(() -> {
                login.getDefaultIp();
                startTask();
                log.info("重新连接成功");
            }, new Date(System.currentTimeMillis() + interval));
        }
    }
}

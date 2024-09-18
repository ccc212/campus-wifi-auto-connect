package cn.ccc212;

import cn.ccc212.core.Login;
import cn.ccc212.exception.BizException;
import cn.ccc212.pojo.ConfigDTO;
import cn.ccc212.utils.NetworkUtil;
import lombok.SneakyThrows;
import org.jsoup.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

@Service
public class CheckService {

    @Value("${wifi.name}")
    private String wifiName;

    @Autowired
    private Login login;
    @Autowired
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> futureTask;
    private LocalTime startTime = LocalTime.of(7, 0); //默认七点执行
    private LocalTime endTime = LocalTime.of(23, 30); //默认晚上十一点半关闭
    private long interval = 10 * 1000; //默认任务执行间隔10秒
    private boolean autoSelect = true; //默认开启自动选择校园网
    private boolean isStop = true;

    public void setConfig(ConfigDTO configDTO) {
        if (!StringUtil.isBlank((configDTO.getStartTime()))) startTime = LocalTime.parse(configDTO.getStartTime());
        if (!StringUtil.isBlank((configDTO.getEndTime()))) endTime = LocalTime.parse(configDTO.getEndTime());
        if (!StringUtil.isBlank((configDTO.getInterval()))) interval = Long.parseLong(configDTO.getInterval()) * 1000;
        if (!StringUtil.isBlank((configDTO.getAutoSelect()))) autoSelect = Boolean.parseBoolean(configDTO.getAutoSelect());
    }

    //定时监测是否需要开启或关闭任务（每分钟检查一次）
    @Scheduled(cron = "0 0/1 * * * ? ")
    public void monitorTask() {
        if (!isStop) {
            LocalTime now = LocalTime.now();
            if (startTime == null || endTime == null) {
                return;
            }
            if (now.isAfter(startTime) && now.isBefore(endTime)) {
                startTask();
            } else {
                stopTask();
            }
        }
    }

    @SneakyThrows
    public void startTask() {
        //当前日期
        LocalDate today = LocalDate.now();

        //将startTime和endTime转换为当天的LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.of(today, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(today, endTime);

        //当前时间
        LocalDateTime now = LocalDateTime.now();

        //计算启动时间和结束时间的延迟（毫秒）
        long startDelay = ChronoUnit.MILLIS.between(now, startDateTime);
        long endDelay = ChronoUnit.MILLIS.between(now, endDateTime);

        if (startDelay < 0 && endDelay > 0) {
            isStop = false;
            //如果有任务,则不用重新分配任务
            if (futureTask != null && !futureTask.isCancelled()) {
                return;
            }
            futureTask = taskScheduler.scheduleAtFixedRate(this::check,
                    new Date(System.currentTimeMillis() + startDelay),
                    interval);
            System.out.println("自动连接在 " + startTime + " 启动");
            System.out.println("自动连接在 " + endTime + " 停止");
        }

        if (endDelay < 0 && futureTask != null) {
            taskScheduler.schedule(this::stopTask, new Date(System.currentTimeMillis() + endDelay));
            isStop = true;
        }
    }

    public void stopTask() {
        isStop = true;
        if (futureTask != null && !futureTask.isCancelled()) {
            futureTask.cancel(true);
        }
        System.out.println("自动连接已停止");
    }

    private void check() {
        String ssid = NetworkUtil.getSSID(NetworkUtil.checkWifiStatus());
        boolean wifiConnected = NetworkUtil.isWifiConnected();
        System.out.println("ssid = " + ssid);
        System.out.println("wifiConnected = " + wifiConnected);
        if (ssid.equals(wifiName) && !wifiConnected) {
            System.out.println(login.login());
        }
        else if (!ssid.equals(wifiName) && autoSelect) {
            stopTask();
            NetworkUtil.connectToWifi(wifiName);
            login.getDefaultIp();
            startTask();
        }
    }
}

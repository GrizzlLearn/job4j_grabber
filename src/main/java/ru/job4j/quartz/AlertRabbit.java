package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static final String path = "./src/main/resources/app.properties";
    private final Properties config;

    public AlertRabbit(Properties config) {
        this.config = config;
    }

    /**
     * Main инициализирует Scheduler, JobDetail, SimpleScheduleBuilder и trigger и запускает job.
     * @see AlertRabbit#main(String[]) 
     * @param args Массив аргументов командной строки, переданных программе при запуске.
     * @throws IOException если происходит ошибка при чтении файла настроек.
     * @see AlertRabbit#loadProp()
     */
    public static void main(String[] args) throws IOException {
        Properties cfg = loadProp();
        AlertRabbit alertRabbit = new AlertRabbit(cfg);
        Optional<String> propVal = Optional.ofNullable(alertRabbit.config.getProperty("rabbit.interval"));
        int interval = 0;

        if (propVal.isPresent()) {
            interval = Integer.parseInt(propVal.get());
        }

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    /**
     * Действие, выполняемое JobDetail.
     */
    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
        }
    }

    /**
     * Метод выполняет чтение файла настроек и отдаёт их наружу
     * @return Объект Properties представляющий настройки для SimpleScheduleBuilder.
     * @throws IOException если происходит ошибка при чтении файла настроек.
     */
    private static Properties loadProp() throws IOException{
        Properties tmp = new Properties();
        try (InputStream fileInputStream = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            tmp.load(fileInputStream);
        }

        return tmp;
    }
}

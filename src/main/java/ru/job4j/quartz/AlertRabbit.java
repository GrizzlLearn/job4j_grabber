package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Optional;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static final String path = "./src/main/resources/app.properties";
    private final Properties config;
    private final Connection cn;

    public AlertRabbit(Properties config, Connection cn) {
        this.config = config;
        this.cn = cn;
    }

    /**
     * Main инициализирует Scheduler, JobDetail, SimpleScheduleBuilder и trigger и запускает job.
     * @see AlertRabbit#main(String[]) 
     * @param args Массив аргументов командной строки, переданных программе при запуске.
     * @throws IOException если происходит ошибка при чтении файла настроек.
     * @throws ClassNotFoundException
     * @throws SQLException
     * @see AlertRabbit#loadProp()
     */
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        Properties cfg = loadProp();
        Connection cn = dbConnection(cfg);
        AlertRabbit alertRabbit = new AlertRabbit(cfg, cn);
        Optional<String> propVal = Optional.ofNullable(alertRabbit.config.getProperty("rabbit.interval"));
        int interval = 0;

        if (propVal.isPresent()) {
            interval = Integer.parseInt(propVal.get());
        }

        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connect", cn);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            System.out.println(cn);
        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Действие, выполняемое JobDetail.
     */
    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println("Rabbit runs here ...");
        }
        @Override
        public void execute(JobExecutionContext context) {
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connect");
            String tableName = "rabbit";
            String sql = String.format("INSERT INTO %s(created_date) VALUES(?);",
                    tableName
            );
            try (PreparedStatement s = cn.prepareStatement(sql)) {
                s.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                s.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод выполняет чтение файла настроек и отдаёт их наружу
     * @return Объект Properties представляющий настройки для SimpleScheduleBuilder.
     * @throws IOException если происходит ошибка при чтении файла настроек.
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static Properties loadProp() throws IOException, ClassNotFoundException, SQLException {
        Properties tmp = new Properties();
        try (InputStream fileInputStream = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            tmp.load(fileInputStream);
        }

        return tmp;
    }

    /**
     *
     * @param prop
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static Connection dbConnection(Properties prop) throws ClassNotFoundException, SQLException {
        Class.forName(prop.getProperty("jdbc.driver"));

        return DriverManager.getConnection(
                prop.getProperty("jdbc.url"),
                prop.getProperty("jdbc.username"),
                prop.getProperty("jdbc.password")
        );
    }
}

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

    /**
     * Main инициализирует Scheduler, JobDetail, SimpleScheduleBuilder и trigger и запускает job.
     * @see AlertRabbit#main(String[]) 
     * @param args Массив аргументов командной строки, переданных программе при запуске.
     * @throws IOException если происходит ошибка при чтении файла настроек.
     * @throws ClassNotFoundException Если класс не может быть найден во время выполнения.
     * @throws SQLException Если произошла ошибка при подключении к БД.
     * @see AlertRabbit#loadProp()
     * @see AlertRabbit#prepareTable(Connection)
     * @see AlertRabbit#dbConnection(Properties)
     */
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        int interval = 0;
        Properties cfg = loadProp();
        try (Connection cn = dbConnection(cfg)) {
            prepareTable(cn);
            Optional<String> propVal = Optional.ofNullable(cfg.getProperty("rabbit.interval"));

            if (propVal.isPresent()) {
                interval = Integer.parseInt(propVal.get());
            }

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
            Connection cn = (Connection) context
                    .getJobDetail()
                    .getJobDataMap()
                    .get("connect");
            String tableName = "rabbit";
            String sql = String.format("INSERT INTO %s(created_date) VALUES(?);",
                    tableName
            );
            try (PreparedStatement s = cn.prepareStatement(sql)) {
                s.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                s.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Метод выполняет чтение файла настроек и отдаёт их наружу
     * @return Объект Properties содержащий в себе настройки для SimpleScheduleBuilder.
     * @throws IOException если происходит ошибка при чтении файла настроек.
     */
    private static Properties loadProp() throws IOException {
        Properties tmp = new Properties();
        try (InputStream fileInputStream = AlertRabbit.class
                .getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            tmp.load(fileInputStream);
        }

        return tmp;
    }

    /**
     * Метод создаёт нужную таблицу в БД, если её не существует.
     * @param cn Объект Connection для выполнения sql запроса.
     * @throws SQLException Если произошла ошибка при выполнении запроса.
     */
    private static void prepareTable(Connection cn) throws SQLException {
        String sql = String.format("CREATE TABLE IF NOT EXISTS %s(%s);",
                "rabbit",
                "created_date TIMESTAMP NOT NULL"
                );
        System.out.println();
        try (PreparedStatement s = cn.prepareStatement(sql)) {
            s.execute();
        }
    }

    /**
     * Метод выполняет соединение с БД PostgreQL
     * @param prop Объект Properties содержащий в себе настройки для SimpleScheduleBuilder.
     * @return Объект Connection всю необходимую информацию для работы с БД.
     * @throws ClassNotFoundException Если класс не может быть найден во время выполнения.
     * @throws SQLException Если произошла ошибка при подключении к БД.
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

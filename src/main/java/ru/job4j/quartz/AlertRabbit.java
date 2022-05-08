package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

  private static Properties rabbitProps;

  public static void main(String[] args) {
    try {
      Class.forName(getRabbitProperty("db.driver"));
      try (Connection connection = DriverManager.getConnection(
              getRabbitProperty("db.url"),
              getRabbitProperty("db.login"),
              getRabbitProperty("db.password")
      )) {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        JobDataMap jobData = new JobDataMap();
        jobData.put("connection", connection);
        JobDetail job = newJob(Rabbit.class)
                .usingJobData(jobData)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt(getRabbitProperty("rabbit.interval")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
        Thread.sleep(10000);
        scheduler.shutdown();
      }
    } catch (Exception se) {
      se.printStackTrace();
    }
  }

  public static class Rabbit implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
      JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
      Connection connection = (Connection) jobDataMap.get("connection");
      try (PreparedStatement statement = connection.prepareStatement("INSERT INTO rabbit (created_date) VALUES (?);")) {
        statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        statement.execute();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      System.out.println("Rabbit runs here ...");
    }
  }

  private static String getRabbitProperty(String property) throws IOException {
    if (rabbitProps == null) {
      try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
        rabbitProps = new Properties();
        rabbitProps.load(in);
      }
    }
    return rabbitProps.getProperty(property);
  }
}
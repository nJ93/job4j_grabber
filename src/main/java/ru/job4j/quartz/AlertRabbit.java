package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

  private static Properties rabbitProps;

  public static void main(String[] args) {
    try {
      Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
      scheduler.start();
      JobDetail job = newJob(Rabbit.class).build();
      SimpleScheduleBuilder times = simpleSchedule()
              .withIntervalInSeconds(Integer.parseInt(getRabbitProperty("rabbit.interval")))
              .repeatForever();
      Trigger trigger = newTrigger()
              .startNow()
              .withSchedule(times)
              .build();
      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException | IOException se) {
      se.printStackTrace();
    }
  }

  public static class Rabbit implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
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
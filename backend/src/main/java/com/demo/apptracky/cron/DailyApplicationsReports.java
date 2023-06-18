package com.demo.apptracky.cron;

import com.demo.apptracky.dao.UserDao;
import com.demo.apptracky.entities.User;
import com.demo.apptracky.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@EnableScheduling
public class DailyApplicationsReports {
    private static Logger log = LoggerFactory.getLogger(DailyApplicationsReports.class);

    private static final long ONE_DAY_INTERVAL = 1000 * 60 * 60 * 24;

    private final UserDao userDao;

    private final EmailService emailService;

    public DailyApplicationsReports(final UserDao userDao, final EmailService emailService) {
        this.userDao = userDao;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 0 * * *" /* fixedRate = 10000 */)
    public void notifyApplicationStates() {
        final List<User> users = userDao.findAllByIsReportingEnabled();
        log.info("Sending emails for {} users", users.size());
        // TODO maybe do it in chunks of 50 (SES limit)
        if (!users.isEmpty()) {
            emailService.sendApplicationsReports(users, ONE_DAY_INTERVAL * 20);
        }
    }
}

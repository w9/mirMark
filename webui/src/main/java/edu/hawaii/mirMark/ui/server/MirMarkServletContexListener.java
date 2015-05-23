package edu.hawaii.mirMark.ui.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MirMarkServletContexListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Results.getReady();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}

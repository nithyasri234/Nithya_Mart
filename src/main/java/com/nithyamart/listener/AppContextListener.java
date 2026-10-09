package com.nithyamart.listener;

import java.sql.Connection;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.nithyamart.util.DatabaseMigrationUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@WebListener
public class AppContextListener implements ServletContextListener {

    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            String jdbcUrl = System.getenv("JDBC_URL");
            if (jdbcUrl == null || jdbcUrl.isBlank()) {
                jdbcUrl = System.getProperty("jdbc.url", "jdbc:h2:file:./data/nithyamart");
            }

            if (jdbcUrl.startsWith("jdbc:h2:file:")) {
                String filePath = jdbcUrl.substring("jdbc:h2:file:".length());
                int semicolonIdx = filePath.indexOf(';');
                if (semicolonIdx != -1) {
                    filePath = filePath.substring(0, semicolonIdx);
                }
                java.io.File dbFile = new java.io.File(filePath);
                java.io.File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
            }

            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(jdbcUrl);
            config.setUsername("sa");
            config.setPassword("");
            config.setDriverClassName("org.h2.Driver");

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);

            dataSource = new HikariDataSource(config);

            try (Connection connection = dataSource.getConnection()) {

                DatabaseMigrationUtil.runMigrations(connection);

                System.out.println("Database migrations completed successfully.");
            }

            ServletContext context = sce.getServletContext();
            context.setAttribute("dataSource", dataSource);

            System.out.println("Database connection pool initialized successfully.");

        } catch (Exception e) {

            System.err.println("Database initialization failed.");

            e.printStackTrace();

            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }

            throw new RuntimeException(
                    "Unable to initialize application database.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        if (dataSource != null) {
            dataSource.close();
            System.out.println("Database connection pool closed.");
        }
    }
}
package com.nithyamart.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.h2.tools.RunScript;

public final class DatabaseMigrationUtil {

   private static final String[] MIGRATIONS = {
    "V1__init_schema.sql",
    "V2__products_schema.sql",
    "V3__cart_schema.sql",
    "V4__orders_schema.sql",
    "V5__reviews_schema.sql",
    "V6__demo_products.sql"
};

    private DatabaseMigrationUtil() {
    }

    public static void runMigrations(Connection connection) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS schema_version (
                    version VARCHAR(50) PRIMARY KEY,
                    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """)) {

            statement.executeUpdate();
        }

        for (String migration : MIGRATIONS) {

            String version = migration.substring(0, migration.indexOf("__"));

            if (isAlreadyApplied(connection, version)) {
                System.out.println("Database migration already applied: " + migration);
                continue;
            }

            System.out.println("Running database migration: " + migration);

            String resourcePath = "db/migrations/" + migration;

            InputStream inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(resourcePath);

            if (inputStream == null) {
                throw new IllegalStateException(
                        "Migration file not found in application: " + resourcePath);
            }

            try (Reader reader = new InputStreamReader(
                    inputStream, StandardCharsets.UTF_8)) {

                RunScript.execute(connection, reader);
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO schema_version (version) VALUES (?)")) {

                statement.setString(1, version);
                statement.executeUpdate();
            }

            System.out.println("Migration completed: " + migration);
        }
        String seedPath = "db/seed.sql";

InputStream seedInputStream = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream(seedPath);

if (seedInputStream == null) {
    throw new IllegalStateException(
            "Seed file not found: " + seedPath);
}

try (Reader reader = new InputStreamReader(
        seedInputStream,
        StandardCharsets.UTF_8)) {

    RunScript.execute(connection, reader);
}

System.out.println("Demo seed data loaded successfully.");
    }

    private static boolean isAlreadyApplied(
            Connection connection,
            String version) throws Exception {

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM schema_version WHERE version = ?")) {

            statement.setString(1, version);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
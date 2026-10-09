package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.AdminDAO;
import com.nithyamart.util.DatabaseMigrationUtil;
import com.nithyamart.util.JsonUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AdminServletTest {

    private static HikariDataSource dataSource;
    private static AdminDAO adminDAO;
    private static Gson gson;

    @BeforeAll
    public static void setUp() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:admin_test_db;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMigrationUtil.runMigrations(connection);
        }

        adminDAO = new AdminDAO(dataSource);
        gson = JsonUtil.getGson();
    }

    @AfterAll
    public static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    public void testGetAdminAllData() throws Exception {
        AdminServlet servlet = new AdminServlet(adminDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("ADMIN");
        when(req.getPathInfo()).thenReturn(null);

        StringWriter writer = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(writer));

        servlet.doGet(req, res);

        String json = writer.toString();
        assertNotNull(json);
        assertFalse(json.contains("Unable to load admin data."));
        assertTrue(json.contains("users"));
        assertTrue(json.contains("orders"));
        assertTrue(json.contains("products"));
    }

    @Test
    public void testGetAdminUsers() throws Exception {
        AdminServlet servlet = new AdminServlet(adminDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("ADMIN");
        when(req.getPathInfo()).thenReturn("/users");

        StringWriter writer = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(writer));

        servlet.doGet(req, res);

        String json = writer.toString();
        assertNotNull(json);
        assertFalse(json.contains("Unable to load admin data."));
        assertTrue(json.contains("admin@nithyamart.com"));
    }
}

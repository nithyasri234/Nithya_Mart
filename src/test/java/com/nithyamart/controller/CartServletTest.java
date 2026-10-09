package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.CartDAO;
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
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CartServletTest {

    private static HikariDataSource dataSource;
    private static CartDAO cartDAO;
    private static Gson gson;
    private static final Long BUYER_ID = 2L; // seeded demo-buyer@nithyamart.com has id 2

    @BeforeAll
    public static void setUp() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:cart_test_db;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMigrationUtil.runMigrations(connection);
        }

        cartDAO = new CartDAO(dataSource);
        gson = JsonUtil.getGson();
    }

    @AfterAll
    public static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    public void testAddToCartViaQueryParamsAndGetCart() throws Exception {
        CartServlet servlet = new CartServlet(cartDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest postReq = mock(HttpServletRequest.class);
        HttpServletResponse postRes = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(postReq.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("BUYER");
        when(session.getAttribute("userId")).thenReturn(BUYER_ID);

        when(postReq.getParameter("productId")).thenReturn("1");
        when(postReq.getParameter("quantity")).thenReturn("2");
        when(postReq.getReader()).thenReturn(new BufferedReader(new StringReader("")));

        StringWriter postWriter = new StringWriter();
        when(postRes.getWriter()).thenReturn(new PrintWriter(postWriter));

        servlet.doPost(postReq, postRes);

        verify(postRes).setStatus(HttpServletResponse.SC_CREATED);
        assertTrue(postWriter.toString().contains("Product added to cart."));

        // Now test doGet: ensures CartItem serialization with Product (LocalDateTime) works!
        HttpServletRequest getReq = mock(HttpServletRequest.class);
        HttpServletResponse getRes = mock(HttpServletResponse.class);
        when(getReq.getSession(false)).thenReturn(session);

        StringWriter getWriter = new StringWriter();
        when(getRes.getWriter()).thenReturn(new PrintWriter(getWriter));

        servlet.doGet(getReq, getRes);

        String json = getWriter.toString();
        assertNotNull(json);
        assertFalse(json.contains("Unable to load cart."));
        assertTrue(json.contains("Wireless Headphones") || json.contains("Samsung Galaxy"));
    }

    @Test
    public void testAddToCartViaJsonBody() throws Exception {
        CartServlet servlet = new CartServlet(cartDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest postReq = mock(HttpServletRequest.class);
        HttpServletResponse postRes = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(postReq.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("BUYER");
        when(session.getAttribute("userId")).thenReturn(BUYER_ID);

        when(postReq.getParameter("productId")).thenReturn(null);
        when(postReq.getParameter("quantity")).thenReturn(null);
        when(postReq.getReader()).thenReturn(new BufferedReader(new StringReader("{\"productId\": 2, \"quantity\": 3}")));

        StringWriter postWriter = new StringWriter();
        when(postRes.getWriter()).thenReturn(new PrintWriter(postWriter));

        servlet.doPost(postReq, postRes);

        verify(postRes).setStatus(HttpServletResponse.SC_CREATED);
        assertTrue(postWriter.toString().contains("Product added to cart."));
    }

    @Test
    public void testUpdateCartViaUrlEncodedBody() throws Exception {
        CartServlet servlet = new CartServlet(cartDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest putReq = mock(HttpServletRequest.class);
        HttpServletResponse putRes = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(putReq.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("BUYER");
        when(session.getAttribute("userId")).thenReturn(BUYER_ID);

        when(putReq.getParameter("productId")).thenReturn(null);
        when(putReq.getParameter("quantity")).thenReturn(null);
        when(putReq.getReader()).thenReturn(new BufferedReader(new StringReader("productId=1&quantity=5")));

        StringWriter putWriter = new StringWriter();
        when(putRes.getWriter()).thenReturn(new PrintWriter(putWriter));

        servlet.doPut(putReq, putRes);

        assertTrue(putWriter.toString().contains("Cart quantity updated."));
    }

    @Test
    public void testRemoveItemFromCart() throws Exception {
        CartServlet servlet = new CartServlet(cartDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest delReq = mock(HttpServletRequest.class);
        HttpServletResponse delRes = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(delReq.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("BUYER");
        when(session.getAttribute("userId")).thenReturn(BUYER_ID);

        when(delReq.getParameter("productId")).thenReturn("1");
        when(delReq.getPathInfo()).thenReturn(null);

        StringWriter delWriter = new StringWriter();
        when(delRes.getWriter()).thenReturn(new PrintWriter(delWriter));

        servlet.doDelete(delReq, delRes);

        assertTrue(delWriter.toString().contains("Product removed from cart."));
    }
}

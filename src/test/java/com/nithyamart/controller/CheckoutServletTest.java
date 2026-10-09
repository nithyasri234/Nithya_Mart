package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.dao.CartDAO;
import com.nithyamart.dao.OrderDAO;
import com.nithyamart.dao.ProductDAO;
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

public class CheckoutServletTest {

    private static HikariDataSource dataSource;
    private static CartDAO cartDAO;
    private static ProductDAO productDAO;
    private static OrderDAO orderDAO;
    private static Gson gson;
    private static final Long BUYER_ID = 2L;

    @BeforeAll
    public static void setUp() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:checkout_test_db;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMigrationUtil.runMigrations(connection);
        }

        cartDAO = new CartDAO(dataSource);
        productDAO = new ProductDAO(dataSource);
        orderDAO = new OrderDAO(dataSource);
        gson = JsonUtil.getGson();
    }

    @AfterAll
    public static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    public void testCheckoutEmptyCartFails() throws Exception {
        CheckoutServlet servlet = new CheckoutServlet(cartDAO, productDAO, orderDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("BUYER");
        when(session.getAttribute("userId")).thenReturn(BUYER_ID);

        StringWriter writer = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(writer));

        cartDAO.clearCart(BUYER_ID);

        servlet.doPost(req, res);

        verify(res).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(writer.toString().contains("Cart is empty."));
    }

    @Test
    public void testCheckoutUnauthorizedWithoutLogin() throws Exception {
        CheckoutServlet servlet = new CheckoutServlet(cartDAO, productDAO, orderDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        StringWriter writer = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(writer));

        servlet.doPost(req, res);

        verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testCheckoutSuccess() throws Exception {
        CheckoutServlet servlet = new CheckoutServlet(cartDAO, productDAO, orderDAO, gson);
        servlet = Mockito.spy(servlet);
        ServletContext context = mock(ServletContext.class);
        doReturn(context).when(servlet).getServletContext();
        doReturn(dataSource).when(context).getAttribute("dataSource");

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("BUYER");
        when(session.getAttribute("userId")).thenReturn(BUYER_ID);

        // Add 1 item of product 1 (Wireless Headphones, stock 20+) to cart
        cartDAO.clearCart(BUYER_ID);
        cartDAO.addItem(BUYER_ID, 1L, 1);

        StringWriter writer = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(writer));

        servlet.doPost(req, res);

        verify(res).setStatus(HttpServletResponse.SC_CREATED);
        String json = writer.toString();
        assertTrue(json.contains("Order placed successfully."));
        assertTrue(json.contains("orderId"));

        // Cart should now be empty
        assertTrue(cartDAO.findByBuyerId(BUYER_ID).isEmpty());
    }
}

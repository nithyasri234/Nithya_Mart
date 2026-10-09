package com.nithyamart.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nithyamart.dao.ProductDAO;
import com.nithyamart.model.Product;
import com.nithyamart.service.ProductService;
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
import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServletTest {

    private static HikariDataSource dataSource;
    private static ProductService productService;
    private static Gson gson;

    @BeforeAll
    public static void setUp() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:product_test_db;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMigrationUtil.runMigrations(connection);
        }

        ProductDAO productDAO = new ProductDAO(dataSource);
        productService = new ProductService(productDAO);
        gson = JsonUtil.getGson();
    }

    @AfterAll
    public static void tearDown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    public void testGetAllProducts() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getPathInfo()).thenReturn(null);
        when(request.getParameter("keyword")).thenReturn(null);
        when(request.getParameter("category")).thenReturn(null);

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doGet(request, response);

        verify(response).setContentType("application/json;charset=UTF-8");
        String json = responseWriter.toString();
        assertNotNull(json);
        assertFalse(json.contains("Unable to load products."));

        Type listType = new TypeToken<List<Product>>() {}.getType();
        List<Product> products = gson.fromJson(json, listType);
        assertNotNull(products);
        assertEquals(33, products.size());
        assertNotNull(products.get(0).getCreatedAt(), "createdAt should be present and valid");
    }

    @Test
    public void testSearchProductsByKeyword() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getPathInfo()).thenReturn("/");
        when(request.getParameter("keyword")).thenReturn("phone");
        when(request.getParameter("category")).thenReturn(null);

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doGet(request, response);

        String json = responseWriter.toString();
        Type listType = new TypeToken<List<Product>>() {}.getType();
        List<Product> products = gson.fromJson(json, listType);
        assertNotNull(products);
        assertEquals(3, products.size());
        for (Product p : products) {
            boolean matches = p.getName().toLowerCase().contains("phone")
                    || (p.getDescription() != null && p.getDescription().toLowerCase().contains("phone"));
            assertTrue(matches, "Product should match keyword 'phone': " + p.getName());
        }
    }

    @Test
    public void testFilterProductsByCategory() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getPathInfo()).thenReturn("");
        when(request.getParameter("keyword")).thenReturn(null);
        when(request.getParameter("category")).thenReturn("Electronics");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doGet(request, response);

        String json = responseWriter.toString();
        Type listType = new TypeToken<List<Product>>() {}.getType();
        List<Product> products = gson.fromJson(json, listType);
        assertNotNull(products);
        assertEquals(8, products.size());
        for (Product p : products) {
            assertEquals("Electronics", p.getCategory());
        }
    }

    @Test
    public void testGetProductById() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getPathInfo()).thenReturn("/1");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doGet(request, response);

        String json = responseWriter.toString();
        Product product = gson.fromJson(json, Product.class);
        assertNotNull(product);
        assertEquals(1L, product.getId());
        assertNotNull(product.getName());
        assertNotNull(product.getCreatedAt());
    }

    @Test
    public void testGetProductByIdNotFound() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getPathInfo()).thenReturn("/999999");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertTrue(responseWriter.toString().contains("Product not found."));
    }

    @Test
    public void testGetProductByIdInvalid() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getPathInfo()).thenReturn("/abc");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertTrue(responseWriter.toString().contains("Invalid product ID."));
    }

    @Test
    public void testCreateProductAsSeller() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("SELLER");
        when(session.getAttribute("userId")).thenReturn(1L);

        String jsonPayload = """
                {
                    "name": "New Test Smartphone",
                    "description": "A brand new smartphone",
                    "price": 19999.00,
                    "stockQuantity": 15,
                    "category": "Electronics",
                    "imageUrl": "https://example.com/phone.jpg"
                }
                """;

        when(request.getReader()).thenReturn(new java.io.BufferedReader(new java.io.StringReader(jsonPayload)));

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        String responseJson = responseWriter.toString();
        assertNotNull(responseJson);
        Product created = gson.fromJson(responseJson, Product.class);
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("New Test Smartphone", created.getName());
        assertEquals(1L, created.getSellerId());
        assertNotNull(created.getCreatedAt());

        // Clean up created product so it does not affect search counts in other tests
        productService.deleteProduct(created.getId(), 1L);
    }

    @Test
    public void testCreateProductForbiddenWithoutSeller() throws Exception {
        ProductServlet servlet = new ProductServlet(productService, gson);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        ServletContext context = mock(ServletContext.class);

        servlet = Mockito.spy(servlet);
        doReturn(context).when(servlet).getServletContext();

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userRole")).thenReturn("BUYER");

        StringWriter responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertTrue(responseWriter.toString().contains("Seller login is required."));
    }
}

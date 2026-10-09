package com.nithyamart.controller;

import com.google.gson.Gson;
import com.nithyamart.util.JsonUtil;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SessionServletTest {

    private final Gson gson = JsonUtil.getGson();

    @Test
    public void testGetSessionLoggedOut() throws Exception {
        SessionServlet servlet = new SessionServlet(gson);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);

        when(req.getSession(false)).thenReturn(null);

        StringWriter writer = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(writer));

        servlet.doGet(req, res);

        verify(res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = writer.toString();
        assertTrue(json.contains("\"loggedIn\":false"));
    }

    @Test
    public void testGetSessionLoggedIn() throws Exception {
        SessionServlet servlet = new SessionServlet(gson);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(42L);
        when(session.getAttribute("userName")).thenReturn("Alice");
        when(session.getAttribute("userEmail")).thenReturn("alice@example.com");
        when(session.getAttribute("userRole")).thenReturn("BUYER");

        StringWriter writer = new StringWriter();
        when(res.getWriter()).thenReturn(new PrintWriter(writer));

        servlet.doGet(req, res);

        String json = writer.toString();
        assertTrue(json.contains("\"loggedIn\":true"));
        assertTrue(json.contains("\"userName\":\"Alice\""));
        assertTrue(json.contains("\"userRole\":\"BUYER\""));
    }
}

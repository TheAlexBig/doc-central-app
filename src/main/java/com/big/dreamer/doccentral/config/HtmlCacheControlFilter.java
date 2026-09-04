package com.big.dreamer.doccentral.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HtmlCacheControlFilter extends OncePerRequestFilter {

    private static final String CACHE_CONTROL = "no-store, no-cache, must-revalidate, max-age=0";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (isApplicationPage(request.getRequestURI())) {
            response.setHeader("Cache-Control", CACHE_CONTROL);
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
        filterChain.doFilter(request, response);
    }

    private boolean isApplicationPage(String requestUri) {
        return "/".equals(requestUri)
                || "/index.html".equals(requestUri)
                || "/compra-venta".equals(requestUri)
                || "/mutuo".equals(requestUri)
                || "/historial".equals(requestUri)
                || "/configuracion".equals(requestUri);
    }
}

package com.eumakase.eumakase.security;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

public class HttpMethodFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();

        switch (method) {
            case "GET":
            case "POST":
            case "PUT":
            case "DELETE":
            case "HEAD":
            case "OPTIONS":
            case "PATCH":
                chain.doFilter(request, response);
                break;
            default:
                throw new ServletException("Unsupported HTTP method: " + method);
        }
    }

    @Override
    public void destroy() {
    }
}

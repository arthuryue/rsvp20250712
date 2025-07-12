package hk.jud.app.lyo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import hk.jud.app.lyo.dto.CustomResponse;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
//@RefreshScope
public class IpAccessFilter extends OncePerRequestFilter {

    @Value("${allowed.public.ips:}")
    private String publicIps;

    @Value("${allowed.admin.ips:}")
    private String adminIps;

    private final ObjectMapper objectMapper;

    public IpAccessFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Handle X-Forwarded-For for proxies
        String clientIp = request.getHeader("X-Forwarded-For") != null 
            ? request.getHeader("X-Forwarded-For").split(",")[0].trim()
            : request.getRemoteAddr();
        
        // Store client IP in request attribute
        request.setAttribute("clientIp", clientIp);

        String requestUri = request.getRequestURI();
        boolean isAllowed = false;

        
        System.out.println("clientIp:" +clientIp);
        
        if (requestUri.startsWith("/api/public")) {
            List<String> allowedPublicIps = Arrays.asList(publicIps.split(","));
            isAllowed = allowedPublicIps.contains(clientIp) || allowedPublicIps.contains("*");
        } else if (requestUri.startsWith("/api/admin") || requestUri.startsWith("/api/auth")) {
            List<String> allowedAdminIps = Arrays.asList(adminIps.split(","));
            isAllowed = allowedAdminIps.contains(clientIp) || allowedAdminIps.contains("*");
        } else {
            isAllowed = true;
        }

        if (isAllowed) {
        	 System.out.println("isAllowed");
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            CustomResponse<Object> errorResponse = CustomResponse.error("Access denied: IP address " + clientIp + " not allowed");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
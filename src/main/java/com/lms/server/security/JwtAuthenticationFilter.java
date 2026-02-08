package com.lms.server.security;

import com.lms.server.entity.Employee;
import com.lms.server.repository.EmployeeRepository;
import com.lms.server.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final EmployeeRepository employeeRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth")
                || request.getMethod().equals("OPTIONS");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 🔥 ADD THESE 3 LINES RIGHT HERE
        System.out.println("==== REQUEST DEBUG ====");
        System.out.println("Method: " + request.getMethod());
        System.out.println("Path: " + request.getRequestURI());
        System.out.println("Authorization Header: " + request.getHeader("Authorization"));

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            UUID employeeId = JwtUtil.validateAndGetEmployeeId(token);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                Employee employee = employeeRepository.findById(employeeId)
                        .orElseThrow(() -> new RuntimeException("Employee not found"));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                employee,
                                null,
                                employee.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }

        } catch (Exception ex) {
            ex.printStackTrace();   // 👈 ALSO ADD THIS
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

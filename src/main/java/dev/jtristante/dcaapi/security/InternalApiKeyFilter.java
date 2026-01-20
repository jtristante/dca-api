package dev.jtristante.dcaapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(InternalApiKeyFilter.class);
    private static final String HEADER_NAME = "X-DCA-Internal-Key";
    private final String internalApiKey;

    public InternalApiKeyFilter(@Value("${internal.api.key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String key = request.getHeader(HEADER_NAME);

        if (key == null) {
            log.warn("Unauthorized (Missing internal API key) access attempt from IP: {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing internal API key");
            return;
        }

        if (!internalApiKey.equals(key)) {
            log.warn("Unauthorized (Invalid internal API key) access attempt from IP: {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid internal API key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

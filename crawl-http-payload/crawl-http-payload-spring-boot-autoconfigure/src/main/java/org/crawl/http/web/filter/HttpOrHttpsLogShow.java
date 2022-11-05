package org.crawl.http.web.filter;

import java.io.IOException;
import java.util.Collection;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Roy
 * @date 2020/10/29
 */
@Component
@Slf4j
public class HttpOrHttpsLogShow extends OncePerRequestFilter implements Ordered{

    private int                 order             = Ordered.LOWEST_PRECEDENCE - 8;
    private static final String LOCALE_ID_COOKIE  = "locale";
    private static final String SET_COOKIE_HEADER = "Set-Cookie";

    @Override
    public int getOrder () {
        return order;
    }

    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
        LogRequest requestWrapper = new LogRequest ((HttpServletRequest) request);
        // 增强 response
        LogResponse responseWrapper = new LogResponse ((HttpServletResponse) response);
        final Collection<String> setCookieHeaders = response.getHeaders (SET_COOKIE_HEADER);
        for (final String setCookieHeader : setCookieHeaders) {
            response.addHeader (SET_COOKIE_HEADER, setCookieHeader + "; Secure; SameSite=None");
        }
        if (setCookieHeaders.size () == 0) {
            final Cookie[] cookies = request.getCookies ();
            if (cookies != null && cookies.length > 0) {
                for (final Cookie cookie : cookies) {
                    if (cookie.getName ().equals (LOCALE_ID_COOKIE)) {
                        response.addHeader (SET_COOKIE_HEADER, buildSessionIdCookie (cookie.getValue ()));
                    }
                }
            }
        }
        filterChain.doFilter (requestWrapper, responseWrapper);
        // log.debug("~~~~~~~~~~~~~~~~响应headers start");
        // Collection<String> responseHeaders = responseWrapper.getHeaderNames();
        // responseHeaders.stream().forEach(headerName -> {
        // log.debug(" " + headerName + ": " + responseWrapper.getHeaders(headerName));
        // });
        // log.debug("~~~~~~~~~~~~~~~~响应headers end \r\n");
        responseWrapper.debugResponse ();
        log.debug ("~~~~~~~~~~~~~~~~响应body start");
        log.debug ("{}", responseWrapper.getBodyString ());
        log.debug ("~~~~~~~~~~~~~~~~响应body end \r\n");
        // 输出 response stream
        responseWrapper.copyToResponse ();
    }

    @Override
    public void destroy () {
    }

    private String buildSessionIdCookie (final String value) {
        return LOCALE_ID_COOKIE + "=" + value + "; " + "Path=/; " + "SameSite=None; " + "Secure; HttpOnly;";
    }
}

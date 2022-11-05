package org.crawl.http.web.filter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 *
 * @author Roy
 *
 * @date 2021年5月12日-下午10:05:59
 */
@Component
public class LoggerFilter extends OncePerRequestFilter implements Ordered{

    private int                          order         = Ordered.LOWEST_PRECEDENCE - 9;
    private static final List<MediaType> VISIBLE_TYPES = Arrays.asList (
                    MediaType.valueOf ("text/*"),
                    MediaType.APPLICATION_FORM_URLENCODED,
                    MediaType.APPLICATION_JSON,
                    MediaType.APPLICATION_XML,
                    MediaType.valueOf ("application/*+json"),
                    MediaType.valueOf ("application/*+xml"),
                    MediaType.MULTIPART_FORM_DATA);
    Logger                               log           = LoggerFactory.getLogger (LoggerFilter.class);
    private static final Path            path          = Paths.get ("/home/loggerReq.txt");
    private static BufferedWriter        writer        = null;

    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
        try {
            writer = Files.newBufferedWriter (path, Charset.forName ("UTF-8"));
            if (isAsyncDispatch (request)) {
                filterChain.doFilter (request, response);
            } else {
                doFilterWrapped (wrapRequest (request), wrapResponse (response), filterChain);
            }
        } finally {
            // writer.close();
        }
    }

    protected void doFilterWrapped (ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                    FilterChain filterChain) throws ServletException, IOException {
        try {
            beforeRequest (request, response);
            filterChain.doFilter (request, response);
        } finally {
            afterRequest (request, response);
            response.copyBodyToResponse ();
        }
    }

    protected void beforeRequest (ContentCachingRequestWrapper request, ContentCachingResponseWrapper response)
                    throws IOException {
        if (log.isInfoEnabled ()) {
            logRequestHeader (request, request.getRemoteAddr () + "|>");
        }
    }

    protected void afterRequest (ContentCachingRequestWrapper request, ContentCachingResponseWrapper response)
                    throws IOException {
        if (log.isInfoEnabled ()) {
            logRequestBody (request, request.getRemoteAddr () + "|>");
            logResponse (response, request.getRemoteAddr () + "|<");
        }
    }

    private void logRequestHeader (ContentCachingRequestWrapper request, String prefix) throws IOException {
        String queryString = request.getQueryString ();
        if (queryString == null) {
            printLines (prefix, request.getMethod (), request.getRequestURI ());
            log.debug ("{} {} {}", prefix, request.getMethod (), request.getRequestURI ());
        } else {
            printLines (prefix, request.getMethod (), request.getRequestURI (), queryString);
            log.debug ("{} {} {}?{}", prefix, request.getMethod (), request.getRequestURI (), queryString);
        }
        Collections.list (request.getHeaderNames ())
                        .forEach (headerName -> Collections.list (request.getHeaders (headerName))
                                        .forEach (headerValue -> log.debug ("{} {}: {}", prefix, headerName,
                                                        headerValue)));
        printLines (prefix);
        printLines (RequestContextHolder.currentRequestAttributes ().getSessionId ());
        log.debug ("{}", prefix);
        log.debug (" Session ID: ", RequestContextHolder.currentRequestAttributes ().getSessionId ());
    }

    private void printLines (String... args) throws IOException {
        try {
            for (String varArgs : args) {
                writer.write (varArgs);
                writer.newLine ();
            }
        } catch (IOException ex) {
            ex.printStackTrace ();
        }
    }

    private void logRequestBody (ContentCachingRequestWrapper request, String prefix) {
        byte[] content = request.getContentAsByteArray ();
        if (content.length > 0) {
            logContent (content, request.getContentType (), request.getCharacterEncoding (), prefix);
        }
    }

    private void logResponse (ContentCachingResponseWrapper response, String prefix) throws IOException {
        int status = response.getStatus ();
        printLines (prefix, String.valueOf (status), HttpStatus.valueOf (status).getReasonPhrase ());
        log.debug ("{} {} {}", prefix, status, HttpStatus.valueOf (status).getReasonPhrase ());
        response.getHeaderNames ().forEach (headerName -> response.getHeaders (headerName)
                        .forEach (headerValue -> log.debug ("{} {}: {}", prefix, headerName, headerValue)));
        printLines (prefix);
        log.debug ("{}", prefix);
        byte[] content = response.getContentAsByteArray ();
        if (content.length > 0) {
            logContent (content, response.getContentType (), response.getCharacterEncoding (), prefix);
        }
    }

    private void logContent (byte[] content, String contentType, String contentEncoding, String prefix) {
        MediaType mediaType = MediaType.valueOf (contentType);
        boolean visible = VISIBLE_TYPES.stream ().anyMatch (visibleType -> visibleType.includes (mediaType));
        if (visible) {
            try {
                String contentString = new String (content, contentEncoding);
                Stream.of (contentString.split ("\r\n|\r|\n")).forEach (line -> {
                    try {
                        printLines (line);
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }
                });
                // log.debug("{} {}", prefix, line));
            } catch (UnsupportedEncodingException e) {
                log.debug ("{} [{} bytes content]", prefix, content.length);
            }
        } else {
            log.debug ("{} [{} bytes content]", prefix, content.length);
        }
    }

    private static ContentCachingRequestWrapper wrapRequest (HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        } else {
            return new ContentCachingRequestWrapper (request);
        }
    }

    private static ContentCachingResponseWrapper wrapResponse (HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        } else {
            return new ContentCachingResponseWrapper (response);
        }
    }

    @Override
    public int getOrder () {
        return order;
    }
}

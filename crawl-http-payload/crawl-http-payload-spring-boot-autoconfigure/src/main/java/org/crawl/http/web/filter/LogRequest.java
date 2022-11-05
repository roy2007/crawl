package org.crawl.http.web.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import javax.servlet.DispatcherType;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.catalina.Globals;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Roy
 * @date 2020/10/29
 */
@Slf4j
public class LogRequest extends HttpServletRequestWrapper {
    private byte[] body;
    
    protected boolean checkServletVersion() {
        // To support Servlet API 3.1 we need to check if getContentLengthLong exists
        // Spring 5 minimum support is 3.0, so this stays
        try {
            HttpServletRequest.class.getMethod("getContentLengthLong");
            return  true;
        } catch(NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request
     *            The request to wrap
     * @throws IllegalArgumentException
     *             if the request is null
     */
    public LogRequest(HttpServletRequest request) {
        super(request);
        long reqContentLen = -1;
        log.debug ("~~~~~~~~~~~~~~~~" + "请求地址:{}", request.getRequestURI ());
        DispatcherType dispatcherType = (DispatcherType) request.getAttribute(Globals.DISPATCHER_TYPE_ATTR);
        log.debug ("~~~~~~~~~~~~~~~~" + "dispatcherType:{}", dispatcherType.name ());
        if(checkServletVersion()){
            reqContentLen = request.getContentLengthLong();
          }else {
              String contentLengthHeader = request.getHeader(HttpHeaders.CONTENT_LENGTH);
              if (contentLengthHeader != null) {
                try {
                    reqContentLen = Long.parseLong(contentLengthHeader);
                }
                catch (NumberFormatException e){}
              }
          }
        if(reqContentLen != -1) {
            reqContentLen = request.getContentLength();    
        }
      
        log.debug ("~~~~~~~~~~~~~~~~" + "请求 Headers:");
        log.debug (" URI |" + request.getRequestURI ());
        log.debug (" URL |" + request.getRequestURL ().toString ());
        log.debug ("Method | " + request.getMethod ());
        log.debug ("RemoteHost|RemoteAddr:remotePort  " + request.getRemoteHost () + "|" + request.getRemoteAddr ()
                        + ":" + request.getRemotePort ());
        log.debug ("ServerName:ServerPort  " + request.getServerName () + ":" + request.getServerPort ());
        log.debug ("LocalName:LocalAddr:LocalPort  " + request.getLocalName () + "|" + request.getLocalAddr () + ":"
                        + request.getLocalPort ());
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = (String)headerNames.nextElement();
            log.debug ("  " + headerName + ": " + request.getHeader (headerName));
        }

        log.debug ("~~~~~~~~~~~~~~~~" + "请求 Parameters:");
        Enumeration<?> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String paramName = (String)params.nextElement();
            log.debug ("  " + paramName + "=" + request.getParameter (paramName));
        }
        /** 获取响应body */
        try {
            javax.servlet.ServletInputStream inputStream = request.getInputStream();
            body = IOUtils.toByteArray(inputStream);
            log.debug ("~~~~~~~~~~~~~~~~请求的body start");
            log.debug ("{}", new String (body, "utf-8"));
            log.debug ("~~~~~~~~~~~~~~~~请求的body end \r\n ");
            
            log.debug ("~~~~~~~~~~~~~~~~" + "请求报文长度:{}", body.length);
        } catch (IOException e) {
            log.error("增强request io异常:{}", request.getRequestURI());
            e.printStackTrace();
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new SignWrapperInputStream(body);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    private class SignWrapperInputStream extends ServletInputStream {

        private ByteArrayInputStream buffer;

        public SignWrapperInputStream(byte[] body) {
            body = (body == null) ? new byte[0] : body;
            this.buffer = new ByteArrayInputStream(body);
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public boolean isFinished() {
            return buffer.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            throw new RuntimeException("Not implemented");

        }
    }
}

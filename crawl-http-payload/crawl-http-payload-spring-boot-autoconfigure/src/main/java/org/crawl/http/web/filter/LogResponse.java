package org.crawl.http.web.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Roy
 * @date 2020/10/29
 */
@Slf4j
public class LogResponse extends HttpServletResponseWrapper {
    private Collection<String> headerNames = new ArrayList<String>() ;
    private final WrapperServletOutputStream wrapperServletOutputStream = new WrapperServletOutputStream();

    public LogResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return wrapperServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(wrapperServletOutputStream);
    }

    /**
     * 获取 数组body
     */
    public byte[] getBodyBytes() {
        return wrapperServletOutputStream.out.toByteArray();
    }

    /**
     * 获取 字符串body utf-8 编码
     */
    public String getBodyString() {
        try {
           String bodys = wrapperServletOutputStream.out.toString("UTF-8");
           return bodys;
        } catch (UnsupportedEncodingException e) {
            return "[UNSUPPORTED ENCODING]";
        }
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        headerNames.add(name);
    }

    public Collection<String> getHeaderNames() {
       Collection<String> colls = super.getHeaderNames();
       headerNames.addAll(colls);
        return headerNames;
    }

    public void debugResponse() {
        HttpServletResponse response = (HttpServletResponse)getResponse();
        log.debug ("=====================response begin==========================");
        log.debug ("status:{}, Content-Length:{} ", response.getStatus (), this.getBodyBytes ().length);
        log.debug ("contentType:{}, characterEncoding:{}", response.getContentType (),
                        response.getCharacterEncoding ());
        log.debug ("===response header begin============================================");
//        Collection<String> headerNames = response.getHeaderNames();
//        for (String name : headerNames) {
//            String value = StringUtils.join(response.getHeaders(name), "||");
        // log.debug("{}={}", name, value);
//        }
        // log.debug("===响应 header end============================================");
        // log.debug("=====================response end==========================");
    }

    /**
     * 将 body内容 重新赋值到 response 中 由于stream 只读一次 需要重写到response中
     *
     */
    public void copyToResponse() {
        try {
            getResponse().getOutputStream().write(getBodyBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class WrapperServletOutputStream extends ServletOutputStream {
        private ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }
    }
}

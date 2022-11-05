package org.crawl.http.web;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509CRL;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Roy
 * @date 2020/10/30
 */
@Slf4j
@Component
public class PostManUtil {
    private final String USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36";

    public HttpClient getClient() {
        HttpClient client = new HttpClient();
//        // truststore
//        KeyStore trustStore = KeyStore.getInstance("JKS", "SUN");
//        trustStore.load(TestSupertype.class.getResourceAsStream("/client-truststore.jks"), "amber%".toCharArray());
//        String alg = KeyManagerFactory.getDefaultAlgorithm();
//        TrustManagerFactory fac = TrustManagerFactory.getInstance(alg);
//        fac.init(trustStore);
//
//        // keystore
//        KeyStore keystore = KeyStore.getInstance("PKCS12", "SunJSSE");
//        keystore.load(X509CRL.class.getResourceAsStream("/etomcat_client.p12"), "etomcat".toCharArray());
//        String keyAlg = KeyManagerFactory.getDefaultAlgorithm();
//        KeyManagerFactory keyFac = KeyManagerFactory.getInstance(keyAlg);
//        keyFac.init(keystore, "etomcat".toCharArray());
//
//        // context
//        SSLContext ctx = SSLContext.getInstance("TLS", "SunJSSE");
//        ctx.init(keyFac.getKeyManagers(), fac.getTrustManagers(), new SecureRandom());
//        SslContextedSecureProtocolSocketFactory secureProtocolSocketFactory = new SslContextedSecureProtocolSocketFactory(ctx);
//        Protocol.registerProtocol("https",
//            new Protocol("https", (ProtocolSocketFactory)secureProtocolSocketFactory, 8443));

        try {
            // test get
            HttpMethod get = new GetMethod("https://127.0.0.1:8443/etomcat_x509");
            client.executeMethod(get);
            // get response body and do what you need with it
            byte[] responseBody = get.getResponseBody();
        } catch (IOException e) {
            // TODO Auto-generated catch block
             e.printStackTrace();
        }
        return client;
    }

    public String getPostPage(String postUrl, NameValuePair[] data, String cookie) {
        String html = "";
        PostMethod method = null;
        String contentStr = null;
        try {
            method = new PostMethod(postUrl);
            method.addRequestHeader("User-Agent", USER_AGENT);
            method.addRequestHeader("Host", "asqx.moni.gucheng.com");
            method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            method.addRequestHeader("Referer", "...");
            method.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
            method.addRequestHeader("Cookie", cookie);
            method.addRequestHeader("X-MicrosoftAjax", "Delta=true");
            method.addRequestHeader("Pragma", "no-cache");
            // method.addRequestHeader("Accept-Encoding", "gzip, deflate");
            method.addRequestHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
            method.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            method.addRequestHeader("Accept-Language", "zh-cn,zh;q=0.5");
            method.setRequestBody(data);
            int statusCode = getClient().executeMethod(method);

            if (statusCode == HttpStatus.SC_OK) {
                InputStream in = method.getResponseBodyAsStream();
                if (in != null) {
                    byte[] tmp = new byte[4096];
                    int bytesRead = 0;
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
                    while ((bytesRead = in.read(tmp)) != -1) {
                        buffer.write(tmp, 0, bytesRead);
                    }
                    byte[] bt = buffer.toByteArray();
                    String gbk = new String(bt, "GBK");
                    String utf8 = new String(bt, "UTF-8");
                    if (gbk.length() < utf8.length()) {
                        bt = null;
                        bt = gbk.getBytes("UTF-8");
                        html = new String(bt, "UTF-8");
                        html = html.replaceFirst(
                            "[cC][hH][aA][rR][sS][eE][tT]\\s*?=\\s*?([gG][bB]2312|[gG][bB][kK]|[gG][bB]18030)",
                            "charset=utf-8");
                    } else if (gbk.length() > utf8.length()) {
                        html = buffer.toString();
                    } else {
                        html = buffer.toString();
                    }
                    buffer.close();
                    contentStr = new String("abc".getBytes(), "UTF-8");
                    contentStr = html;

                    in.close();
                    in = null;
                }
            } else {
                contentStr = null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (method != null)
                method.releaseConnection();
        }
        return contentStr;
    }

    public void initSSLServerSocket() {
        try {
            String key = "c:/.keystore";
            KeyStore keystore = KeyStore.getInstance("JKS");
            // keystore的类型，默认是jks
            keystore.load(new FileInputStream(key), "123456".toCharArray());
            // 创建jkd密钥访问库 123456是keystore密码。
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, "asdfgh".toCharArray());
            // asdfgh是key密码。
            // 创建管理jks密钥库的x509密钥管理器，用来管理密钥，需要key的密码
            SSLContext sslc = SSLContext.getInstance("SSLv3");
            // 构造SSL环境，指定SSL版本为3.0，也可以使用TLSv1，但是SSLv3更加常用。
            sslc.init(kmf.getKeyManagers(), null, null);
            // 第二个参数TrustManager[] 是认证管理器，在需要双向认证时使用，
            // 构造ssl环境

            SSLServerSocketFactory sslfactory = sslc.getServerSocketFactory();
            SSLServerSocket serversocket = (SSLServerSocket)sslfactory.createServerSocket(9999);
            // 创建serversocket，监听，并传输数据来验证授权
            for (int i = 0; i < 15; i++) {
                final Socket socket = serversocket.accept();
                new Thread() {
                    public void run() {
                        InputStream is = null;
                        OutputStream os = null;
                        try {
                            is = socket.getInputStream();
                            os = socket.getOutputStream();
                            byte[] buf = new byte[1024];
                            int len = is.read(buf);
                            System.out.println(new String(buf));
                            os.write("ssl test".getBytes());
                            os.close();
                            is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            IOUtils.closeQuietly(os);
                            IOUtils.closeQuietly(is);
                        }
                    }
                }.start();
            }

            serversocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initClientSocket() {
        InputStream is = null;
        OutputStream os = null;
        try {
            String key = "c:/client";
            KeyStore keystore = KeyStore.getInstance("JKS"); // 创建一个keystore来管理密钥库
            keystore.load(new FileInputStream(key), "123456".toCharArray());
            // 创建jkd密钥访问库
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore); // 验证数据，可以不传入key密码
            // 创建TrustManagerFactory,管理授权证书
            SSLContext sslc = SSLContext.getInstance("SSLv3");
            // 构造SSL环境，指定SSL版本为3.0，也可以使用TLSv1，但是SSLv3更加常用。
            sslc.init(null, tmf.getTrustManagers(), null);
            // KeyManager[] 第一个参数是授权的密钥管理器，用来授权验证。第二个是被授权的证书管理器，
            // 用来验证服务器端的证书。只验证服务器数据，第一个管理器可以为null
            // 构造ssl环境

            SSLSocketFactory sslfactory = sslc.getSocketFactory();
            SSLSocket socket = (SSLSocket)sslfactory.createSocket("127.0.0.1", 9999);
            // 创建serversocket通过传输数据来验证授权

            is = socket.getInputStream();
            os = socket.getOutputStream();
            os.write("client".getBytes());
            byte[] buf = new byte[1024];
            int len = is.read(buf);
            System.out.println(new String(buf));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

    }
    
    public static void main(String[] args) {
        String hex = "559bc47f";

        if(hex.length()%2!=0){
           System.err.println("Invlid hex string.");
           return;
        }
        
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hex.length(); i = i + 2) {
           // Step-1 Split the hex string into two character group
           String s = hex.substring(i, i + 2);
           // Step-2 Convert the each character group into integer using valueOf method
           int n = Integer.valueOf(s, 16);
           // Step-3 Cast the integer value to char
           builder.append((char)n);
        }

        System.out.println("Hex = " + hex);
        System.out.println("ASCII = " + builder.toString());
        String str = "SEQ";
         // display in uppercase
         char[] chars = Hex.encodeHex(str.getBytes(StandardCharsets.UTF_8), false);
            
        //char[] chars = Hex.encodeHex(str.getBytes(StandardCharsets.UTF_8));

        System.out.println( String.valueOf(chars));
     }
}

package com.mengxiang.base.datatask.util;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HttpClient {

    static Logger log = LoggerFactory.getLogger(HttpClient.class);

    private static int connectionRequestTimeout = 1000;
    private static int connectTimeout = 3000;
    private static int socketTimeout = 3000;

    private static CloseableHttpClient httpClient;
    private static PoolingHttpClientConnectionManager cm;
    static {
        init();
        closeExpiredConnectionsPeriodTask(60);
    }
    static void init(){
        cm = new PoolingHttpClientConnectionManager();
        // max connections
        cm.setMaxTotal(200);
        // max connections per route
        cm.setDefaultMaxPerRoute(100);
        // set max connections for a specified route
        //cm.setMaxPerRoute(new HttpRoute(new HttpHost("locahost", 80)), 50);

        final RequestConfig requestConfig = RequestConfig.custom()
                // the socket timeout (SO_TIMEOUT) in milliseconds
                .setSocketTimeout(socketTimeout)
                // the timeout in milliseconds until a connection is established.
                .setConnectTimeout(connectTimeout)
                // the timeout in milliseconds used when requesting a connection from the connection pool.
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();

        SSLConnectionSocketFactory ssf = null;
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] arg0,
                                               String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            ssf = new SSLConnectionSocketFactory(
                    ctx, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            log.error("init SSLSocketFactory error", e);
        }

        httpClient = HttpClients.custom().setConnectionManager(cm).setSSLSocketFactory(ssf).setDefaultRequestConfig(requestConfig).build();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {

                try {
                    cm.close();
                } catch (Exception e) {
                    log.error("close httpclient",e);
                }

            }
        });
    }

    private static void closeExpiredConnectionsPeriodTask(int timeUnitBySecond){
        new Thread(new Runnable() {

            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    try {

                        TimeUnit.SECONDS.sleep(timeUnitBySecond);
                        cm.closeExpiredConnections();
                    } catch (Exception e) {
                        log.error("closeExpiredConnections httpclient",e);
                    }
                }

            }
        },"httpclient-closeExpiredConnections").start();
    }



    public static<T> HttpResponse<T> get(String url, Map<String,Object> params, Class<T> clazz) {
        HttpResponse<T> hs = new HttpResponse<T>();

        URIBuilder uriBuilder = null;
        HttpGet httpget = null;
        CloseableHttpResponse response = null;

        try {

            uriBuilder = new URIBuilder(url);
            if(null != params && !params.isEmpty()) {
                Set<Map.Entry<String, Object>> kvs = params.entrySet();
                for (Map.Entry<String, Object> stringObjectEntry:kvs) {
                    if(null != stringObjectEntry.getKey() && null != stringObjectEntry.getValue()) {
                        uriBuilder.setParameter(stringObjectEntry.getKey().toString(),stringObjectEntry.getValue().toString());
                    }
                }
            }
            httpget = new HttpGet(uriBuilder.build());
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setConnectTimeout(connectTimeout)
                    .setSocketTimeout(socketTimeout).build();
            httpget.setConfig(requestConfig);

            response = httpClient.execute(httpget);
            hs.setCode(response.getStatusLine().getStatusCode());
            hs.setMessage(response.getStatusLine().getReasonPhrase());
            if(200 == response.getStatusLine().getStatusCode()) {
                byte[] rst = IOUtils.toByteArray(response.getEntity().getContent());
                //byte[] rst = IOUtils.readFully(response.getEntity().getContent(),(int)response.getEntity().getContentLength());
                String rstText = new String(rst, Charset.forName("UTF-8"));
                hs.setResponse(rstText);
                if(null != clazz && !clazz.equals(Object.class) && !clazz.equals(String.class)) {
                    //T result = new Gson().fromJson(rstText,clazz);
                    T result = JSON.parseObject(rstText,clazz);
                    hs.setResult(result);
                }
                hs.setSuccess(true);
            }
        } catch (ConnectTimeoutException cte) {
            log.error("请求通信[" + url + "]时连接超时", cte);
            hs.setCode(-1);
            hs.setMessage("connectTimeout");
        } catch (SocketTimeoutException ste) {
            log.error("请求通信[" + url + "]时读取超时", ste);
            hs.setCode(-2);
            hs.setMessage("socketTimeout");
        } catch (Exception e) {
            log.error("请求通信[" + url + "]时异常", e);
            hs.setCode(0);
            hs.setMessage(StackTraceUtil.getStackTrace(e,100));
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != httpget) {
                    httpget.releaseConnection();
                }
            } catch (Exception e) {
            }
        }

        return hs;
    }


    public static<T> HttpResponse<T> postJSON(String url, Object bodyParams, Map<String,Object> urlParams, Class<T> clazz) {
        HttpResponse<T> hs = new HttpResponse<T>();

        HttpPost httpost = null;

        CloseableHttpResponse response = null;
        URIBuilder uriBuilder = null;

        try { //(CloseableHttpClient httpClient = HttpClients.createDefault())

            uriBuilder = new URIBuilder(url);
            if(null != urlParams && !urlParams.isEmpty()) {
                Set<Map.Entry<String, Object>> kvs = urlParams.entrySet();
                for (Map.Entry<String, Object> stringObjectEntry:kvs) {
                    if(null != stringObjectEntry.getKey() && null != stringObjectEntry.getValue()) {
                        uriBuilder.setParameter(stringObjectEntry.getKey().toString(),stringObjectEntry.getValue().toString());
                    }
                }
            }
            httpost = new HttpPost(uriBuilder.build());
            if(null != bodyParams) {
                httpost.setEntity(new StringEntity(JSON.toJSONString(bodyParams), ContentType.APPLICATION_JSON));
            }

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setConnectTimeout(connectTimeout)
                    .setSocketTimeout(socketTimeout).build();
            httpost.setConfig(requestConfig);

            response = httpClient.execute(httpost);
            hs.setCode(response.getStatusLine().getStatusCode());
            hs.setMessage(response.getStatusLine().getReasonPhrase());
            if(200 == response.getStatusLine().getStatusCode()) {
                byte[] rst = IOUtils.toByteArray(response.getEntity().getContent());
                //byte[] rst = IOUtils.readFully(response.getEntity().getContent(),(int)response.getEntity().getContentLength());
                String rstText = new String(rst, Charset.forName("UTF-8"));
                hs.setResponse(rstText);
                if(null != clazz && !clazz.equals(Object.class) && !clazz.equals(String.class)) {
                    //T result = new Gson().fromJson(rstText,clazz);
                    T result = JSON.parseObject(rstText,clazz);
                    hs.setResult(result);
                }
                hs.setSuccess(true);
            }
        } catch (ConnectTimeoutException cte) {
            log.error("请求通信[" + url + "]时连接超时", cte);
            hs.setCode(-1);
            hs.setMessage("connectTimeout");
        } catch (SocketTimeoutException ste) {
            log.error("请求通信[" + url + "]时读取超时", ste);
            hs.setCode(-2);
            hs.setMessage("socketTimeout");
        } catch (Exception e) {
            log.error("请求通信[" + url + "]时异常", e);
            hs.setCode(0);
            hs.setMessage(StackTraceUtil.getStackTrace(e,100));
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != httpost) {
                    httpost.releaseConnection();
                }
            } catch (Exception e) {
            }
        }

        return hs;
    }

    public static<T> HttpResponse<T> post(String url, Map<String,Object> postParams, Map<String,Object> urlParams, Class<T> clazz) {
        HttpResponse<T> hs = new HttpResponse<T>();

        HttpPost httpost = null;

        CloseableHttpResponse response = null;
        URIBuilder uriBuilder = null;

        try { //(CloseableHttpClient httpClient = HttpClients.createDefault())

            uriBuilder = new URIBuilder(url);
            if(null != urlParams && !urlParams.isEmpty()) {
                Set<Map.Entry<String, Object>> kvs = urlParams.entrySet();
                for (Map.Entry<String, Object> stringObjectEntry:kvs) {
                    if(null != stringObjectEntry.getKey() && null != stringObjectEntry.getValue()) {
                        uriBuilder.setParameter(stringObjectEntry.getKey().toString(),stringObjectEntry.getValue().toString());
                    }
                }
            }
            httpost = new HttpPost(uriBuilder.build());

            if(null != postParams && !postParams.isEmpty()) {
                Set<Map.Entry<String, Object>> kvs = postParams.entrySet();
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                for (Map.Entry<String, Object> stringObjectEntry:kvs) {
                    if(null != stringObjectEntry.getKey() && null != stringObjectEntry.getValue()) {
                        list.add(new BasicNameValuePair(stringObjectEntry.getKey().toString(),stringObjectEntry.getValue().toString()));
                    }
                }

                httpost.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));

            }

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(connectionRequestTimeout)
                    .setConnectTimeout(connectTimeout)
                    .setSocketTimeout(socketTimeout).build();
            httpost.setConfig(requestConfig);

            response = httpClient.execute(httpost);
            hs.setCode(response.getStatusLine().getStatusCode());
            hs.setMessage(response.getStatusLine().getReasonPhrase());
            if(200 == response.getStatusLine().getStatusCode()) {
                byte[] rst = IOUtils.toByteArray(response.getEntity().getContent());
                //byte[] rst = IOUtils.readFully(response.getEntity().getContent(),(int)response.getEntity().getContentLength());
                String rstText = new String(rst, Charset.forName("UTF-8"));
                hs.setResponse(rstText);
                if(null != clazz && !clazz.equals(Object.class) && !clazz.equals(String.class)) {
                    //T result = new Gson().fromJson(rstText,clazz);
                    T result = JSON.parseObject(rstText,clazz);
                    hs.setResult(result);
                }
                hs.setSuccess(true);
            }
        } catch (ConnectTimeoutException cte) {
            log.error("请求通信[" + url + "]时连接超时", cte);
            hs.setCode(-1);
            hs.setMessage("connectTimeout");
        } catch (SocketTimeoutException ste) {
            log.error("请求通信[" + url + "]时读取超时", ste);
            hs.setCode(-2);
            hs.setMessage("socketTimeout");
        } catch (Exception e) {
            log.error("请求通信[" + url + "]时异常", e);
            hs.setCode(0);
            hs.setMessage(StackTraceUtil.getStackTrace(e,100));
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != httpost) {
                    httpost.releaseConnection();
                }
            } catch (Exception e) {
            }
        }

        return hs;
    }

    public static<T> HttpResponse<T> postMultipart(
            String url,
            Map<String, ContentBody> mapParam,
            Map<String,String> headers,
            TypeReference<T> typeReference,
            int connectTimeout,
            int socketTimeout
            ) {


        HttpResponse<T> hs = new HttpResponse<T>();
        HttpPost httpost = null;
        CloseableHttpResponse response = null;
        try {
            httpost = new HttpPost( url );

            //setConnectTimeout：设置连接超时时间，单位毫秒。
            // setConnectionRequestTimeout：设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
            // setSocketTimeout：请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout( 5000 )
                    .setConnectTimeout( connectTimeout )
                    .setSocketTimeout( socketTimeout )
                    .build();
            httpost.setConfig( defaultRequestConfig );

            MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

            for (Map.Entry<String, ContentBody> param : mapParam.entrySet()) {
                multipartEntityBuilder.addPart( param.getKey(), param.getValue() );
            }
            //multipartEntityBuilder.setCharset( Charset.forName( "utf-8" ));
            multipartEntityBuilder.setMode( HttpMultipartMode.BROWSER_COMPATIBLE ); //加上此行代码解决返回中文乱码问题

            HttpEntity reqEntity = multipartEntityBuilder.build();
            httpost.setEntity( reqEntity );

            // 处理头部信息
            if (null != headers && !headers.isEmpty()) {
                Set<Map.Entry<String, String>> entrySet = headers.entrySet();
                for (Map.Entry<String, String> kvEntry : entrySet) {
                    httpost.addHeader( kvEntry.getKey(), kvEntry.getValue() );
                }
            }

            response = httpClient.execute(httpost);
            hs.setCode(response.getStatusLine().getStatusCode());
            hs.setMessage(response.getStatusLine().getReasonPhrase());
            if(200 == response.getStatusLine().getStatusCode()) {
                byte[] rst = IOUtils.toByteArray(response.getEntity().getContent());
                //byte[] rst = IOUtils.readFully(response.getEntity().getContent(),(int)response.getEntity().getContentLength());
                String rstText = new String(rst, Charset.forName("UTF-8"));
                hs.setResponse(rstText);
                if(null != typeReference) {
                    //T result = new Gson().fromJson(rstText,clazz);
                    T result = JSON.parseObject(rstText,typeReference);
                    hs.setResult(result);
                }
                hs.setSuccess(true);
                /**
                 HttpEntity httpEntity = response.getEntity();
                 if (httpEntity != null) {
                 result = EntityUtils.toString( httpEntity, Charset.forName( "UTF-8" ) );
                 }
                 */
            }
        } catch (ConnectTimeoutException cte) {
            log.error("请求通信[" + url + "]时连接超时", cte);
            hs.setCode(-1);
            hs.setMessage("connectTimeout");
        } catch (SocketTimeoutException ste) {
            log.error("请求通信[" + url + "]时读取超时", ste);
            hs.setCode(-2);
            hs.setMessage("socketTimeout");
        } catch (Exception e) {
            log.error("请求通信[" + url + "]时异常", e);
            hs.setCode(0);
            hs.setMessage(StackTraceUtil.getStackTrace(e,100));
        } finally {
            try {
                if (null != response) {
                    response.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != httpost) {
                    httpost.releaseConnection();
                }
            } catch (Exception e) {
            }
        }

        return hs;
    }



}

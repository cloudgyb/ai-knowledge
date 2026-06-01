package com.github.cloudgyb.ai.knowledge.server.config;

import dev.langchain4j.http.client.HttpClientBuilderFactory;
import dev.langchain4j.http.client.okhttp.OkHttpClientBuilder;
import dev.langchain4j.http.client.okhttp.OkHttpClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 自定义 OkHttpClientBuilderFactory，
 * 必须使用以下 JVM 参数：
 * <pre>
 * -Dlangchain4j.http.clientBuilderFactory=com.github.cloudgyb.ai.knowledge.server.config.OkHttpClientCustomBuilderFactory
 * </pre>
 *
 * @author cloudgyb
 * @since 2026/5/28 13:40
 */
@Slf4j
@Component
public class OkHttpClientCustomBuilderFactory extends OkHttpClientBuilderFactory implements HttpClientBuilderFactory,
        ApplicationContextAware {
    private static volatile boolean debug = true;

    @Override
    public OkHttpClientBuilder create() {
        OkHttpClientBuilder okHttpClientBuilder = super.create();
        if (!debug) {
            return okHttpClientBuilder;
        }
        if (log.isInfoEnabled()) {
            log.info("启用 okhttp debug,将输出请求和响应详情");
        }
        okhttp3.OkHttpClient.Builder nativeOkHttpClientBuilder = new okhttp3.OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        nativeOkHttpClientBuilder.addNetworkInterceptor(loggingInterceptor);
        okHttpClientBuilder.okHttpClientBuilder(nativeOkHttpClientBuilder);
        return okHttpClientBuilder;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        Boolean property = applicationContext.getEnvironment().getProperty("ai.http.debug", boolean.class);
        debug = property != null && property;
    }
}

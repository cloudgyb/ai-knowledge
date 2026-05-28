package com.github.cloudgyb.ai.knowledge.server.config;

import dev.langchain4j.http.client.HttpClientBuilderFactory;
import dev.langchain4j.http.client.okhttp.OkHttpClientBuilder;
import dev.langchain4j.http.client.okhttp.OkHttpClientBuilderFactory;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 自定义 OkHttpClientBuilderFactory
 *
 * @author cloudgyb
 * @since 2026/5/28 13:40
 */
public class OkHttpClientCustomBuilderFactory extends OkHttpClientBuilderFactory implements HttpClientBuilderFactory {
    @Override
    public OkHttpClientBuilder create() {
        OkHttpClientBuilder okHttpClientBuilder = super.create();
        okhttp3.OkHttpClient.Builder nativeOkHttpClientBuilder = new okhttp3.OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        nativeOkHttpClientBuilder.addNetworkInterceptor(loggingInterceptor);
        okHttpClientBuilder.okHttpClientBuilder(nativeOkHttpClientBuilder);
        return okHttpClientBuilder;
    }
}

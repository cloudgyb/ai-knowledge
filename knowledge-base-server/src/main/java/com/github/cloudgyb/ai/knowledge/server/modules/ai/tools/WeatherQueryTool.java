package com.github.cloudgyb.ai.knowledge.server.modules.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * 天气查询工具
 *
 * @author cloudgyb
 * @since 2026/5/28 10:52
 */
public class WeatherQueryTool {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WeatherQueryTool.class);
    private static final String API_URL = "https://uapis.cn/api/v1/misc/weather";
    private static final OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(5L))
                .readTimeout(Duration.ofSeconds(5L))
                .build();
    }

    /**
     * 查询天气
     *
     * @param city 城市，例如北京、上海等，如果为空，则查询当前位置的天气
     * @return 天气信息
     */
    @Tool(name = "queryWeather", value = "查询天气")
    public String queryWeather(@P(value = "城市名称，如果为空，则查询当前位置的天气") String city) {
        // 使用HttpClient或其他HTTP库发送HTTP请求，并返回结果
        Request request = new Request.Builder().get().url(StringUtils.isBlank(city) ? API_URL : API_URL + "?city=" + city).build();
        try (Response execute = client.newCall(request).execute()) {
            return execute.body().string();
        } catch (Exception e) {
            logger.error("查询天气失败", e);
            return "查询天气失败";
        }
    }
}

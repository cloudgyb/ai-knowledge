package com.github.cloudgyb.ai.knowledge.server.modules.commons.util;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.onnx.OnnxScoringModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 *
 * @author cloudgyb
 * @since 2026/5/29 16:36
 */
@SpringBootTest
public class OnnxRankingModelTest {
    @Value("${onnx.scoring.model.pathToModel}")
    private String modelPath;

    @Value("${onnx.scoring.model.pathToTokenizer}")
    private String tokenizerPath;

    @Test
    public void testOnnxRankingModel() {
        OnnxScoringModel onnxRankingModel = new OnnxScoringModel(modelPath, tokenizerPath);
        Response<Double> response = onnxRankingModel.score("耿远播是一个老板", "耿远播是谁？");
        System.out.println(response); // 输出结果0.85

        Response<List<Double>> listResponse = onnxRankingModel.scoreAll(List.of(TextSegment.from("耿远播是一个老板"),
                        TextSegment.from("耿远播是谁？"), TextSegment.from("耿远播28岁")),
                "耿远播是谁？");
        System.out.println(listResponse);
    }
}

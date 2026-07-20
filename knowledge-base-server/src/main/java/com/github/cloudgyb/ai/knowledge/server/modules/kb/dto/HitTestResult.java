package com.github.cloudgyb.ai.knowledge.server.modules.kb.dto;

/**
 * 命中测试结果 DTO
 *
 * @author cloudgyb
 * @since 2026/7/18
 */
public class HitTestResult {
    /**
     * 匹配的文本内容
     */
    private String text;

    /**
     * 相似度得分 (0~1)
     */
    private Double score;

    /**
     * 所属文档ID
     */
    private Integer docId;

    /**
     * 所属文档标题
     */
    private String docTitle;

    public HitTestResult() {
    }

    public HitTestResult(String text, Double score, Integer docId, String docTitle) {
        this.text = text;
        this.score = score;
        this.docId = docId;
        this.docTitle = docTitle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }
}

package com.github.cloudgyb.ai.knowledge.server.modules.kb.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 知识库关联的文档存储表
 *
 * @author cloudgyb
 * @since 2026-03-02 16:59:29
 */
@TableName(value = "knowledge_base_doc")
public class KnowledgeBaseDoc {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联的知识库id，对应 knowledge_base 表的 id 字段
     */
    @TableField(value = "kb_id")
    private Integer kbId;

    /**
     * 文档名称
     */
    @TableField(value = "title")
    private String title;

    /**
     * 文件名
     */
    @TableField(value = "filename")
    private String filename;

    /**
     * 文件路径
     */
    @TableField(value = "filepath")
    private String filepath;

    /**
     * 文件类型
     */
    @TableField(value = "file_type")
    private String fileType;

    /**
     * 文档类型，支持单文件，压缩文件和文本内容
     */
    @TableField(value = "doc_type")
    private String docType;

    /**
     * 文件向量化状态
     */
    @TableField(value = "status")
    private String status;

    /**
     *
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     *
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     *
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 关联的知识库id，对应 knowledge_base 表的 id 字段
     */
    public Integer getKbId() {
        return kbId;
    }

    /**
     * 关联的知识库id，对应 knowledge_base 表的 id 字段
     */
    public void setKbId(Integer kbId) {
        this.kbId = kbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 文件名
     */
    public String getFilename() {
        return filename;
    }

    /**
     * 文件名
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * 文件路径
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * 文件路径
     */
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * 文件类型
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * 文件类型
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * 文档类型，支持单文件，压缩文件和文本内容
     */
    public String getDocType() {
        return docType;
    }

    /**
     * 文档类型，支持单文件，压缩文件和文本内容
     */
    public void setDocType(String docType) {
        this.docType = docType;
    }

    /**
     *
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     *
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     *
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     *
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
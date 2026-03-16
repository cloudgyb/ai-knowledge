export interface KnowledgeBaseDoc {
    /**
     * 文档 ID
     */
    id?: number;

    /**
     * 关联的知识库 id，对应 knowledge_base 表的 id 字段
     */
    kbId?: number;

    /**
     * 文档名称
     */
    title?: string;

    /**
     * 文件名
     */
    filename?: string;

    /**
     * 文件路径
     */
    filepath?: string;

    /**
     * 文件类型
     */
    fileType?: string;

    /**
     * 文档类型，支持单文件、压缩文件和文本内容
     */
    docType?: string;

    /**
     * 文件向量化状态
     */
    status: string;

    /**
     * 是否启用
     */
    enable?: boolean;

    /**
     * 创建时间
     */
    createTime?: string;

    /**
     * 更新时间
     */
    updateTime?: string;
}

// 文档状态枚举
export const DocStatus = {
    VECTORIZING: {text: '正在向量化', color: 'green'}, // 向量化中
    VECTORIZED: {text: '已向量化', color: 'blue'},   // 已向量化
    FAILED: {text: '向量化失败', color: 'red'},   // '向量化失败'
}

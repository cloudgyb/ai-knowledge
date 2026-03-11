package com.github.cloudgyb.ai.knowledge.server.modules.kb.mapper;

import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBaseDoc;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【knowledge_base_doc(知识库关联的文档存储表)】的数据库操作Mapper
 *
 * @author cloudgyb
 * @since 2026-03-02 17:01:49
 */
@Mapper
public interface KnowledgeBaseDocMapper extends BaseMapper<KnowledgeBaseDoc> {

}





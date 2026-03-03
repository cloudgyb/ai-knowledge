package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.config.KnowledgeBaseDocStorageProperties;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocStatus;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocType;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBaseDoc;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.mapper.KnowledgeBaseDocMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 针对表【knowledge_base_doc(知识库关联的文档存储表)】的数据库操作Service实现
 *
 * @author cloudgyb
 * @since 2026-03-02 17:01:49
 */
@Service
public class KnowledgeBaseDocService extends ServiceImpl<KnowledgeBaseDocMapper, KnowledgeBaseDoc> {
    private final KnowledgeBaseService knowledgeBaseService;
    private final ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private final KnowledgeBaseDocStorageProperties knowledgeBaseDocStorageProperties;

    public KnowledgeBaseDocService(KnowledgeBaseService knowledgeBaseService,
                                   ThreadPoolTaskExecutor threadPoolTaskExecutor,
                                   KnowledgeBaseDocStorageProperties knowledgeBaseDocStorageProperties) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.knowledgeBaseDocStorageProperties = knowledgeBaseDocStorageProperties;
    }

    public int addDoc(Integer kbId, String title, MultipartFile file) {
        validateKbId(kbId);
        String basePath = knowledgeBaseDocStorageProperties.getPath();
        File saveDir = new File(basePath, String.valueOf(kbId));
        File targetFile = new File(saveDir, file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败！", e);
        }
        KnowledgeBaseDoc doc = new KnowledgeBaseDoc();
        doc.setTitle(title);
        doc.setFilename(file.getOriginalFilename());
        doc.setFilepath(targetFile.getName());
        doc.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
        doc.setDocType(DocType.FILE.name());
        doc.setKbId(kbId);
        doc.setStatus(DocStatus.VECTORIZING.name());
        this.save(doc);

        threadPoolTaskExecutor.submit(() -> {
            vectoringDoc(kbId, doc.getId(), targetFile);
        });
        return doc.getId();
    }

    private void vectoringDoc(Integer kbId, Integer docId, File targetFile) {
    }

    private void validateKbId(Integer kbId) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase != null) {
            return;
        }
        throw new RuntimeException("知识库不存在");
    }
}





package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.config.KnowledgeBaseDocStorageProperties;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocStatus;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocType;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBaseDoc;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.dto.KnowledgeBaseAddDTO;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.mapper.KnowledgeBaseDocMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingModelFactory;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingStoreFactory;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import org.apache.commons.io.FilenameUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
    private final AiModelService aiModelService;
    private final EmbeddingStoreFactory embeddingStoreFactory;
    private final EmbeddingModelFactory embeddingModelFactory;

    public KnowledgeBaseDocService(KnowledgeBaseService knowledgeBaseService,
                                   ThreadPoolTaskExecutor threadPoolTaskExecutor,
                                   KnowledgeBaseDocStorageProperties knowledgeBaseDocStorageProperties,
                                   AiModelService aiModelService,
                                   EmbeddingStoreFactory embeddingStoreFactory,
                                   EmbeddingModelFactory embeddingModelFactory) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.knowledgeBaseDocStorageProperties = knowledgeBaseDocStorageProperties;
        this.aiModelService = aiModelService;
        this.embeddingStoreFactory = embeddingStoreFactory;
        this.embeddingModelFactory = embeddingModelFactory;
    }

    @Transactional(rollbackFor = Exception.class)
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

        threadPoolTaskExecutor.submit(() -> vectoringDoc(kbId, doc.getId(), targetFile));
        return doc.getId();
    }

    private void vectoringDoc(Integer kbId, Integer docId, File targetFile) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase == null) {
            throw new RuntimeException("知识库不存在");
        }
        Integer aiVectorModelId = knowledgeBase.getAiVectorModelId();
        AiModel aiModel = aiModelService.getById(aiVectorModelId);
        if (aiModel == null) {
            throw new RuntimeException("知识库AI向量模型不存在！");
        }
        String modelType = aiModel.getModelType();
        if (!AiModelType.VECTOR.name().equals(modelType)) {
            throw new RuntimeException("知识库AI向量模型类型不正确！");
        }
        EmbeddingModel embeddingModel = embeddingModelFactory.create(aiModel);
        EmbeddingStore<TextSegment> embeddingStore = embeddingStoreFactory.create(embeddingModel.dimension());
        // 删除旧的vector
        embeddingStore.removeAll(MetadataFilterBuilder.metadataKey("docId").isEqualTo(docId));
        Document document = FileSystemDocumentLoader.loadDocument(targetFile.getPath());
        DocumentSplitter documentSplitter = DocumentSplitters
                .recursive(1000, 100);
        List<TextSegment> textSegments = documentSplitter.split(document);
        // 设置元数据标识
        textSegments.forEach(textSegment -> textSegment.metadata().put("docId", docId));
        Response<List<Embedding>> listResponse = embeddingModel.embedAll(textSegments);
        List<Embedding> embeddings = listResponse.content();
        embeddingStore.addAll(embeddings, textSegments);
    }

    private void validateKbId(Integer kbId) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase != null) {
            return;
        }
        throw new RuntimeException("知识库不存在");
    }

    public void updateDoc(KnowledgeBaseAddDTO dto) {
    }
}





package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.config.KnowledgeBaseDocStorageProperties;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocStatus;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocType;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBaseDoc;
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
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
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
        File targetFile = saveFile(kbId, file);
        KnowledgeBaseDoc doc = new KnowledgeBaseDoc();
        doc.setTitle(title);
        doc.setFilename(file.getOriginalFilename());
        doc.setFilepath(targetFile.getAbsolutePath());
        doc.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
        doc.setDocType(DocType.FILE.name());
        doc.setKbId(kbId);
        doc.setStatus(DocStatus.VECTORIZING.name());
        this.save(doc);

        threadPoolTaskExecutor.submit(() -> vectoringDoc(kbId, doc.getId(), targetFile));
        return doc.getId();
    }

    private File saveFile(Integer kbId, MultipartFile file) {
        String basePath = knowledgeBaseDocStorageProperties.getPath();
        File saveDir = new File(basePath, String.valueOf(kbId));
        File targetFile = new File(saveDir, file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败！", e);
        }
        return targetFile;
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
        KnowledgeBaseDoc knowledgeBaseDoc = new KnowledgeBaseDoc();
        knowledgeBaseDoc.setId(docId);
        knowledgeBaseDoc.setStatus(DocStatus.VECTORIZED.name());
        boolean b = this.updateById(knowledgeBaseDoc);
        if (!b) {
            throw new RuntimeException("更新文档失败！");
        }
    }

    private void validateKbId(Integer kbId) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase != null) {
            return;
        }
        throw new RuntimeException("知识库不存在");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDoc(Integer id, String title, Boolean enable, MultipartFile file) {
        KnowledgeBaseDoc knowledgeBaseDoc = getById(id);
        if (knowledgeBaseDoc == null) {
            throw new RuntimeException("文档不存在");
        }

        KnowledgeBaseDoc updateKnowledgeBaseDoc = new KnowledgeBaseDoc();
        updateKnowledgeBaseDoc.setId(id);
        updateKnowledgeBaseDoc.setTitle(title);
        updateKnowledgeBaseDoc.setEnable(enable);
        if (file == null) {
            boolean b = updateById(updateKnowledgeBaseDoc);
            if (!b) {
                throw new RuntimeException("更新文档失败！");
            }
        }
        if (file != null) {
            String filepath = knowledgeBaseDoc.getFilepath();
            // 保证删除文件安全
            if (filepath.startsWith(knowledgeBaseDocStorageProperties.getPath())) {
                try {
                    FileUtils.delete(new File(filepath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            File savedFile = saveFile(knowledgeBaseDoc.getKbId(), file);
            updateKnowledgeBaseDoc.setFilename(file.getOriginalFilename());
            updateKnowledgeBaseDoc.setFilepath(savedFile.getAbsolutePath());
            updateKnowledgeBaseDoc.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
            updateKnowledgeBaseDoc.setStatus(DocStatus.VECTORIZING.name());
            boolean b = updateById(updateKnowledgeBaseDoc);
            if (!b) {
                throw new RuntimeException("更新文档失败！");
            }
            threadPoolTaskExecutor.submit(() -> vectoringDoc(knowledgeBaseDoc.getKbId(), id, savedFile));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delDocByKbId(@NotNull Integer kbId) {
        List<KnowledgeBaseDoc> list = list(new LambdaQueryWrapper<KnowledgeBaseDoc>().eq(KnowledgeBaseDoc::getKbId, kbId));
        list.forEach(knowledgeBaseDoc -> delDoc(knowledgeBaseDoc.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delDoc(@NotNull Integer id) {
        KnowledgeBaseDoc knowledgeBaseDoc = getById(id);
        if (knowledgeBaseDoc == null) {
            throw new RuntimeException("文档不存在");
        }
        boolean b = removeById(id);
        if (!b) {
            throw new RuntimeException("删除文档失败！");
        }
        String filepath = knowledgeBaseDoc.getFilepath();
        if (filepath.startsWith(knowledgeBaseDocStorageProperties.getPath())) {
            try {
                FileUtils.delete(new File(filepath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(knowledgeBaseDoc.getKbId());
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
        embeddingStore.removeAll(MetadataFilterBuilder.metadataKey("docId").isEqualTo(id));
    }
}





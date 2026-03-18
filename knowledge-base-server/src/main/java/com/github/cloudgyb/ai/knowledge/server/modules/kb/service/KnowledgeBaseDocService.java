package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.cloudgyb.ai.knowledge.server.config.KnowledgeBaseDocStorageProperties;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.AiModelType;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.domain.AiModel;
import com.github.cloudgyb.ai.knowledge.server.modules.ai.service.AiModelService;
import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocStatus;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.DocType;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBase;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.domain.KnowledgeBaseDoc;
import com.github.cloudgyb.ai.knowledge.server.modules.kb.mapper.KnowledgeBaseDocMapper;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingModelFactory;
import com.github.cloudgyb.ai.knowledge.server.modules.rag.EmbeddingStoreFactory;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
    private final DocValidationService docValidationService;

    public KnowledgeBaseDocService(KnowledgeBaseService knowledgeBaseService,
                                   ThreadPoolTaskExecutor threadPoolTaskExecutor,
                                   KnowledgeBaseDocStorageProperties knowledgeBaseDocStorageProperties,
                                   AiModelService aiModelService,
                                   EmbeddingStoreFactory embeddingStoreFactory,
                                   EmbeddingModelFactory embeddingModelFactory,
                                   DocValidationService docValidationService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
        this.knowledgeBaseDocStorageProperties = knowledgeBaseDocStorageProperties;
        this.aiModelService = aiModelService;
        this.embeddingStoreFactory = embeddingStoreFactory;
        this.embeddingModelFactory = embeddingModelFactory;
        this.docValidationService = docValidationService;
    }

    public Page<KnowledgeBaseDoc> page(@NotNull Integer kbId, String title,
                                       @NotNull Integer pageNum, @NotNull Integer pageSize) {
        LambdaQueryWrapper<KnowledgeBaseDoc> queryWrapper = new LambdaQueryWrapper<KnowledgeBaseDoc>().
                eq(KnowledgeBaseDoc::getKbId, kbId)
                .like(StringUtils.isNotBlank(title), KnowledgeBaseDoc::getTitle, title);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Integer> addDoc(Integer kbId, String title, MultipartFile[] files) {
        if (files.length == 0) {
            throw new BusinessException("请上传文件！");
        }
        docValidationService.validateFiles(files);
        validateKbId(kbId);
        ArrayList<Integer> docIds = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            Integer id = addDoc(kbId, title, file);
            docIds.add(id);
        }

        return docIds;
    }

    private Integer addDoc(Integer kbId, String title, MultipartFile file) {
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
        // 如果目录不存在则创建
        if (!saveDir.exists()) {
            boolean mkdirs = saveDir.mkdirs();
            if (!mkdirs) {
                log.error("创建目录失败！");
                throw new BusinessException("创建目录失败！");
            }
        }
        File targetFile = new File(saveDir, file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            log.error("保存文件失败！", e);
            throw new BusinessException("保存文件失败！", e);
        }
        return targetFile;
    }

    private void vectoringDoc(Integer kbId, Integer docId, File targetFile) {
        try {
            KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
            if (knowledgeBase == null) {
                throw new BusinessException("知识库不存在");
            }
            Integer aiVectorModelId = knowledgeBase.getAiVectorModelId();
            AiModel aiModel = aiModelService.getById(aiVectorModelId);
            if (aiModel == null) {
                throw new BusinessException("知识库AI向量模型不存在！");
            }
            String modelType = aiModel.getModelType();
            if (!AiModelType.VECTOR.name().equals(modelType)) {
                throw new BusinessException("知识库AI向量模型类型不正确！");
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
                throw new BusinessException("更新文档失败！");
            }
        } catch (Exception e) {
            log.error("向量化文档失败！", e);
            KnowledgeBaseDoc knowledgeBaseDoc = new KnowledgeBaseDoc();
            knowledgeBaseDoc.setId(docId);
            knowledgeBaseDoc.setStatus(DocStatus.FAILED.name());
            boolean b = this.updateById(knowledgeBaseDoc);
            if (!b) {
                throw new BusinessException("更新文档失败！");
            }
        }
    }

    private void validateKbId(Integer kbId) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase != null) {
            return;
        }
        throw new BusinessException("知识库不存在");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDoc(Integer id, String title, Boolean enable, MultipartFile file) {
        KnowledgeBaseDoc knowledgeBaseDoc = getById(id);
        if (knowledgeBaseDoc == null) {
            throw new BusinessException("文档不存在");
        }

        KnowledgeBaseDoc updateKnowledgeBaseDoc = new KnowledgeBaseDoc();
        updateKnowledgeBaseDoc.setId(id);
        updateKnowledgeBaseDoc.setTitle(title);
        updateKnowledgeBaseDoc.setEnable(enable);
        if (file == null) {
            boolean b = updateById(updateKnowledgeBaseDoc);
            if (!b) {
                throw new BusinessException("更新文档失败！");
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
                throw new BusinessException("更新文档失败！");
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
            throw new BusinessException("文档不存在");
        }
        boolean b = removeById(id);
        if (!b) {
            throw new BusinessException("删除文档失败！");
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
            throw new BusinessException("知识库不存在");
        }
        Integer aiVectorModelId = knowledgeBase.getAiVectorModelId();
        AiModel aiModel = aiModelService.getById(aiVectorModelId);
        if (aiModel == null) {
            throw new BusinessException("知识库AI向量模型不存在！");
        }
        String modelType = aiModel.getModelType();
        if (!AiModelType.VECTOR.name().equals(modelType)) {
            throw new BusinessException("知识库AI向量模型类型不正确！");
        }
        EmbeddingModel embeddingModel = embeddingModelFactory.create(aiModel);
        EmbeddingStore<TextSegment> embeddingStore = embeddingStoreFactory.create(embeddingModel.dimension());
        embeddingStore.removeAll(MetadataFilterBuilder.metadataKey("docId").isEqualTo(id));
    }

    public void testDoc(@NotNull Integer kbId, @NotBlank String text) {
        KnowledgeBase knowledgeBase = knowledgeBaseService.getById(kbId);
        if (knowledgeBase == null) {
            throw new BusinessException("知识库不存在");
        }
        Integer aiVectorModelId = knowledgeBase.getAiVectorModelId();
        AiModel aiModel = aiModelService.getById(aiVectorModelId);
        if (aiModel == null) {
            throw new BusinessException("知识库AI向量模型不存在！");
        }
        String modelType = aiModel.getModelType();
        if (!AiModelType.VECTOR.name().equals(modelType)) {
            throw new BusinessException("知识库AI向量模型类型不正确！");
        }
        EmbeddingModel embeddingModel = embeddingModelFactory.create(aiModel);
        Response<Embedding> embedResponse = embeddingModel.embed(text);
        Embedding embedding = embedResponse.content();
        EmbeddingStore<TextSegment> embeddingStore = embeddingStoreFactory.create(embeddingModel.dimension());
        EmbeddingSearchRequest docId1 = EmbeddingSearchRequest.builder()
                .queryEmbedding(embedding)
                .maxResults(5)
                .minScore(0.65)
                .filter(MetadataFilterBuilder.metadataKey("docId").isIn(1))
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(docId1);
        List<EmbeddingMatch<TextSegment>> matches = result.matches();
        for (EmbeddingMatch<TextSegment> match : matches) {
            String text1 = match.embedded().text();
            Metadata metadata = match.embedded().metadata();
            System.out.println(text1 + "  " + metadata.getString("docId"));
        }
    }

    public List<Integer> listIdsByKbId(Integer id) {
        return list(new LambdaQueryWrapper<KnowledgeBaseDoc>()
                .select(KnowledgeBaseDoc::getId).eq(KnowledgeBaseDoc::getKbId, id))
                .stream().map(KnowledgeBaseDoc::getId).toList();
    }
}





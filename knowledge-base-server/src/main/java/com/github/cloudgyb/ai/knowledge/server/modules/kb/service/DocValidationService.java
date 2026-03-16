package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import com.github.cloudgyb.ai.knowledge.server.modules.commons.BusinessException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档校验服务
 *
 * @author cloudgyb
 * @since 2026/3/16 11:14
 */
@Service
public class DocValidationService {
    public void validateFiles(MultipartFile[] files) {
        for (MultipartFile file : files) {
            // todo 文件格式校验
        }
    }
}

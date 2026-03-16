package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import org.apache.tika.mime.MediaType;

import javax.print.attribute.standard.Media;
import java.io.File;
import java.io.IOException;

/**
 * 文件MimeType检测器
 *
 * @author cloudgyb
 * @since 2026/3/16 11:19
 */
public interface MediaTypeDetector {
    /**
     * 仅根据文件名探测文件MimeType
     *
     * @param fileName fileName
     * @return MediaType
     */
    MediaType detect(String fileName);

    MediaType detect(File file) throws IOException;

    MediaType detect(byte[] prefix);

}

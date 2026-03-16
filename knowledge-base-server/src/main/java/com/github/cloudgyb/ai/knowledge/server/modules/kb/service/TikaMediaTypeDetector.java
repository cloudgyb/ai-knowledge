package com.github.cloudgyb.ai.knowledge.server.modules.kb.service;

import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;

import java.io.File;
import java.io.IOException;

/**
 * 文件MimeType检测器
 *
 * @author cloudgyb
 * @since 2026/3/16 11:19
 */
public class TikaMediaTypeDetector implements MediaTypeDetector {
    private final Tika tika = new Tika();

    @Override
    public MediaType detect(String fileName) {
        String detect = tika.detect(fileName);
        return MediaType.parse(detect);
    }

    @Override
    public MediaType detect(File file) throws IOException {
        String detect = tika.detect(file);
        return MediaType.parse(detect);
    }

    @Override
    public MediaType detect(byte[] prefix) {
        String mediaTypeStr = tika.detect(prefix);
        return MediaType.parse(mediaTypeStr);
    }

    public static void main(String[] args) throws IOException {
        TikaMediaTypeDetector tikaMediaTypeDetector = new TikaMediaTypeDetector();
        System.out.println(tikaMediaTypeDetector.detect("C:\\Users\\Lenovo\\Downloads\\《云存储文件加密与访问审计系统》功能需求规格.docx"));
        System.out.println(tikaMediaTypeDetector.detect("C:\\Users\\Lenovo\\Downloads\\xterminal-repo-password.txt"));
        System.out.println(tikaMediaTypeDetector.detect("D:\\EBooks\\[代码大全2中文版(完整清晰版)].pdf"));
        System.out.println(tikaMediaTypeDetector.detect("D:\\EBooks\\ncurses - 副本.txt"));
        System.out.println(tikaMediaTypeDetector.detect(new File("C:\\Users\\Lenovo\\Downloads\\《云存储文件加密与访问审计系统》功能需求规格 - 副本")));
    }
}

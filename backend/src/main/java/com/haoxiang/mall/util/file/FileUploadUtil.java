package com.haoxiang.mall.util.file;

import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.util.date.DateUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件上传帮助类
 */
public class FileUploadUtil {

    public static String uploadFile(MultipartFile file, String filePath) throws IOException {

        //检查文件名长度
        int fileNameLength = file.getOriginalFilename().length();
        if (fileNameLength > 100) {
            throw new BusinessException("文件名称过长");
        }
        String fileName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(fileName);
        //获取文件扩展名
        if (StringUtils.isEmpty(extension)) {
            extension = MimeTypeUtils.getExtension(file.getContentType());
        }
        //重新生成文件名
        String encodingFilename = FileUtils.encodingFilename(fileName);
        fileName = DateUtils.datePath() + "/" + encodingFilename + "." + extension;
        //创建目录
        File desc = new File(filePath, fileName);
        //检查目录存在
        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        //检查文件是否存在
        if (!desc.exists()) {
            desc.createNewFile();
        }
        //保存文件
        file.transferTo(desc);
        return fileName;
    }
}

package com.campus.lostfound.file;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, "文件不能为空"));
        }

        try {
            String fileUrl = saveFile(file);
            return ResponseEntity.ok(ApiResponse.success("上传成功", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.failure(ResultCode.INTERNAL_ERROR, "文件保存失败: " + e.getMessage()));
        }
    }

    @PostMapping("/uploads")
    public ResponseEntity<ApiResponse<List<String>>> uploadMultiple(@RequestParam("files") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                errors.add("空文件: " + file.getOriginalFilename());
                continue;
            }
            try {
                String fileUrl = saveFile(file);
                fileUrls.add(fileUrl);
            } catch (IOException e) {
                errors.add("保存失败: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && fileUrls.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, String.join("; ", errors)));
        }

        return ResponseEntity.ok(ApiResponse.success("上传完成", fileUrls));
    }

    private String saveFile(MultipartFile file) throws IOException {
        // Create upload directory if not exists
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        // Save file
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        // Return URL path
        return "/" + uploadPath + "/" + filename;
    }
}
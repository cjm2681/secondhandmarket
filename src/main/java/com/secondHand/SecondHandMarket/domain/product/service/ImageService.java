package com.secondHand.SecondHandMarket.domain.product.service;

import com.secondHand.SecondHandMarket.global.exception.CustomException;
import com.secondHand.SecondHandMarket.global.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {


    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();
        return files.stream()
                .map(this::uploadImage)
                .toList();
    }

    public String uploadImage(MultipartFile file) {
        validateImageFile(file);

        String ext = getExtension(file.getOriginalFilename());
        String fileName = "products/" + UUID.randomUUID() + ext;  // S3 폴더 구조

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String url = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + fileName;
            log.info("S3 업로드 완료: {}", url);
            return url;

        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            // URL에서 S3 key 추출
            String key = imageUrl.substring(imageUrl.indexOf("products/"));
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            log.info("S3 이미지 삭제 완료: {}", key);
        } catch (Exception e) {
            log.warn("S3 이미지 삭제 실패: {}", imageUrl);
        }
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) throw new CustomException(ErrorCode.EMPTY_FILE);
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }


//    // 나중에 S3Config로 교체할 부분
//    private final String uploadDir = "uploads/products/";
//
//    @PostConstruct
//    public void init() {
//        // 업로드 디렉토리 없으면 생성
//        File dir = new File(uploadDir);
//        if (!dir.exists()) dir.mkdirs();
//    }
//
//    public List<String> uploadImages(List<MultipartFile> files) {
//        if (files == null || files.isEmpty()) return List.of();
//
//        return files.stream()
//                .map(this::uploadImage)
//                .toList();
//    }
//
//    public String uploadImage(MultipartFile file) {
//        validateImageFile(file);
//
//        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//        Path path = Paths.get(uploadDir + fileName);
//
//        try {
//            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//        } catch (IOException e) {
//            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
//        }
//
//        // 나중에 S3 URL로 교체
//        return "/images/products/" + fileName;
//    }
//
//    public void deleteImage(String imageUrl) {
//        String fileName = imageUrl.replace("/images/products/", "");
//        Path path = Paths.get(uploadDir + fileName);
//        try {
//            Files.deleteIfExists(path);
//        } catch (IOException e) {
//            log.warn("이미지 삭제 실패: {}", imageUrl);
//        }
//    }
//
//    private void validateImageFile(MultipartFile file) {
//        if (file.isEmpty()) throw new CustomException(ErrorCode.EMPTY_FILE);
//
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
//        }
//
//        // 10MB 제한
//        if (file.getSize() > 10 * 1024 * 1024) {
//            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
//        }
//    }

}
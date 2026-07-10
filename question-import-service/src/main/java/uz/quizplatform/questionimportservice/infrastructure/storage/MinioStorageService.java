package uz.quizplatform.questionimportservice.infrastructure.storage;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.imports:question-imports}")
    private String bucketName;

    public InputStream getFile(String filePath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to fetch file from MinIO: {}", filePath, e);
            throw new RuntimeException("Storage error: " + e.getMessage(), e);
        }
    }

    public void uploadFile(String objectName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760) // 10MB chunk parts
                            .contentType(contentType)
                            .build()
            );
            log.info("File uploaded successfully to MinIO bucket={}: {}", bucketName, objectName);
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", objectName, e);
            throw new RuntimeException("Storage upload error: " + e.getMessage(), e);
        }
    }
}

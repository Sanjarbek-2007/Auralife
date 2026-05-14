package uz.project.auralife.services;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uz.auralife.filesapi.grpc.DeleteFileRequest;
import uz.auralife.filesapi.grpc.DeleteFileResponse;
import uz.auralife.filesapi.grpc.FileServiceGrpc;
import uz.auralife.filesapi.grpc.UploadFileRequest;
import uz.auralife.filesapi.grpc.UploadFileResponse;

import java.io.IOException;

@Service
@Slf4j
public class FilesGrpcClient {

    @GrpcClient("files-api")
    private FileServiceGrpc.FileServiceBlockingStub fileServiceStub;

    public String uploadFile(MultipartFile file, String dependency, String purpose) throws IOException {
        UploadFileRequest request = UploadFileRequest.newBuilder()
                .setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown")
                .setDependency(dependency)
                .setPurpose(purpose)
                .setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .setContent(ByteString.copyFrom(file.getBytes()))
                .build();

        UploadFileResponse response = fileServiceStub.uploadFile(request);
        return response.getFileId();
    }

    public void deleteFile(String fileId) {
        DeleteFileRequest request = DeleteFileRequest.newBuilder()
                .setFileId(fileId)
                .build();
        DeleteFileResponse response = fileServiceStub.deleteFile(request);
        if (!response.getSuccess()) {
            log.error("Failed to delete file via gRPC: {}", fileId);
        }
    }
}

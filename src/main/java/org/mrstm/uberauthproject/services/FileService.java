package org.mrstm.uberauthproject.services;

import jakarta.persistence.AccessType;
import org.mrstm.uberauthproject.helpers.S3InputStreamWrapper;
import org.springframework.stereotype.Service;

@Service
public interface FileService {
    String generateGetPresignedUrl(String filePath);
    String generatePutPresignedUrl(String filePath, FileAccessType fileAccessType);
    S3InputStreamWrapper downloadFile(String fileName);
}

package com.example.filehubshering.Model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "files")
@Data
public class FileMetadata 
{
    @Id
    private String id;
    private String filename;
    private String userId;
    private String filePath;
    private LocalDateTime expiryTime; // Auto-delete file after expiry

}

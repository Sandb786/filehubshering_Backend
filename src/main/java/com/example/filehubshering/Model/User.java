package com.example.filehubshering.Model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "users")
@Data
public class User 
{
    @Id
    private String id;
    private String userid;
    private String username;
    private String password;
    private LocalDateTime expiryTime; // Auto-delete file after expiry

}

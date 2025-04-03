package com.example.filehubshering.Reposetries;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.filehubshering.Model.FileMetadata;

public interface FileRepository extends MongoRepository<FileMetadata, String>
{
    void deleteByUserId(String userId);
    void deleteByFilename(String filename);
    FileMetadata findByUserIdAndFilename(String userId,String filename);
    List<FileMetadata> findByUserId(String userId);
}

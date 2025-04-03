package com.example.filehubshering.Reposetries;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.filehubshering.Model.User;

public interface UserRepository extends MongoRepository<User, String>
{
    User findByUsername(String username);
    User findByUserid(String userId);
    void deleteByUsername(String username);
}

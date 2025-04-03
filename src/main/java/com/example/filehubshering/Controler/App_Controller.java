package com.example.filehubshering.Controler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.filehubshering.Model.FileMetadata;
import com.example.filehubshering.Model.User;
import com.example.filehubshering.Reposetries.FileRepository;
import com.example.filehubshering.Reposetries.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend requests
public class App_Controller 
{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    String UPLOAD_DIR = Paths.get(System.getProperty("user.dir"), "uploads").toString();  

    // ✅ Register Temporary User
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) 
    {
        
        if (user==null || user.getUsername().equals("")|| user.getUserid().equals("") || user.getPassword().equals(""))
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username,UserId and Password are required!"); 
        }

        // Check if user already exists
        if (userRepository.findByUsername(user.getUsername()) != null || userRepository.findByUserid(user.getUserid()) != null) 
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already exists!"); // 403
        }
        
          // Create new user
          user.setExpiryTime(LocalDateTime.now().plusMinutes(30)); // Auto-delete after 1 hours
          userRepository.save(user);

          System.out.println("\n\n Temporary User Created: " + user.getUsername() + "\n\n");

        return ResponseEntity.status(HttpStatus.CREATED).body("Temporary user created!");
    }
    

    // ✅ User Login (Simple String Comparison)
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) 
    {
        User existingUser = userRepository.findByUserid(user.getUserid());

        if (existingUser == null) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found!");
        }

        if(!existingUser.getPassword().equals(user.getPassword())) 
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong Password!");
        }
       
        System.out.println("\n\n User Login: " + user.getUsername() + "\n\n");

        return ResponseEntity.status(HttpStatus.OK).body("Login successful!");
    }
    

    // ✅ Upload File
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("userid") String userid,
            @RequestParam("password") String password) throws IOException 
    {


        if (file.isEmpty()) 
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty!");
        }

        if(userRepository.findByUserid(userid) == null) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found!");
            
        }

         if (fileRepository.findByUserIdAndFilename(userid, file.getOriginalFilename()) != null)
         {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("File already exists!"); // 403
         }

        String filePath = UPLOAD_DIR+"\\"+userid+file.getOriginalFilename();
        File dest = new File(filePath);
        file.transferTo(dest);

        FileMetadata metadata = new FileMetadata();
        metadata.setFilename(file.getOriginalFilename());
        metadata.setUserId(userid);
        metadata.setFilePath(filePath);
        metadata.setExpiryTime(LocalDateTime.now().plusMinutes(30)); // Auto-delete after
        fileRepository.save(metadata);

        System.out.println("\n\nFile Uploaded: " + file.getOriginalFilename() + "\nPath: " + filePath);

        return ResponseEntity.status(HttpStatus.CREATED).body("File uploaded successfully!");
    }

    @DeleteMapping("/deletefile")
    public ResponseEntity<?> deleteFile(@RequestParam("userid") String userid,@RequestParam("filename") String filename) throws IOException 
    {
        
        FileMetadata fileMetadata = fileRepository.findByUserIdAndFilename(userid, filename);

        if (fileMetadata == null) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found!");
        }
        // File  
        File file = new File(fileMetadata.getFilePath());
        

        if (file.exists()&& fileMetadata != null) 
        {
            file.delete(); // Delete the file from the server
            fileRepository.delete(fileMetadata); // Delete the file metadata from the database
            System.out.println("\n\nFile Deleted: " + filename + "\nPath: " + fileMetadata.getFilePath());
            return ResponseEntity.status(HttpStatus.OK).body("File deleted successfully!");
        } 
        else 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Data not found!");
        }
    }


    @GetMapping("/getallfile")
    public ResponseEntity<?> getAllFile(@RequestParam("userid") String userid) throws IOException 
    {
        
         User user = userRepository.findByUserid(userid);

        if (user == null) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found!");
        }
        
        List<FileMetadata> fileMetadata = fileRepository.findByUserId(userid);
    
        if (fileMetadata.isEmpty()) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No file found for this user.");
        }
        
        // System.out.println("\n\n File List: " + fileMetadata + "\n\n");

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(fileMetadata); // Return the list of files for the user
    }


    // ✅ Download File
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("userid") String userid, 
                                          @RequestParam("filename") String filename) throws IOException {
       
        
        // Find user by ID
        User user = userRepository.findByUserid(userid);

        // Check if user exists
        if (user == null) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found!");
        }
        
    
        // Find file by user ID and filename
        FileMetadata fileMetadata = fileRepository.findByUserIdAndFilename(userid, filename);
        if (fileMetadata==null) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found!");
        }
    
        // Get file path and return as resource
        Path path = Paths.get(fileMetadata.getFilePath());
        Resource resource = new UrlResource(path.toUri());
    
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File not accessible!");
        }

        System.out.println("\n\nFile Downloaded: " + filename + "\nPath: " + fileMetadata.getFilePath());

        // Set headers for download
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getFilename() + "\"")
                .body(resource);
    }
    

    @GetMapping("/getuser")
    public ResponseEntity<?> getUser(@RequestParam("userid") String userid) throws IOException 
    {

        User user = userRepository.findByUserid(userid);

        if (user == null) 
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found!");
        }
        
        System.out.println("\n\n User List: " + user + "\n\n");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(user); // Return the list of files for the user
    }
    


    // ✅ Auto-delete Expired Users & Files
    @Scheduled(fixedRate = 15 * 60 * 1000) // ✅ Runs every 15 Minut
    public void deleteExpiredData() 
    {
        System.out.println("\n\n[Scheduler] Checking for expired users and files...");

        LocalDateTime now = LocalDateTime.now();

        userRepository.findAll().forEach(user -> {

            // Check if user is expired
            if (user.getExpiryTime() != null && user.getExpiryTime().isBefore(now)) 
            {
                
                List<FileMetadata> filesData=fileRepository.findByUserId(user.getUserid());

                 if (!filesData.isEmpty()) 
                 {
                    for (FileMetadata fileData : filesData) 
                    {
                        File file = new File(fileData.getFilePath());
                        file.delete();
                        
                        System.out.println("\n\nfile Deleted: "+fileData.getFilename());
                    }
                }
                
                System.out.println("\n\n[Scheduler] Deleting user: " + user.getId());
                fileRepository.deleteByUserId(user.getUserid()); // ✅ Delete user's files
                userRepository.delete(user); // ✅ Delete user
            }
        });

        System.out.println("[Scheduler] Expired users and files deleted.");
    }

}

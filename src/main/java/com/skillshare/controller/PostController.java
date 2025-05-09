package com.skillshare.controller;

import com.skillshare.model.MediaFile;
import com.skillshare.model.Post;
import com.skillshare.model.PostType;
import com.skillshare.model.User;
import com.skillshare.repository.MediaFileRepository;
import com.skillshare.repository.PostRepository;
import com.skillshare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MediaFileRepository mediaFileRepository;

    @GetMapping
    public ResponseEntity<Page<Post>> getPosts(Pageable pageable) {
        return ResponseEntity.ok(postRepository.findAll(pageable));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<Post>> getFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(postRepository.findFollowingUsersPosts(user.getId(), pageable));
    }

    @PostMapping
public ResponseEntity<Post> createPost(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam("content") String content,
        @RequestParam("type") String type,
        @RequestParam(value = "media", required = false) List<MultipartFile> media
) {
    User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

    Post post = new Post();
    post.setContent(content);
    post.setUser(user);
    post.setType(PostType.valueOf(type));
    
    // Save the post first to get its ID
    Post savedPost = postRepository.save(post);
    
    // Handle media file upload and storage
    if (media != null && !media.isEmpty()) {
        List<MediaFile> mediaFiles = new ArrayList<>();
        
        for (MultipartFile file : media) {
            if (!file.isEmpty()) {
                try {
                    // Create a unique filename
                    String originalFilename = file.getOriginalFilename();
                    String fileExtension = getFileExtension(originalFilename);
                    String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
                    
                    // Define the file path where media will be stored
                    String uploadDir = "uploads/" + savedPost.getId();
                    Path uploadPath = Paths.get(uploadDir);
                    
                    // Create directories if they don't exist
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    
                    // Save the file
                    Path filePath = uploadPath.resolve(uniqueFilename);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Create and save MediaFile entity
                    MediaFile mediaFile = new MediaFile();
                    mediaFile.setFilename(uniqueFilename);
                    mediaFile.setOriginalFilename(originalFilename);
                    mediaFile.setContentType(file.getContentType());
                    mediaFile.setSize(file.getSize());
                    mediaFile.setFilePath(uploadDir + "/" + uniqueFilename);
                    mediaFile.setPost(savedPost);
                    
                    mediaFiles.add(mediaFileRepository.save(mediaFile));
                    
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store media file", e);
                }
            }
        }
        
        // Update post with media files
        savedPost.setMediaFiles(mediaFiles);
        savedPost = postRepository.save(savedPost);
    }
    
    return ResponseEntity.ok(savedPost);
}

/**
 * Extract file extension from filename
 */
private String getFileExtension(String filename) {
    if (filename == null) {
        return "";
    }
    int dotIndex = filename.lastIndexOf('.');
    return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
}

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Post updatedPost
    ) {
        return postRepository.findById(id)
                .map(post -> {
                    if (!post.getUser().getEmail().equals(userDetails.getUsername())) {
                        return ResponseEntity.badRequest()
                                .body("You can only update your own posts");
                    }
                    post.setContent(updatedPost.getContent());
                    return ResponseEntity.ok(postRepository.save(post));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        return postRepository.findById(id)
                .map(post -> {
                    if (!post.getUser().getEmail().equals(userDetails.getUsername())) {
                        return ResponseEntity.badRequest()
                                .body("You can only delete your own posts");
                    }
                    postRepository.delete(post);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return postRepository.findById(id)
                .map(post -> {
                    post.getLikes().add(user);
                    return ResponseEntity.ok(postRepository.save(post));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikePost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return postRepository.findById(id)
                .map(post -> {
                    post.getLikes().remove(user);
                    return ResponseEntity.ok(postRepository.save(post));
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 
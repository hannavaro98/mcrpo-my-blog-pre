package com.myblog.controller;

import com.myblog.dto.CreateCommentRequest;
import com.myblog.dto.UpdateCommentRequest;
import com.myblog.model.Comment;
import com.myblog.model.Post;
import com.myblog.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts/{postId}/comments")
public class CommentController {

    private static final Logger log = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
        log.debug("GET /api/posts/{}/comments", postId);
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        
        log.debug("GET /api/posts/{}/comments/{}", postId, commentId);
        Optional<Comment> comment = commentService.getCommentById(commentId);
        return comment.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @RequestBody CreateCommentRequest request) {
        
        log.debug("POST /api/posts/{}/comments - text: {}", postId, request.getText());
        Comment createdComment = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequest request) {

        log.debug("PUT /api/posts/{}/comments/{}", postId, commentId);
        //Вызов commentService.updateComment(commentId, request)
        try {
            Comment updatedComment = commentService.updateComment(commentId, request);
            return ResponseEntity.ok(updatedComment); //Возврат статус 200 OK при успехе
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();//404 Not Found, если коммент не найден
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        log.debug("DELETE /api/posts/{}/comments/{}", postId, commentId);
        //Вызов commentService.deleteComment(commentId)
        commentService.deleteComment(commentId);
        //Возврат статус 200 OK
        return ResponseEntity.ok().build();
    }
}


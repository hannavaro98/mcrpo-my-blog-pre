package com.myblog.dao;

import com.myblog.config.DatabaseConfig;
import com.myblog.dao.impl.CommentDaoImpl;
import com.myblog.dao.impl.PostDaoImpl;
import com.myblog.dao.impl.TagDaoImpl;
import com.myblog.model.Comment;
import com.myblog.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DatabaseConfig.class, PostDaoImpl.class, TagDaoImpl.class, CommentDaoImpl.class})
@Transactional
class CommentDaoIntegrationTest {

    @Autowired
    private PostDao postDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentDao commentDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM post_images");
        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");
    }

    //Тест метода updateComment() класса CommentDaoImpl на успешное обновление коммента
    @Test
    void testUpdateComment() {
        // Given
        Post post = new Post();
        post.setTitle("Original Title");
        post.setText("Original content");
        post.setTags(Arrays.asList("tag1", "tag2"));
        Post createdPost = postDao.create(post);

        Comment comment = new Comment();
        comment.setPostId(createdPost.getId());
        comment.setText("New comment");
        Comment createdComment = commentDao.create(comment);

        // When
        createdComment.setText("Updated comment");
        Comment updatedComment = commentDao.update(createdComment);

        // Then
        assertEquals("Updated comment", updatedComment.getText());

    }

    //Тест метода deleteComment() класса CommentDaoImpl на успешное удаление коммента
    @Test
    void testDeleteComment() {
        // Given
        Post post = new Post();
        post.setTitle("Original Title");
        post.setText("Original content");
        post.setTags(Arrays.asList("tag1", "tag2"));
        Post createdPost = postDao.create(post);

        Comment comment = new Comment();
        comment.setPostId(createdPost.getId());
        comment.setText("New comment");
        Comment createdComment = commentDao.create(comment);

        // When
        commentDao.delete(createdComment.getId());


        // Then
        Optional<Comment> foundComment = commentDao.findById(createdComment.getId());
        assertFalse(foundComment.isPresent());
    }


}


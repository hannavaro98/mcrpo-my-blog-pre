package com.myblog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myblog.config.DatabaseConfig;
import com.myblog.config.RootConfig;
import com.myblog.config.WebConfig;
import com.myblog.dto.CreateCommentRequest;
import com.myblog.dto.CreatePostRequest;
import com.myblog.dto.UpdateCommentRequest;
import com.myblog.dto.UpdatePostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, WebConfig.class, DatabaseConfig.class})
@WebAppConfiguration
@Transactional
class CommentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        jdbcTemplate.execute("DELETE FROM post_images");
        jdbcTemplate.execute("DELETE FROM post_tags");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM tags");
        jdbcTemplate.execute("DELETE FROM posts");
    }

    //Тест метода UpdateComment() класса CommentController на успешное обновление коммента
    @Test
    void testUpdateComment() throws Exception {
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("New post");
        postRequest.setText("New content");
        postRequest.setTags(Arrays.asList("tag1", "tag2"));

        //Создание поста
        String postResponse = mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long postId = objectMapper.readTree(postResponse).get("id").asLong();

        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setText("New comment");
        commentRequest.setPostId(postId);

        //Создание коммента
        String commentResponse = mockMvc.perform(post("/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(commentResponse).get("id").asLong();


        UpdateCommentRequest updateRequest = new UpdateCommentRequest();
        updateRequest.setId(commentId);
        updateRequest.setPostId(postId);
        updateRequest.setText("Updated comment");


        //Обновление коммента
        mockMvc.perform(put("/posts/" + postId + "/comments/" + commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    //Тест метода UpdateComment() класса CommentController при несуществующем комменте
    @Test
    void testUpdateCommentNonExistent() throws Exception {
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("New post");
        postRequest.setText("New content");
        postRequest.setTags(Arrays.asList("tag1", "tag2"));

        //Создание поста
        String postResponse = mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long postId = objectMapper.readTree(postResponse).get("id").asLong();

        Long commentId = 100L;


        UpdateCommentRequest updateRequest = new UpdateCommentRequest();
        updateRequest.setId(commentId);
        updateRequest.setPostId(postId);
        updateRequest.setText("Updated comment");


        //Обновление коммента
        mockMvc.perform(put("/posts/" + postId + "/comments/" + commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    //Тест метода DeleteComment() класса CommentController на успешное удаление коммента
    @Test
    void testDeleteComment() throws Exception {
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setTitle("New post");
        postRequest.setText("New content");
        postRequest.setTags(Arrays.asList("tag1", "tag2"));

        //Создание поста
        String postResponse = mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long postId = objectMapper.readTree(postResponse).get("id").asLong();

        CreateCommentRequest commentRequest = new CreateCommentRequest();
        commentRequest.setText("New comment");
        commentRequest.setPostId(postId);

        //Создание коммента
        String commentResponse = mockMvc.perform(post("/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(commentResponse).get("id").asLong();


        //Обновление коммента
        mockMvc.perform(delete("/posts/" + postId + "/comments/" + commentId))
                .andExpect(status().isOk());

        //Проверка коммента
        mockMvc.perform(get("/posts/" + postId + "/comments/" + commentId))
                .andExpect(status().isNotFound());
    }
}


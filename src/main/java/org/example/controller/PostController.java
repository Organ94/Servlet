package org.example.controller;

import com.google.gson.Gson;
import org.example.model.Post;
import org.example.service.PostService;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;

@Controller
public class PostController {
    public static final String APPLICATION_JSON = "application/json";
    private final PostService service;
    private final Gson gson = new Gson();

    public PostController(PostService service) {
        this.service = service;
    }

    public void all(HttpServletResponse response) throws IOException {
        deserialiseAndSerialise(response, service.all());
    }

    public void getById(long id, HttpServletResponse response) throws IOException {
        deserialiseAndSerialise(response, service.getById(id));
    }

    public void save(Reader body, HttpServletResponse response) throws IOException {
        Post post = gson.fromJson(body, Post.class);
        deserialiseAndSerialise(response, service.save(post));
    }

    public void removeById(long id, HttpServletResponse response) throws IOException {
        service.removeById(id);
        deserialiseAndSerialise(response, "post with id=" + id + "delete");
    }

    private <T> void deserialiseAndSerialise(HttpServletResponse resp, T data) throws IOException {
        resp.setContentType(APPLICATION_JSON);
        String toJson = gson.toJson(data);
        resp.getWriter().print(toJson);
    }
}

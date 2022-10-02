package org.example.servlet;

import org.example.Handler;
import org.example.config.JavaConfig;
import org.example.controller.PostController;
import org.example.exception.NotFoundException;
import org.example.repository.PostRepository;
import org.example.repository.PostRepositoryImpl;
import org.example.service.PostService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainServlet extends HttpServlet {

    private PostController controller;
    private final Map<String, Map<String, Handler>> handlers = new ConcurrentHashMap<>();
    private static final String PATH = "/api/posts";
    private static final String PATH_WITH_PARAMS = "/api/posts/";


    @Override
    public void init() {

        final var context = new AnnotationConfigApplicationContext("org.example");
        controller = context.getBean(PostController.class);

        addHandler("GET", PATH, (path, req, resp) -> {
            controller.all(resp);
            resp.setStatus(HttpServletResponse.SC_OK);
        });
        addHandler("GET", PATH_WITH_PARAMS, (path, req, resp) -> {
            try {
                controller.getById(getByIdParsePath(path), resp);
                resp.setStatus(HttpServletResponse.SC_OK);
            } catch (NotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });
        addHandler("POST", PATH, (path, req, resp) -> {
            controller.save(req.getReader(), resp);
            resp.setStatus(HttpServletResponse.SC_OK);
        });
        addHandler("DELETE", PATH_WITH_PARAMS, (path, req, resp) -> {
            try {
                controller.removeById(getByIdParsePath(path), resp);
                resp.setStatus(HttpServletResponse.SC_OK);
            } catch (NotFoundException e) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        });

    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var method = req.getMethod();
            final var path = req.getRequestURI();

            String pathToHandler = path;
            if (path.startsWith(PATH_WITH_PARAMS) && path.matches(PATH_WITH_PARAMS + "\\d+")) {
                pathToHandler = PATH_WITH_PARAMS;
            } else if (path.startsWith(PATH)) {
                pathToHandler = PATH;
            }

            Handler handler = handlers.get(method).get(pathToHandler);
            handler.handle(path, req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void addHandler(String method, String path, Handler handler) {
        Map<String, Handler> map = new ConcurrentHashMap<>();
        if (handlers.containsKey(method)) {
            map = handlers.get(method);
        }
        map.put(path, handler);
        handlers.put(method, map);
    }

    private long getByIdParsePath(String path) {
        return Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
    }
}

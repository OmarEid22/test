package com.test.security.ai;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class PythonApiProxyController {

    @Autowired
    private RestTemplate restTemplate;



    @PostMapping("/regenerate-placements")
    public ResponseEntity<String> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) String body,
                                        @RequestHeader HttpHeaders headers) {

        String url = "http://localhost:8000" + extractProxiedPath(request);
//        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping(path = "/upload/**", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> proxyMultipart(MultipartHttpServletRequest request) throws IOException {
        MultipartFile file = request.getFile("file");

        String url = "http://localhost:8000" + extractProxiedPath(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    private String extractProxiedPath(HttpServletRequest request) {
        String path = request.getRequestURI().replaceFirst("/ai", "");
        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }
        return path;
    }

    private String extractProxiedPath(MultipartHttpServletRequest request) {
        String path = request.getRequestURI().replaceFirst("/ai/upload", "");
        if (request.getQueryString() != null) {
            path += "?" + request.getQueryString();
        }
        return path;
    }
}



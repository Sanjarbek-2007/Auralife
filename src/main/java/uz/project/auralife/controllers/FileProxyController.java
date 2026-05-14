package uz.project.auralife.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class FileProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${files.api.url:http://localhost:20008}")
    private String filesApiUrl;

    @GetMapping("/files/proxy/{fileId}")
    public ResponseEntity<Resource> proxyFile(@PathVariable String fileId) {
        String url = filesApiUrl + "/files/" + fileId;
        return restTemplate.getForEntity(url, Resource.class);
    }
}

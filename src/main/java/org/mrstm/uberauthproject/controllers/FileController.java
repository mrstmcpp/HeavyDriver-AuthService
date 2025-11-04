package org.mrstm.uberauthproject.controllers;

import lombok.RequiredArgsConstructor;
import org.mrstm.uberauthproject.helpers.S3InputStreamWrapper;
import org.mrstm.uberauthproject.services.FileAccessType;
import org.mrstm.uberauthproject.services.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @GetMapping("/{fileName}")
    public ResponseEntity<String> getUrl(@PathVariable String fileName){
        String url = fileService.generateGetPresignedUrl(fileName);
        return ResponseEntity.ok(url);
    }

    @PostMapping("/pre-signed-url")
    public ResponseEntity<Map<String, Object>> generateUrl(@RequestParam(name = "filename" , required = false, defaultValue = "") String filename,
                                                           @RequestParam(name = "fileAccessType" , required = false , defaultValue = "PRIVATE") FileAccessType fileAccessType
                                                           ){
        String url = fileService.generatePutPresignedUrl(filename , fileAccessType);
        return ResponseEntity.ok(Map.of("url" , url, "file" , filename));
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<S3InputStreamWrapper> downloadFile(@PathVariable("fileName") String fileName) throws Exception {
        return new ResponseEntity<>(fileService.downloadFile(fileName) , HttpStatus.OK);
    }

}

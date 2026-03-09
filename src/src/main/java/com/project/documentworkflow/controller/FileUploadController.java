package com.project.documentworkflow.controller;

import com.project.documentworkflow.dto.ApiResponse;
import com.project.documentworkflow.dto.UploadResponse;
import com.project.documentworkflow.service.DocumentWorkflowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Autowired
    private DocumentWorkflowService documentWorkflowService;

    @PostMapping("/upload")
    public ApiResponse<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {

        UploadResponse response = documentWorkflowService.processUpload(file);

        return new ApiResponse<>(true, response, null);
    }
}

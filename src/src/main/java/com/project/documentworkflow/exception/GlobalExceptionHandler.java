package com.project.documentworkflow.exception;

import com.project.documentworkflow.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleRuntimeException(RuntimeException ex) {

        return new ApiResponse<>(
                false,
                null,
                ex.getMessage()
        );
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<String> handleDocumentNotFound(DocumentNotFoundException ex) {

        return new ApiResponse<>(false, null, ex.getMessage());
    }

}

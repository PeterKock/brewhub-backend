package nl.pkock.brewhub_backend.community.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<String> handleDuplicateReport(DuplicateReportException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
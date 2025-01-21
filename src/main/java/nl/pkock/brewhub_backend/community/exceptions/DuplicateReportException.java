package nl.pkock.brewhub_backend.community.exceptions;

public class DuplicateReportException extends RuntimeException {
    public DuplicateReportException(String message) {
        super(message);
    }
}
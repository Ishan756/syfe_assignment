package com.financemanager.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesNotFound() {
        var response = handler.handleNotFound(new ResourceNotFoundException("Not found"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((Map<String, String>) response.getBody()).containsEntry("message", "Not found");
    }

    @Test
    void handlesConflict() {
        var response = handler.handleConflict(new ConflictException("Conflict"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handlesBadRequest() {
        var response = handler.handleBadRequest(new BadRequestException("Bad request"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handlesForbidden() {
        var response = handler.handleForbidden(new ForbiddenException("Forbidden"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
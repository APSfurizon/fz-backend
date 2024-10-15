package net.furizon.backend.web.handlers;

import net.furizon.backend.web.entities.HttpErrorResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<String, String>();
        List<String> generalErrors = new ArrayList<String>();
        ex.getBindingResult().getAllErrors().forEach((error)-> {
            if (error instanceof FieldError ferr){
                String fieldName = ferr.getField();
                String errorMessage = ferr.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            } else {
                generalErrors.add(error.getDefaultMessage());
            }
        });
        HttpErrorResponse her = HttpErrorResponse.of("Unprocessable entity", 422, errors, generalErrors);
        return new ResponseEntity<>(her, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<HttpErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception", e);
        var response = HttpErrorResponse.of("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

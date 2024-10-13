package net.furizon.backend.web.entities;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class HttpErrorResponse {
    private String message;
    private int status;
    private Map<String, String> errors;
    private List<String> generalErrors;

    public static HttpErrorResponse of(String message, int status, Map<String, String> errors, List<String> generalErrors) {
        HttpErrorResponse toReturn = new HttpErrorResponse();
        toReturn.message = message;
        toReturn.status = status;
        toReturn.errors = errors;
        toReturn.generalErrors = generalErrors;
        return toReturn;
    }

    public static HttpErrorResponse of(String message, int status) {
        HttpErrorResponse toReturn = new HttpErrorResponse();
        toReturn.message = message;
        toReturn.status = status;
        return toReturn;
    }

}

package dev.joguenco.http.client;

import java.util.Map;

/**
 *
 * @author < Jorge Luis from http://joguenco.dev >
 */
public class ErrorResponse {

    private String message;
    private Map<String, String[]> errors;

    public Map<String, String[]> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String[]> value) {
        this.errors = value;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

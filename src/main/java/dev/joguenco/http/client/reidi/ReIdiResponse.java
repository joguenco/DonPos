package dev.joguenco.http.client.reidi;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Jorge Luis from https://joguenco.dev
 * @web jorgeluis@resolvedor.dev
 */
public class ReIdiResponse {

    @Getter
    @Setter
    private Boolean error;
    @Getter
    @Setter
    private String message;
    @Getter
    @Setter
    private ReIdiData data;

    public ReIdiResponse(ReIdiData data) {
        this.error = false;
        this.message = "";
        this.data = data;
    }

    public ReIdiResponse(String message) {
        this.error = true;
        this.message = message;
    }
}

package dev.joguenco.http.client.authorization;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Jorge Luis from https://joguenco.dev
 * @web jorgeluis@joguenco.dev
 */
public class Document {

    @Getter @Setter private String code;
    @Getter @Setter private String number;

    public Document(String code, String number) {
        this.code = code;
        this.number = number;
    }
}

package dev.joguenco.http.client.reidi;

import lombok.Getter;

/**
 *
 * @author Jorge Luis from https://joguenco.dev
 * @web jorgeluis@resolvedor.dev
 */
public class ReIdiData {

    @Getter
    private String name;

    public ReIdiData(String name) {
        this.name = name;
    }
}

package dev.joguenco.http.client.entity;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author < Jorge Luis from http://joguenco.dev >
 */
public class EntityResponse {

    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    private String identification;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String address;
    @Getter
    @Setter
    private String tradeName;
    @Getter
    @Setter
    private String source;
    @Getter
    @Setter
    private String createdAt;
}

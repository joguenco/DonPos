package dev.joguenco.http.client.entity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 *
 * @author < Jorge Luis from http://joguenco.dev >
 */
public interface EntityService {

    @GET("entity/{identification}")
    public Call<EntityResponse> getEntity(
            @Path("identification") String identification
    );
}

package dev.joguenco.http.client.ping;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 *
 * @author < Jorge Luis from http://joguenco.dev >
 */
public interface PingService {

    @GET("ping")
    public Call<PingResponse> ping();
}

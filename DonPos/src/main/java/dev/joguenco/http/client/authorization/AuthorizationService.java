package dev.joguenco.http.client.authorization;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author < Jorge Luis from http://joguenco.dev >
 */
public interface AuthorizationService {

    // La clave viaja en la cabecera, asi que no hace falta login previo.
    @POST("roqui/v2/invoice/authorize")
    public Call<StatusResponse> autorizeInvoiceV2(@Body Document document);

    @POST("roqui/v2/credit/note/authorize")
    public Call<StatusResponse> autorizeCreditNoteV2(@Body Document document);

    @POST("roqui/v2/debit/note/authorize")
    public Call<StatusResponse> autorizeDebitNoteV2(@Body Document document);

    @POST("roqui/v2/liquidation/authorize")
    public Call<StatusResponse> autorizeLiquidationV2(@Body Document document);

    @POST("roqui/v2/withhold/authorize")
    public Call<StatusResponse> autorizeWithholdV2(@Body Document document);

    @POST("roqui/v2/delivery/note/authorize")
    public Call<StatusResponse> autorizeDeliveryNoteV2(@Body Document document);
}

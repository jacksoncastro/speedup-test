package br.com.jackson.retrofit;
import br.com.jackson.retrofit.model.VectorResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PrometheusRest {

    @GET("api/v1/query")
    Call<VectorResponse> query(
        @Query("query") String query,
        @Query("time") String time,
        @Query("timeout") String timeout
    );
}
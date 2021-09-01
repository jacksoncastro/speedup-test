package br.com.jackson.retrofit;
import java.io.IOException;

import br.com.jackson.retrofit.model.VectorResponse;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class PrometheusApiClient {

    private Retrofit retrofit;
    private PrometheusRest service;

    public PrometheusApiClient(String baseUrl) {
        this.retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
        service = retrofit.create(PrometheusRest.class);
    }

    public VectorResponse query(String query) throws IOException {
        return service.query(query, null, null).execute().body();
    }

    public VectorResponse query(String query, String time) throws IOException {
        return service.query(query, time, null).execute().body();
    }

    public VectorResponse query(String query, String time, String timeout) throws IOException {
        return service.query(query, time, timeout).execute().body();
    }
}
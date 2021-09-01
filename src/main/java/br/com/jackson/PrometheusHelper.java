package br.com.jackson;

import java.io.IOException;

import br.com.jackson.retrofit.PrometheusApiClient;
import br.com.jackson.retrofit.model.VectorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrometheusHelper {

	private static PrometheusApiClient prometheusApiClient;

	private static PrometheusApiClient getPrometheusApiClient() {
		if (prometheusApiClient == null) {
			synchronized (PrometheusHelper.class) {
				if (prometheusApiClient == null) {
					String prometheusUrl = EnvironmentHelper.getPrometheusUrl();
					prometheusApiClient = new PrometheusApiClient(prometheusUrl);
				}
			}
		}
		return prometheusApiClient;
	}

	public static VectorResponse executeQuery(String query) {
		try {
			return getPrometheusApiClient().query(query);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

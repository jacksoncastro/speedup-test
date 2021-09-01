package br.com.jackson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnvironmentHelper {

	private static final String DEFAULT_SCENARIOS_FILE = "kustomize/scenarios.yml";
	private static final String DEFAULT_ORQUESTRATION_DIR = "orquestration";
	private static final String DEFAULT_K6_SCRIPT_FILE = "script.js";
	private static final String DEFAULT_PROMETHEUS_URL = "http://localhost:9090";

	private static String getEnv(String key, String defaultValue) {
		return System.getenv().getOrDefault(key, defaultValue);
	}

	public static String getOrquestrationDir() {
		return getEnv(Constants.ENV_ORQUESTRATION_DIR, DEFAULT_ORQUESTRATION_DIR);
	}

	public static String getScenariosFile() {
		return getEnv(Constants.ENV_SCENARIOS_FILE, DEFAULT_SCENARIOS_FILE);
	}

	public static String getK6ScriptFile() {
		return getEnv(Constants.ENV_K6_SCRIPT_FILE, DEFAULT_K6_SCRIPT_FILE);
	}

	public static String getPrometheusUrl() {
		return getEnv(Constants.ENV_PROMETHEUS_URL, DEFAULT_PROMETHEUS_URL);
	}
}

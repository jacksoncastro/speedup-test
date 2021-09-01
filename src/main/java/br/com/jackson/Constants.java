package br.com.jackson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

	public static final String ENV_ACCESS_KEY = "ACCESS_KEY";
	public static final String ENV_SECRET_KEY = "SECRET_KEY";
	public static final String ENV_SCENARIOS_FILE = "SCENARIOS_FILE";
	public static final String ENV_K6_SCRIPT_FILE = "K6_SCRIPT_FILE";
	public static final String ENV_PROMETHEUS_URL = "PROMETHEUS_URL";
	public static final String ENV_ORQUESTRATION_DIR = "ORQUESTRATION_DIR";

	@Deprecated
	public static final String ORQUESTRATION_DIR = "orquestration";
	@Deprecated
	public static final String WORKER_DIR_SCRIPT = ORQUESTRATION_DIR + "/scripts";
	public static final String SCRIPT_INIT = "init.sh";

}

package br.com.jackson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

	public static final String ENV_ACCESS_KEY = "ACCESS_KEY";
	public static final String ENV_SECRET_KEY = "SECRET_KEY";
	public static final String ENV_SCENARIOS_FILE = "SCENARIOS_FILE";

	public static final String WORKER_DIR = "/orquestration";
	public static final String WORKER_DIR_SCRIPT = WORKER_DIR + "/scripts";

}

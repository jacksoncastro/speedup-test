package br.com.jackson;

import java.io.File;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class K6Helper {

	private static final String FORMAT_RUN_K6 = "k6 run --vus=%d --iterations=%d --summary-export=%s %s";

	public static void runTest(File script, File summary, Integer vus, Integer iterations) {
		if (vus == null) {
			vus = 1;
		}

		if (iterations == null) {
			iterations = 1;
		}

		String command = String.format(FORMAT_RUN_K6, vus, iterations, summary.getAbsolutePath(), script.getAbsolutePath());
		log.info("Running k6 test");
		FunctionHelper.exec(command);
		log.info("Finished k6 test");
	}

	public static void deleteTest() {
		log.info("Deleting test");
		FunctionHelper.exec("pkill k6 || true");
		log.info("Deleted test");
	}
}

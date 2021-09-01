package br.com.jackson;

import java.io.File;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class K6Helper {

	private static final String FORMAT_RUN_K6 = "k6 run --summary-export=%s %s";

	public static void runTest(File script, File summary) {
		String command = String.format(FORMAT_RUN_K6, summary.getAbsolutePath(), script.getAbsolutePath());
		log.info("Running k6 test");
		FuntionHelper.exec(command);
		log.info("Finished k6 test");
	}

	public static void deleteTest() {
		log.info("Deleting test");
		FuntionHelper.exec("pkill k6 || true");
		log.info("Deleted test");
	}
}

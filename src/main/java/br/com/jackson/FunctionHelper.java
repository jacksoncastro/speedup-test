package br.com.jackson;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FunctionHelper {

	private static final String ENV_PATH_KEY = "PATH";
	private static final String ENV_PATH_VALUE = "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin";
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

	public static void sleep(int timeout) {
		try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			log.error("Error in sleep", e);
		}
	}

	public static List<String> exec(String command) {
		return exec(command, true);
	}

	public static List<String> exec(String command, boolean printOutput) {
		return exec(command, TEMP_DIR, printOutput);
	}

	public static List<String> exec(String command, String workdir) {
		return exec(command, workdir, true);
	}

	public static List<String> exec(String command, String workdir, boolean printOutput) {

		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;

		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.environment().put(ENV_PATH_KEY, ENV_PATH_VALUE);
			builder.command("bash", "-c", command);
			builder.directory(new File(workdir));
			builder.redirectErrorStream(true);
			Process process = builder.start();
			inputStreamReader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
			bufferedReader = new BufferedReader(inputStreamReader);

			List<String> output = bufferedReader
					.lines()
					.peek(line -> {
						if (printOutput) {
							log.info(line);
						}
					})
					.collect(Collectors.toList());

			int code = process.waitFor();
			if (code == 0) {
				return output;
			}
			String message = String.format("Error to execute command: %s. Code exit: %d", command, code);
			throw new RuntimeException(message);
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			closeQuietily(bufferedReader, inputStreamReader);
		}
	}

	private static void closeQuietily(Closeable ...closeables) {

		if (closeables == null) {
			return;
		}

		for (Closeable closeable : closeables) {
			try {
				closeable.close();
			} catch (Exception e) {
				// omitting error
			}
		}
	}
}

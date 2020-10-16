package br.com.jackson;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Helper {

	private static final Logger logger = LoggerFactory.getLogger(Helper.class);

	private Helper() {
	}

	public static void sleep(int timeout) {
		try {
			TimeUnit.SECONDS.sleep(timeout);
		} catch (InterruptedException e) {
			logger.error("Error in sleep", e);
		}
	}

	public static List<String> exec(String command, String workdir) {

		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;

		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("bash", "-c", command);
			builder.directory(new File(workdir));
			builder.redirectErrorStream(true);
			Process process = builder.start();
			inputStreamReader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
			bufferedReader = new BufferedReader(inputStreamReader);

			List<String> output = bufferedReader
					.lines()
					.peek(Helper.logger::info)
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

		for (int i=0; i < closeables.length; i++) {
			try {
				closeables[i].close();
			} catch (Exception e) {
				// omitting error
			}
		}
	}
}

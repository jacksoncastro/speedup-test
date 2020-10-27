package br.com.jackson;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import br.com.jackson.dto.Scenarie;
import br.com.jackson.dto.Scenaries;
import br.com.jackson.dto.Summary;
import br.com.jackson.dto.Test;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static final String JSON_PATH_HTTP_REQS_RATE = "metrics.http_reqs.rate";
	private static final String JSON_PATH_DURATION_MED = "metrics.iteration_duration.med";
	private static final String SUMMARY_KEY = "%s/%s/summary-%d.json";

	private static final String ROLE_ITERATION = "iteration";
	private static final String ROLE_RPS = "rps";

	private static final String SCENARIES_FILE_DEFAULT = "kustomize/scenaries.yml";

	public Main() throws Exception {
	}

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.init();
	}

	private void init() throws Exception {

		String path = System.getenv().getOrDefault(Constants.ENV_SCENARIES_FILE, SCENARIES_FILE_DEFAULT);

		File file = new File(path);

		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

		Scenaries scenaries = objectMapper.readValue(file, Scenaries.class);

		scenaries.getScenaries().forEach(this::runScenarie);

	}

	private void runScenarie(Scenarie scenarie) {
		try {
			for (int round = 1; round <= scenarie.getRounds(); round++) {
				logger.info("Begin test number {}", round);
				test(scenarie, round);
				logger.info("Ending test number {}", round);
			}
			HipersterHelper.clean();
		} catch (Exception e) {
			logger.error("Error in scenarie:", e);
		}
	}

	private void test(Scenarie scenarie, int round) throws Exception {
		if (scenarie.getTitle() == null || scenarie.getTitle().isEmpty()) {
			throw new Exception("Tittle cannot be empty");
		}
		scenarie.getTests().forEach(test -> {
			if (existsTest(scenarie.getTitle(), test.getName(), round)) {
				logger.info("Skipped scenarie: {}, test: {}, round: {}", scenarie.getTitle(), test.getName(), round);
				return;
			}
			HipersterHelper.clean();
			HipersterHelper.createApp();
			applyVirtualServices(test);
			stabilization();
			runK6(scenarie, test, round);
		});
	}

	private boolean existsTest(String title, String name, int round) {
		String key = String.format(SUMMARY_KEY, title, name, round);
		return S3Singleton.existsItem(key);
	}

	private void runK6(Scenarie scenarie, Test test, int round) {

		if (test.getLimite() != null) {
			Summary summary = getSummary(scenarie.getTitle(), test.getLimite().getFrom(), round);

			List<String> roles = Arrays.asList(test.getLimite().getRoles());

			Integer iteration = null;
			if (roles.contains(ROLE_ITERATION)) {
				iteration = (int) Math.ceil(summary.getIteration());
			}

			Integer rps = null;
			if (roles.contains(ROLE_RPS)) {
				rps = (int) Math.ceil(summary.getRps());
			}

			HipersterHelper.runK6(scenarie, test.getName(), round, iteration, rps);
		} else {
			HipersterHelper.runK6(scenarie, test.getName(), round);
		}
	}

	private void applyVirtualServices(Test test) {
		if (test.getVirtualServices() != null && !test.getVirtualServices().isEmpty()) {
			test.getVirtualServices().forEach(virtualService -> {
				if (virtualService.getAllExceptTarget()) {					
					HipersterHelper.virtualService(virtualService.getDelay(), virtualService.getTarget());
				} else {						
					HipersterHelper.virtualServiceOnly(virtualService.getDelay(), virtualService.getTarget());
				}
			});
		}
	}

	private Summary getSummary(String title, String name, int round) {

		String key = String.format(SUMMARY_KEY, title, name, round);
		String json = S3Singleton.getItem(key);

		DocumentContext documentContext = JsonPath.parse(json);

		float iteration = documentContext.read(JSON_PATH_DURATION_MED, Float.class);
		float rps = documentContext.read(JSON_PATH_HTTP_REQS_RATE, Float.class);

		return new Summary(iteration, rps);
	}

	private void stabilization() {
		logger.info("Waiting for stabilization");
		FuntionHelper.sleep(20);
		logger.info("Done!");
	}
}
package br.com.jackson;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import br.com.jackson.dto.Scenario;
import br.com.jackson.dto.Scenarios;
import br.com.jackson.dto.Summary;
import br.com.jackson.dto.Test;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

	private static final String JSON_PATH_HTTP_REQS_RATE = "metrics.http_reqs.rate";
	private static final String JSON_PATH_DURATION_MED = "metrics.iteration_duration.med";
	private static final String SUMMARY_KEY = "%s/%s/summary-%d.json";

	private static final String ROLE_ITERATION = "iteration";
	private static final String ROLE_RPS = "rps";

	private static final String SCENARIOS_FILE_DEFAULT = "kustomize/scenarios.yml";

	public Main() throws Exception {
	}

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.init();
	}

	private void init() throws Exception {

		String path = System.getenv().getOrDefault(Constants.ENV_SCENARIOS_FILE, SCENARIOS_FILE_DEFAULT);

		File file = new File(path);

		ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

		Scenarios scenarios = objectMapper.readValue(file, Scenarios.class);

		scenarios.getScenarios().forEach(this::runScenario);

	}

	private void runScenario(Scenario scenario) {
		try {
			if (scenario.isForceRemoveFolder()) {
				log.info("Removing folder {}", scenario.getTitle());
				S3Singleton.deleteFolder(scenario.getTitle());
				log.info("Removed folder {}", scenario.getTitle());
			}
			for (int round = 1; round <= scenario.getRounds(); round++) {
				log.info("Begin test number {}", round);
				test(scenario, round);
				log.info("Ending test number {}", round);
			}
			HipersterHelper.clean();
		} catch (Exception e) {
			log.error("Error in scenario:", e);
		}
	}

	private void test(Scenario scenario, int round) throws Exception {
		if (scenario.getTitle() == null || scenario.getTitle().isEmpty()) {
			throw new Exception("Tittle cannot be empty");
		}

		String[] testsName = this.getTestsName(scenario.getTests());

		if (existsRound(scenario.getTitle(), round, testsName)) {
			log.info("Skipped scenario: {} and round: {}", scenario.getTitle(), round);
			return;
		}

		S3Singleton.deleteRound(scenario.getTitle(), round);

		scenario.getTests().forEach(test -> {
			HipersterHelper.clean();
			HipersterHelper.createApp();
			applyVirtualServices(test);
			stabilization();
			runK6(scenario, test, round);
		});
	}

	private String[] getTestsName(List<Test> tests) {
		if (tests == null) {
			return new String[]{};
		}
		return tests
				.stream()
				.map(Test::getName)
				.toArray(String[]::new);
	}

	private boolean existsRound(String title, int round, String[] strings) {

		for (String test: strings) {
			String key = String.format(SUMMARY_KEY, title, test, round);
			boolean exists = S3Singleton.existsItem(key);

			if (!exists) {
				return false;
			}
		}
		return true;
	}

	private void runK6(Scenario scenario, Test test, int round) {

		if (test.getLimite() != null) {
			Summary summary = getSummary(scenario.getTitle(), test.getLimite().getFrom(), round);

			List<String> roles = Arrays.asList(test.getLimite().getRoles());

			Integer iteration = null;
			if (roles.contains(ROLE_ITERATION)) {
				iteration = (int) Math.ceil(summary.getIteration());
			}

			Integer rps = null;
			if (roles.contains(ROLE_RPS)) {
				rps = (int) Math.ceil(summary.getRps());
			}

			HipersterHelper.runK6(scenario, test.getName(), round, iteration, rps);
		} else {
			HipersterHelper.runK6(scenario, test.getName(), round);
		}
	}

	private void applyVirtualServices(Test test) {
		if (test.getVirtualServices() != null && !test.getVirtualServices().isEmpty()) {
			test.getVirtualServices().forEach(virtualService -> {
				if (virtualService.getTarget() != null) {
					if (virtualService.isAllButTarget()) {
						HipersterHelper.virtualService(virtualService.getDelay(), virtualService.getTarget());
					} else {
						HipersterHelper.virtualServiceOnly(virtualService.getDelay(), virtualService.getTarget());
					}
				} else {
					HipersterHelper.virtualService(virtualService.getDelay());
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
		log.info("Waiting for stabilization");
		FuntionHelper.sleep(20);
		log.info("Done!");
	}
}
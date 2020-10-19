package br.com.jackson;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import br.com.jackson.dto.Scenarie;
import br.com.jackson.dto.Scenaries;
import br.com.jackson.dto.Test;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static final String PATTERN_DATE = "yyyy-MM-dd-HH-mm-ss";

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
		String date = getDate();
		for (int round = 1; round <= scenarie.getRounds(); round++) {
			logger.info("Begin test number {}", round);
			test(scenarie, date, round);
			logger.info("Ending test number {}", round);
		}
		HipersterHelper.clean();
	}

	private String getDate() {
		SimpleDateFormat format = new SimpleDateFormat(PATTERN_DATE);
		format.setTimeZone(TimeZone.getTimeZone("America/Fortaleza"));
		return format.format(new Date());
	}

	private void test(Scenarie scenarie, String date, int round) {
		scenarie.getTests().forEach(test -> {
			HipersterHelper.clean();
			HipersterHelper.createApp();
			applyVirtualServices(test);
			stabilization();
			runK6(scenarie, test, date, round);
		});
	}

	private void runK6(Scenarie scenarie, Test test, String date, int round) {

		if (test.getLimite() != null) {
			SummaryDTO summary = getSummary(date, test.getLimite().getFrom(), round);

			List<String> roles = Arrays.asList(test.getLimite().getRoles());

			Integer iteration = null;
			if (roles.contains(ROLE_ITERATION)) {
				iteration = (int) Math.ceil(summary.getIteration());
			}

			Integer rps = null;
			if (roles.contains(ROLE_RPS)) {
				rps = (int) Math.ceil(summary.getRps());
			}

			HipersterHelper.runK6(scenarie, test.getName(), date, round, iteration, rps);
		} else {
			HipersterHelper.runK6(scenarie, test.getName(), date, round);
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

	private SummaryDTO getSummary(String date, String name, int round) {

		String key = String.format(SUMMARY_KEY, date, name, round);
		String json = S3Singleton.getItem(key);

		DocumentContext documentContext = JsonPath.parse(json);

		float iteration = documentContext.read(JSON_PATH_DURATION_MED, Float.class);
		float rps = documentContext.read(JSON_PATH_HTTP_REQS_RATE, Float.class);

		return new SummaryDTO(iteration, rps);
	}

	private void stabilization() {
		logger.info("Waiting for stabilization");
		FuntionHelper.sleep(20);
		logger.info("Done!");
	}
}
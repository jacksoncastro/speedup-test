package br.com.jackson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.util.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.opencsv.CSVWriter;

import br.com.jackson.app.App;
import br.com.jackson.app.BluePerf;
import br.com.jackson.dto.Image;
import br.com.jackson.dto.Scenario;
import br.com.jackson.dto.Scenarios;
import br.com.jackson.dto.Test;
import br.com.jackson.dto.VirtualService;
import br.com.jackson.dto.metrics.Metrics;
import br.com.jackson.retrofit.model.VectorResponse;
import br.com.jackson.retrofit.model.VectorResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

	private static final String SUMMARY_KEY = "%s/%s/summary-%d.json";

	private static final App app = new BluePerf();

	public Main() throws Exception {
	}

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.init();
	}

	private void init() throws Exception {

		String path = EnvironmentHelper.getScenariosFile();

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
			clean();
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
			clean();
			createApp();
			applyCustoms(test.getVirtualServices());
			applyVirtualServices(test);
			stabilization(20);
			Path pathTest = getPathTest(scenario.getTitle(), test.getName());
			runK6(pathTest, round);
			stabilization(15);
			executeMetrics(pathTest, round);
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

	private void runK6(Path pathTest, int round) {
		try {
			File script = new File(EnvironmentHelper.getK6ScriptFile());
			File summary = File.createTempFile("summary-", ".json");
			try {
				K6Helper.runTest(script, summary);
				Path path = pathTest.resolve("summary-" + round + ".json");
				S3Singleton.uploadFile(path, summary);
			} finally {
				summary.delete();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void applyCustoms(List<VirtualService> virtualServices) {
		virtualServices.forEach(virtualService -> {
			// custom for image
			if (virtualService.getImage() != null) {
				Image image = virtualService.getImage();
				if (StringUtils.hasValue(image.getName()) && StringUtils.hasValue(image.getContainer())) {
					KubernetesHelper.setImage(virtualService.getTarget(), image.getContainer(), image.getName());
				}
			}
		});
	}

	private void applyVirtualServices(Test test) {
		if (test.getVirtualServices() != null && !test.getVirtualServices().isEmpty()) {
			test.getVirtualServices().forEach(virtualService -> {
				if (virtualService.getDelay() == null) {
					return;
				}
				if (virtualService.getTarget() != null) {
					if (virtualService.isAllButTarget()) {
						IstioHelper.setFaultAllVirtualServicesButTarget(virtualService.getTarget(), IstioHelper.ONE_HUNDRED_PERCENT_FAULT, virtualService.getDelay());
					} else {
						IstioHelper.setFaultVirtualService(virtualService.getTarget(), IstioHelper.ONE_HUNDRED_PERCENT_FAULT, virtualService.getDelay());
					}
				} else {
					IstioHelper.setFaultAllVirtualServices(IstioHelper.ONE_HUNDRED_PERCENT_FAULT, virtualService.getDelay());
				}
			});
		}
	}

	private void stabilization(int time) {
		log.info("Waiting for stabilization");
		FuntionHelper.sleep(time);
		log.info("Done!");
	}

	private void clean() {
		K6Helper.deleteTest();
		IstioHelper.unsetFaultAllVirtualServices();
		app.deleteApp();
	}

	private void createApp() {
		app.createApp();
		File initScript = new File(app.getScriptDir(), Constants.SCRIPT_INIT);
		if (initScript.exists()) {
			FuntionHelper.exec(initScript.getAbsolutePath());
		}
	}

	private void executeMetrics(Path pathTest, int round) {
		try {
			File file = new File(app.getPrometheusDir(), "metrics.yaml");

			if (file.exists()) {
				ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
				List<Metrics> metrics = objectMapper.readValue(file, new TypeReference<List<Metrics>>(){});

				for (Metrics metric : metrics) {
					VectorResponse vectorResponse = PrometheusHelper.executeQuery(metric.getQuery());
					Path path = pathTest.resolve(metric.getName() + "-" + round + ".csv");
					uploadResponse(path, vectorResponse);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void uploadResponse(Path path, VectorResponse vectorResponse) throws IOException {

		Stream<String> stream = vectorResponse.getData()
			.getResult()
			.get(0)
			.getMetric()
			.keySet()
			.stream();

		Stream<String> headers = Stream.concat(stream, Stream.of("value"));

        List<String[]> list = new ArrayList<>();
        list.add(headers.toArray(String[]::new));

		for (VectorResult vectorResult : vectorResponse.getData().getResult()) {
			List<String> row = vectorResult.getMetric()
					.values()
					.stream()
					.collect(Collectors.toList());
			row.add(String.valueOf(vectorResult.getValue().get(1)));
			list.add(row.toArray(new String[] {}));
		}

		File file = File.createTempFile("metrics-", ".csv");
        try {
        	createCSV(list, file);
            S3Singleton.uploadFile(path, file);
        } finally {
        	file.delete();
		}
	}

	private void createCSV(List<String[]> data, File output) {
		try (CSVWriter writer = new CSVWriter(new FileWriter(output))) {
            writer.writeAll(data);
        } catch (Exception e) {
        	throw new RuntimeException(e);
		}
	}

	private Path getPathTest(String scenarie, String testName) {
		return Paths.get(scenarie, testName);
	}
}
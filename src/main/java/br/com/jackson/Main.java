package br.com.jackson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static final String TEST_NT = "NT";
	private static final String TEST_AT = "AT";
	private static final String TEST_DT = "DT";
	private static final String TEST_DTSi = "DTSi";

	private static final String PATTERN_DATE = "yyyy-MM-dd-HH-mm-ss";
	private static final String FORMAT_SECONDS = "%.2fs";

	private static final String JSON_PATH_HTTP_REQS_RATE = "metrics.http_reqs.rate";
	private static final String JSON_PATH_DURATION_MED = "metrics.iteration_duration.med";
	private static final String SUMMARY_KEY = "%s/%s/summary-%d.json";

	private static final String WORKER_DIR = "/Users/jacksoncastro/git/hipstershop-aws";
	private static final String SERVICE_TARGET = "productcatalogservice";
	private static final double EXTRA_LATENCY = 0.2d; // in seconds
	private static final double PERFORMANCE_GAIN = 0.1d; // in seconds
	private static final int ROUNDS = 5;

	private static final double SUM_EXTRA_GAIN = EXTRA_LATENCY + PERFORMANCE_GAIN;
	private static final double SUB_EXTRA_GAIN = EXTRA_LATENCY - PERFORMANCE_GAIN;

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.init();
	}

	private void init() throws Exception {
		String date = getDate();
		for (int i = 1; i <= ROUNDS; i++) {
			logger.info("Begin test number {}", i);
			test(date, i);
			logger.info("Ending test number {}", i);
		}
		clean();
	}

	private String getDate() {
		return new SimpleDateFormat(PATTERN_DATE).format(new Date());
	}

	private void test(String date, int round) {
		testNT(date, round);
		testAT(date, round);
		testDT(date, round);
		testDTSi(date, round);
	}

	private void testNT(String date, int round) {
		clean();
		createApp();
		virtualServiceOnly(EXTRA_LATENCY, SERVICE_TARGET);
		stabilization();
		runK6(TEST_NT, date, round);
	}

	private void testAT(String date, int round) {

		clean();

		String key = String.format(SUMMARY_KEY, date, TEST_NT, round);
		String item = S3Singleton.getItem(key);

		String readRPS = JsonPath.read(item, JSON_PATH_HTTP_REQS_RATE).toString();
		String readIteration = JsonPath.read(item, JSON_PATH_DURATION_MED).toString();

		int rps = (int) Math.ceil(Double.valueOf(readRPS));
		int iteration = (int) Math.ceil(Double.valueOf(readIteration));

		createApp();
		virtualServiceOnly(SUB_EXTRA_GAIN, SERVICE_TARGET);
		stabilization();
		runK6(TEST_AT, date, round, iteration, rps);
	}

	private void testDT(String date, int round) {
		clean();
		createApp();

		virtualService(PERFORMANCE_GAIN);
	    virtualServiceOnly(SUM_EXTRA_GAIN, SERVICE_TARGET);

	    stabilization();

	    runK6(TEST_DT, date, round);
	}

	private void testDTSi(String date, int round) {

	    clean();

	    String key = String.format(SUMMARY_KEY, date, TEST_DT, round);
		String item = S3Singleton.getItem(key);

		String readRPS = JsonPath.read(item, JSON_PATH_HTTP_REQS_RATE).toString();
		String readIteration = JsonPath.read(item, JSON_PATH_DURATION_MED).toString();

		int rps = (int) Math.ceil(Double.valueOf(readRPS));
		int iteration = (int) Math.ceil(Double.valueOf(readIteration));

	    createApp();

	    virtualService(PERFORMANCE_GAIN, SERVICE_TARGET);
	    virtualServiceOnly(EXTRA_LATENCY, SERVICE_TARGET);

	    runK6(TEST_DTSi, date, round, iteration, rps);
	}

	private void createApp() {
		logger.info("Create project");
	    Helper.exec("/usr/local/bin/kustomize build kustomize-app | /usr/local/bin/kubectl apply -f -", WORKER_DIR);

	    logger.info("Wait create...");
	    Helper.sleep(1);

	    Helper.exec("/usr/local/bin/kubectl wait po -l group=app --for=condition=ready --timeout=1800s", WORKER_DIR);
		logger.info("Created");
	}

	private void virtualService(double timeout) {
		virtualService(timeout, null);
	}

	private void virtualService(double timeout, String exclude) {

		String format = String.format(FORMAT_SECONDS, timeout);

		String command;
	    if (exclude != null && !exclude.isEmpty()) {
	    	command = String.format("./virtual-service.sh --delay=\"%s\" --exclude=\"%s\" | /usr/local/bin/kubectl apply -f -", format, exclude);
	    } else {
	    	command = String.format("./virtual-service.sh --delay=\"%s\" | /usr/local/bin/kubectl apply -f -", format);
	    }
	    Helper.exec(command, WORKER_DIR);
	}

	private void virtualServiceOnly(double timeout, String target) {
		String format = String.format(FORMAT_SECONDS, timeout);
		logger.info("Apply {} in service {}", format, target);
		String command = String.format("./virtual-service.sh --delay=\"%s\" --only=\"%s\" | /usr/local/bin/kubectl apply -f -", format, target);
		Helper.exec(command, WORKER_DIR);
	}

	private void clean() {
		deleteTest();
	    deleteVirtualServices();
	    deleteApp();
	}

	private void deleteTest() {
		logger.info("Deleting test");
	    Helper.exec("/usr/local/bin/kustomize build k6/ | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", WORKER_DIR);
		logger.info("Deleted test");
	}

	private void deleteVirtualServices() {
		logger.info("Deleting virtual services...");
	    Helper.exec("./virtual-service.sh --delay=0s | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", WORKER_DIR);
		logger.info("Deleted virtual services.");
	}

	private void deleteApp() {

		logger.info("Deleting app...");

		Helper.exec("/usr/local/bin/kustomize build kustomize-app | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", WORKER_DIR);

		List<String> output = Helper.exec("/usr/local/bin/kubectl get po -l group=app -o NAME", WORKER_DIR);

		if (output != null && !output.isEmpty()) {
			logger.info("Wait delete...");
			Helper.exec("/usr/local/bin/kubectl wait po -l group=app --for=delete --timeout=1800s", WORKER_DIR);
		}
	    logger.info("Deleted app.");
	}
	
	private void stabilization() {
		logger.info("Waiting for stabilization");
	    Helper.sleep(20);
	    logger.info("Done!");
	}

	private void runK6(String name, String date, int round) {
		runK6(name, date, round, null, null);
	}

	private void runK6(String name, String date, int round, Integer iteration, Integer rps) {

		setEnvironmentK6(date, name, round, iteration, rps);

		logger.info("Creating test k6 - {}", name);
		Helper.exec("/usr/local/bin/kustomize build k6/ | /usr/local/bin/kubectl apply -f -", WORKER_DIR);
		logger.info("Created test k6 - {}", name);
		
		logger.info("Wait test {}", name);
		Helper.exec("/usr/local/bin/kubectl -n k6 wait job/k6 --for=condition=complete --timeout=1800s", WORKER_DIR);
		logger.info("Fineshed test {}", name);
	}

	private void setEnvironmentK6(String date, String name, int round, Integer iteration, Integer rps) {

		String title = date + "/" + name;

		String format = "TITLE=\"%s\" ROUND=\"%d\" ITERATION=\"%s\" RPS=\"%s\" /usr/local/bin/envsubst < k6/k6-config.env.example > k6/k6-config.env";
		String command = String.format(format, title, round, iteration == null ? "" : iteration, rps == null ? "" : rps);

		Helper.exec(command, WORKER_DIR);
	}
}
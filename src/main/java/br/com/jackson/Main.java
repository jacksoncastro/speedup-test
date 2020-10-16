package br.com.jackson;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static final String TEST_NT = "NT";
	private static final String TEST_AT = "AT";
	private static final String TEST_DT = "DT";
	private static final String TEST_DTSi = "DTSi";

	private static final String PATTERN_DATE = "yyyy-MM-dd-HH-mm-ss";

	private static final String JSON_PATH_HTTP_REQS_RATE = "metrics.http_reqs.rate";
	private static final String JSON_PATH_DURATION_MED = "metrics.iteration_duration.med";
	private static final String SUMMARY_KEY = "%s/%s/summary-%d.json";

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
		HipersterHelper.clean();
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
		HipersterHelper.clean();
		HipersterHelper.createApp();
		HipersterHelper.virtualServiceOnly(EXTRA_LATENCY, SERVICE_TARGET);
		stabilization();
		HipersterHelper.runK6(TEST_NT, date, round);
	}

	private void testAT(String date, int round) {

		HipersterHelper.clean();

		SummaryDTO summary = getSummary(date, TEST_NT, round);

		int iteration = (int) Math.ceil(summary.getIteration());
		int rps = (int) Math.ceil(summary.getRps());

		HipersterHelper.createApp();
		HipersterHelper.virtualServiceOnly(SUB_EXTRA_GAIN, SERVICE_TARGET);
		stabilization();
		HipersterHelper.runK6(TEST_AT, date, round, iteration, rps);
	}

	private void testDT(String date, int round) {
		HipersterHelper.clean();
		HipersterHelper.createApp();

		HipersterHelper.virtualService(PERFORMANCE_GAIN);
		HipersterHelper.virtualServiceOnly(SUM_EXTRA_GAIN, SERVICE_TARGET);

	    stabilization();

	    HipersterHelper.runK6(TEST_DT, date, round);
	}

	private void testDTSi(String date, int round) {

		HipersterHelper.clean();

		SummaryDTO summary = getSummary(date, TEST_DT, round);

		int iteration = (int) Math.ceil(summary.getIteration());
		int rps = (int) Math.ceil(summary.getRps());

		HipersterHelper.createApp();

		HipersterHelper.virtualService(PERFORMANCE_GAIN, SERVICE_TARGET);
		HipersterHelper.virtualServiceOnly(EXTRA_LATENCY, SERVICE_TARGET);

		HipersterHelper.runK6(TEST_DTSi, date, round, iteration, rps);
	}

	private SummaryDTO getSummary(String date, String name, int round) {

		String key = String.format(SUMMARY_KEY, date, name, round);
		String json = S3Singleton.getItem(key);

		DocumentContext documentContext = JsonPath.parse(json);

		double iteration = documentContext.read(JSON_PATH_DURATION_MED, Double.class);
		double rps = documentContext.read(JSON_PATH_HTTP_REQS_RATE, Double.class);

		return new SummaryDTO(iteration, rps);
	}

	private void stabilization() {
		logger.info("Waiting for stabilization");
	    FuntionHelper.sleep(20);
	    logger.info("Done!");
	}
	
}
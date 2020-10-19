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
	private static final String TEST_ATS = "ATS";
	private static final String TEST_DT = "DT";
	private static final String TEST_DTS = "DTS";

	private static final String TEST_ATS_LIMITED = "ATS+";
	private static final String TEST_DTS_LIMITED = "DTS+";

	private static final String PATTERN_DATE = "yyyy-MM-dd-HH-mm-ss";

	private static final String JSON_PATH_HTTP_REQS_RATE = "metrics.http_reqs.rate";
	private static final String JSON_PATH_DURATION_MED = "metrics.iteration_duration.med";
	private static final String SUMMARY_KEY = "%s/%s/summary-%d.json";

	private static final String ENV_ROUNDS = "ROUNDS";
	private static final String ENV_SERVICE_TARGET = "SERVICE_TARGET";
	private static final String ENV_EXTRA_LATENCY = "EXTRA_LATENCY";
	private static final String ENV_PERFORMANCE_GAIN = "PERFORMANCE_GAIN";
	
	private static final String SERVICE_TARGET = FuntionHelper.getEnv(ENV_SERVICE_TARGET, null, String.class);
	private static final int ROUNDS = FuntionHelper.getEnv(ENV_ROUNDS, 1, Integer.class);
	// in seconds
	private static final float EXTRA_LATENCY = FuntionHelper.getEnv(ENV_EXTRA_LATENCY, 0f, Float.class);
	private static final float PERFORMANCE_GAIN = FuntionHelper.getEnv(ENV_PERFORMANCE_GAIN, 0f, Float.class);

	private static final float SUM_EXTRA_GAIN = EXTRA_LATENCY + PERFORMANCE_GAIN;
	private static final float SUB_EXTRA_GAIN = EXTRA_LATENCY - PERFORMANCE_GAIN;

	public Main() throws Exception {
		if (SERVICE_TARGET == null) {
			throw new Exception("Env SERVICE_TARGET cannot be empty");
		}
	}

	public static void main(String[] args) throws Exception {
		Main main = new Main();
		main.init();
	}

	private void init() throws Exception {
		tests();
	}

	private void tests() {
		String date = getDate();
		for (int round = 1; round <= ROUNDS; round++) {
			logger.info("Begin test number {}", round);
			test(date, round);
			logger.info("Ending test number {}", round);
		}
		HipersterHelper.clean();
	}

	private String getDate() {
		return new SimpleDateFormat(PATTERN_DATE).format(new Date());
	}

	private void test(String date, int round) {
		testNT(date, round);
		testATS(date, round);
		testATSLimited(date, round);
		testDT(date, round);
		testDTS(date, round);
		testDTSLimited(date, round);
	}

	private void testNT(String date, int round) {
		HipersterHelper.clean();
		HipersterHelper.createApp();
		HipersterHelper.virtualServiceOnly(EXTRA_LATENCY, SERVICE_TARGET);
		stabilization();
		HipersterHelper.runK6(TEST_NT, date, round);
	}

	private void testATS(String date, int round) {

		HipersterHelper.clean();

		HipersterHelper.createApp();
		HipersterHelper.virtualServiceOnly(SUB_EXTRA_GAIN, SERVICE_TARGET);
		stabilization();
		HipersterHelper.runK6(TEST_ATS, date, round);
	}

	private void testATSLimited(String date, int round) {

		HipersterHelper.clean();

		SummaryDTO summary = getSummary(date, TEST_NT, round);

		int iteration = (int) Math.ceil(summary.getIteration());
		int rps = (int) Math.ceil(summary.getRps());

		HipersterHelper.createApp();
		HipersterHelper.virtualServiceOnly(SUB_EXTRA_GAIN, SERVICE_TARGET);
		stabilization();
		HipersterHelper.runK6(TEST_ATS_LIMITED, date, round, iteration, rps);
	}

	private void testDT(String date, int round) {
		HipersterHelper.clean();
		HipersterHelper.createApp();

		HipersterHelper.virtualService(PERFORMANCE_GAIN);
		HipersterHelper.virtualServiceOnly(SUM_EXTRA_GAIN, SERVICE_TARGET);

		stabilization();

		HipersterHelper.runK6(TEST_DT, date, round);
	}

	private void testDTS(String date, int round) {

		HipersterHelper.clean();

		HipersterHelper.createApp();

		HipersterHelper.virtualService(PERFORMANCE_GAIN, SERVICE_TARGET);
		HipersterHelper.virtualServiceOnly(EXTRA_LATENCY, SERVICE_TARGET);

		HipersterHelper.runK6(TEST_DTS, date, round);
	}

	private void testDTSLimited(String date, int round) {

		HipersterHelper.clean();

		SummaryDTO summary = getSummary(date, TEST_DT, round);

		int iteration = (int) Math.ceil(summary.getIteration());
		int rps = (int) Math.ceil(summary.getRps());

		HipersterHelper.createApp();

		HipersterHelper.virtualService(PERFORMANCE_GAIN, SERVICE_TARGET);
		HipersterHelper.virtualServiceOnly(EXTRA_LATENCY, SERVICE_TARGET);

		HipersterHelper.runK6(TEST_DTS_LIMITED, date, round, iteration, rps);
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
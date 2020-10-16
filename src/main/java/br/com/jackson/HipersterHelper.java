package br.com.jackson;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HipersterHelper {

	private static final Logger logger = LoggerFactory.getLogger(HipersterHelper.class);

	private static final String WORKER_DIR = "/Users/jacksoncastro/git/hipstershop-aws";
	private static final String FORMAT_SECONDS = "%.2fs";

	private HipersterHelper() {
	}

	public static void createApp() {
		logger.info("Create project");
	    FuntionHelper.exec("/usr/local/bin/kustomize build kustomize-app | /usr/local/bin/kubectl apply -f -", WORKER_DIR);

	    logger.info("Wait create...");
	    FuntionHelper.sleep(1);

	    FuntionHelper.exec("/usr/local/bin/kubectl wait po -l group=app --for=condition=ready --timeout=1800s", WORKER_DIR);
		logger.info("Created");
	}

	public static void virtualService(double timeout) {
		virtualService(timeout, null);
	}

	public static void virtualService(double timeout, String exclude) {

		String format = String.format(FORMAT_SECONDS, timeout);

		String command;
	    if (exclude != null && !exclude.isEmpty()) {
	    	command = String.format("./virtual-service.sh --delay=\"%s\" --exclude=\"%s\" | /usr/local/bin/kubectl apply -f -", format, exclude);
	    } else {
	    	command = String.format("./virtual-service.sh --delay=\"%s\" | /usr/local/bin/kubectl apply -f -", format);
	    }
	    FuntionHelper.exec(command, WORKER_DIR);
	}

	public static void virtualServiceOnly(double timeout, String target) {
		String format = String.format(FORMAT_SECONDS, timeout);
		logger.info("Apply {} in service {}", format, target);
		String command = String.format("./virtual-service.sh --delay=\"%s\" --only=\"%s\" | /usr/local/bin/kubectl apply -f -", format, target);
		FuntionHelper.exec(command, WORKER_DIR);
	}

	public static void clean() {
		deleteTest();
	    deleteVirtualServices();
	    deleteApp();
	}

	private static void deleteTest() {
		logger.info("Deleting test");
	    FuntionHelper.exec("/usr/local/bin/kustomize build k6/ | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", WORKER_DIR);
		logger.info("Deleted test");
	}

	private static void deleteVirtualServices() {
		logger.info("Deleting virtual services...");
	    FuntionHelper.exec("./virtual-service.sh --delay=0s | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", WORKER_DIR);
		logger.info("Deleted virtual services.");
	}

	private static void deleteApp() {

		logger.info("Deleting app...");

		FuntionHelper.exec("/usr/local/bin/kustomize build kustomize-app | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", WORKER_DIR);

		List<String> output = FuntionHelper.exec("/usr/local/bin/kubectl get po -l group=app -o NAME", WORKER_DIR);

		if (output != null && !output.isEmpty()) {
			logger.info("Wait delete...");
			FuntionHelper.exec("/usr/local/bin/kubectl wait po -l group=app --for=delete --timeout=1800s", WORKER_DIR);
		}
	    logger.info("Deleted app.");
	}

	public static void runK6(String name, String date, int round) {
		runK6(name, date, round, null, null);
	}

	public static void runK6(String name, String date, int round, Integer iteration, Integer rps) {

		setEnvironmentK6(date, name, round, iteration, rps);

		logger.info("Creating test k6 - {}", name);
		FuntionHelper.exec("/usr/local/bin/kustomize build k6/ | /usr/local/bin/kubectl apply -f -", WORKER_DIR);
		logger.info("Created test k6 - {}", name);
		
		logger.info("Wait test {}", name);
		FuntionHelper.exec("/usr/local/bin/kubectl -n k6 wait job/k6 --for=condition=complete --timeout=1800s", WORKER_DIR);
		logger.info("Fineshed test {}", name);
	}

	private static void setEnvironmentK6(String date, String name, int round, Integer iteration, Integer rps) {

		String title = date + "/" + name;

		String format = "TITLE=\"%s\" ROUND=\"%d\" ITERATION=\"%s\" RPS=\"%s\" /usr/local/bin/envsubst < k6/k6-config.env.example > k6/k6-config.env";
		String command = String.format(format, title, round, iteration == null ? "" : iteration, rps == null ? "" : rps);

		FuntionHelper.exec(command, WORKER_DIR);
	}

}

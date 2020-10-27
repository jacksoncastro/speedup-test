package br.com.jackson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.jackson.dto.Scenarie;

public final class HipersterHelper {

	private static final Logger logger = LoggerFactory.getLogger(HipersterHelper.class);

	private static final String FORMAT_SECONDS = "%ss";

	private HipersterHelper() {
	}

	public static void createApp() {
		logger.info("Create project");
		FuntionHelper.exec("/usr/local/bin/kustomize build hipstershop | /usr/local/bin/kubectl apply -f -", Constants.WORKER_DIR);

		logger.info("Wait create...");
		FuntionHelper.sleep(1);

		FuntionHelper.exec("/usr/local/bin/kubectl wait po -l group=app --for=condition=ready --timeout=1800s", Constants.WORKER_DIR);
		logger.info("Created");
	}

	public static void virtualService(float timeout) {
		virtualService(timeout, null);
	}

	public static void virtualService(float timeout, String exclude) {

		String format = String.format(FORMAT_SECONDS, timeout);

		String command;
		if (exclude != null && !exclude.isEmpty()) {
			command = String.format("./virtual-service.sh --delay=\"%s\" --exclude=\"%s\" | /usr/local/bin/kubectl apply -f -", format, exclude);
			logger.info("Apply {} in service all services, except {}", format, exclude);
		} else {
			command = String.format("./virtual-service.sh --delay=\"%s\" | /usr/local/bin/kubectl apply -f -", format);
			logger.info("Apply {} in service all services", format);
		}
		FuntionHelper.exec(command, Constants.WORKER_DIR_SCRIPT);
	}

	public static void virtualServiceOnly(float timeout, String target) {
		String format = String.format(FORMAT_SECONDS, timeout);
		logger.info("Apply {} in service {}", format, target);
		String command = String.format("./virtual-service.sh --delay=\"%s\" --only=\"%s\" | /usr/local/bin/kubectl apply -f -", format, target);
		FuntionHelper.exec(command, Constants.WORKER_DIR_SCRIPT);
	}

	public static void clean() {
		deleteTest();
		deleteVirtualServices();
		deleteApp();
	}

	public static void runK6(Scenarie scenarie, String name, int round) {
		runK6(scenarie, name, round, null, null);
	}

	public static void runK6(Scenarie scenarie, String name, int round, Integer minDurationIteration, Integer rps) {

		String folder = scenarie.getTitle() + "/" + name;

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("TITLE", folder);
		parameters.put("ROUND", round);
		parameters.put("MIN_DURATION_ITERATION", minDurationIteration);
		parameters.put("VUS", scenarie.getUsers());
		parameters.put("ITERATIONS", scenarie.getIterations());
		parameters.put("RPS", rps);

		setEnvironmentK6(parameters);

		logger.info("Creating test k6 - {}", name);
		FuntionHelper.exec("/usr/local/bin/kustomize build k6/ | /usr/local/bin/kubectl apply -f -", Constants.WORKER_DIR);
		logger.info("Created test k6 - {}", name);

		logger.info("Wait test {}", name);
		FuntionHelper.exec("/usr/local/bin/kubectl -n k6 wait job/k6 --for=condition=complete --timeout=1800s", Constants.WORKER_DIR);
		logger.info("Fineshed test {}", name);
	}

	private static void deleteTest() {
		logger.info("Deleting test");
		FuntionHelper.exec("/usr/local/bin/kustomize build k6/ | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", Constants.WORKER_DIR);
		logger.info("Deleted test");
	}
	
	private static void deleteVirtualServices() {
		logger.info("Deleting virtual services...");
		FuntionHelper.exec("./virtual-service.sh --delay=0s | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", Constants.WORKER_DIR_SCRIPT);
		logger.info("Deleted virtual services.");
	}

	private static void deleteApp() {

		logger.info("Deleting app...");

		FuntionHelper.exec("/usr/local/bin/kustomize build hipstershop | /usr/local/bin/kubectl delete --ignore-not-found=true -f -", Constants.WORKER_DIR);

		List<String> output = FuntionHelper.exec("/usr/local/bin/kubectl get po -l group=app -o NAME", Constants.WORKER_DIR);
		
		if (output != null && !output.isEmpty()) {
			logger.info("Wait delete...");
			FuntionHelper.exec("/usr/local/bin/kubectl wait po -l group=app --for=delete --timeout=1800s", Constants.WORKER_DIR);
		}
		logger.info("Deleted app.");
	}

	private static void setEnvironmentK6(Map<String, Object> map) {

		String parameters = map.entrySet()
				.stream()
				.map(entry -> {
					String value = entry.getValue() == null ? "" : entry.getValue().toString();
					return entry.getKey() + "=\"" + value + "\"";
				})
				.collect(Collectors.joining(" "));

		String format = "%s /usr/bin/envsubst < k6/k6-config.env.example > k6/k6-config.env";
		String command = String.format(format, parameters);

		FuntionHelper.exec(command, Constants.WORKER_DIR);
	}
}

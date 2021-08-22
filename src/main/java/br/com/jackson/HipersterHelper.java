package br.com.jackson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.jackson.dto.Scenario;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HipersterHelper {

	private static final String FORMAT_SECONDS = "%ss";

	public static void createApp() {
		log.info("Create project");
		FuntionHelper.exec("kustomize build hipstershop | kubectl apply -f -", Constants.WORKER_DIR);

		waitCreate();
	}

	public static void virtualService(float timeout) {
		virtualService(timeout, null);
	}

	public static void virtualService(float timeout, String exclude) {

		if ((timeout * 1000) < 1) {
			log.info("Timeout duration must be greater than 1ms");
			return;
		}

		String format = String.format(FORMAT_SECONDS, timeout);

		String command;
		if (exclude != null && !exclude.isEmpty()) {
			command = String.format("./virtual-service.sh --delay=\"%s\" --exclude=\"%s\" | kubectl apply -f -", format, exclude);
			log.info("Apply {} in service all services, except {}", format, exclude);
		} else {
			command = String.format("./virtual-service.sh --delay=\"%s\" | kubectl apply -f -", format);
			log.info("Apply {} in service all services", format);
		}
		FuntionHelper.exec(command, Constants.WORKER_DIR_SCRIPT);
	}

	public static void virtualServiceOnly(float timeout, String target) {

		if ((timeout * 1000) < 1) {
			log.info("Timeout duration must be greater than 1ms");
			return;
		}

		String format = String.format(FORMAT_SECONDS, timeout);
		log.info("Apply {} in service {}", format, target);
		String command = String.format("./virtual-service.sh --delay=\"%s\" --only=\"%s\" | kubectl apply -f -", format, target);
		FuntionHelper.exec(command, Constants.WORKER_DIR_SCRIPT);
	}

	public static void clean() {
		deleteTest();
		deleteVirtualServices();
		deleteApp();
	}

	public static void runK6(Scenario scenario, String name, int round) {
		runK6(scenario, name, round, null, null);
	}

	public static void runK6(Scenario scenario, String name, int round, Integer minDurationIteration, Integer rps) {

		String folder = scenario.getTitle() + "/" + name;

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("TITLE", folder);
		parameters.put("ROUND", round);
		parameters.put("MIN_DURATION_ITERATION", minDurationIteration);
		parameters.put("VUS", scenario.getUsers());
		parameters.put("ITERATIONS", scenario.getIterations());
		parameters.put("RPS", rps);

		setEnvironmentK6(parameters);

		log.info("Creating test k6 - {}", name);
		FuntionHelper.exec("kustomize build k6/ | kubectl apply -f -", Constants.WORKER_DIR);
		log.info("Created test k6 - {}", name);

		log.info("Wait test {}", name);
		FuntionHelper.exec("kubectl -n k6 wait job/k6 --for=condition=complete --timeout=1800s", Constants.WORKER_DIR);
		log.info("Fineshed test {}", name);

		log.info("Print result of test {} >>>>>>>>>>>> ", name);
		FuntionHelper.exec("kubectl -n k6 logs job/k6", Constants.WORKER_DIR);
		log.info("Print result of test {} <<<<<<<<<<<< ", name);
	}

	public static void setImage(String deployment, String container, String name) {
		log.info("Set custom image {} for deploy {}", name, deployment);
		String command = String.format("kubectl -n default set image deployment/%s %s=%s", deployment, container, name);
		FuntionHelper.exec(command, Constants.WORKER_DIR);
		log.info("Setted custom image {}", name);

		waitCreate();
	}

	private static void waitCreate() {
		log.info("Wait create...");
		FuntionHelper.sleep(1);

		FuntionHelper.exec("kubectl wait po -l group=app --for=condition=ready --timeout=1800s", Constants.WORKER_DIR);
		log.info("Created");
	}

	private static void deleteTest() {
		log.info("Deleting test");
		FuntionHelper.exec("kustomize build k6/ | kubectl delete --ignore-not-found=true -f -", Constants.WORKER_DIR);
		log.info("Deleted test");
	}

	private static void deleteVirtualServices() {
		log.info("Deleting virtual services...");
		FuntionHelper.exec("./virtual-service.sh --delay=0s | kubectl delete --ignore-not-found=true -f -", Constants.WORKER_DIR_SCRIPT);
		log.info("Deleted virtual services.");
	}

	private static void deleteApp() {

		log.info("Deleting app...");

		FuntionHelper.exec("kustomize build hipstershop | kubectl delete --ignore-not-found=true -f -", Constants.WORKER_DIR);

		List<String> output = FuntionHelper.exec("kubectl get po -l group=app -o NAME", Constants.WORKER_DIR);

		if (output != null && !output.isEmpty()) {
			log.info("Wait delete...");
			FuntionHelper.exec("kubectl wait po -l group=app --for=delete --timeout=1800s", Constants.WORKER_DIR);
		}
		log.info("Deleted app.");
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

package br.com.jackson;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KubernetesHelper {

	private static final String FORMAT_SET_IMAGE = "kubectl -n default set image deployment/%s %s=%s";
	private static final String FORMAT_WAIT_CREATE = "kubectl wait po -l %s=%s --for=condition=ready --timeout=1800s";

	public static void setImage(String deployment, String container, String name) {
		log.info("Set custom image {} for deploy {}", name, deployment);

		String command = String.format(FORMAT_SET_IMAGE, deployment, container, name);
		FuntionHelper.exec(command);
		log.info("Setted custom image {}", name);

		waitCreateByLabel("group", "app");
	}

	public static void waitCreateByLabel(String name, String value) {
		log.info("Wait create...");
		FuntionHelper.sleep(1);

		FuntionHelper.exec(String.format(FORMAT_WAIT_CREATE, name, value));
		log.info("Created");
	}
}

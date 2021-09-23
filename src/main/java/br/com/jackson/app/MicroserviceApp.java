package br.com.jackson.app;

import java.util.List;

import br.com.jackson.Environment;
import br.com.jackson.FunctionHelper;
import br.com.jackson.KubernetesHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MicroserviceApp implements App {

	abstract String getProjectDir();

	@Override
	public void createApp() {
		log.info("Create project");
		FunctionHelper.exec("kubectl apply -k app/", getWorkerDir());
		KubernetesHelper.waitCreateByLabel("group", "app");
	}

	@Override
	public void deleteApp() {
		log.info("Deleting app...");

		FunctionHelper.exec("kubectl delete --ignore-not-found=true -k app/", getWorkerDir());

		List<String> output = FunctionHelper.exec("kubectl get po -l group=app -o NAME", getWorkerDir());

		if (output != null && !output.isEmpty()) {
			log.info("Wait delete...");
			FunctionHelper.exec("kubectl wait po -l group=app --for=delete --timeout=1800s", getWorkerDir());
		}
		log.info("Deleted app.");
	}

	@Override
	public String getWorkerDir() {
		return Environment.getOrquestrationDir() + getProjectDir();
	}

	@Override
	public String getScriptDir() {
		return getWorkerDir() + "/scripts";
	}

	@Override
	public String getPrometheusDir() {
		return getWorkerDir() + "/prometheus";
	}
}

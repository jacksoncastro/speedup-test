package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

public class Scenarios implements Serializable {

	private static final long serialVersionUID = -1202818232172698061L;

	private List<Scenario> scenarios;

	public Scenarios() {
	}

	public Scenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}
}

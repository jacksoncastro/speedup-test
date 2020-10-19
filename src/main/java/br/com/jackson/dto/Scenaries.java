package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

public class Scenaries implements Serializable {

	private static final long serialVersionUID = -1202818232172698061L;

	private List<Scenarie> scenaries;

	public Scenaries() {
	}

	public Scenaries(List<Scenarie> scenaries) {
		this.scenaries = scenaries;
	}

	public List<Scenarie> getScenaries() {
		return scenaries;
	}

	public void setScenaries(List<Scenarie> scenaries) {
		this.scenaries = scenaries;
	}
}

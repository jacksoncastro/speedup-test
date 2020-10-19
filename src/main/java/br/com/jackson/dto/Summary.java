package br.com.jackson.dto;

public class Summary {

	private final float iteration;

	private final float rps;

	public Summary(float iteration, float rps) {
		this.iteration = iteration;
		this.rps = rps;
	}

	public float getIteration() {
		return iteration;
	}

	public float getRps() {
		return rps;
	}
}

package br.com.jackson;

public class SummaryDTO {

	private final float iteration;

	private final float rps;

	public SummaryDTO(float iteration, float rps) {
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

package br.com.jackson;

public class SummaryDTO {

	private final double iteration;

	private final double rps;

	public SummaryDTO(double iteration, double rps) {
		this.iteration = iteration;
		this.rps = rps;
	}

	public double getIteration() {
		return iteration;
	}

	public double getRps() {
		return rps;
	}
}

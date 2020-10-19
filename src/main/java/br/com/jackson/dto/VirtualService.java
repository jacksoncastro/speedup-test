package br.com.jackson.dto;

import java.io.Serializable;

public class VirtualService implements Serializable {

	private static final long serialVersionUID = 3763980030565199850L;

	private String target;

	private float delay;

	private boolean allExceptTarget;

	public VirtualService() {
	}

	public VirtualService(String target, float delay, boolean allExceptTarget) {
		this.target = target;
		this.delay = delay;
		this.allExceptTarget = allExceptTarget;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public float getDelay() {
		return delay;
	}

	public void setDelay(float delay) {
		this.delay = delay;
	}

	public boolean getAllExceptTarget() {
		return allExceptTarget;
	}

	public void setAllExceptTarget(boolean allExceptTarget) {
		this.allExceptTarget = allExceptTarget;
	}
}

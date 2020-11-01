package br.com.jackson.dto;

import java.io.Serializable;

public class VirtualService implements Serializable {

	private static final long serialVersionUID = 3763980030565199850L;

	private float delay;

	private String target;

	private boolean allButTarget;

	public VirtualService() {
	}

	public VirtualService(float delay, String target, boolean allButTarget) {
		this.delay = delay;
		this.target = target;
		this.allButTarget = allButTarget;
	}

	public float getDelay() {
		return delay;
	}

	public void setDelay(float delay) {
		this.delay = delay;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public boolean isAllButTarget() {
		return allButTarget;
	}

	public void setAllButTarget(boolean allButTarget) {
		this.allButTarget = allButTarget;
	}
}

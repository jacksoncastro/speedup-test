package br.com.jackson.dto;

import java.io.Serializable;

public class Limite implements Serializable {

	private static final long serialVersionUID = 9088566429007733911L;

	private String from;

	private String[] roles;

	public Limite() {
	}

	public Limite(String from, String[] roles) {
		this.from = from;
		this.roles = roles;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String[] getRoles() {
		return roles;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}
}

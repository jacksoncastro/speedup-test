package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

public class Scenarie implements Serializable {

	private static final long serialVersionUID = 636940468340453812L;

	private int users;

	private int iterations;

	private int rounds;

	private List<Test> tests;

	public Scenarie() {
	}

	public Scenarie(int users, int iterations, int rounds, List<Test> tests) {
		this.users = users;
		this.iterations = iterations;
		this.rounds = rounds;
		this.tests = tests;
	}

	public int getUsers() {
		return users;
	}

	public void setUsers(int users) {
		this.users = users;
	}

	public int getIterations() {
		return iterations;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	public int getRounds() {
		return rounds;
	}

	public void setRounds(int rounds) {
		this.rounds = rounds;
	}

	public List<Test> getTests() {
		return tests;
	}

	public void setTests(List<Test> tests) {
		this.tests = tests;
	}
}

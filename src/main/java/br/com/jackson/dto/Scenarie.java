package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

public class Scenarie implements Serializable {

	private static final long serialVersionUID = 636940468340453812L;

	private String title;

	private int users;

	private int iterations;

	private int rounds;

	private boolean forceRemoveFolder;

	private List<Test> tests;

	public Scenarie() {
	}

	public Scenarie(String title, int users, int iterations, int rounds, boolean forceRemoveFolder, List<Test> tests) {
		this.title = title;
		this.users = users;
		this.iterations = iterations;
		this.rounds = rounds;
		this.forceRemoveFolder = forceRemoveFolder;
		this.tests = tests;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public boolean isForceRemoveFolder() {
		return forceRemoveFolder;
	}

	public void setForceRemoveFolder(boolean forceRemoveFolder) {
		this.forceRemoveFolder = forceRemoveFolder;
	}

	public List<Test> getTests() {
		return tests;
	}

	public void setTests(List<Test> tests) {
		this.tests = tests;
	}
}

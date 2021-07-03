package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scenario implements Serializable {

	private static final long serialVersionUID = 636940468340453812L;

	private String title;

	private int users;

	private int iterations;

	private int rounds;

	private boolean forceRemoveFolder;

	private List<Test> tests;

}

package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scenarios implements Serializable {

	private static final long serialVersionUID = -1202818232172698061L;

	private List<Scenario> scenarios;

}

package br.com.jackson.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deployments implements Serializable {

	private static final long serialVersionUID = -9178284181241110078L;

	private String target;

	private Image image;

}

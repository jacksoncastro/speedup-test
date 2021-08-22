package br.com.jackson.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VirtualService implements Serializable {

	private static final long serialVersionUID = 3763980030565199850L;

	private float delay;

	private String target;

	private boolean allButTarget;

	private Image image;

}

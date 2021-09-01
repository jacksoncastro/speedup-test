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

	// Format: 1h/1m/1s/1ms. MUST be >=1ms.
	private String delay;

	private String target;

	private boolean allButTarget;

	private Image image;

}

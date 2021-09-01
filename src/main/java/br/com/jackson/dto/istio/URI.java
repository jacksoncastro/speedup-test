package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class URI implements Serializable {

	private static final long serialVersionUID = -4683780216276035001L;

	private String prefix;

}

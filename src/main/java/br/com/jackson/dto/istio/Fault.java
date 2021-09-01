package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fault implements Serializable {

	private static final long serialVersionUID = -5024296489219565469L;

	private Delay delay;

}

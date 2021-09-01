package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route implements Serializable {

	private static final long serialVersionUID = 6842361602677248723L;

	private Destination destination;

}

package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Destination implements Serializable {

	private static final long serialVersionUID = -674445754792711251L;

	private String host;

	private Port port;

}

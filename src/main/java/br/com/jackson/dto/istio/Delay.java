package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delay implements Serializable {

	private static final long serialVersionUID = -578687645405144578L;

	private Integer percent;

	private String fixedDelay;

}

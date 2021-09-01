package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Port implements Serializable {

	private static final long serialVersionUID = 8809485563825843273L;

	private Integer number;

}

package br.com.jackson.dto.istio;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Spec implements Serializable {

	private static final long serialVersionUID = -5294229057386590550L;

	private List<String> hosts;

	private List<String> gateways;

	private List<Http> http;

}

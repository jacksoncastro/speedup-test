package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

// TODO: rename this class to VirtualService

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VirtualServiceKind implements Serializable {

	private static final long serialVersionUID = 5854003209057817196L;

	@Default
	private String apiVersion = "networking.istio.io/v1alpha3";

	@Default
	private String kind = "VirtualService";

	private Metadata metadata;

	private Spec spec;

}

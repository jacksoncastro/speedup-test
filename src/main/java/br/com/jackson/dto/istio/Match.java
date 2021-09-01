package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match implements Serializable {

	private static final long serialVersionUID = -3245287387170178262L;

	private URI uri;

}

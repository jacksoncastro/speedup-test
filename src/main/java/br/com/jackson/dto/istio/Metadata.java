package br.com.jackson.dto.istio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metadata implements Serializable {

	private static final long serialVersionUID = -642646452421556067L;

	private String name;

}

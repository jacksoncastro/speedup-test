package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test implements Serializable {

	private static final long serialVersionUID = -8594997683649296601L;

	private String name;

	private Limite limite;

	private List<VirtualService> virtualServices;

}

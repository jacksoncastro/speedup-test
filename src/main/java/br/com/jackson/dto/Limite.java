package br.com.jackson.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Limite implements Serializable {

	private static final long serialVersionUID = 9088566429007733911L;

	private String from;

	private String[] roles;

}

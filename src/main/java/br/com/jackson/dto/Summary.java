package br.com.jackson.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Summary implements Serializable {

	private static final long serialVersionUID = -9108333936558712407L;

	private float iteration;

	private float rps;

}

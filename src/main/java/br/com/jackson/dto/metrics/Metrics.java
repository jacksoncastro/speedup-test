package br.com.jackson.dto.metrics;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metrics implements Serializable {

	private static final long serialVersionUID = 6939283393383233582L;

	private String name;

	private String query;

}

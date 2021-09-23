package br.com.jackson.retrofit.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorResult {

	private Map<String, String> metric;

	private List<String> value;
}
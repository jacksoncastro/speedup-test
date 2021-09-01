package br.com.jackson.retrofit.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorData {

	private String resultType;

    private List<VectorResult> result;
}


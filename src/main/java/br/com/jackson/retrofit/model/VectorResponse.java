package br.com.jackson.retrofit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorResponse {

	private String status;

    private VectorData data;
}


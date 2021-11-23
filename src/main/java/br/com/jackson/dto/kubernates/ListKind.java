package br.com.jackson.dto.kubernates;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListKind<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = -5177662412401905327L;

	private String apiVersion;

	private List<T> items;
}

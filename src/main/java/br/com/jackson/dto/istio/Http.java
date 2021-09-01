package br.com.jackson.dto.istio;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Http implements Serializable {

	private static final long serialVersionUID = 5584507461720977720L;

	private List<Match> match;

	private List<Route> route;

	private Fault fault;
}

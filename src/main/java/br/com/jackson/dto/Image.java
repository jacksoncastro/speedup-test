package br.com.jackson.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image implements Serializable {

	private static final long serialVersionUID = -3083957888405896369L;

	private String name;

	private String container;

}

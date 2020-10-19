package br.com.jackson.dto;

import java.io.Serializable;
import java.util.List;

public class Test implements Serializable {

	private static final long serialVersionUID = -8594997683649296601L;

	private String name;

	private Limite limite;

	private List<VirtualService> virtualServices;

	public Test() {
	}

	public Test(String name, Limite limite, List<VirtualService> virtualServices) {
		this.name = name;
		this.limite = limite;
		this.virtualServices = virtualServices;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Limite getLimite() {
		return limite;
	}

	public void setLimite(Limite limite) {
		this.limite = limite;
	}

	public List<VirtualService> getVirtualServices() {
		return virtualServices;
	}

	public void setVirtualServices(List<VirtualService> virtualServices) {
		this.virtualServices = virtualServices;
	}
}

package br.com.app.model;

import java.util.ArrayList;
import java.util.List;

public class CategoriaRegistro {

	private String nome;
	private List<Registro> registros;
	
	public CategoriaRegistro(String nome) {
		super();
		this.nome = nome;
		this.registros = new ArrayList<Registro>();
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<Registro> getRegistros() {
		return registros;
	}

	public void setRegistros(List<Registro> registros) {
		this.registros = registros;
	}

	@Override
	public String toString() {
		return nome;
	}
	
}

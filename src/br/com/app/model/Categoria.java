package br.com.app.model;

import java.util.ArrayList;
import java.util.List;

public class Categoria {

	private String nome;
	private String imagem;
	private Integer ordem;
	private List<Contato> contatos;

	public Categoria(String nome, Integer ordem) {
		super();
		this.nome = nome;
		this.imagem = "img_padrao";
		this.ordem = ordem;
		this.contatos = new ArrayList<Contato>();
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public List<Contato> getContatos() {
		return contatos;
	}

	public void setContatos(List<Contato> contatos) {
		this.contatos = contatos;
	}
}

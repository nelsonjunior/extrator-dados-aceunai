package br.com.app.builder;

import java.util.ArrayList;
import java.util.List;

import br.com.app.model.Categoria;
import br.com.app.util.StringUtil;

public class CategoriaSqlBuilder {

	
	private static final String IMG_PADRAO = "img_padrao";

	public static List<String> builderSQL(Integer id, Categoria categoria){
		List<String> linhasRet = new ArrayList<String>();
		Object[] params = new Object[]{StringUtil.formatarString(categoria.getNome()), categoria.getImagem(), categoria.getOrdem()};
		linhasRet.add(String.format("INSERT INTO CATEGORIA (NOME, ID_IMAGEM, ORDEM) VALUES ('%s', '%s', %d)", params));
		return linhasRet;
	}
	
	public static String builderSQL(String nome, Integer ordem){
		String nomeFormatado = StringUtil.formatarString(nome);
		Object[] params = new Object[]{nomeFormatado, IMG_PADRAO, ordem, StringUtil.normatizarString(nomeFormatado)};
		return String.format("INSERT INTO CATEGORIA (NOME, ID_IMAGEM, ORDEM, NOME_ASCII) VALUES ('%s', '%s', %d, '%s');", params);
	}
	
}

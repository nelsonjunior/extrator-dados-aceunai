package br.com.app.builder;

import java.util.ArrayList;
import java.util.List;

import br.com.app.model.Telefone;

public class TelefoneSqlBuilder {

	
	public static List<String> builderSQL(Integer idContato, List<Telefone> telefones){
		List<String> linhasRet = new ArrayList<String>();

		telefones.forEach(tel -> 
			linhasRet.add(String.format("INSERT INTO TELEFONE (ID_CONTATO, TELEFONE) "
					+ "VALUES (%d, '%s');", 
					new Object[]{idContato, tel.getTelefone()}))
			);
		
		return linhasRet;
	}
	
	public static List<String> builderClassificadoSQL(List<Telefone> telefones){
		List<String> linhasRet = new ArrayList<String>();

		telefones.forEach(tel -> 
			linhasRet.add(String.format("INSERT INTO TELEFONE (ID_CONTATO, TELEFONE) VALUES ((SELECT MAX(ID) FROM CONTATO), '%s');", 
					new Object[]{tel.getTelefone()}))
			);
		return linhasRet;
	}
	
}

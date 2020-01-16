package br.com.app.builder;

import java.util.ArrayList;
import java.util.List;

import br.com.app.model.Contato;
import br.com.app.sql.BaseDao;
import br.com.app.util.StringUtil;

public class ContatoSqlBuilder {

	
	public static List<String> builderSQL(Contato contato){
		List<String> linhasRet = new ArrayList<String>();
		contato.setNome(StringUtil.formatarString(contato.getNome()));
		contato.setEndereco(StringUtil.formatarString(contato.getEndereco()));
		contato.setTelefoneRaw(contato.getTelefoneRaw().trim());

		Object[] params = new Object[]{contato.getNome(), 
				contato.getEndereco(), 
				contato.getTelefoneRaw(),
				StringUtil.normatizarString(contato.getNome())};
		
		String registroLinha = String.format("INSERT INTO CONTATO (NOME, ENDERECO, TELEFONE, NOME_ASCII) VALUES ('%s', '%s', '%s', '%s');", params);

		if(!isContemContato(linhasRet, contato.getNome())){
			linhasRet.add(registroLinha);
			linhasRet.addAll(TelefoneSqlBuilder.builderClassificadoSQL(contato.getTelefones()));
		}
		return linhasRet;
	}
	
	private static Boolean isContemContato(List<String> lista, CharSequence nome){
		for (String string : lista) {
			if(string.contains(nome)){
				return true;
			}
		}
		return false;
	}
	
	public static List<String> builderClassificadoSQL(Contato contato){
		List<String> linhasRet = new ArrayList<String>();
		contato.setNome(StringUtil.formatarString(contato.getNome()));
		contato.setEndereco(StringUtil.formatarString(contato.getEndereco()));
		contato.setTelefoneRaw(contato.getTelefoneRaw().trim());
		
		Integer idContato = BaseDao.getInstance().obterIDContatoPorHash(contato);
		
		if(idContato != null){
			linhasRet.add("INSERT INTO CLASSIFICADO (ID_CATEGORIA, ID_CONTATO) VALUES ((SELECT MAX(ID) FROM CATEGORIA), " + idContato + ");");
		}else{
			Object[] params = new Object[]{contato.getNome(), contato.getEndereco(), contato.getTelefoneRaw(), StringUtil.normatizarString(contato.getNome())};
			linhasRet.add(String.format("INSERT INTO CONTATO (NOME, ENDERECO, TELEFONE, NOME_ASCII) VALUES ('%s', '%s', '%s', '%s');", params));
			linhasRet.addAll(TelefoneSqlBuilder.builderClassificadoSQL(contato.getTelefones()));
			linhasRet.add("INSERT INTO CLASSIFICADO (ID_CATEGORIA, ID_CONTATO) VALUES ((SELECT MAX(ID) FROM CATEGORIA), (SELECT MAX(ID) FROM CONTATO));");
		}
		return linhasRet;
	}

}

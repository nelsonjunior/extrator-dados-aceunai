package br.com.app.old;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import br.com.app.builder.CategoriaSqlBuilder;
import br.com.app.builder.ContatoSqlBuilder;
import br.com.app.model.CategoriaRegistro;
import br.com.app.model.Contato;
import br.com.app.model.Registro;
import br.com.app.model.Telefone;


public class ExtratorClassificadoOld {
	
	private static final Pattern TELEFONE_9_DIG = Pattern.compile("[8-9]{0,1}[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE = Pattern.compile("[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE_PART = Pattern.compile("[0-9]{4}");

	private static final Pattern NOME_PART = Pattern.compile(".*[A-Z]{3}");

	public static void main(String[] args) throws IOException {
		
		Boolean debugMode = Boolean.FALSE;
		
		if(debugMode) System.out.println("##### RECUPERAR ARQUIVO!");
		Path caminho = Paths.get(System.getProperty("user.home"), "Extrator/entrada/class.txt");
		Stream<String> baseDados = Files.lines(caminho);
		
		if(debugMode) System.out.println("##### REMOVER LINHAS EM BRANCO!");		
		Stream<String> linhas = baseDados.filter(lin -> !lin.isEmpty());
		
		if(debugMode) System.out.println("##### OBTER REGISTROS!");
		
		
		List<CategoriaRegistro> registroClassficados = new ArrayList<>();
		
		List<Registro> registros = new ArrayList<Registro>();
		Iterator<String> iterator = linhas.iterator();
		String categoriaAtual = null;
		while (iterator.hasNext()) {
			String linha = iterator.next();
			if(linha.trim().isEmpty()){
				continue;
			}
			if(linha.contains("#")){
				categoriaAtual = linha.trim().replace("#", "");
				registroClassficados.add(new CategoriaRegistro(categoriaAtual));
			}else{
				if(categoriaAtual != null){

					if(!linha.startsWith("\t")){
						registroClassficados.get(registroClassficados.size()-1).getRegistros().add(new Registro(linha));
					}else{
						List<Registro> listaAtual = registroClassficados.get(registroClassficados.size()-1).getRegistros(); 
						listaAtual.get(listaAtual.size()-1).getComplementos().add(linha);
					}
				}else{
					if(!linha.startsWith("\t")){
						registros.add(new Registro(linha));
					}else{
						registros.get(registros.size()-1).getComplementos().add(linha);
						
					}
				}
			}
			
		}
		if(debugMode) System.out.println("##### REGISTROS ENCONTRADOS: " + registros.size());
		if(debugMode) System.out.println("##### OBTER CONTATOS!");
		
		if(registroClassficados.size() > 0){
			
			int ordem = 1;

			if(debugMode) System.out.println("##### CATEGORIAS ENCONTRADAS");
			if(debugMode) registroClassficados.forEach(System.out::println);

			for (CategoriaRegistro categoria : registroClassficados) {
				
				if(debugMode) System.out.println("PROCESSANDO CATEGORIA: " + categoria.getNome());

				System.out.println(CategoriaSqlBuilder.builderSQL(categoria.getNome(), ordem++));
				
				Map<String, List<?>> retorno = processarRegistros(categoria.getRegistros());
				processarResultado(retorno.get("CONTATOS"), retorno.get("PROCESSAMENTO_MANUAL"), retorno.get("PROCESSAMENTO_MANUAL_TEL"), debugMode);
			}
			
		}else{
			Map<String, List<?>> retorno = processarRegistros(registros);
			processarResultado(retorno.get("CONTATOS"), retorno.get("PROCESSAMENTO_MANUAL"), retorno.get("PROCESSAMENTO_MANUAL_TEL"), debugMode);
		}
		
	}

	@SuppressWarnings("unchecked")
	private static void processarResultado(List<?> list1, List<?> list2, List<?> list3, Boolean debugMode) {

		List<Contato> contatos = (List<Contato>) list1;
		List<Registro> processamentoManualPharserTelefone = (List<Registro>) list2;
		List<Registro> processamentoManual = (List<Registro>) list3;

		Boolean possuiErros = processamentoManual.size() > 0 || processamentoManualPharserTelefone.size() > 0;
		if(possuiErros){
			System.out.println("##### ERRO ENCONTRATADOS #####");
			processamentoManual.forEach(System.out::println);
			processamentoManualPharserTelefone.forEach(System.out::println);
//			debugMode = Boolean.TRUE;
		}
		
		if(debugMode) System.out.println("##### CONTATOS ENCONTRADOS: " + contatos.size());
		if(debugMode) System.out.println("##### REGISTROS PROCESSAMENTO MANUAL: " + processamentoManual.size());
		if(debugMode) System.out.println("##### REGISTROS PROCESSAMENTO MANUAL TELEFONE: " + processamentoManualPharserTelefone.size());
		if(debugMode) System.out.println("##### PROCESSAMENTO MANUAL TELEFONE #####");

		if(!possuiErros){
			List<String> linhasSQL = new ArrayList<String>(); 
			for (int i = 1; i <= contatos.size(); i++) {
				linhasSQL.addAll(ContatoSqlBuilder.builderClassificadoSQL(contatos.get(i - 1)));
			}
			
			linhasSQL.forEach(System.out::println);
		}
	}


	private static Map<String, List<?>> processarRegistros(List<Registro> registros) {
		List<Contato> contatos = new ArrayList<Contato>();
		List<Registro> processamentoManual = new ArrayList<Registro>();
		List<Registro> processamentoManualPharserTelefone = new ArrayList<Registro>();

		
		for (Registro registro : registros) {
			Contato contato = new Contato(registro.getNome());
			
			if(registro.getComplementos().size() == 0){
				Matcher mTelefone = TELEFONE.matcher(registro.getNome());
				if(mTelefone.find()){
					String nomeRaw = registro.getNome();
					Matcher mNome = NOME_PART.matcher(nomeRaw);
					if(mNome.find()){
						
						contato.setNome(nomeRaw.substring(0, mNome.end()));
						contato.setEndereco(nomeRaw.substring(mNome.end(), mTelefone.start()));
					}else{
						processamentoManual.add(registro);
						continue;
					}
					contato.setTelefoneRaw(registro.getNome().substring(mTelefone.start()));
					contato.getTelefones().addAll(processarTelefone(contato.getTelefoneRaw()));
				}else{
					processamentoManual.add(registro);
					continue;
				}
			}else{
				
				for (String comp : registro.getComplementos()) {
					Matcher mTelefone = TELEFONE.matcher(comp);
					if(mTelefone.find()){
						contato.setEndereco(comp.substring(0, mTelefone.start()));
						contato.setTelefoneRaw(comp.substring(mTelefone.start()));
						contato.getTelefones().addAll(processarTelefone(contato.getTelefoneRaw()));
					}else{
						processamentoManualPharserTelefone.add(registro);
						continue;
					}
					
				}
			}
			contatos.add(contato);
			
		}
		
		Map<String, List<?>> retorno = new HashMap<>();
		retorno.put("CONTATOS", contatos);
		retorno.put("PROCESSAMENTO_MANUAL", processamentoManual);
		retorno.put("PROCESSAMENTO_MANUAL_TEL", processamentoManualPharserTelefone);
		
		return retorno;
	}


	private static List<Telefone> processarTelefone(String telefoneRaw) {
		List<Telefone> retorno = new ArrayList<Telefone>();
		String[] split = telefoneRaw.split("/");
		for (String part : split) {
			Matcher mTelefone = TELEFONE.matcher(part);
			Matcher mTelefone9Dig = TELEFONE_9_DIG.matcher(part);
			if(mTelefone.matches() || mTelefone9Dig.matches()){
				retorno.add(new Telefone(part));
			}else {
				Matcher mTelefonePart = TELEFONE_PART.matcher(part);
				if(mTelefonePart.matches()){
					retorno.add(new Telefone(retorno.get(retorno.size()-1).getTelefone().substring(0,5).concat(part)));
				}
			}
		}
		return retorno;
	}

}

package br.com.app.old;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import br.com.app.builder.ContatoSqlBuilder;
import br.com.app.model.Contato;
import br.com.app.model.Registro;
import br.com.app.model.Telefone;


public class ExtratorOld {
	
	private static final Pattern TELEFONE_9_DIG = Pattern.compile("[8-9]{0,1}[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE = Pattern.compile("[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE_PART = Pattern.compile("[0-9]{4}");

	private static final Pattern NOME_PART = Pattern.compile(".*[A-Z]{3}");


	public static void main(String[] args) throws IOException {
		
		
		System.out.println("##### RECUPERAR ARQUIVO!");
		Path caminho = Paths.get(System.getProperty("user.home"), "Extrator/entrada/base2017.txt");
		Stream<String> baseDados = Files.lines(caminho);
		
		System.out.println("##### REMOVER LINHAS EM BRANCO!");		
		Stream<String> linhas = baseDados.filter(lin -> !lin.isEmpty());
		
		System.out.println("##### OBTER REGISTROS!");		
		List<Registro> registros = new ArrayList<Registro>();
		Iterator<String> iterator = linhas.iterator();
		while (iterator.hasNext()) {
			String linha = iterator.next();
			if(linha.trim().isEmpty()){
				continue;
			}
			if(!linha.startsWith("\t")){
				registros.add(new Registro(linha));
			}else{
				registros.get(registros.size()-1).getComplementos().add(linha);
				
			}
		}
		System.out.println("##### REGISTROS ENCONTRADOS: " + registros.size());
		
		
		System.out.println("##### OBTER CONTATOS!");
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
//			if(contato.getNome().contains("VÍDEO GAME DO MÁRCIO")){
//				System.out.println();
//			}
			contatos.add(contato);
			
		}
		System.out.println("##### CONTATOS ENCONTRADOS: " + contatos.size());
		System.out.println("##### REGISTROS PROCESSAMENTO MANUAL: " + processamentoManual.size());
		System.out.println("##### REGISTROS PROCESSAMENTO MANUAL TELEFONE: " + processamentoManualPharserTelefone.size());

		System.out.println("##### PROCESSAMENTO MANUAL TELEFONE #####");
		
		
		
		
		List<String> linhasSQL = new ArrayList<String>(); 
		for (int i = 1; i <= contatos.size(); i++) {
			linhasSQL.addAll(ContatoSqlBuilder.builderSQL(contatos.get(i - 1)));
		}
		
		linhasSQL.forEach(System.out::println);
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

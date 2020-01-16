package br.com.app.util;

import java.text.Normalizer;

public class StringUtil {

	public static String normatizarString(String texto){
		String asciiName = Normalizer.normalize(texto, Normalizer.Form.NFD)
			    .replaceAll("[^\\p{ASCII}]", "");
		return asciiName;
	}
	
	public static String formatarString(String textoRAW) {
		if(textoRAW.contains("Av. J. L. Adjuto, 54")){
			System.out.println("");
		}
		String retorno = textoRAW.trim().replaceAll("  ", " ").replaceAll("\\.\\.", "");
		if(retorno.endsWith(".")){
			retorno = retorno.substring(0, retorno.length()-1);
		}
		return retorno;
	}
}

package cielo.environment.util;

import java.io.InputStream;
import java.util.Properties;

public class Configuracao {
	
	private static Configuracao instancia;
	
	private Properties props;
	
	private Configuracao() {
						
		this.props = new Properties();		
		try {
			InputStream is = this.getClass().getResourceAsStream("/cielo/environment/util/config.properties");		
			this.props.load(is);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static Configuracao getInstancia() {
		if (Configuracao.instancia == null) {
			Configuracao.instancia = new Configuracao();
		}
		return Configuracao.instancia;
		
	}
	
	public String getProperty(String key) {
		return this.props.getProperty(key);
	}

}

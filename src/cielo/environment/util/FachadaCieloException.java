package cielo.environment.util;

public class FachadaCieloException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private Exception excecaoOriginal;
	private String mensagem;
	
	public FachadaCieloException(Exception excecaoOriginal, String mensagem) {
		this.excecaoOriginal = excecaoOriginal;
		this.mensagem = mensagem;		
	}

	public Exception getExcecaoOriginal() {
		return excecaoOriginal;
	}

	public void setExcecaoOriginal(Exception excecaoOriginal) {
		this.excecaoOriginal = excecaoOriginal;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}	

}

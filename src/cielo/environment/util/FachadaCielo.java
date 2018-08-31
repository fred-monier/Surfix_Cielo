package cielo.environment.util;

import java.io.IOException;

import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.CieloEcommerce;
import cieloecommerce.sdk.ecommerce.Environment;
import cieloecommerce.sdk.ecommerce.Payment;
import cieloecommerce.sdk.ecommerce.QueryMerchantOrderResponse;
import cieloecommerce.sdk.ecommerce.RecurrentPayment;
import cieloecommerce.sdk.ecommerce.RecurrentSale;
import cieloecommerce.sdk.ecommerce.Sale;
import cieloecommerce.sdk.ecommerce.SaleResponse;
import cieloecommerce.sdk.ecommerce.request.CieloError;
import cieloecommerce.sdk.ecommerce.request.CieloRequestException;

public class FachadaCielo {
	
	public static final String BANDEIRA_VISA = "Visa"; 
	public static final String BANDEIRA_MASTERCARD = "Master";
	
	public static final String RECORRENCIA_MENSAL = "Monthly";//1 - default 
	public static final String RECORRENCIA_BIMESTRAL = "Bimonthly";//2
	public static final String RECORRENCIA_TRIMESTRAL = "Quarterly";//3	
	//public static final String RECORRENCIA_QUADRIMESTRAL = "FourMonths";//4
	//A altera��o de Intervalo aceita n�meros de 1 em diante, representando o n�mero de meses do intervalo	
	public static final String RECORRENCIA_SEMESTRAL = "SemiAnnual";//6	
	public static final String RECORRENCIA_ANUAL = "Annual";//12		
					
	private static FachadaCielo instancia;
				
	private FachadaCielo() {		
	}
	
	public static FachadaCielo getInstancia() {
		if (FachadaCielo.instancia == null) {
			FachadaCielo.instancia = new FachadaCielo();
		}
		return FachadaCielo.instancia;
	}
	
	//PARTE I - Compra n�o recorrente
	
	/**
    * M�todo para realizar pagamento no cart�o de cr�dito � vista na Cielo
	*
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param numPedidoVirtual N�mero do pedido gerado pela lojaVirtual
    * @param valor Valor do pedido em centavos
    * @param bandeiraCartao Bandeira do cartao (FachadaCielo.BANDEIRA_VISA ou FachadaCielo.BANDEIRA_MASTERCARD)
    * @param numCartao N�mero do cart�o
    * @param mesAnoExpDate Data que o cart�o expira (formato MM/YYYY)
    * @param nomeClienteCartao Nome do cliente do cart�o
    * @param cvv C�digo de seguran�a do cart�o
    * @param descricaoVenda Texto de at� 13 caracteres impresso na fatura bancaria comprador - Exclusivo para VISA/MASTER
    * 
    * @return Payment Dados do pagamento realizado (PaymentId, TID e demais informa��es)
    */
	public Payment gerarPagamentoCreditoAVista(boolean modoProd, String mecId, String mecKey, 
			String numPedidoVirtual, Integer valor,
			String bandeiraCartao, String numCartao, String mesAnoExpDate, 
			String nomeClienteCartao, String cvv,
			String descricaoVenda) throws FachadaCieloException {
		
		Payment res;
						
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//		
					
		Sale sale = new Sale(numPedidoVirtual);
		
		sale.customer(nomeClienteCartao); 
		
		Payment payment = sale.payment(valor);
		
		if (descricaoVenda.length() > 13) {
			descricaoVenda = descricaoVenda.substring(0, 13);
		}
		payment.setSoftDescriptor(descricaoVenda);
		
		payment.creditCard(cvv, bandeiraCartao)
			.setExpirationDate(mesAnoExpDate)
			.setCardNumber(numCartao)
			.setHolder(nomeClienteCartao);
		
		try {
			
		    sale = new CieloEcommerce(merchant, environment).createSale(sale);
		    
		    if (sale != null) {		    		    	
		    	res = sale.getPayment();		    	
		    } else {		
		    	
		    	System.out.println();
		    	
		    	throw new FachadaCieloException(null, "Transa��o n�o autorizada pela Cielo");
		    }
	    		   
		} catch (CieloRequestException e) {
			
			System.out.println();
			
		    CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    		    		    
		} catch (IOException e) {
			
			System.out.println();
					
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}
		
		return res;
	}
	
	/**
    * M�todo para consultar venda no cart�o de cr�dito � vista na Cielo por paymentId
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param paymentId N�mero do pagamento gerado pela Cielo
    * 
    * @return Sale Dados da venda realizada
    */	
	public Sale consultarVendaCreditoAVistaPorPaymentId(boolean modoProd, String mecId, String mecKey, String paymentId) 
			throws FachadaCieloException {
		
		Sale sale;
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//		
		
		try {
			
			sale = new CieloEcommerce(merchant, environment).querySale(paymentId);
		
			if (sale == null) {		
				
				System.out.println();
				
		    	throw new FachadaCieloException(null, "Consulta n�o autorizada pela Cielo");
		    }
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}
					
		return sale;
		
	}
	
	/**
    * M�todo para consultar vendas na Cielo pelo n�mero do pedido gerado pela loja virtual
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param numPedidoVirtual N�mero do pedido gerado pela loja virtual
    * 
    * @return Payment[] Array dos pagamentos gerados pelo n�mero do pedido enviado
    */		
	public Payment[] consultarVendasPorNumPedidoVirtual(boolean modoProd, String mecId, String mecKey, String numPedidoVirtual) 
			throws FachadaCieloException {
		
		Payment[] payments;
		
		Merchant merchant;
		Environment environment;
			
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			QueryMerchantOrderResponse query = new CieloEcommerce(merchant, environment).
					queryMerchantOrder(numPedidoVirtual);
		
			if (query != null) {				
				payments = query.getPayments();				
			} else {
				
				System.out.println();
				
		    	throw new FachadaCieloException(null, "Consulta n�o autorizada pela Cielo");
		    }
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}
		
		return payments;
	}
	
    /**      
    * M�todo para cancelar vendas na Cielo por paymentId
    * 
    * O Cancelamento de pagamentos via API (outra forma seria via BackOffice) 
    * � permitido apenas para Cart�es de Cr�dito 
    * (Total, ou Parcial - apenas para transa��es capturadas - em fra��es at� somar o valor total) 
    * ou D�bito (Total)
	* Pode ser via paymentId (garantido) ou merchantOrderId (cancela apenas o �ltimo pagamento 
	* com o referido Id, impossibilitando o cancelamento de anteriores que possam existir
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param paymentId N�mero do pagamento gerado pela Cielo
    * 
    * @return SaleResponse Dados da venda que teve seu pagamento cancelado
    */		
	public SaleResponse cancelarPagamentoTotalCreditoAVista(boolean modoProd, String mecId, String mecKey, String paymentId) 
			throws FachadaCieloException {
		
		SaleResponse saleResp;
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			saleResp = new CieloEcommerce(merchant, environment).cancelSale(paymentId);
		
			if (saleResp == null) {		
				
				System.out.println();
				
		    	throw new FachadaCieloException(null, "Consulta n�o autorizada pela Cielo");
		    }
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}
		
		
		return saleResp;
	}
		
	//M�todos n�o Implementados:
	
	//***public Payment gerarPagamentoDebitoAVista
	//O Cart�o de d�bito por padr�o exige que o portador seja direcionado para 
	//o ambiente Banc�rio, onde ser� avaliada a senha e dados informados pela loja. 
	//Existe a op��o de n�o autenticar transa��es de d�bito, porem � necessario que o 
	//banco emissor do cart�o permita tal transa��o Essa n�o � uma permiss�o concedida pela cielo, 
	//o lojista deve acionar o banco e solicitar a permiss�o
	
	//***public Payment gerarPagamentoTransferenciaEletronica
	//Para realizar essa opera��o a Cielo devolve uma URL para qual o Lojista deve redirecionar 
	//o Cliente para o fluxo de Transfer�ncia Eletronica.
	
	//***public Payment gerarPagamentoBoletoBancario
	//A API suporta boletos registrados e n�o registrados, sendo o provider 
	//o diferenciador entre eles. Sugerimos que valide com seu banco qual o tipo 
	//de boleto suportado por sua carteira. A API Aceita apenas boletos Bradesco e Banco do Brasil
	//O objeto Payment retorna um atributo BarCodeNumber	
	
	//D�vidas esclarecidas: 
	
	//O Cancelamento 300 dias p�s-autoriza��o, significa o qu�? 
	//(PRAZO DO LOJISTA PARA CANCELAR)
		
	//A Consulta por paymentId acima, � apenas para Cart�o de Cr�dito?
	//(N�O) S�o todos os meios
	
	//A Consulta de vendas por MerchantOrderId retorna os receivedDates nulo ap�s a convers�o de Json
	//porque a consulta deles tem como par�metro no JSon ReceveidDate e n�o ReceivedDate como est� na classe 
	
	//Possibilidade de Ratear o pagamento do cart�o para outro cliente al�m do
	//MerchantId cadastrado? Us�vamos um SubAdquirente  
	//(N�O)
		
		
	//PARTE II - Compra recorrente
	
	/**
    * M�todo para realizar pagamento recorrente no cart�o de cr�dito com primeira parcela � vista na Cielo
    * OBS: Guardar o paymentId individual dessa parcela inicial bem como o Payment.RecurrentPayment.recurrementPaymentId da recorr�ncia
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param numPedidoVirtual N�mero do pedido gerado pela lojaVirtual
    * @param valor Valor do pedido em centavos
    * @param bandeiraCartao Bandeira do cartao (FachadaCielo.BANDEIRA_VISA ou FachadaCielo.BANDEIRA_MASTERCARD)
    * @param numCartao N�mero do cart�o
    * @param mesAnoExpDate Data que o cart�o expira (formato MM/YYYY)
    * @param nomeClienteCartao Nome do cliente do cart�o
    * @param cvv C�digo de seguran�a do cart�o
    * @param descricaoVenda Texto de at� 13 caracteres impresso na fatura bancaria comprador - Exclusivo para VISA/MASTER
    * @param intervalo Intervalo desejado de recorr�ncia (FachadaCielo.RECORRENCIA_MENSAL, 
    * 													  FachadaCielo.RECORRENCIA_BIMESTRAL, 
    * 													  FachadaCielo.RECORRENCIA_TRIMESTRAL, 
    * 													  FachadaCielo.RECORRENCIA_SEMESTRAL ou 
    * 							   						  FachadaCielo.RECORRENCIA_ANUAL)
    * 													  Sendo nulo = FachadaCielo.RECORRENCIA_MENSAL.
    * @param dataFinal Data em que a recorr�ncia ser� cancelada (formato YYYY-MM-DD)
    * 													  Sendo nulo n�o ter� data final
    * 
    * @return Payment Dados do pagamento realizado (PaymentId, TID e demais informa��es)
    */
	public Payment gerarPagamentoCreditoAVistaRecProg(boolean modoProd, String mecId, String mecKey, 
			String numPedidoVirtual, Integer valor,
			String bandeiraCartao, String numCartao, String mesAnoExpDate, String nomeClienteCartao, 
			String cvv, String descricaoVenda, String intervalo, String dataFinal) throws FachadaCieloException {
					
		Payment res;
						
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
					
		Sale sale = new Sale(numPedidoVirtual);
		
		sale.customer(nomeClienteCartao); 
		
		Payment payment = sale.payment(valor);
		
		if (descricaoVenda.length() > 13) {
			descricaoVenda = descricaoVenda.substring(0, 13);
		}
		payment.setSoftDescriptor(descricaoVenda);
		
		//Recorr�ncia
		RecurrentPayment.Interval inter;
		RecurrentPayment recurrentPayment = payment.recurrentPayment(true);
		
		if (intervalo == null || intervalo.equals(RECORRENCIA_MENSAL)) {				
			inter = RecurrentPayment.Interval.Monthly; 
			
		}  else if (intervalo.equals(RECORRENCIA_BIMESTRAL)) {				
			inter = RecurrentPayment.Interval.Bimonthly;
			
		}  else if (intervalo.equals(RECORRENCIA_TRIMESTRAL)) {
			inter = RecurrentPayment.Interval.Quarterly;	
			
		}  else if (intervalo.equals(RECORRENCIA_SEMESTRAL)) {
			inter = RecurrentPayment.Interval.SemiAnnual;		
			
		}  else if (intervalo.equals(RECORRENCIA_ANUAL)) {
			inter = RecurrentPayment.Interval.Annual;		
			
		} else {
			inter = RecurrentPayment.Interval.Monthly; 
		}
		recurrentPayment.setInterval(inter);
		recurrentPayment.setEndDate(dataFinal);
		//
					
		payment.creditCard(cvv, bandeiraCartao)
			.setExpirationDate(mesAnoExpDate)
			.setCardNumber(numCartao)
			.setHolder(nomeClienteCartao);
		
		try {
			
		    sale = new CieloEcommerce(merchant, environment).createSale(sale);
		    
		    if (sale != null) {		    		    	
		    	res = sale.getPayment();		    	
		    } else {		 
		    	
		    	System.out.println();
		    	
		    	throw new FachadaCieloException(null, "Transa��o n�o autorizada pela Cielo");
		    }

		    		    
		} catch (CieloRequestException e) {
						    
			System.out.println();
			
		    CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    		    		    
		} catch (IOException e) {
			
			System.out.println();
			
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}
		
		return res;
	}
	
	/**
    * M�todo para realizar pagamento recorrente no cart�o de cr�dito com primeira parcela agendada na Cielo
    * OBS: Guardar o paymentId individual dessa parcela inicial bem como o Payment.RecurrentPayment.recurrementPaymentId da recorr�ncia
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param numPedidoVirtual N�mero do pedido gerado pela lojaVirtual
    * @param valor Valor do pedido em centavos
    * @param bandeiraCartao Bandeira do cartao (FachadaCielo.BANDEIRA_VISA ou FachadaCielo.BANDEIRA_MASTERCARD)
    * @param numCartao N�mero do cart�o
    * @param mesAnoExpDate Data que o cart�o expira (formato MM/YYYY)
    * @param nomeClienteCartao Nome do cliente do cart�o
    * @param cvv C�digo de seguran�a do cart�o
    * @param descricaoVenda Texto de at� 13 caracteres impresso na fatura bancaria comprador - Exclusivo para VISA/MASTER
    * @param dataInicial Data em que o primeiro pagamento ser� autorizado dando in�cio � recorr�ncia (formato YYYY-MM-DD)
    * @param intervalo Intervalo desejado de recorr�ncia (FachadaCielo.RECORRENCIA_MENSAL, 
    * 													  FachadaCielo.RECORRENCIA_BIMESTRAL, 
    * 													  FachadaCielo.RECORRENCIA_TRIMESTRAL, 
    * 													  FachadaCielo.RECORRENCIA_SEMESTRAL ou 
    * 							   						  FachadaCielo.RECORRENCIA_ANUAL)
    * 													  Sendo nulo = FachadaCielo.RECORRENCIA_MENSAL.
    * @param dataFinal Data em que a recorr�ncia ser� cancelada (formato YYYY-MM-DD). Sendo nulo n�o ter� data final
    * 
    * @return Payment Dados do pagamento realizado (PaymentId, TID e demais informa��es)
    */
	public Payment gerarPagamentoCreditoAgendadoRecProg(boolean modoProd, String mecId, String mecKey, 
			String numPedidoVirtual, Integer valor,
			String bandeiraCartao, String numCartao, String mesAnoExpDate, String nomeClienteCartao, 
			String cvv, String descricaoVenda, String dataInicial, String intervalo, String dataFinal) throws FachadaCieloException {
					
		Payment res;
						
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
					
		Sale sale = new Sale(numPedidoVirtual);
		
		sale.customer(nomeClienteCartao); 
		
		Payment payment = sale.payment(valor);
		
		if (descricaoVenda.length() > 13) {
			descricaoVenda = descricaoVenda.substring(0, 13);
		}
		payment.setSoftDescriptor(descricaoVenda);
		
		//Recorr�ncia
		RecurrentPayment.Interval inter;
		RecurrentPayment recurrentPayment = payment.recurrentPayment(false);
		recurrentPayment.setStartDate(dataInicial);
		
		if (intervalo == null || intervalo.equals(RECORRENCIA_MENSAL)) {				
			inter = RecurrentPayment.Interval.Monthly; 
			
		}  else if (intervalo.equals(RECORRENCIA_BIMESTRAL)) {				
			inter = RecurrentPayment.Interval.Bimonthly;
			
		}  else if (intervalo.equals(RECORRENCIA_TRIMESTRAL)) {
			inter = RecurrentPayment.Interval.Quarterly;	
			
		}  else if (intervalo.equals(RECORRENCIA_SEMESTRAL)) {
			inter = RecurrentPayment.Interval.SemiAnnual;		
			
		}  else if (intervalo.equals(RECORRENCIA_ANUAL)) {
			inter = RecurrentPayment.Interval.Annual;		
			
		} else {
			inter = RecurrentPayment.Interval.Monthly; 
		}
		recurrentPayment.setInterval(inter);
		recurrentPayment.setEndDate(dataFinal);
		//
					
		payment.creditCard(cvv, bandeiraCartao)
			.setExpirationDate(mesAnoExpDate)
			.setCardNumber(numCartao)
			.setHolder(nomeClienteCartao);
		
		try {
			
		    sale = new CieloEcommerce(merchant, environment).createSale(sale);
		    
		    if (sale != null) {		    		    	
		    	res = sale.getPayment();		    	
		    } else {		    	
		    	
		    	System.out.println();
		    	
		    	throw new FachadaCieloException(null, "Transa��o n�o autorizada pela Cielo");
		    }

		    		    
		} catch (CieloRequestException e) {
						    
			System.out.println();
			
		    CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    		    		    
		} catch (IOException e) {
			
			System.out.println();
			
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}
		
		return res;
	}
		
	/**
    * M�todo para consultar venda com pagamento recorrente no cart�o de cr�dito por recurrentPaymentId 
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * 
    * @return RecurrentSale Dados da venda recorrente realizada (veririfcar campo recurrentPayment.status
    * 																1 - Ativo 
    *																2 - Finalizado 
    *																3 - Desativada pelo Lojista 
    *																4 - Desativada por numero de retentativas 
    *																5 - Desativada por cart�o de cr�dito vencido)
    */	
	public RecurrentSale consultarVendaCreditoRecProgPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, 
			String recurrentPaymentId) throws FachadaCieloException {
		
		RecurrentSale recSale;
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			recSale = new CieloEcommerce(merchant, environment).queryRecurrentSale(recurrentPaymentId);
		
			if (recSale == null) {		
				
				System.out.println();
				
		    	throw new FachadaCieloException(null, "Consulta n�o autorizada pela Cielo");
		    }
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}
					
		return recSale;
		
	}
	
	/**
    * M�todo para alterar os dados de um pagamento recorrente no cart�o de cr�dito na Cielo por recurrentPaymentId
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * @param valor Valor do pedido em centavos
    * @param bandeiraCartao Bandeira do cartao (FachadaCielo.BANDEIRA_VISA ou FachadaCielo.BANDEIRA_MASTERCARD)
    * @param numCartao N�mero do cart�o
    * @param mesAnoExpDate Data que o cart�o expira (formato MM/YYYY)
    * @param nomeClienteCartao Nome do cliente do cart�o  
    * @param cvv C�digo de seguran�a do cart�o  
    * @param descricaoVenda Texto de at� 13 caracteres impresso na fatura bancaria comprador - Exclusivo para VISA/MASTER
    * 
    * @return Payment Dados do pagamento realizado (PaymentId, TID e demais informa��es)
    */
	public void alterarPagamentoCreditoRecProgPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, 
			String recurrentPaymentId, int valor,
			String bandeiraCartao, String numCartao, String mesAnoExpDate, String nomeClienteCartao, 
			String cvv, String descricaoVenda) throws FachadaCieloException {
										
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//					 
		
		Payment payment = new Payment(valor);
		
		if (descricaoVenda.length() > 13) {
			descricaoVenda = descricaoVenda.substring(0, 13);
		}
		payment.setSoftDescriptor(descricaoVenda);
							
		payment.creditCard(cvv, bandeiraCartao)
			.setExpirationDate(mesAnoExpDate)
			.setCardNumber(numCartao)
			.setHolder(nomeClienteCartao);
		
		try {
			
		    new CieloEcommerce(merchant, environment).UpdateRecurrentSale(recurrentPaymentId, payment);
		    		    		    
		} catch (CieloRequestException e) {
			
			System.out.println();
						    
		    CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    		    		    
		} catch (IOException e) {
			
			System.out.println();
			
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}		
	}			
	
	/**
    * M�todo para alterar a data final de uma venda com pagamento recorrente no cart�o de cr�dito por recurrentPaymentId 
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * @param dataFinal Data em que a recorr�ncia ser� cancelada (formato YYYY-MM-DD). Sendo nulo n�o ter� data final
    * 
    * @return void
    */	
	public void alterarVendaCreditoRecProgDataFinalPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, 
			String recurrentPaymentId, String dataFinal) throws FachadaCieloException {				
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			new CieloEcommerce(merchant, environment).updateEndDateRecurrentSale(recurrentPaymentId, dataFinal);		
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}								
		
	}
			
	/**
    * M�todo para alterar o dia do pagamento de uma venda com pagamento recorrente no cart�o de 
    * cr�dito na Cielo por recurrentPaymentId 
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * @param diaRec Dia do m�s em que a recorr�ncia se repetir�
    * 
    * @return void
    */	
	public void alterarVendaCreditoRecProgDiaRecPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, 
			String recurrentPaymentId, int diaRec) throws FachadaCieloException {				
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			new CieloEcommerce(merchant, environment).updateRecurrencyDayRecurrentSale(recurrentPaymentId, diaRec);		
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}								
		
	}	

	/**
    * M�todo para alterar o valor recorrente de uma venda com pagamento recorrente no cart�o de 
    * cr�dito na Cielo por recurrentPaymentId 
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * @param valorRec Valor em centavos do pagamento recorrente
    * 
    * @return void
    */	
	public void alterarVendaCreditoRecProgValorRecPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, 
			String recurrentPaymentId, int valorRec) throws FachadaCieloException {				
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			new CieloEcommerce(merchant, environment).updateAmountRecurrentSale(recurrentPaymentId, valorRec);		
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
							
			System.out.println();
			
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}								
		
	}	

	/**
    * M�todo para modificar a data do pr�ximo pagamento de uma venda com pagamento recorrente no cart�o de 
    * cr�dito na Cielo por recurrentPaymentId (n�o altera o dia da recorr�ncia nem o intervalo, apenas define a data do pr�ximo pagamento)
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * @param dataProxRec Data da pr�xima recorr�ncia (formato YYYY-MM-DD)
    * 
    * @return void
    */	
	public void alterarVendaCreditoRecProgDataProxRecPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, 
			String recurrentPaymentId, String dataProxRec) throws FachadaCieloException {				
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			new CieloEcommerce(merchant, environment).UpdateNextPaymentDateRecurrentSale(recurrentPaymentId, dataProxRec);		
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}								
		
	}	
	
	/**
    * M�todo para alterar o intervalo de recorr�ncia de uma venda com pagamento recorrente no cart�o de 
    * cr�dito na Cielo por recurrentPaymentId (n�o altera a data da pr�xima recorr�ncia nem o dia, apenas define um novo intervalo ap�s o pr�ximo pagamento previsto)
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * @param intervalo Intervalo desejado de recorr�ncia (FachadaCielo.RECORRENCIA_MENSAL, 
    * 													  FachadaCielo.RECORRENCIA_BIMESTRAL, 
    * 													  FachadaCielo.RECORRENCIA_TRIMESTRAL, 
    * 													  FachadaCielo.RECORRENCIA_SEMESTRAL ou 
    * 							   						  FachadaCielo.RECORRENCIA_ANUAL)
    * 													  Sendo nulo = FachadaCielo.RECORRENCIA_MENSAL.
    * 
    * @return void
    */	
	public void alterarVendaCreditoRecProgIntervaloPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, 
			String recurrentPaymentId, String intervalo) throws FachadaCieloException {				
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		int months;
		if (intervalo == null || intervalo.equals(RECORRENCIA_MENSAL)) {				
			months = 1; 
			
		}  else if (intervalo.equals(RECORRENCIA_BIMESTRAL)) {				
			months = 2;
			
		}  else if (intervalo.equals(RECORRENCIA_TRIMESTRAL)) {
			months = 3;	
			
		}  else if (intervalo.equals(RECORRENCIA_SEMESTRAL)) {
			months = 6;		
			
		}  else if (intervalo.equals(RECORRENCIA_ANUAL)) {
			months = 12;		
			
		} else {
			months = 1; 
		}		
				
		try {
			
			new CieloEcommerce(merchant, environment).updateIntervalRecurrentSale(recurrentPaymentId, months);		
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}								
		
	}		
		
	/**
    * M�todo para desabilitar uma venda com pagamento recorrente no cart�o de cr�dito na Cielo por recurrentPaymentId 
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * 
    * @return void
    */	
	public void desabilitarVendaCreditoRecProgPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, String recurrentPaymentId) 
			throws FachadaCieloException {				
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			new CieloEcommerce(merchant, environment).deactivateRecurrentSale(recurrentPaymentId);		
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}								
		
	}
		
	/**
    * M�todo para reabilitar uma venda com pagamento recorrente no cart�o de cr�dito na Cielo por recurrentPaymentId 
    * 
    * @param modoProd Indica se esta uma requisi��o de produ��o
    * @param mecId merchantId na Cielo
    * @param mecKey merchantKey na Cielo
    * @param recurrentPaymentId N�mero da recorr�ncia gerado pela Cielo
    * 
    * @return void
    */	
	public void reabilitarVendaCreditoRecProgPorRecurrentPaymentId(boolean modoProd, String mecId, String mecKey, String recurrentPaymentId) 
			throws FachadaCieloException {				
		
		Merchant merchant;
		Environment environment;
		
		//	
		if (mecId == null || mecKey == null) {
			throw new FachadaCieloException(null, "MerchantId/MerchantKey n�o definido");
		} else {
			merchant = new Merchant(mecId, mecKey);
		}
		if (modoProd) {						
			environment = Environment.PRODUCTION;
		} else {						
			environment = Environment.SANDBOX;
		}		
		//
		
		try {
			
			new CieloEcommerce(merchant, environment).reactivateRecurrentSale(recurrentPaymentId);		
			
		} catch (CieloRequestException e) {
			
			System.out.println();
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
			
			System.out.println();
							
			throw new FachadaCieloException(e, "Erro de comunica��o"); 
		}								
		
	}	
	
	//M�todos n�o Implementados:
	
	//1)M�todo de altera��o de dados do comprador de venda recorrente n�o implementado na SDK Java da Cielo (Modificando dados do comprador)
	//N�o parece �til para a Surfix

	//D�vidas:		
	
	//Tentando gerar 4x uma transa��o posterior de recorrencia e n�o autorizando
	//como o lojista fica sabendo? Notificado via URL de notifica��o (para ok ou n�o). Como � isso?
	//R: Pelo fluxograma, cancela a recorr�ncia (pelo atendente, n�o se sabe)
	
	//M�todo de consulta de recorr�ncia diz o seguinte: A Consulta da Recorr�ncia tras dados sobre 
	//o agendamento e sobre o processo de transa��es que se repetem. 
	//Elas n�o retornam dados sobre as transa��es em si. Para isso, deve ser realizado um 
	//GET na transa��o (Disponivel em �Consultanto vendas (???)
	
	//N�o h� como saber se o cart�o foi alterado com sucesso pois n�o s�o retornados dados do mesmo ap�s a altera��o,
	//nem mesmo mascarados??
	
	//Como saber se uma recorrencia foi descontada no caso de um cancelamento de cart�o?? 
	//R: Via consulta de status	
	
	//Recorrencia por intervalo de meses	
	//Quando alreramos data do prox pagamento, n�o se altera o dia de recorrencia nem o intervalo 
	//Quando se altera o intervalo, n�o se altera a data da prox recorrenca nem o dia	
		
	//Como saber se uma recorrencia programada est� realmente desabilitada ap�s sua consulta? status 1 ou 3.
	//E qd eu reabilit�-la? se houver uma recorencia que passou um dia antes?

	private String recuperarErroIntegracao(int codigoErro) {
		
		String res = "";
		
		switch (codigoErro) {								 
			case 0: res = "Internal error. Dado enviado excede o tamanho do campo"; break;
			case 100: res = "RequestId is required. Campo enviado est� vazio ou invalido"; break;
			case 101: res = "MerchantId is required.	Campo enviado est� vazio ou invalido"; break;
			case 102: res = "Payment Type is required. Campo enviado est� vazio ou invalido"; break;
			case 103: res = "Payment Type can only contain letters. Caracteres especiais n�o permitidos"; break;
			case 104: res = "Customer Identity is required. Campo enviado est� vazio ou invalido"; break;
			case 105: res = "Customer Name is required. Campo enviado est� vazio ou invalido"; break;
			case 106: res = "Transaction ID is required. Campo enviado est� vazio ou invalido"; break;
			case 107: res = "OrderId is invalid or does not exists. Campo enviado excede o tamanho ou contem caracteres especiais"; break;
			case 108: res = "Amount must be greater or equal to zero. Valor da transa��o deve ser maior que 0"; break;
			case 109: res = "Payment Currency is required. Campo enviado est� vazio ou invalido"; break;
			case 110: res = "Invalid Payment Currency. Campo enviado est� vazio ou invalido"; break;
			case 111: res = "Payment Country is required. Campo enviado est� vazio ou invalido"; break;
			case 112: res = "Invalid Payment Country. Campo enviado est� vazio ou invalido"; break;
			case 113: res = "Invalid Payment Code. Campo enviado est� vazio ou invalido"; break;
			case 114: res = "The provided MerchantId is not in correct format. O MerchantId enviado n�o � um GUID"; break;
			case 115: res = "The provided MerchantId was not found. O MerchantID n�o existe ou pertence a outro ambiente (EX: Sandbox)"; break;
			case 116: res = "The provided MerchantId is blocked. Loja bloqueada, entre em contato com o suporte Cielo"; break;
			case 117: res = "Credit Card Holder is required. Campo enviado est� vazio ou invalido"; break;
			case 118: res = "Credit Card Number is required. Campo enviado est� vazio ou invalido"; break;
			case 119: res = "At least one Payment is required. N� Payment n�o enviado"; break;
			case 120: res = "Request IP not allowed. Check your IP White List. IP bloqueado por quest�es de seguran�a"; break;
			case 121: res = "Customer is required. N� Customer n�o enviado"; break;
			case 122: res = "MerchantOrderId is required. Campo enviado est� vazio ou invalido"; break;
			case 123: res = "Installments must be greater or equal to one. Numero de parcelas deve ser superior a 1"; break;
			case 124: res = "Credit Card is Required. Campo enviado est� vazio ou invalido"; break;
			case 125: res = "Credit Card Expiration Date is required. Campo enviado est� vazio ou invalido"; break;
			case 126: res = "Credit Card Expiration Date is invalid. Campo enviado est� vazio ou invalido"; break;
			case 127: res = "You must provide CreditCard Number. Numero do cart�o de cr�dito � obrigat�rio"; break;
			case 128: res = "Card Number length exceeded. Numero do cart�o superiro a 16 digitos"; break;
			case 129: res = "Affiliation not found. Meio de pagamento n�o vinculado a loja ou Provider invalido"; break;
			case 130: res = "Could not get Credit Card."; break;
			case 131: res = "MerchantKey is required. Campo enviado est� vazio ou invalido"; break;
			case 132: res = "MerchantKey is invalid. O Merchantkey enviado n�o � um v�lido"; break;
			case 133: res = "Provider is not supported for this Payment Type. Provider enviado n�o existe"; break;
			case 134: res = "FingerPrint length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 135: res = "MerchantDefinedFieldValue length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 136: res = "ItemDataName length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 137: res = "ItemDataSKU length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 138: res = "PassengerDataName length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 139: res = "PassengerDataStatus length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 140: res = "PassengerDataEmail length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 141: res = "PassengerDataPhone length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 142: res = "TravelDataRoute length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 143: res = "TravelDataJourneyType length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 144: res = "TravelLegDataDestination length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 145: res = "TravelLegDataOrigin length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 146: res = "SecurityCode length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 147: res = "Address Street length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 148: res = "Address Number length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 149: res = "Address Complement length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 150: res = "Address ZipCode length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 151: res = "Address City length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 152: res = "Address State length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 153: res = "Address Country length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 154: res = "Address District length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 155: res = "Customer Name length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 156: res = "Customer Identity length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 157: res = "Customer IdentityType length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 158: res = "Customer Email length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 159: res = "ExtraData Name length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 160: res = "ExtraData Value length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 161: res = "Boleto Instructions length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 162: res = "Boleto Demostrative length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 163: res = "Return Url is required. URL de retorno n�o � valida - N�o � aceito pagina��o ou exten��es (EX .PHP) na URL de retorno"; break;
			case 166: res = "AuthorizeNow is required."; break;
			case 167: res = "Antifraud not configured. Antifraude n�o vinculado ao cadastro do lojista"; break;
			case 168: res = "Recurrent Payment not found. Recorrencia n�o encontrada"; break;
			case 169: res = "Recurrent Payment is not active. Recorrencia n�o est� ativa. Execu��o paralizada"; break;
			case 170: res = "Cart�o Protegido not configured. Cart�o protegido n�o vinculado ao cadastro do lojista"; break;
			case 171: res = "Affiliation data not sent. Falha no processamento do pedido - Entre em contato com o suporte Cielo"; break;
			case 172: res = "Credential Code is required. Falha na valida��o das credenciadas enviadas"; break;
			case 173: res = "Payment method is not enabled. Meio de pagamento n�o vinculado ao cadastro do lojista"; break;
			case 174: res = "Card Number is required. Campo enviado est� vazio ou invalido"; break;
			case 175: res = "EAN is required. Campo enviado est� vazio ou invalido"; break;
			case 176: res = "Payment Currency is not supported. Campo enviado est� vazio ou invalido"; break;
			case 177: res = "Card Number is invalid. Campo enviado est� vazio ou invalido"; break;
			case 178: res = "EAN is invalid. Campo enviado est� vazio ou invalido"; break;
			case 179: res = "The max number of installments allowed for recurring payment is 1. Campo enviado est� vazio ou invalido"; break;
			case 180: res = "The provided Card PaymentToken was not found. Token do Cart�o protegido n�o encontrado"; break;
			case 181: res = "The MerchantIdJustClick is not configured. Token do Cart�o protegido bloqueado"; break;
			case 182: res = "Brand is required. Bandeira do cart�o n�o enviado"; break;
			case 183: res = "Invalid customer bithdate. Data de nascimento invalida ou futura"; break;
			case 184: res = "Request could not be empty. Falha no formado ta requisi��o. Verifique o c�digo enviado"; break;
			case 185: res = "Brand is not supported by selected provider. Bandeira n�o suportada pela API Cielo"; break;
			case 186: res = "The selected provider does not support the options provided (Capture, Authenticate, Recurrent or Installments). Meio de pagamento n�o suporta o comando enviado"; break;
			case 187: res = "ExtraData Collection contains one or more duplicated names."; break;
			case 188: res = "Avs with CPF invalid."; break;
			case 189: res = "Avs with length of street exceeded. Dado enviado excede o tamanho do campo"; break;
			case 190: res = "Avs with length of number/complement exceeded. Dado enviado excede o tamanho do campo"; break;
			case 191: res = "Avs with length of district exceeded. Dado enviado excede o tamanho do campo"; break;
			case 192: res = "Avs with zip code invalid. CEP enviado � invalido"; break;
			case 193: res = "Split Amount must be greater than zero. Valor para realiza��o do SPLIT deve ser superior a 0"; break;
			case 194: res = "Split Establishment is Required. SPLIT n�o habilitado para o cadastro da loja"; break;
			case 195: res = "The PlataformId is required. Validados de plataformas n�o enviado"; break;
			case 196: res = "DeliveryAddress is required. Campo obrigat�rio n�o enviado"; break;
			case 197: res = "Street is required. Campo obrigat�rio n�o enviado"; break;
			case 198: res = "Number is required. Campo obrigat�rio n�o enviado"; break;
			case 199: res = "ZipCode is required. Campo obrigat�rio n�o enviado"; break;
			case 200: res = "City is required. Campo obrigat�rio n�o enviado"; break;
			case 201: res = "State is required. Campo obrigat�rio n�o enviado"; break;
			case 202: res = "District is required. Campo obrigat�rio n�o enviado"; break;
			case 203: res = "Cart item Name is required. Campo obrigat�rio n�o enviado"; break;
			case 204: res = "Cart item Quantity is required. Campo obrigat�rio n�o enviado"; break;
			case 205: res = "Cart item type is required. Campo obrigat�rio n�o enviado"; break;
			case 206: res = "Cart item name length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 207: res = "Cart item description length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 208: res = "Cart item sku length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 209: res = "Shipping addressee sku length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 210: res = "Shipping data cannot be null. Campo obrigat�rio n�o enviado"; break;
			case 211: res = "WalletKey is invalid. Dados da Visa Checkout invalidos"; break;
			case 212: res = "Merchant Wallet Configuration not found. Dado de Wallet enviado n�o � valido"; break;
			case 213: res = "Credit Card Number is invalid. Cart�o de cr�dito enviado � invalido"; break;
			case 214: res = "Credit Card Holder Must Have Only Letters. Portador do cart�o n�o deve conter caracteres especiais"; break;
			case 215: res = "Agency is required in Boleto Credential. Campo obrigat�rio n�o enviado"; break;
			case 216: res = "Customer IP address is invalid. IP bloqueado por quest�es de seguran�a"; break;
			case 300: res = "MerchantId was not found"; break;
			case 301: res = "Request IP is not allowed"; break;
			case 302: res = "Sent MerchantOrderId is duplicated"; break;
			case 303: res = "Sent OrderId does not exist"; break;
			case 304: res = "Customer Identity is required"; break;
			case 306: res = "Merchant is blocked"; break;
			case 307: res = "Transaction not found.	Transa��o n�o encontrada ou n�o existente no ambiente."; break;
			case 308: res = "Transaction not available to capture. Transa��o n�o pode ser capturada - Entre em contato com o suporte Cielo"; break;
			case 309: res = "Transaction not available to void. Transa��o n�o pode ser Cancelada - Entre em contato com o suporte Cielo"; break;
			case 310: res = "Payment method doest not support this operation. Comando enviado n�o suportado pelo meio de pagamento"; break;
			case 311: res = "Refund is not enabled for this merchant. Cancelamento ap�s 24 horas n�o liberado para o lojista"; break;
			case 312: res = "Transaction not available to refund. Transa��o n�o permite cancelamento ap�s 24 horas"; break;
			case 313: res = "Recurrent Payment not found. Transa��o recorrente n�o encontrada ou n�o disponivel no ambiente"; break;
			case 314: res = "Invalid Integration"; break;
			case 315: res = "Cannot change NextRecurrency with pending payment"; break;
			case 316: res = "Cannot set NextRecurrency to past date. N�o � permitido alterada dada da recorrencia para uma data passada"; break;
			case 317: res = "Invalid Recurrency Day"; break;
			case 318: res = "No transaction found"; break;
			case 319: res = "Smart recurrency is not enabled. Recorrencia n�o vinculada ao cadastro do lojista"; break;
			case 320: res = "Can not Update Affiliation Because this Recurrency not Affiliation saved"; break;
			case 321: res = "Can not set EndDate to before next recurrency."; break;
			case 322: res = "Zero Dollar Auth is not enabled. Zero Dollar n�o vinculado ao cadastro do lojista"; break;
			case 323: res = "Bin Query is not enabled. Consulta de Bins n�o vinculada ao cadastro do lojista"; break;	    								 						
			default: res = "C�digo n�o identificado";		
		}
						
		return res;		
		
	}

}

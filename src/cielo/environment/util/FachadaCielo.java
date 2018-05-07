package cielo.environment.util;

import java.io.IOException;

import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.CieloEcommerce;
import cieloecommerce.sdk.ecommerce.Environment;
import cieloecommerce.sdk.ecommerce.Payment;
import cieloecommerce.sdk.ecommerce.QueryMerchantOrderResponse;
import cieloecommerce.sdk.ecommerce.Sale;
import cieloecommerce.sdk.ecommerce.SaleResponse;
import cieloecommerce.sdk.ecommerce.request.CieloError;
import cieloecommerce.sdk.ecommerce.request.CieloRequestException;

public class FachadaCielo {
	
	public static final String BANDEIRA_VISA = "Visa"; 
	public static final String BANDEIRA_MASTERCARD = "Master";
			
	private static final String MERCHANT_ID_TESTE = "5da83acc-6fd6-48ec-b22a-f7e9b5de8bef";
	private static final String MERCHANT_KEY_TESTE = "ILWIORZCPKQUYZCYNSSJGXPDUAOPCODLGGAOFDGH";
	
	private static final String MERCHANT_ID_PRODUCAO = "7e326012-288a-4acb-a961-c71e545b32bc";
	private static final String MERCHANT_KEY_PRODUCAO = "UQR7HP9VrTGwoo0dFx8SRCKaUvfLi9gb47xMtnro";
	
	private static FachadaCielo instancia;
	
	private FachadaCielo() {		
	}
	
	public static FachadaCielo getInstancia() {
		if (FachadaCielo.instancia == null) {
			FachadaCielo.instancia = new FachadaCielo();
		}
		return FachadaCielo.instancia;
	}
	
	/**
    * Método para realizar pagamento no cartão de crédito à vista na Cielo
    * 
    * @param producao Indica se é uma chamada de produção
    * @param numPedidoVirtual Número do pedido gerado pela lojaVirtual
    * @param valor Valor do pedido em centavos
    * @param bandeiraCartao Bandeira do cartao (FachadaCielo.BANDEIRA_VISA ou FachadaCielo.BANDEIRA_MASTERCARD)
    * @param numCartao Número do cartão
    * @param mesAnoExpDate Data que o cartão expira (formato MM/YYYY)
    * @param nomeClienteCartao Nome do cliente do cartão
    * @param cvv Código de segurança do cartão
    * @param descricaoVenda Texto de até 13 caracteres impresso na fatura bancaria comprador - Exclusivo para VISA/MASTER
    * 
    * @return Payment Dados do pagamento realizado (PaymentId, TID e demais informações)
    */
	public Payment gerarPagamentoCreditoAVista(boolean producao, String numPedidoVirtual, Integer valor,
			String bandeiraCartao, String numCartao, String mesAnoExpDate, 
			String nomeClienteCartao, String cvv,
			String descricaoVenda) throws FachadaCieloException {
		
		Payment res;
						
		Merchant merchant;
		Environment environment;
		
		if (producao) {
			merchant = new Merchant(MERCHANT_ID_PRODUCAO, MERCHANT_KEY_PRODUCAO);
			environment = Environment.PRODUCTION;
		} else {
			merchant = new Merchant(MERCHANT_ID_TESTE, MERCHANT_KEY_TESTE);
			environment = Environment.SANDBOX;
		}
					
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
		    	throw new FachadaCieloException(null, "Transação não autorizada pela Cielo");
		    }

		    // Com a venda criada na Cielo, já temos o ID do pagamento, TID e demais
		    // dados retornados pela Cielo
		    // String paymentId = sale.getPayment().getPaymentId();	
		    
		    /*
		      
		    Principais campos retornados: 

			ProofOfSale:		Número da autorização, identico ao NSU.					Texto	6	Texto alfanumérico
			
			Tid:				Id da transação na adquirente.							Texto	20	Texto alfanumérico
			
			AuthorizationCode:	Código de autorização.									Texto	6	Texto alfanumérico
			
			SoftDescriptor:		Texto impresso na fatura bancaria comprador 
								- Exclusivo para VISA/MASTER 
								- não permite caracteres especiais - Ver Anexo Cielo	Texto	13	Texto alfanumérico
								
			PaymentId:			Campo Identificador do Pedido.							Guid	36	xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
			
			ECI:				Eletronic Commerce Indicator. 
								Representa o quão segura é uma transação.				Texto	2	Exemplos: 7
								
			Status:				Status da Transação.									Byte	—	2
			
			ReturnCode:			Código de retorno da Adquirência.						Texto	32	Texto alfanumérico
								---
								Autorizado				0000.0000.0000.0001
														0000.0000.0000.0004	Code: 4/6	Operação realizada com sucesso
								Não Autorizado			0000.0000.0000.0002	Code: 05	Não Autorizada
								Não Autorizado			0000.0000.0000.0003	Code: 57	Cartão Expirado
								Não Autorizado			0000.0000.0000.0005	Code: 78	Cartão Bloqueado
								Não Autorizado			0000.0000.0000.0006	Code: 99	Time Out
								Não Autorizado			0000.0000.0000.0007	Code: 77	Cartão Cancelado
								Não Autorizado			0000.0000.0000.0008	Code: 70	Problemas com o Cartão de Crédito
								Autorização Aleatória	0000.0000.0000.0009	Code: 99	Operation Successful / Time Out
								---
									
			ReturnMessage:		Mensagem de retorno da Adquirência.						Texto	512	Texto alfanumérico		     *
			 
		     */
	    
		    
		} catch (CieloRequestException e) {
			
		    // Em caso de erros de integração, podemos tratar o erro aqui.
		    // os códigos de erro estão todos disponíveis no manual de integração.
		    CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    		    		    
		} catch (IOException e) {
			
			//e.printStackTrace();			
			throw new FachadaCieloException(e, "Erro de comunicação"); 
		}
		
		return res;
	}
	
	//***public Payment gerarPagamentoDebitoAVista
	//O Cartão de débito por padrão exige que o portador seja direcionado para 
	//o ambiente Bancário, onde será avaliada a senha e dados informados pela loja. 
	//Existe a opção de não autenticar transações de débito, porem é necessario que o 
	//banco emissor do cartão permita tal transação Essa não é uma permissão concedida pela cielo, 
	//o lojista deve acionar o banco e solicitar a permissão
	
	//***public Payment gerarPagamentoTransferenciaEletronica
	//Para realizar essa operação a Cielo devolve uma URL para qual o Lojista deve redirecionar 
	//o Cliente para o fluxo de Transferência Eletronica.
	
	//***public Payment gerarPagamentoBoletoBancario
	//A API suporta boletos registrados e não registrados, sendo o provider 
	//o diferenciador entre eles. Sugerimos que valide com seu banco qual o tipo 
	//de boleto suportado por sua carteira. A API Aceita apenas boletos Bradesco e Banco do Brasil
	//O objeto Payment retorna um atributo BarCodeNumber
	
	/**
    * Método para consultar venda no cartão de crédito à vista na Cielo por paymentId
    * 
    * @param producao Indica se é uma chamada de produção
    * @param paymentId Número do pagamento gerado pela Cielo
    * 
    * @return Sale Dados da venda realizada
    */	
	public Sale consultarVendaCreditoAVistaPorPaymentId(boolean producao, String paymentId) 
			throws FachadaCieloException {
		
		Sale sale;
		
		Merchant merchant;
		Environment environment;
		
		if (producao) {
			merchant = new Merchant(MERCHANT_ID_PRODUCAO, MERCHANT_KEY_PRODUCAO);
			environment = Environment.PRODUCTION;
		} else {
			merchant = new Merchant(MERCHANT_ID_TESTE, MERCHANT_KEY_TESTE);
			environment = Environment.SANDBOX;
		}
		
		try {
			
			sale = new CieloEcommerce(merchant, environment).querySale(paymentId);
		
			if (sale == null) {		    		    	
		    	throw new FachadaCieloException(null, "Consulta não autorizada pela Cielo");
		    }
			
		} catch (CieloRequestException e) {
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
							
			throw new FachadaCieloException(e, "Erro de comunicação"); 
		}
					
		return sale;
		
	}
	
	/**
    * Método para consultar vendas na Cielo pelo número do pedido gerado pela loja virtual
    * 
    * @param producao Indica se é uma chamada de produção
    * @param numPedidoVirtual Número do pedido gerado pela loja virtual
    * 
    * @return Payment[] Array dos pagamentos gerados pelo número do pedido enviado
    */		
	public Payment[] consultarVendasPorNumPedidoVirtual(boolean producao, String numPedidoVirtual) 
			throws FachadaCieloException {
		
		Payment[] payments;
		
		Merchant merchant;
		Environment environment;
		
		if (producao) {
			merchant = new Merchant(MERCHANT_ID_PRODUCAO, MERCHANT_KEY_PRODUCAO);
			environment = Environment.PRODUCTION;
		} else {
			merchant = new Merchant(MERCHANT_ID_TESTE, MERCHANT_KEY_TESTE);
			environment = Environment.SANDBOX;
		}
		
		try {
			
			QueryMerchantOrderResponse query = new CieloEcommerce(merchant, environment).
					queryMerchantOrder(numPedidoVirtual);
		
			if (query != null) {				
				payments = query.getPayments();				
			} else {
		    	throw new FachadaCieloException(null, "Consulta não autorizada pela Cielo");
		    }
			
		} catch (CieloRequestException e) {
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
							
			throw new FachadaCieloException(e, "Erro de comunicação"); 
		}
		
		return payments;
	}
	
    /**      
    * Método para cancelar vendas na Cielo por paymentId
    * 
    * O Cancelamento de pagamentos via API (outra forma seria via BackOffice) 
    * é permitido apenas para Cartões de Crédito 
    * (Total, ou Parcial - apenas para transações capturadas - em frações até somar o valor total) 
    * ou Débito (Total)
	* Pode ser via paymentId (garantido) ou merchantOrderId (cancela apenas o último pagamento 
	* com o referido Id, impossibilitando o cancelamento de anteriores que possam existir
    * 
    * @param producao Indica se é uma chamada de produção
    * @param paymentId Número do pagamento gerado pela Cielo
    * 
    * @return SaleResponse Dados da venda que teve seu pagamento cancelado
    */		
	public SaleResponse cancelarPagamentoTotalCreditoAVista(boolean producao, String paymentId) 
			throws FachadaCieloException {
		
		SaleResponse saleResp;
		
		Merchant merchant;
		Environment environment;
		
		if (producao) {
			merchant = new Merchant(MERCHANT_ID_PRODUCAO, MERCHANT_KEY_PRODUCAO);
			environment = Environment.PRODUCTION;
		} else {
			merchant = new Merchant(MERCHANT_ID_TESTE, MERCHANT_KEY_TESTE);
			environment = Environment.SANDBOX;
		}
		
		try {
			
			saleResp = new CieloEcommerce(merchant, environment).cancelSale(paymentId);
		
			if (saleResp == null) {				
		    	throw new FachadaCieloException(null, "Consulta não autorizada pela Cielo");
		    }
			
		} catch (CieloRequestException e) {
			
			CieloError error = e.getError();
		    
		    throw new FachadaCieloException(e, e.getMessage() + " (" + this.recuperarErroIntegracao(error.getCode()) + ")");
		    				
		} catch (IOException e) {
							
			throw new FachadaCieloException(e, "Erro de comunicação"); 
		}
		
		
		return saleResp;
	}
		
	//***CriarRecorrencia
	//***ConsultarRecorrencia (ver o que foi de fato realizado)
	//***AlterarRecorrencia (desativar, cancelar)	
	
		
	//DÚVIDA: 
	
	//O Cancelamento 300 dias pós-autorização, significa o quê? 
	//(PRAZO DO LOJISTA PARA CANCELAR)
		
	//A Consulta por paymentId acima, é apenas para Cartão de Crédito?
	//(NÃO) São todos os meios
	
	//A Consulta de vendas por MerchantOrderId retorna os receivedDates nulo após a conversão de Json
	//porque a consulta deles tem como parâmetro no JSon ReceveidDate e não ReceivedDate como está na classe 
	//(EVIDENCIAR)
	
	//Possibilidade de Ratear o pagamento do cartão para outro cliente além do
	//MerchantId cadastrado? Usávamos um SubAdquirente  
	//(NÃO)
	
	
	private String recuperarErroIntegracao(int codigoErro) {
		
		String res = "";
		
		switch (codigoErro) {								 
			case 0: res = "Internal error. Dado enviado excede o tamanho do campo"; break;
			case 100: res = "RequestId is required. Campo enviado está vazio ou invalido"; break;
			case 101: res = "MerchantId is required.	Campo enviado está vazio ou invalido"; break;
			case 102: res = "Payment Type is required. Campo enviado está vazio ou invalido"; break;
			case 103: res = "Payment Type can only contain letters. Caracteres especiais não permitidos"; break;
			case 104: res = "Customer Identity is required. Campo enviado está vazio ou invalido"; break;
			case 105: res = "Customer Name is required. Campo enviado está vazio ou invalido"; break;
			case 106: res = "Transaction ID is required. Campo enviado está vazio ou invalido"; break;
			case 107: res = "OrderId is invalid or does not exists. Campo enviado excede o tamanho ou contem caracteres especiais"; break;
			case 108: res = "Amount must be greater or equal to zero. Valor da transação deve ser maior que 0"; break;
			case 109: res = "Payment Currency is required. Campo enviado está vazio ou invalido"; break;
			case 110: res = "Invalid Payment Currency. Campo enviado está vazio ou invalido"; break;
			case 111: res = "Payment Country is required. Campo enviado está vazio ou invalido"; break;
			case 112: res = "Invalid Payment Country. Campo enviado está vazio ou invalido"; break;
			case 113: res = "Invalid Payment Code. Campo enviado está vazio ou invalido"; break;
			case 114: res = "The provided MerchantId is not in correct format. O MerchantId enviado não é um GUID"; break;
			case 115: res = "The provided MerchantId was not found. O MerchantID não existe ou pertence a outro ambiente (EX: Sandbox)"; break;
			case 116: res = "The provided MerchantId is blocked. Loja bloqueada, entre em contato com o suporte Cielo"; break;
			case 117: res = "Credit Card Holder is required. Campo enviado está vazio ou invalido"; break;
			case 118: res = "Credit Card Number is required. Campo enviado está vazio ou invalido"; break;
			case 119: res = "At least one Payment is required. Nó Payment não enviado"; break;
			case 120: res = "Request IP not allowed. Check your IP White List. IP bloqueado por questões de segurança"; break;
			case 121: res = "Customer is required. Nó Customer não enviado"; break;
			case 122: res = "MerchantOrderId is required. Campo enviado está vazio ou invalido"; break;
			case 123: res = "Installments must be greater or equal to one. Numero de parcelas deve ser superior a 1"; break;
			case 124: res = "Credit Card is Required. Campo enviado está vazio ou invalido"; break;
			case 125: res = "Credit Card Expiration Date is required. Campo enviado está vazio ou invalido"; break;
			case 126: res = "Credit Card Expiration Date is invalid. Campo enviado está vazio ou invalido"; break;
			case 127: res = "You must provide CreditCard Number. Numero do cartão de crédito é obrigatório"; break;
			case 128: res = "Card Number length exceeded. Numero do cartão superiro a 16 digitos"; break;
			case 129: res = "Affiliation not found. Meio de pagamento não vinculado a loja ou Provider invalido"; break;
			case 130: res = "Could not get Credit Card."; break;
			case 131: res = "MerchantKey is required. Campo enviado está vazio ou invalido"; break;
			case 132: res = "MerchantKey is invalid. O Merchantkey enviado não é um válido"; break;
			case 133: res = "Provider is not supported for this Payment Type. Provider enviado não existe"; break;
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
			case 163: res = "Return Url is required. URL de retorno não é valida - Não é aceito paginação ou extenções (EX .PHP) na URL de retorno"; break;
			case 166: res = "AuthorizeNow is required."; break;
			case 167: res = "Antifraud not configured. Antifraude não vinculado ao cadastro do lojista"; break;
			case 168: res = "Recurrent Payment not found. Recorrencia não encontrada"; break;
			case 169: res = "Recurrent Payment is not active. Recorrencia não está ativa. Execução paralizada"; break;
			case 170: res = "Cartão Protegido not configured. Cartão protegido não vinculado ao cadastro do lojista"; break;
			case 171: res = "Affiliation data not sent. Falha no processamento do pedido - Entre em contato com o suporte Cielo"; break;
			case 172: res = "Credential Code is required. Falha na validação das credenciadas enviadas"; break;
			case 173: res = "Payment method is not enabled. Meio de pagamento não vinculado ao cadastro do lojista"; break;
			case 174: res = "Card Number is required. Campo enviado está vazio ou invalido"; break;
			case 175: res = "EAN is required. Campo enviado está vazio ou invalido"; break;
			case 176: res = "Payment Currency is not supported. Campo enviado está vazio ou invalido"; break;
			case 177: res = "Card Number is invalid. Campo enviado está vazio ou invalido"; break;
			case 178: res = "EAN is invalid. Campo enviado está vazio ou invalido"; break;
			case 179: res = "The max number of installments allowed for recurring payment is 1. Campo enviado está vazio ou invalido"; break;
			case 180: res = "The provided Card PaymentToken was not found. Token do Cartão protegido não encontrado"; break;
			case 181: res = "The MerchantIdJustClick is not configured. Token do Cartão protegido bloqueado"; break;
			case 182: res = "Brand is required. Bandeira do cartão não enviado"; break;
			case 183: res = "Invalid customer bithdate. Data de nascimento invalida ou futura"; break;
			case 184: res = "Request could not be empty. Falha no formado ta requisição. Verifique o código enviado"; break;
			case 185: res = "Brand is not supported by selected provider. Bandeira não suportada pela API Cielo"; break;
			case 186: res = "The selected provider does not support the options provided (Capture, Authenticate, Recurrent or Installments). Meio de pagamento não suporta o comando enviado"; break;
			case 187: res = "ExtraData Collection contains one or more duplicated names."; break;
			case 188: res = "Avs with CPF invalid."; break;
			case 189: res = "Avs with length of street exceeded. Dado enviado excede o tamanho do campo"; break;
			case 190: res = "Avs with length of number/complement exceeded. Dado enviado excede o tamanho do campo"; break;
			case 191: res = "Avs with length of district exceeded. Dado enviado excede o tamanho do campo"; break;
			case 192: res = "Avs with zip code invalid. CEP enviado é invalido"; break;
			case 193: res = "Split Amount must be greater than zero. Valor para realização do SPLIT deve ser superior a 0"; break;
			case 194: res = "Split Establishment is Required. SPLIT não habilitado para o cadastro da loja"; break;
			case 195: res = "The PlataformId is required. Validados de plataformas não enviado"; break;
			case 196: res = "DeliveryAddress is required. Campo obrigatório não enviado"; break;
			case 197: res = "Street is required. Campo obrigatório não enviado"; break;
			case 198: res = "Number is required. Campo obrigatório não enviado"; break;
			case 199: res = "ZipCode is required. Campo obrigatório não enviado"; break;
			case 200: res = "City is required. Campo obrigatório não enviado"; break;
			case 201: res = "State is required. Campo obrigatório não enviado"; break;
			case 202: res = "District is required. Campo obrigatório não enviado"; break;
			case 203: res = "Cart item Name is required. Campo obrigatório não enviado"; break;
			case 204: res = "Cart item Quantity is required. Campo obrigatório não enviado"; break;
			case 205: res = "Cart item type is required. Campo obrigatório não enviado"; break;
			case 206: res = "Cart item name length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 207: res = "Cart item description length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 208: res = "Cart item sku length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 209: res = "Shipping addressee sku length exceeded. Dado enviado excede o tamanho do campo"; break;
			case 210: res = "Shipping data cannot be null. Campo obrigatório não enviado"; break;
			case 211: res = "WalletKey is invalid. Dados da Visa Checkout invalidos"; break;
			case 212: res = "Merchant Wallet Configuration not found. Dado de Wallet enviado não é valido"; break;
			case 213: res = "Credit Card Number is invalid. Cartão de crédito enviado é invalido"; break;
			case 214: res = "Credit Card Holder Must Have Only Letters. Portador do cartão não deve conter caracteres especiais"; break;
			case 215: res = "Agency is required in Boleto Credential. Campo obrigatório não enviado"; break;
			case 216: res = "Customer IP address is invalid. IP bloqueado por questões de segurança"; break;
			case 300: res = "MerchantId was not found"; break;
			case 301: res = "Request IP is not allowed"; break;
			case 302: res = "Sent MerchantOrderId is duplicated"; break;
			case 303: res = "Sent OrderId does not exist"; break;
			case 304: res = "Customer Identity is required"; break;
			case 306: res = "Merchant is blocked"; break;
			case 307: res = "Transaction not found.	Transação não encontrada ou não existente no ambiente."; break;
			case 308: res = "Transaction not available to capture. Transação não pode ser capturada - Entre em contato com o suporte Cielo"; break;
			case 309: res = "Transaction not available to void. Transação não pode ser Cancelada - Entre em contato com o suporte Cielo"; break;
			case 310: res = "Payment method doest not support this operation. Comando enviado não suportado pelo meio de pagamento"; break;
			case 311: res = "Refund is not enabled for this merchant. Cancelamento após 24 horas não liberado para o lojista"; break;
			case 312: res = "Transaction not available to refund. Transação não permite cancelamento após 24 horas"; break;
			case 313: res = "Recurrent Payment not found. Transação recorrente não encontrada ou não disponivel no ambiente"; break;
			case 314: res = "Invalid Integration"; break;
			case 315: res = "Cannot change NextRecurrency with pending payment"; break;
			case 316: res = "Cannot set NextRecurrency to past date. Não é permitido alterada dada da recorrencia para uma data passada"; break;
			case 317: res = "Invalid Recurrency Day"; break;
			case 318: res = "No transaction found"; break;
			case 319: res = "Smart recurrency is not enabled. Recorrencia não vinculada ao cadastro do lojista"; break;
			case 320: res = "Can not Update Affiliation Because this Recurrency not Affiliation saved"; break;
			case 321: res = "Can not set EndDate to before next recurrency."; break;
			case 322: res = "Zero Dollar Auth is not enabled. Zero Dollar não vinculado ao cadastro do lojista"; break;
			case 323: res = "Bin Query is not enabled. Consulta de Bins não vinculada ao cadastro do lojista"; break;	    								 						
			default: res = "Código não identificado";		
		}
						
		return res;		
		
	}

}

package cielo.environment.util;

import java.io.IOException;

import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.CieloEcommerce;
import cieloecommerce.sdk.ecommerce.Environment;
import cieloecommerce.sdk.ecommerce.Payment;
import cieloecommerce.sdk.ecommerce.Sale;
import cieloecommerce.sdk.ecommerce.request.CieloRequestException;

public class TesteJSON {
	
	private static final String TesteMerchantId = "5da83acc-6fd6-48ec-b22a-f7e9b5de8bef";
	private static final String TesteMerchantKey = "ILWIORZCPKQUYZCYNSSJGXPDUAOPCODLGGAOFDGH";
	
	private static final String TesteOrderId = "2018010101";
	

	public static void main(String[] args) {
		
		//Configurando Merchant
		Merchant merchant = new Merchant(TesteMerchantId, TesteMerchantKey);
		
		//Configurando Sale
		Sale sale = new Sale(TesteOrderId);
		
		//Customer custumer = sale.customer("Teste Holder"); //Nome no Cartão	
		sale.customer("Teste Holder"); //Nome no Cartão
		
		Payment payment = sale.payment(15700); //Valor da compra em centavos
		
		payment.creditCard("123", "Visa") //Código de segurança e bandeira
			.setExpirationDate("12/2018") //Data de expiração
			.setCardNumber("0000000000000001") //Número do cartão
			.setHolder("Teste Holder"); //Nome no Cartão
		
		
		// Crie o pagamento na Cielo
		try {
		    // Configure o SDK com seu merchant e o ambiente apropriado para criar a venda
		    sale = new CieloEcommerce(merchant, Environment.SANDBOX).createSale(sale);

		    // Com a venda criada na Cielo, já temos o ID do pagamento, TID e demais
		    // dados retornados pela Cielo
		    String paymentId = sale.getPayment().getPaymentId();
		    
		    System.out.println("Id do pagamento: " + paymentId);

		    /*
		    // Com o ID do pagamento, podemos fazer sua captura, se ela não tiver sido capturada ainda
		    sale = new CieloEcommerce(merchant, Environment.SANDBOX).captureSale(paymentId, 15700, 0);

		    // E também podemos fazer seu cancelamento, se for o caso
		    sale = new CieloEcommerce(merchant, Environment.SANDBOX).cancelSale(paymentId, 15700);
		    */
		    
		    
		} catch (CieloRequestException e) {
		    // Em caso de erros de integração, podemos tratar o erro aqui.
		    // os códigos de erro estão todos disponíveis no manual de integração.
		    //CieloError error = e.getError();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}

				
	}

}

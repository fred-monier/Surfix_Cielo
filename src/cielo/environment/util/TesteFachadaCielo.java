package cielo.environment.util;

import cieloecommerce.sdk.ecommerce.Sale;

public class TesteFachadaCielo {

	public static void main(String[] args) {
		
		FachadaCielo fachada = FachadaCielo.getInstancia();
		
		
		//Pagamento no Cartão de Crédito
		/*
		try {
					
			Payment payment = fachada.gerarPagamentoCreditoAVista(false, "2018010102", 66700, 
					FachadaCielo.BANDEIRA_VISA, "0000000000000001", 
					"12/2018", "Fulano de Tal", "123", "Sistema Orion@");
			
			System.out.println("Operação concluída");			
			System.out.println("Payment: " + payment.toString());
			
			
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/
		
		//Consulta de Venda no Cartão de Crédito por paymentId
		//
		try {
			Sale sale = fachada.consultarVendaCreditoAVistaPorPaymentId(false, "49388f61-c51d-4ebd-8969-db16383339d0");
			
			System.out.println("Operação concluída");			
			System.out.println("Sale: " + sale.toString());
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		//
		
		//Consulta de Vendas por merchantOrderId
		/*
		try {
			Payment[] payments = fachada.consultarVendasPorNumPedidoVirtual(false, "2018010102");
			
			System.out.println("Operação concluída");	
			System.out.println();
			
			for (int i = 0; i < payments.length; i++) {
				System.out.println("PaymentId: " + payments[i].getPaymentId());
				System.out.println("ReceivedDate: " + payments[i].getReceivedDate());
				System.out.println();
			}
					
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}		
		*/
		
		//Cancelamento total de venda no Cartão de Crédito
		/*
		try {
			SaleResponse saleResp = fachada.cancelarPagamentoTotalCreditoAVista(false, "49388f61-c51d-4ebd-8969-db16383339d0");
			
			System.out.println("Operação concluída");	
			System.out.println();
			
			System.out.println("Satatus: " + saleResp.getStatus());
			System.out.println("ReturnCode: " + saleResp.getReturnCode());
			System.out.println("ReturnMessage: " + saleResp.getReturnMessage());
			System.out.println();
			
					
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}		
		*/
		

	}

}

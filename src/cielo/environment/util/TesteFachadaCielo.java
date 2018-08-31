package cielo.environment.util;

public class TesteFachadaCielo {

	public static void main(String[] args) {
		
		//FachadaCielo fachada = FachadaCielo.getInstancia();
		
		//Módulo 1 ******************************************************
		
		//Pagamento no Cartão de Crédito (à vista)
		/*
		try {
					
			Payment payment = fachada.gerarPagamentoCreditoAVista("0001", "2018010102", 66700, 
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
		/*
		try {
			Sale sale = fachada.consultarVendaCreditoAVistaPorPaymentId("0001", "3962d113-cb81-4101-becf-dabb16fbde77");
			
			System.out.println("Operação concluída");			
			System.out.println("Sale: " + sale.toString());
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/
		
		//Consulta de Vendas por merchantOrderId
		/*
		try {
			Payment[] payments = fachada.consultarVendasPorNumPedidoVirtual(false, "2018000001");
			
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
		
		//Módulo 1 ******************************************************
		
		//Módulo 2 ******************************************************
		
		//Pagamento no Cartão de Crédito por Recorrência Programada (à vista)
		/*
		try {
					
			Payment payment = fachada.gerarPagamentoCreditoAVistaRecProg(false, "2018000444", 11100, 
					FachadaCielo.BANDEIRA_VISA, "0000000000000001", 
					"12/2022", "Sicrano de Tal", "456", "Sistema Ennon@", FachadaCielo.RECORRENCIA_MENSAL, "2020-11-10");
			
			System.out.println("Operação concluída");			
			System.out.println("Payment: " + payment.toString());
			
			
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/				
		
		//Pagamento no Cartão de Crédito por Recorrência Programada (agendado)
		/*
		try {
					
			Payment payment = fachada.gerarPagamentoCreditoAgendadoRecProg(false, "2018003321", 55500, 
					FachadaCielo.BANDEIRA_VISA, "0000000000000002", 
					"12/2018", "Fulano de Tal", "123", "Sistema Orion@", "2018-05-30", FachadaCielo.RECORRENCIA_MENSAL, "2019-11-10");
			
			System.out.println("Operação concluída");			
			System.out.println("Payment: " + payment.toString());
			
			
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/								
		
		//Alteração de Data Final de Venda no Cartão de Crédito via recorrência por recurrrentPaymentId	
		/*
		try {
			fachada.alterarVendaCreditoRecProgDataFinalPorRecurrentPaymentId("0000", "6c7d3f18-6a85-4207-948e-510822da3481", "2020-08-15");
			
			System.out.println("Operação concluída");						
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/			
		
		//Alteração do dia de pagamento no Cartão de Crédito via recorrência por recurrrentPaymentId	
		/*
		try {
			fachada.alterarVendaCreditoRecProgDiaRecPorRecurrentPaymentId(false, "b7046d95-11c0-4025-9f5d-71e5102e3dd8", 4);
			
			System.out.println("Operação concluída");						
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/			
		
		//Alteração do valor de pagamento no Cartão de Crédito via recorrência por recurrrentPaymentId	
		/*
		try {
			fachada.alterarVendaCreditoRecProgValorRecPorRecurrentPaymentId(false, "b7046d95-11c0-4025-9f5d-71e5102e3dd8", 777888);
			
			System.out.println("Operação concluída");						
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/		
		
		//Alteração da data do próximo pagamento de um pagamento no Cartão de Crédito via recorrência por recurrrentPaymentId	
		/*
		try {
			fachada.alterarVendaCreditoRecProgDataProxRecPorRecurrentPaymentId(false, "682c3327-b9ce-41c3-a501-627b12e5d739", "2018-06-01");
			
			System.out.println("Operação concluída");						
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/		
		
		//Alteração do intervalo de recorrência de pagamento no Cartão de Crédito via recorrência por recurrrentPaymentId	
		/*
		try {
			fachada.alterarVendaCreditoRecProgIntervaloPorRecurrentPaymentId(false, "b7046d95-11c0-4025-9f5d-71e5102e3dd8", FachadaCielo.RECORRENCIA_BIMESTRAL);
			
			System.out.println();
			System.out.println("Operação concluída");						
		
		} catch (FachadaCieloException e) {
			
			System.out.println();
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/			
		
		//Desabilita um pagamento no Cartão de Crédito via recorrência por recurrrentPaymentId	
		/*
		try {
			fachada.desabilitarVendaCreditoRecProgPorRecurrentPaymentId(false, "682c3327-b9ce-41c3-a501-627b12e5d739");
			
			System.out.println("Operação concluída");						
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/				
		
		//Reabilita um pagamento no Cartão de Crédito via recorrência por recurrrentPaymentId	
		/*
		try {
			fachada.reabilitarVendaCreditoRecProgPorRecurrentPaymentId(false, "682c3327-b9ce-41c3-a501-627b12e5d739");
			
			System.out.println("Operação concluída");						
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/	
		
		//Alteração de dados de pagamento recorrente no Cartão de Crédito por recurrrentPaymentId
		/*
		try {
		
			fachada.alterarPagamentoCreditoRecProgPorRecurrentPaymentId(false, "b7046d95-11c0-4025-9f5d-71e5102e3dd8",
					99900, FachadaCielo.BANDEIRA_VISA, "1111111111111111", "12/2030", "John Lendo", "789", "Sistema Secreto");
			
			System.out.println("Operação concluída");			
		
		} catch (FachadaCieloException e) {
			
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/			
		
		//Consulta de Venda no Cartão de Crédito via recorrência por recurrrentPaymentId
		/*
		try {
			RecurrentSale recSale = fachada.consultarVendaCreditoRecProgPorRecurrentPaymentId(false, "85b817bf-495b-4e6c-b2cc-53c836dc9c94");
			
			System.out.println();
			System.out.println("Operação concluída");			
			System.out.println("Sale: " + recSale.toString());
		
		} catch (FachadaCieloException e) {
			
			System.out.println();
			System.out.println("Operação NÃO concluída: " + e.getMensagem());
			
			if (e.getExcecaoOriginal() != null) {
				e.getExcecaoOriginal().printStackTrace();
			}							
		}
		*/					
		
		//Módulo 2 ******************************************************		
				
	}
	
	

}

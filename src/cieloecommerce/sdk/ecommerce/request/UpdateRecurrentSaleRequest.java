//MONIER - CIELO
package cieloecommerce.sdk.ecommerce.request;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import com.google.gson.GsonBuilder;

import cieloecommerce.sdk.Environment;
import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.Payment;
import cieloecommerce.sdk.ecommerce.RecurrentSale;

public class UpdateRecurrentSaleRequest extends AbstractSaleRequest<Payment, RecurrentSale> {
	
	private final String recurrentPaymentId;
	
	public UpdateRecurrentSaleRequest(String recurrentPaymentId, Merchant merchant, Environment environment) {
		super(merchant, environment);
		
		this.recurrentPaymentId  = recurrentPaymentId;
	}

	@Override
	public RecurrentSale execute(Payment param) throws IOException, CieloRequestException {
				
		String url = environment.getApiUrl() + "1/RecurrentPayment/" + recurrentPaymentId + "/Payment";
        
        HttpPut request = new HttpPut(url);		

		request.setEntity(new StringEntity(new GsonBuilder().create().toJson(param)));

		HttpResponse response = sendRequest(request);

		return readResponse(response, RecurrentSale.class);
	}
}

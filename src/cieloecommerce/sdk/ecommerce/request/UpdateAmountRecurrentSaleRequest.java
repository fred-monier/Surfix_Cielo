//MONIER - CIELO
package cieloecommerce.sdk.ecommerce.request;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import cieloecommerce.sdk.Environment;
import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.RecurrentSale;

public class UpdateAmountRecurrentSaleRequest extends AbstractSaleRequest<String, RecurrentSale> {
	
	private final int amount;
	
	public UpdateAmountRecurrentSaleRequest(int amount, Merchant merchant, Environment environment) {
        super(merchant, environment);
        
        this.amount = amount;
    }
	
    @Override
    public RecurrentSale execute(String recurrentPaymentId) throws IOException, CieloRequestException {
        String url = environment.getApiUrl() + "1/RecurrentPayment/" + recurrentPaymentId + "/Amount";
                       
        HttpPut request = new HttpPut(url);
        
        //
        if (amount > 0) {
        	request.setEntity(new StringEntity(amount + ""));
        }
        //
                
        HttpResponse response = sendRequest(request);

        return readResponse(response, RecurrentSale.class);
    }

}

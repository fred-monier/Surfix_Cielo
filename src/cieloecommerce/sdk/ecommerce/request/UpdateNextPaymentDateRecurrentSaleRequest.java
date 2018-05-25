//MONIER - CIELO
package cieloecommerce.sdk.ecommerce.request;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import cieloecommerce.sdk.Environment;
import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.RecurrentSale;

public class UpdateNextPaymentDateRecurrentSaleRequest extends AbstractSaleRequest<String, RecurrentSale> {
	
	private final String nextPaymentDate;
	
	public UpdateNextPaymentDateRecurrentSaleRequest(String nextPaymentDate, Merchant merchant, Environment environment) {
        super(merchant, environment);
        
        this.nextPaymentDate = nextPaymentDate;
    }
	
    @Override
    public RecurrentSale execute(String recurrentPaymentId) throws IOException, CieloRequestException {
        String url = environment.getApiUrl() + "1/RecurrentPayment/" + recurrentPaymentId + "/NextPaymentDate";
                       
        HttpPut request = new HttpPut(url);
        
        //
        if (nextPaymentDate != null) {
        	request.setEntity(new StringEntity("\"" + nextPaymentDate + "\""));
        }
        //
                
        HttpResponse response = sendRequest(request);

        return readResponse(response, RecurrentSale.class);
    }

}

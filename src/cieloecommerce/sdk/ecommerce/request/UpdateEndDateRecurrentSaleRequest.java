//MONIER - CIELO
package cieloecommerce.sdk.ecommerce.request;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import cieloecommerce.sdk.Environment;
import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.RecurrentSale;

public class UpdateEndDateRecurrentSaleRequest extends AbstractSaleRequest<String, RecurrentSale> {
	
	private final String endDate;
	
	public UpdateEndDateRecurrentSaleRequest(String endDate, Merchant merchant, Environment environment) {
        super(merchant, environment);
        
        this.endDate = endDate;
    }
	
    @Override
    public RecurrentSale execute(String recurrentPaymentId) throws IOException, CieloRequestException {
        String url = environment.getApiUrl() + "1/RecurrentPayment/" + recurrentPaymentId + "/EndDate";
                       
        HttpPut request = new HttpPut(url);
        
        //
        if (endDate != null) {
        	request.setEntity(new StringEntity("\"" + endDate + "\""));
        }
        //
                
        HttpResponse response = sendRequest(request);

        return readResponse(response, RecurrentSale.class);
    }

}

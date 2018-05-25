//MONIER - CIELO
package cieloecommerce.sdk.ecommerce.request;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

import cieloecommerce.sdk.Environment;
import cieloecommerce.sdk.Merchant;
import cieloecommerce.sdk.ecommerce.RecurrentSale;

public class UpdateRecurrencyDayRecurrentSaleRequest extends AbstractSaleRequest<String, RecurrentSale> {
	
	private final int recurrencyDay;
	
	public UpdateRecurrencyDayRecurrentSaleRequest(int recurrencyDay, Merchant merchant, Environment environment) {
        super(merchant, environment);
        
        this.recurrencyDay = recurrencyDay;
    }
	
    @Override
    public RecurrentSale execute(String recurrentPaymentId) throws IOException, CieloRequestException {
        String url = environment.getApiUrl() + "1/RecurrentPayment/" + recurrentPaymentId + "/RecurrencyDay";
                       
        HttpPut request = new HttpPut(url);
        
        //
        if (recurrencyDay > 0 && recurrencyDay <= 31) {
        	request.setEntity(new StringEntity(recurrencyDay + ""));
        }
        //
                
        HttpResponse response = sendRequest(request);

        return readResponse(response, RecurrentSale.class);
    }

}

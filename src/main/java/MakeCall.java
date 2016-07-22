import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jonathan Chiou on 7/22/2016.
 */
public class MakeCall {
    public static void makeCall(HttpServletRequest req, HttpServletResponse resp, Connection connection, JSONObject jsonObject)
            throws IOException, TwilioRestException, JSONException {
        String receiverNumber = jsonObject.getString(Constants.RECEIVER_NUMBER_KEY);
        TwilioRestClient client = new TwilioRestClient(Constants.ACCOUNT_SID, Constants.AUTH_TOKEN);
        Account account = client.getAccount();
        CallFactory callFactory = account.getCallFactory();
        Map<String, String> callParams = new HashMap<>();
        callParams.put(Constants.TO, receiverNumber);
        callParams.put(Constants.FROM, Constants.FROM_NUMBER);
        callParams.put(Constants.URL, "http://demo.twilio.com/welcome/voice/");
        Call call = callFactory.create(callParams);
    }
}

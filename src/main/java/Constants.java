/**
 * Created by jman0_000 on 7/18/2016.
 */
public class Constants {
    public static final String ACCOUNT_SID = "AC181994597bdbbc2f83c85fe1e8cc287a";
    public static final String AUTH_TOKEN = "b52d5d02553af74de5945d78c83f905c";

    public static final String TO = "To";
    public static final String FROM = "From";
    public static final String BODY = "Body";
    public static final String URL = "Url";
    public static final String GET = "Get";
    public static final String METHOD = "Method";
    public static final String FROM_NUMBER = "6502851269";

    //for the JSON
    public static final String RECEIVER_NUMBER_KEY = "number";

    //creating account JSON
    public static final String PHONE_NUMBER = "phone_number";
    public static final String PASSWORD = "password";

    //login
    public static final String ID = "id";

    //activation
    public static final String KEY = "key";

    //confirmation msg strings
    public static final String MSG = "Your verification number is \n";

    //ResultSet Strings
    public static final String ACCOUNT__ID = "account__id";
    public static final String ACCOUNT__PHONE_NUMBER = "account__phone_number";
    public static final String ACCOUNT__ACTIVE = "account__active";
    public static final String ACCOUNT__LAST_CALL = "account__last_call";
    public static final String ACCOUNT__DAILY_CALL_CNTR = "account__daily_call_cntr";
    public static final String ACCOUNT__SUBBED = "account__subbed";
    public static final String TRIAL_CALL__LAST_CALL = "trial_call__last_call";
    public static final String TRIAL_CALL__DAILY_CALL_CNTR = "trial_call__daily_call_cntr";
    public static final String HISTORY__FROM = "history__from";
    public static final String HISTORY__TO = "history__to";
    public static final String HISTORY__TIMESTAMP = "history__timestamp";
    public static final String TOTAL = "total";

    //HISTORY MODE
    public static final String LOAD_MODE = "load";
    public static final String REFRESH_MODE = "refresh";

    //ERROR MESSAGES
    public static final String INVALID_CREDENTIALS = "Invalid credentials.";
    public static final String EXCEEDED_CALL_LIMIT = "User has exceeded daily call limit.";
    public static final String TWILIO_ERROR = "Error relating to Twilio.";

    public static final int BAD_REQUEST = 400;
    public static final int UNATHORIZED = 401;
    public static final int NOT_FOUND = 404;
    public static final int INTERNAL_SERVER_ERROR = 500;
}

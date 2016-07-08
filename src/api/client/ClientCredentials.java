package api.client;
/**
 * Created by Dewes on 08/07/2016.
 */

class ClientCredentials {
    static final String API_KEY =
            ""
                    + ClientCredentials.class;

    static void errorIfNotSpecified() {
        if (API_KEY.startsWith("Enter ")) {
            System.err.println(API_KEY);
            System.exit(1);
        }
    }
}


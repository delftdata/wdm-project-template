import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class localhostTestCheckout {
    public static void main(String[] args) throws IOException {
        String[] reqs = { "http://localhost:5001/create_user", "http://localhost:5000/create/1", "http://localhost:5002/item/create/1",
                "http://localhost:5002/add/1/7", "http://localhost:5001/add_funds/1/42", "http://localhost:5000/addItem/1/1",
                "http://localhost:5000/checkout/1"};

        for (String u : reqs) {
            URL url = new URL(u);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            System.out.println(con.getResponseCode());
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            System.out.println(content);
        }
    }
}

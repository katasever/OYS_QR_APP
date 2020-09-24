package atasever.oys_qr_app.Activity;

import androidx.appcompat.app.AppCompatActivity;
import atasever.oys_qr_app.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ConcurrentModificationException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    public static String QRCode= "";
    public static String urlPayment= "";
    EditText edt_odemeMiktar;
    Button btn_qr_code;
    Button btn_qr_code_exp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this); //tanım kısaltma için kütüphane

        btn_qr_code = (Button)findViewById(R.id.btn_qr_code);
        edt_odemeMiktar = (EditText) findViewById(R.id.edt_odemeMiktar);
        btn_qr_code_exp = (Button) findViewById(R.id.btn_qr_code_exp);

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");

            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        btn_qr_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanQRCode();
            }
        });
        btn_qr_code_exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCode = "00020153039495403500800201810200821926-12-2017 18:54:558305312-5" + //örnek qr girişi
                        "8608800-500#8712EC0000000001890118402648844secureqrsigniturewillbehereinthenearfuture1=";
                new PaymentAsync().execute();
            }
        });

    }


    public void ScanQRCode(){
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("...");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                QRCode = result.getContents();
                new PaymentAsync().execute();
                Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Toast.makeText(getApplicationContext(), result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class PaymentAsync extends AsyncTask<Void, Void, Void>{
    Exception errorException = null;
    final ProgressDialog progress = new ProgressDialog(MainActivity.this);

    @Override
    protected void onPreExecute() {
        errorException = null;
        progress.setMessage("Loading...");
        progress.show();
        progress.setCancelable(false);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String urlStr = "";

        HttpsURLConnection urlConn;
        BufferedReader bufferedReader;

        urlStr = "https://sandbox-api.payosy.com/api/payment";

        try {

            URL url = new URL(urlStr);
            urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setRequestProperty("Content-type", "application/json");
            urlConn.setRequestProperty("accept", "application/json");
            urlConn.setRequestProperty("x-ibm-client-id", "d56a0277-2ee3-4ae5-97c8-467abeda984d");
            urlConn.setRequestProperty("x-ibm-client-secret", "U1yT3qD2jW6oO4uH8gB8bN1xW0xH3aL7jN2lT7dP5aL5rQ1vK4 ");
            urlConn.setRequestMethod("POST");
            urlConn.setInstanceFollowRedirects(false);
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);

            JSONObject postDataParams = new JSONObject();
            postDataParams.put("returnCode",100l);
            postDataParams.put("returnDesc","success");
            postDataParams.put("receiptMsgCustomer","beko Campaign");
            postDataParams.put("receiptMsgMerchant","beko Campaign Merchant");
            JSONArray paymentInfoList = new JSONArray();

            JSONObject jsonObjectInfoList = new JSONObject();
            jsonObjectInfoList.put("paymentProcessorID",67l);
            JSONArray paymentActionList = new JSONArray();
            JSONObject jsonObjectPaymentList = new JSONObject();
            jsonObjectPaymentList.put("paymentType",3l);
            jsonObjectPaymentList.put("amount",Long.valueOf(edt_odemeMiktar.getText().toString()));
            jsonObjectPaymentList.put("currencyID",949l);
            jsonObjectPaymentList.put("vatRate",800l);
            paymentActionList.put(jsonObjectPaymentList);
            jsonObjectInfoList.put("paymentActionList",paymentActionList);

            paymentInfoList.put(jsonObjectInfoList);
            postDataParams.put("paymentInfoList",paymentInfoList);
            postDataParams.put("QRdata",QRCode);

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8"));
            bw.write(postDataParams.toString());
            bw.flush();

            bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "UTF-8"));
            StringBuffer stringBuffer = new StringBuffer();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }
            if(stringBuffer.toString().length()>0){

                JSONObject jsonObjects = new JSONObject(stringBuffer.toString());

                if (jsonObjects.has("exception")) {
                    JSONObject exception = jsonObjects.getJSONObject("exception");
                    errorException = new ConcurrentModificationException(exception.get("message").toString());
                }
            }

        } catch (Exception e) {
            errorException = e;
        }
        return null;
    }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();

            if(errorException == null){
                Toast.makeText(getApplicationContext(),"Success!",Toast.LENGTH_LONG);

                }else {

                Toast.makeText(getApplicationContext(),errorException.getMessage(),Toast.LENGTH_LONG);
            }
        }
}

}

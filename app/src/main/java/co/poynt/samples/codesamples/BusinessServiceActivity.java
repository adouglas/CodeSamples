package co.poynt.samples.codesamples;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import co.poynt.api.model.Business;
import co.poynt.api.model.Phone;
import co.poynt.api.model.Store;
import co.poynt.os.model.PoyntError;
import co.poynt.os.services.v1.IPoyntBusinessReadListener;
import co.poynt.os.services.v1.IPoyntBusinessService;

public class BusinessServiceActivity extends Activity {
    private static final String TAG = BusinessServiceActivity.class.getSimpleName();
    
    private TextView console;
    private Button getBizInfoBtn;
    
    private IPoyntBusinessService businessService;
    private ServiceConnection bizServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            businessService = IPoyntBusinessService.Stub.asInterface(service);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getBizInfoBtn.setEnabled(true);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    private IPoyntBusinessReadListener bizListener = new IPoyntBusinessReadListener.Stub() {
        @Override
        public void onResponse(Business business, PoyntError poyntError) throws RemoteException {
/*            Store Name *
            Business Type *
            Your First Name *
                    Last Name *
                    Country *
                    Email *
                    Phone **/
            String businessName = business.getLegalName();
            String mcc = business.getMcc();
            List<Store> stores = business.getStores();
            String storeName = null;
            //only display 1st store
            if (!stores.isEmpty()){
                storeName = stores.get(0).getDisplayName();
            }
            Map<String,String> attributes = business.getAttributes();
            String firstName = attributes.get("merchantContactFirstName");
            String lastName = attributes.get("merchantContactLastName");

            String countryCode = business.getAddress().getCountryCode();

            String email = business.getEmailAddress();
            Phone p = business.getPhone();
            String phone = p.getAreaCode() + p.getLocalPhoneNumber();

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Business: %s\n",businessName));
            sb.append(String.format("Store: %s\n",storeName));
            sb.append(String.format("MCC: %s\n",mcc));
            sb.append(String.format("First Name: %s\n",firstName));
            sb.append(String.format("Last Name: %s\n",lastName));
           // sb.append(String.format("Attributes: %s\n",attributes.toString()));
            sb.append(String.format("Email: %s\n",email));
            sb.append(String.format("Country code: %s\n",countryCode));
            sb.append(String.format("Phone: %s\n",phone));
            final String output = sb.toString();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    console.setText(output);
                }
            });
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_service);
        console = (TextView) findViewById(R.id.consoleText);
        getBizInfoBtn = (Button) findViewById(R.id.getBizInfoBtn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(IPoyntBusinessService.class.getName()),
                bizServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(bizServiceConnection);
    }

    /*
     * executed when getBizInfoBtn is pressed
     */
    public void getBusinessInfo(View view){
        //TODO call business service
        try{
            businessService.getBusiness(bizListener);
        } catch (RemoteException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

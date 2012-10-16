package @PackageName@;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson.JacksonFactory;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.appspot.api.services.helloEndpoint.HelloEndpoint;
import com.appspot.api.services.helloEndpoint.HelloEndpoint.Builder;
import @PackageName@.R;

import java.io.IOException;
import java.util.ArrayList;

public class HelloActivity extends Activity {
  private static final int REQUEST_ACCOUNT_PICKER = 0;
  private static final String PREF_ACCOUNT_NAME = "accountName";
  private HelloEndpoint endpoint;
  private Context mContext;
  
  public HelloActivity() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    mContext = getApplicationContext(); 
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ping);
  }

  /**
   * On resume, if no account is found start the account add intent.
   * Else use previously selected account for configuring HelloEndpoint and
   * DeviceInfoEndpoint.
   */
  @Override
  protected void onResume() {
    super.onResume();
    String defaultAccount = getGoogleAccount();
    if (defaultAccount == null) {
      startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
    } else {
      SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
      String account = settings.getString(PREF_ACCOUNT_NAME, null);
      if (account == null) {
        account = defaultAccount;
      }
      configureGcmAndEndpoints(account);
      configureButton();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_ping, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_accounts:
        chooseAccount();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * When a new account in selected, reconfigure the endpoints with the new account.
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_ACCOUNT_PICKER:
        if (data != null && data.getExtras() != null) {
          String account = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
          if (account != null) {
            configureGcmAndEndpoints(account);
          }
        }
      break;
    }  
  }

  void chooseAccount() {
    GoogleAccountCredential credential = GoogleAccountCredential
      .usingAudience(mContext, Ids.AUDIENCE);
    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
  }

  /**
   * Configures the endpoints with the new account.
   * 
   * @param account the currently selected account.
   */
  void configureGcmAndEndpoints(String account) {
    SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(PREF_ACCOUNT_NAME, account);
    editor.commit();
    TextView textView = (TextView) findViewById(R.id.textView);
    textView.setText("Logged in as " + account +"\nClick \"Say Hello\" to send a hello " +
      "to all your registered devices.");
    GoogleAccountCredential credential = GoogleAccountCredential
      .usingAudience(mContext, Ids.AUDIENCE);
    credential.setAccountName(account);
    GCMIntentService.register(getApplicationContext(), account);
    Builder endpointBuilder = new HelloEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
        new JacksonFactory(), credential);
    endpoint = CloudEndpointUtils.updateBuilder(endpointBuilder).build();
  }

  /**
   * Used for detecting if the device already has accounts and also 
   * getting the first account.
   */
  private String getGoogleAccount() {
    ArrayList<String> result = new ArrayList<String>();
    Account[] accounts = AccountManager.get(getApplicationContext()).getAccounts();
    for (Account account : accounts) {
      if (account.type.equals("com.google")) {
        return account.name;
      }
    }
    return null;
  }

  private void configureButton() {
    Button button = (Button) findViewById(R.id.sayHelloButton);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new SendHelloTask().execute("Android Device");
      }
    });
  }

  /**
   * Sends hello to all registered devices by useing HelloEndpoint.
   */
  private class SendHelloTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
      try {
        endpoint.sendHello(params[0]).execute();
      } catch (IOException e) {
        if (e.getCause() instanceof UserRecoverableAuthException) {
          UserRecoverableAuthException re = (UserRecoverableAuthException) e.getCause();
          mContext.startActivity(re.getIntent().setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
      }
      return null;
    }
  }
}

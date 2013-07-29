package hr.koris.yamba;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

public class StatusActivity extends BaseActivity implements
        View.OnClickListener,
        TextWatcher,
        LocationListener {
    // Dijagnostika.
    private static final String TAG = StatusActivity.class.getSimpleName();
    // GUI varijable.
    EditText txtStatus;
    Button btnUpdate;
    TextView lblCount;
    // Lokacijske varijable.
    LocationManager locationManager;
    Location location;
    String locationProvider;
    private static final long LOCATION_MIN_TIME = 3600000; // Jedna vura.
    private static final long LOCATION_MIN_DISTANCE = 1000; // metri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        txtStatus = (EditText)findViewById(R.id.txtStatus);
        txtStatus.addTextChangedListener(this);
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(this);
        lblCount = (TextView)findViewById(R.id.lblCount);
        lblCount.setText(Integer.toString(140));
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationProvider = app.getLocationProvider(); // Treba za drugdje!
        if (!YambaApplication.LOCATION_PROVIDER_NONE.equals(locationProvider)) {
            locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        }
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(locationProvider);
            locationManager.requestLocationUpdates(locationProvider, LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onClick(View view) {
        // Ne treba zatvarati u try-catch block jer je to učinjeno u asinkronoj zadaći,
        // skupa s obradom greške.
        if (txtStatus.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.msgEnterStatus, Toast.LENGTH_SHORT).show();
            return;
        }
        // inače...
        new PostToTwitter().execute(txtStatus.getText().toString());
        Log.d(TAG, "onClick");
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        // MORAM premostiti.
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        // MORAM premostiti.
    }

    @Override
    public void afterTextChanged(Editable editable) {
        int count = 140 - txtStatus.length();
        lblCount.setText(Integer.toString(count));
        if (count < 10)
            lblCount.setTextColor(R.color.Yellow);
        if (count < 0)
            lblCount.setTextColor(R.color.FireBrick);
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        location = newLocation;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // Ne treba nam ovdje, ali moramo premostiti.
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (locationProvider.equals(provider)) {
            locationManager.requestLocationUpdates(locationProvider, LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, this);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Treba štedjeti bateriju.
        if (locationProvider.equals(provider)) {
            locationManager.removeUpdates(this);
        }
    }

    class PostToTwitter extends AsyncTask<String, Integer, String> {
        String result;
        @Override // Ovo se obavlja u posebnoj dretvi.
        protected String doInBackground(String... statusi) {
            try {
                Twitter twitter = ((YambaApplication)getApplication()).getTwitter();
                if (twitter == null) {
                    result = getText(R.string.msgUsernamePassword).toString();
                    return result;
                }
                // inače...
                if (location != null) {
                    twitter.setMyLocation(new double[]{location.getLatitude(), location.getLongitude()});
                }
                Twitter.Status status = twitter.updateStatus(statusi[0]);
                result = status.text;
            } catch (TwitterException ex) {
                Log.e(TAG, "ERROR: Status posting failed.", ex);
                result = "Status posting failed. " + ex.getLocalizedMessage();
            }
            return result;
        }

        @Override // Ovo se obavlja u GUI dretvi. Zato mogu koristiti DialogBox AKA Toast.
        protected void onPostExecute(String result) {
            Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
            txtStatus.setText(null);
        }
    }
}

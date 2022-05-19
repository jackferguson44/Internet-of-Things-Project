package uk.ac.tees.t7099806.iotbottomnavigation;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {


    private Spinner spinner;
    private String spinValue;

    private EditText foodAmount, feedTime, southWestlat, southWestLng, northEastLat, northEastLng;
    private Button save;

    private String topicCamera;

    String clientId;
    MqttAndroidClient client;
    private static String MQTTHOST = "";
    private static String USERNAME = "ferg";
    private static String PASSWORD = "pass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        spinner = (Spinner) findViewById(R.id.cameraOnSpin);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.spinner_list));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);
        spinner.setOnItemSelectedListener(this);

        foodAmount = findViewById(R.id.foodAmount);
        feedTime = findViewById(R.id.feedTime);
        southWestlat = findViewById(R.id.southWestBoundsLat);
        southWestLng = findViewById(R.id.southWestBoundsLng);
        northEastLat = findViewById(R.id.northEastBoundslat);
        northEastLng = findViewById(R.id.northEastBoundslng);

        spinValue = "On";

        save = findViewById(R.id.SaveButton);
        save.setOnClickListener(this);

        clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(SettingsActivity.this, "tcp://broker.hivemq.com:1883",
                        clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(SettingsActivity.this, "we are connected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(SettingsActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getItemAtPosition(position).equals("ON"))
        {
            spinValue = "ON";

        }
        else
        {
            spinValue = "OFF";
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {

        // if value hasn't changed don't publish it, if no value don't publish 
        if(v == save)
        {
            publish("/petprotector/camera_actuator", spinValue);


            publish("/petprotector/feeder_actuator/feeding_times", feedTime.getText().toString());
            if(!foodAmount.getText().toString().equals(""))
            {
                publish("/petprotector/feeder_actuator/meal_size", foodAmount.getText().toString());
            }
            if(!feedTime.getText().toString().equals(""))
            {
               // publish("/petprotector/feeder_actuator/feeding_times", feedTime.getText().toString());
            }




            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    private void publish(String topic, String message)
    {
        byte[] encodedPayload = new byte[0];
        try {
            client.publish(topic, message.getBytes(), 0, false);
        } catch (MqttException e){
            e.printStackTrace();
        }
    }
}
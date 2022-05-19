package uk.ac.tees.t7099806.iotbottomnavigation.ui.Information;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import uk.ac.tees.t7099806.iotbottomnavigation.MainActivity;
import uk.ac.tees.t7099806.iotbottomnavigation.R;
import uk.ac.tees.t7099806.iotbottomnavigation.SettingsActivity;
import uk.ac.tees.t7099806.iotbottomnavigation.SettingsDialog;

public class InformationFragment extends Fragment implements View.OnClickListener, SettingsDialog.OnInputSelected{

    private static  final String TAG = "InformationFragment";

    Button settingsBtn, releaseBtn;

    String clientId;
    MqttAndroidClient client;
    private String USERNAME = "ferg";
    private String PASSWORD = "pass";

    MqttConnectOptions options;

    TextView cameraOn, feedWeight, feedAmount, feedingTimes, petTemp, collarBattery, micOn, speakerO, motion;
    public String cameraO, foodAmount, feedTime, speakerOn, microphoneOn;
    TextView  notif;

    String store;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_information, container, false);

        settingsBtn = root.findViewById(R.id.buttonChangeSettings);
        settingsBtn.setOnClickListener(this);

        releaseBtn = root.findViewById(R.id.buttonReleaseFood);
        releaseBtn.setOnClickListener(this);

        clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(getContext(), "tcp://broker.hivemq.com:1883",
                        clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        cameraOn =  root.findViewById(R.id.cameraOn);
        feedAmount = root.findViewById(R.id.feedAmount);
        feedingTimes = root.findViewById(R.id.feedingTimes);
        petTemp = root.findViewById(R.id.petTemp);
        collarBattery = root.findViewById(R.id.collarBattery);
        feedWeight = root.findViewById(R.id.feederWeight);
        micOn = root.findViewById(R.id.micOn);
        speakerO = root.findViewById(R.id.speakerO);
        motion = root.findViewById(R.id.motionS);

        notif = root.findViewById(R.id.NotificationsRec);

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getContext(), "Failure", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        Toast.makeText(getContext(), store, Toast.LENGTH_SHORT).show();

        return root;
    }

    @Override
    public void onClick(View v) {
        if(v == settingsBtn)
        {
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.setTargetFragment(InformationFragment.this, 1);
            settingsDialog.show(getFragmentManager(),"SettingsDialog");
        }
        if(v == releaseBtn)
        {
            Date date = Calendar.getInstance().getTime();
            DateFormat format = new SimpleDateFormat("HHmm");
            String strDate = format.format(date);

            String firstHalf = strDate.substring(0, strDate.length() - 2);
            String secondHalf = strDate.substring(2);
            String full = firstHalf + ":" + secondHalf;

            publish("/petprotector/feeder_actuator/feeding_times", full);
            publish("/petprotector/feeder_actuator/meal_size", "620");
        }
    }



    private void subscribe()
    {
        try{
            if(client.isConnected())
            {
                client.subscribe("/petprotector/camera_actuator/data", 0);
                client.subscribe("/petprotector/microphone_actuator/data", 0);
                client.subscribe("/petprotector/speaker_actuator/data", 0);
                client.subscribe("/petprotector/feeder_actuator/feeding_times", 0);
                client.subscribe("/petprotector/feeder_actuator/meal_size", 0);
                client.subscribe("/petprotector/feeder_actuator/data", 0);
                client.subscribe("/petprotector/collar_battery_sensor", 0);
//                client.subscribe("/petprotector/gps_sensor", 0);
                client.subscribe("/petprotector/motion_sensor",0);
                client.subscribe("/petprotector/temperature_sensor", 0);
                client.subscribe("/petprotector/food_weight_sensor", 0);
                client.subscribe("/petprotector/user_notification", 0);


                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        if(topic.equals("/petprotector/camera_actuator/data"))
                        {
                            cameraOn.setText(message.toString());
                            System.out.println("camera: " + message.toString());
                            notif.setText(notif.getText().toString() + "\n" + message.toString());;
                        }
                        else if(topic.equals("/petprotector/feeder_actuator/data"))
                        {
                            System.out.println(message.toString());
                            if(message.toString().contains("TIMER"))
                            {
                                String time = message.toString();
                                time = time.substring(13);
                                time = time.substring(0, time.length() - 2);
                                feedingTimes.setText(time);
                                System.out.println( "Timer: " +message);
                                System.out.println("TIME= " + time);

                                String formN = message.toString();
                                formN = formN.substring(1);
                                formN = formN.substring(0, formN.length() - 9);

                                notif.setText(notif.getText().toString() + "\n" + formN);
                            }
                            if(message.toString().contains("WEIGHT"))
                            {
                                String weight = message.toString();
                                weight = weight.substring(14);
                                weight = weight.substring(0, weight.length() - 2);
                                feedAmount.setText(weight);
                                System.out.println( "Weight: " +message);


                                String formN = message.toString();
                                formN = formN.substring(1);
                                formN = formN.substring(0, formN.length() - 7);

                                notif.setText(notif.getText().toString() + "\n" + formN);;
                            }

                        }
                        else if(topic.equals("/petprotector/temperature_sensor"))
                        {
                            petTemp.setText(message.toString());
                        }
                        else if(topic.equals("/petprotector/collar_battery_sensor"))
                        {
                            collarBattery.setText(message.toString());
                        }
//                        else if(topic.equals("/petprotector/gps_sensor"))
//                        {
//                            notif.setText(notif.getText().toString() + "\n" + message.toString());
//                        }
                        else if(topic.equals("/petprotector/temperature_sensor"))
                        {
                            notif.setText(notif.getText().toString() + "/n" + message.toString());
                        }
                        else if(topic.equals("/petprotector/food_weight_sensor"))
                        {
                            feedWeight.setText(message.toString());
                        }
                        else if(topic.equals("/petprotector/speaker_actuator/data"))
                        {
                            speakerO.setText(message.toString());
                        }
                        else if(topic.equals("/petprotector/microphone_actuator/data"))
                        {
                            micOn.setText(message.toString());
                        }
                        else if(topic.equals("/petprotector/user_notification"))
                        {
                            notif.setText(notif.getText().toString() + "\n" + message.toString());
                            System.out.println("Notification: " + message.toString());
                        }
                        else if(topic.equals("/petprotector/motion_sensor"))
                        {
                            motion.setText(message.toString());
                        }

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
            }
        } catch (Exception e){
            Log.d("tag", "Error: " + e);
        }
    }



    @Override
    public void sendInput(String c, String f, String fT, String s, String m) {
        cameraO = c;
        foodAmount = f;
        feedTime = fT;
        speakerOn = s;
        microphoneOn= m;

        publish("/petprotector/camera_actuator", cameraO);
        publish("/petprotector/feeder_actuator/meal_size", foodAmount);



        String firstHalf = feedTime.substring(0, feedTime.length() - 3);
        String secondHalf = feedTime.substring(2);
        String full = firstHalf + secondHalf;
        publish("/petprotector/feeder_actuator/feeding_times", full);


        publish("/petprotector/speaker_actuator", speakerOn);
        publish("/petprotector/microphone_actuator", microphoneOn);
        System.out.println("Feed time: " + feedTime);

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
package uk.ac.tees.t7099806.iotbottomnavigation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import uk.ac.tees.t7099806.iotbottomnavigation.ui.Information.InformationFragment;

public class SettingsDialog extends DialogFragment {

    private static final String TAG = "MyCustomDialog";

    public interface OnInputSelected{
        void sendInput(String c, String f, String fT, String s, String m);
    }

    public  OnInputSelected mOnInputSelected;

    private EditText cameraOn, foodAmount, feedTime, speakerOn, microphoneOn;


    private TextView mActionOk, mActionCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_dialog, container, false);

        mActionOk = view.findViewById(R.id.action_ok);
        mActionCancel = view.findViewById(R.id.action_cancel);
        cameraOn = view.findViewById(R.id.cameraOn);
        foodAmount = view.findViewById(R.id.FoodAmountG);
        feedTime = view.findViewById(R.id.FeedTimeG);
        speakerOn = view.findViewById(R.id.speakerOn);
        microphoneOn = view.findViewById(R.id.MicrophoneOn);



        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();
            }


        });

        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ONCLICK: cpaturing input.");

                String camera = cameraOn.getText().toString();
                String food = foodAmount.getText().toString();
                String feed = feedTime.getText().toString();
                String speaker = speakerOn.getText().toString();
                String mic = microphoneOn.getText().toString();
                if(!camera.equals("") && !food.equals("") && !feed.equals("") && !speaker.equals("") && !mic.equals(""))
                {
                    mOnInputSelected.sendInput(camera, food, feed, speaker, mic);
//                   //easy
                  // InformationFragment fragment = (InformationFragment) getActivity().getFragmentManager().findFragmentByTag("InformationFragment");
                 }



                getDialog().dismiss();

            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnInputSelected = (OnInputSelected) getTargetFragment();
        } catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException : " + e.getMessage() );
        }
    }
}

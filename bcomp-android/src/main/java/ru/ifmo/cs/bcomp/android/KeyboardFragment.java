package ru.ifmo.cs.bcomp.android;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class KeyboardFragment extends Fragment {

    public interface KeyboardCallbacks {
        void onKeyboardPressed();
    }

    private KeyboardCallbacks callbacks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View keyboardView = inflater.inflate(R.layout.keyboard_fragment, container, false);

        keyboardView.findViewById(R.id.ok_keyboard_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onKeyboardPressed();
            }
        });

        return keyboardView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (KeyboardCallbacks) activity;
    }
}

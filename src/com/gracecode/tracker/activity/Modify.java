package com.gracecode.tracker.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.gracecode.tracker.R;
import com.gracecode.tracker.activity.base.Activity;

public class Modify extends Activity {

    private Button mBtnConfirm;
    private EditText mDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify);

        mBtnConfirm = (Button) findViewById(R.id.confirm);
        mDescription = (EditText) findViewById(R.id.description);
    }


}

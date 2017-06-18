package de.lennartmeinhardt.android.sliderdemo;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.lennartmeinhardt.android.sliderdemo.databinding.ActivitySliderDemoBinding;

public class SliderDemoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySliderDemoBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_slider_demo);

        binding.setInputEnabled(true);
        binding.setValue(50);
    }
}

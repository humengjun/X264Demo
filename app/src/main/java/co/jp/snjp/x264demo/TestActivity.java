package co.jp.snjp.x264demo;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NiceSpinner spinner = new NiceSpinner(this);
        List<String> list = new ArrayList<>(Arrays.asList("one","two","three","four","five"));
        spinner.attachDataSource(list);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(500, 100);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        spinner.setLayoutParams(layoutParams);
        setContentView(spinner);
        spinner.setTextSize(35);
        spinner.setTextColor(Color.BLUE);
        spinner.setBackgroundColor(Color.GREEN);




    }

    public StateListDrawable getBg(int bgcolor) {
        //获取对应的属性值 Android框架自带的属性 attr
        int pressed = android.R.attr.state_pressed;
        int focused = android.R.attr.state_focused;

        StateListDrawable sd = new StateListDrawable();

        GradientDrawable pressedDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#ffc2b7"), Color.parseColor("#999999")});
        pressedDrawable.setCornerRadius(2);
//        pressedDrawable.setStroke(1, bgcolor);
//        sd.addState(new int[]{focused}, pressedDrawable);

//        sd.addState(new int[]{pressed}, pressedDrawable);

        GradientDrawable normal = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#777777"), bgcolor});
        normal.setCornerRadius(2);
        normal.setStroke(1, Color.parseColor("#999999"));
        sd.addState(new int[] { android.R.attr.state_enabled }, normal);

        return sd;
    }
}

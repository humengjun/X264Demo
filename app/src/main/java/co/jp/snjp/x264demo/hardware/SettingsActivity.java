package co.jp.snjp.x264demo.hardware;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.List;

import co.jp.snjp.x264demo.R;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity {

    private List<String> formats;

    private List<String> sizes;

    private Spinner spinner1, spinner2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        sizes = getIntent().getStringArrayListExtra("image_size");
        formats = getIntent().getStringArrayListExtra("image_format");
        initView();
    }

    private void initView() {
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String size = spinner1.getSelectedItem().toString();
                String format = spinner2.getSelectedItem().toString();
                Intent intent = new Intent();
                intent.putExtra("size", size);
                intent.putExtra("format", format);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        //绑定要显示的texts
        spinner1.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sizes));
        spinner2.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, formats));
    }
}

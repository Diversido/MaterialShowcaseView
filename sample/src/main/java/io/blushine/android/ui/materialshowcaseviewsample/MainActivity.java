package io.blushine.android.ui.materialshowcaseviewsample;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.blushine.android.ui.showcase.MaterialShowcaseView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	Button button = (Button) findViewById(R.id.btn_target_example);
	button.setOnClickListener(this);
	button = (Button) findViewById(R.id.btn_fullscreen_example);
	button.setOnClickListener(this);
	button = (Button) findViewById(R.id.btn_sequence_example);
	button.setOnClickListener(this);
	button = (Button) findViewById(R.id.btn_reset_all);
	button.setOnClickListener(this);

}

@Override
public void onClick(View v) {

	Intent intent = null;

	switch (v.getId()) {
	case R.id.btn_target_example:
		intent = new Intent(this, TargetExamples.class);
		break;

	case R.id.btn_fullscreen_example:
		intent = new Intent(this, FullscreenExample.class);
		break;

	case R.id.btn_sequence_example:
		intent = new Intent(this, SequenceExample.class);
		break;

	case R.id.btn_reset_all:
		MaterialShowcaseView.resetAll(this);
		Toast.makeText(this, "All Showcases reset", Toast.LENGTH_SHORT).show();
		break;
	}

	if (intent != null) {
		startActivity(intent);
	}
}


}

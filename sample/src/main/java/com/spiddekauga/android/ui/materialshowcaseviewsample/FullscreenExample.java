package com.spiddekauga.android.ui.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spiddekauga.android.ui.showcase.MaterialShowcaseView;

public class FullscreenExample extends AppCompatActivity implements View.OnClickListener {

private static final String SHOWCASE_ID = "custom example";

@Override
protected void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_fullscreen_example);
	Button buttonShow = (Button) findViewById(R.id.show_button);
	buttonShow.setOnClickListener(this);

	Button buttonReset = (Button) findViewById(R.id.reset_button);
	buttonReset.setOnClickListener(this);
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	getMenuInflater().inflate(R.menu.activity_custom_example, menu);
	return super.onCreateOptionsMenu(menu);
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
	if (item.getItemId() == R.id.menu_sample_action) {
		View view = findViewById(R.id.menu_sample_action);
		new MaterialShowcaseView.Builder(this)
				.setTarget(view)
				.setDismissText("Got it")
				.setContentText("Example of how to setup a MaterialShowcaseView for menu items in action bar.")
				.show();
	}

	return super.onOptionsItemSelected(item);
}

@Override
public void onClick(View v) {

	if (v.getId() == R.id.show_button) {
		presentShowcaseView();
	} else if (v.getId() == R.id.reset_button) {
		MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);
		Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show();
	}

}

private void presentShowcaseView() {
	new MaterialShowcaseView.Builder(this)
			.setContentText("Not setting a target makes the showcase go into fullscreen mode, and you can click anywhere to dismiss the text, or simply the got it text")
			.setDismissText("got it")
			.setTitleText("Fullscreen example")
			.setDelay(0) // optional but starting animations immediately in onCreate can make them choppy
			.setSingleUse(SHOWCASE_ID) // provide a unique ID used to ensure it is only shown once
			.show();
}
}

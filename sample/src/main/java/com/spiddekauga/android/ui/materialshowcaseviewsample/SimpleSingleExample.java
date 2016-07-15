package com.spiddekauga.android.ui.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spiddekauga.android.ui.showcase.MaterialShowcaseView;

public class SimpleSingleExample extends AppCompatActivity implements View.OnClickListener {

private static final String SHOWCASE_CENTER_ID = "center example";
private static final String SHOWCASE_CORNER_ID = "corner example";
private FloatingActionButton mCornerButton;
private FloatingActionButton mCenterButton;

@Override
protected void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_simple_single_example);
	Button button = (Button) findViewById(R.id.show_center_button);
	button.setOnClickListener(this);
	button = (Button) findViewById(R.id.show_corner_button);
	button.setOnClickListener(this);
	button = (Button) findViewById(R.id.reset_button);
	button.setOnClickListener(this);

	mCornerButton = (FloatingActionButton) findViewById(R.id.corner_button);
	mCornerButton.setOnClickListener(this);
	mCenterButton = (FloatingActionButton) findViewById(R.id.center_button);
	mCenterButton.setOnClickListener(this);
}

@Override
public void onClick(View v) {
	if (v.getId() == R.id.show_corner_button) {
		presentShowcaseCorner();
	} else if (v.getId() == R.id.show_center_button) {
		presentShowcaseCenter();
	} else if (v.getId() == R.id.corner_button) {
		Toast.makeText(this, "Pressed corner button", Toast.LENGTH_SHORT).show();
	} else if (v.getId() == R.id.center_button) {
		Toast.makeText(this, "Pressed center button", Toast.LENGTH_SHORT).show();
	} else if (v.getId() == R.id.reset_button) {
		MaterialShowcaseView.resetSingleUse(this, SHOWCASE_CENTER_ID);
		MaterialShowcaseView.resetSingleUse(this, SHOWCASE_CORNER_ID);
		Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show();
	}
}

private void presentShowcaseCorner() {
	new MaterialShowcaseView.Builder(this)
			.setTarget(mCornerButton)
			.setTitleText("Near border")
			.setDismissText("got it")
			.setContentText("Targets near borders have a large circle according to Material's design document.")
			.setDelay(0) // optional but starting animations immediately in onCreate can make them choppy
			.setSingleUse(SHOWCASE_CORNER_ID) // provide a unique ID used to ensure it is only shown once
			.show();
}

private void presentShowcaseCenter() {
	new MaterialShowcaseView.Builder(this)
			.setTarget(mCenterButton)
			.setTitleText("Other targets")
			.setDismissText("got it")
			.setContentText("Targets that aren't close to a border have a small circle.")
			.setDelay(0) // optional but starting animations immediately in onCreate can make them choppy
			.setSingleUse(SHOWCASE_CENTER_ID) // provide a unique ID used to ensure it is only shown once
			.show();
}
}

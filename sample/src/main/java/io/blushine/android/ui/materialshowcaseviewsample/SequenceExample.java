package io.blushine.android.ui.materialshowcaseviewsample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.blushine.android.ui.showcase.MaterialShowcaseSequence;
import io.blushine.android.ui.showcase.MaterialShowcaseView;
import io.blushine.android.ui.showcase.ShowcaseConfig;


public class SequenceExample extends AppCompatActivity implements View.OnClickListener {

private static final String SHOWCASE_ID = "sequence example";
private FloatingActionButton mTopButton;
private FloatingActionButton mBottomRightButton;

@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_sequence_example);
	mTopButton = (FloatingActionButton) findViewById(R.id.top_button);
	mTopButton.setOnClickListener(this);

	mBottomRightButton = (FloatingActionButton) findViewById(R.id.bottom_right_button);
	mBottomRightButton.setOnClickListener(this);

	Button button = (Button) findViewById(R.id.reset_button);
	button.setOnClickListener(this);

	button = (Button) findViewById(R.id.show_button);
	button.setOnClickListener(this);
}

@Override
public void onClick(View v) {
	if (v.getId() == R.id.top_button) {
		Toast.makeText(this, "Top button pressed", Toast.LENGTH_SHORT).show();
	} else if (v.getId() == R.id.bottom_right_button) {
		Toast.makeText(this, "Bottom right button pressed", Toast.LENGTH_SHORT).show();
	} else if (v.getId() == R.id.show_button) {
		presentShowcaseSequence();
	} else if (v.getId() == R.id.reset_button) {
		MaterialShowcaseView.resetSingleUse(this, SHOWCASE_ID);
		Toast.makeText(this, "Showcase reset", Toast.LENGTH_SHORT).show();
	}

}

private void presentShowcaseSequence() {
	// You can use a config to easy set common settings for each showcase
	ShowcaseConfig config = new ShowcaseConfig(this);
	config.setDelay(0);

	MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);
	sequence.setConfig(config);

	// 1
	sequence.addSequenceItem(
			new MaterialShowcaseView.Builder(this)
					.setTarget(mTopButton)
					.setTitleText("Press button")
					.setContentText("You can click on the target if you don't call setTargetTouchable(false) or set a hide text")
					.build()
	);


	// We update the config so that there is half second delay between each showcase view
	config.setDelay(500);

	// 2
	sequence.addSequenceItem(mBottomRightButton, "Click outside to hide", "Click outside the area hide", null);

	// 3
	sequence.addSequenceItem(
			new MaterialShowcaseView.Builder(this)
					.setDismissText("got it")
					.setTitleText("Fullscreen showcase")
					.setContentText("You can use both fullsceen and target showcases in your sequence :)")
					.build()
	);

	sequence.show();

	// 4
	new MaterialShowcaseView.Builder(this)
			.setTitleText("Automatically queued")
			.setContentText("Calling show() on while a sequence or showcase is active will queue the showcase. Therefor it is not entirely necessary to use a sequence")
			.show();
}

}

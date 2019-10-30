package com.example.hj.overscrollscrollview;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
	public static boolean EFFECT_ON = true;
	private OverScrollView overScrollView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		String[] item = {"List item 1",
				"List item 2",
				"List item 3",
				"List item 4",
				"List item 5",
				"List item 6",
				"List item 7",
				"List item 8",
				"List item 9",
				"List item 10",
				"List item 11",
				"List item 12",
				"List item 13",
				"List item 14",
				"List item 15",
				"List item 16"};
		overScrollView = findViewById(R.id.overScrollView);
		overScrollView.setData(item, R.layout.list_header, R.layout.list_item, R.layout.list_footer);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_state :
				overScrollView.debug();
				break;
			case R.id.effect_on_off :
				EFFECT_ON = !EFFECT_ON;
				item.setTitle(EFFECT_ON ? "Effect Off" : "Effect On");
				break;
			case R.id.edit_constants :
				View settingView = LayoutInflater.from(this).inflate(R.layout.setting_constants, null);
				final EditText boun_deg = settingView.findViewById(R.id.et_boun_1);
				final EditText multiplier = settingView.findViewById(R.id.et_boun_2);
				final EditText boun_height_multi = settingView.findViewById(R.id.et_boun_3);
				final EditText over_deg = settingView.findViewById(R.id.et_over_1);
				final EditText rele_deg = settingView.findViewById(R.id.et_rele_1);
				final EditText rele_dur = settingView.findViewById(R.id.et_rele_2);

				boun_deg.setText(String.valueOf(overScrollView.getBOUN_DEG()));
				multiplier.setText(String.valueOf(overScrollView.getMULTIPLIER()));
				boun_height_multi.setText(String.valueOf(overScrollView.getBOUN_HEIGHT_MULTI()));
				over_deg.setText(String.valueOf(overScrollView.getOVER_DEG()));
				rele_deg.setText(String.valueOf(overScrollView.getRELE_DEG()));
				rele_dur.setText(String.valueOf(overScrollView.getRELE_DURATION()));

				new AlertDialog.Builder(this)
						.setTitle("Setting Board")
						.setView(settingView)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									int BOUN_DEG = Integer.parseInt(boun_deg.getText().toString());
									float MULTIPLIER = Float.parseFloat(multiplier.getText().toString());
									float BOUN_HEIGHT_MULTI = Float.parseFloat(boun_height_multi.getText().toString());
									double OVER_DEG = Double.parseDouble(over_deg.getText().toString());
									int RELE_DEG = Integer.parseInt(rele_deg.getText().toString());
									int RELE_DUR = Integer.parseInt(rele_dur.getText().toString());
									overScrollView.setBOUN_DEG(BOUN_DEG);
									overScrollView.setMULTIPLIER(MULTIPLIER);
									overScrollView.setBOUN_HEIGHT_MULTI(BOUN_HEIGHT_MULTI);
									overScrollView.setOVER_DEG(OVER_DEG);
									overScrollView.setRELE_DEG(RELE_DEG);
									overScrollView.setRELE_DURATION(RELE_DUR);
									overScrollView.newCalculate();
									Toast.makeText(MainActivity.this, "Success to update settings.", Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									Toast.makeText(MainActivity.this, "Failed to update settings.", Toast.LENGTH_SHORT).show();
								}
							}
						})
						.setNegativeButton("Cancel", null)
						.create()
						.show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}

package de.lennartmeinhardt.game.colormatching;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.Color;
import android.support.transition.TransitionManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Random;

import de.lennartmeinhardt.game.colormatching.databinding.ActivityColorGameBinding;

public class ColorGameActivity extends AppCompatActivity {

    private static final String KEY_REOPEN_GAME_FINISHED_DIALOG = "de.lennartmeinhardt.game.colormatching:colorGameActivity:finishedDialogOpen";

    private final Random random = new Random();
    private MenuItem swapModeMenuItem;
    private HintGiver hintGiver;

    private boolean finishedDialogOpen;

    ActivityColorGameBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_color_game);
        binding.setCurrentColor(new RgbColor());

        hintGiver = new HintGiver(this);

        binding.colorGameRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! finishedDialogOpen &&
                        (binding.getCurrentColor().getRgb() == binding.getTargetColor()))
                    openGameFinishedDialog();
            }
        });

        binding.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if(propertyId == BR.difficulty) {
                    // trim both colors
                    binding.setTargetColor(RgbColor.trimColorToDifficulty(binding.getTargetColor(), binding.getDifficulty()));
                    binding.getCurrentColor().trimToDifficulty(binding.getDifficulty());
                }
            }
        });

        PreferencesHelper.loadFromPreferences(this);

        // show message when colors coincide
        binding.getCurrentColor().addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if(propertyId == BR.rgb) {
                    if (binding.getCurrentColor().getRgb() == binding.getTargetColor())
                        openGameFinishedDialog();
                }
            }
        });

        if(savedInstanceState != null) {
            if(savedInstanceState.getBoolean(KEY_REOPEN_GAME_FINISHED_DIALOG))
                openGameFinishedDialog();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_REOPEN_GAME_FINISHED_DIALOG, finishedDialogOpen);
    }

    @Override
    protected void onPause() {
        super.onPause();

        PreferencesHelper.storeToPreferences(this);
    }

    private void openGameFinishedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.tit_correct)
                .setMessage(R.string.msg_correct)
                .setIcon(R.drawable.ic_cake)
                .setNeutralButton(R.string.game_finished_nothing, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishedDialogOpen = false;
                    }
                })
                .setNegativeButton(R.string.game_finished_quit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishedDialogOpen = false;
                        finish();
                    }
                })
                .setPositiveButton(R.string.game_finished_new_game, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishedDialogOpen = false;
                        startNewGame();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finishedDialogOpen = false;
                    }
                })
                .show();
        finishedDialogOpen = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.color_game, menu);
        swapModeMenuItem = menu.findItem(R.id.action_swap_mode);

        MenuItem showValuesMenuItem = menu.findItem(R.id.action_show_values);
        showValuesMenuItem.setChecked(binding.getShowValues());
        MenuItem showButtonsMenuItem = menu.findItem(R.id.action_show_buttons);
        showButtonsMenuItem.setChecked(binding.getShowButtons());

        updateEditMenuItemText();
        return true;
    }

    private void updateEditMenuItemText() {
        // if rgb is active, display the "mix cmy" title
        swapModeMenuItem.setTitle(isRgbInputActive() ? R.string.action_edit_cmy : R.string.action_edit_rgb);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_swap_mode:
                swapInputMode();
                return true;
            case R.id.action_give_hint:
                giveHint();
                return true;
            case R.id.action_difficulty:
                openDifficultySelectionDialog();
                return true;
            case R.id.action_new_game:
                startNewGame();
                return true;
            case R.id.action_show_values:
                TransitionManager.beginDelayedTransition(binding.colorGameRoot);
                binding.setShowValues(! item.isChecked());
                item.setChecked(binding.getShowValues());
                return true;
            case R.id.action_show_buttons:
                TransitionManager.beginDelayedTransition(binding.colorGameRoot);
                binding.setShowButtons(! item.isChecked());
                item.setChecked(binding.getShowButtons());
                return true;
            // debug
            case R.id.debug_action_show_solution:
                int targetColor = binding.getTargetColor();
                @SuppressLint("DefaultLocale")
                String message = String.format("Solution: (r=%d, g=%d, b=%d)", Color.red(targetColor), Color.green(targetColor), Color.blue(targetColor));
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.debug_action_cheat:
                binding.getCurrentColor().setRgb(binding.getTargetColor());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openDifficultySelectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.tit_select_difficulty)
                .setItems(R.array.difficulties, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setDifficulty(which);
                    }
                })
                .show();
    }

    private void setDifficulty(int index) {
        Difficulty newDifficulty = Difficulty.ofIndex(index);
        binding.setDifficulty(newDifficulty);
    }

    private void startNewGame() {
        int newColor = RgbColor.generateDifferentColor(random, binding.getCurrentColor().getRgb(), binding.getDifficulty());
        binding.setTargetColor(newColor);
    }

    boolean isRgbInputActive() {
        return binding.editControlsSwitcher.getDisplayedChild() == 0;
    }

    private void giveHint() {
        String hintText = hintGiver.generateHintMessage(binding.getCurrentColor().getRgb(), binding.getTargetColor(), isRgbInputActive());
        Toast.makeText(this, hintText, Toast.LENGTH_SHORT).show();
    }

    private void swapInputMode() {
        binding.editControlsSwitcher.showNext();
        if(swapModeMenuItem != null)
            updateEditMenuItemText();
    }

    private void showAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void setEditRgb(boolean editRgb) {
        // run the swap animation at least once
        swapInputMode();
        if(isRgbInputActive() != editRgb)
            swapInputMode();
    }
}

package de.lennartmeinhardt.game.colormatching;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;

class PreferencesHelper {

    // Compat settings keys
    private static final String COMPAT_KEY_EDIT_COLOR="currentColor",
            COMPAT_KEY_TARGET_COLOR="targetColor",
            COMPAT_KEY_IS_GAME_PERSISTED="isGamePersisted",
            COMPAT_KEY_IS_EDIT_RGB="showRGB",
            COMPAT_KEY_STEP_SIZE="valuesDist";

    private static final String KEY_LAST_USED_VERSION = "de.lennartmeinhardt.game.colormatching:application:lastUsedVersion";

    // game settings' keys
    private static final String KEY_CURRENT_COLOR = "de.lennartmeinhardt.game.colormatching:colorGame:currentColorRgb";
    private static final String KEY_TARGET_COLOR = "de.lennartmeinhardt.game.colormatching:colorGame:targetColorRgb";
    private static final String KEY_DIFFICULTY_INDEX = "de.lennartmeinhardt.game.colormatching:colorGame:difficultyIndex";
    // ui settings' keys
    private static final String KEY_EDIT_RGB = "de.lennartmeinhardt.game.colormatching:userInterface:editRgb";
    private static final String KEY_SHOW_VALUES = "de.lennartmeinhardt.game.colormatching:userInterface:showValues";
    private static final String KEY_SHOW_BUTTONS = "de.lennartmeinhardt.game.colormatching:userInterface:showButtons";

    public static void storeToPreferences(ColorGameActivity activity) {
        PreferenceManager.getDefaultSharedPreferences(activity).edit()
                // save game settings
                .putInt(KEY_CURRENT_COLOR, activity.binding.getCurrentColor().getRgb())
                .putInt(KEY_TARGET_COLOR, activity.binding.getTargetColor())
                .putInt(KEY_DIFFICULTY_INDEX, activity.binding.getDifficulty().ordinal())
                // save ui settings
                .putBoolean(KEY_EDIT_RGB, activity.isRgbInputActive())
                .putBoolean(KEY_SHOW_VALUES, activity.binding.getShowValues())
                .putBoolean(KEY_SHOW_BUTTONS, activity.binding.getShowButtons())
                // store version number for later use
                .putInt(KEY_LAST_USED_VERSION, BuildConfig.VERSION_CODE)
                // flush changes
                .apply();
    }

    public static int getLastUsedVersionCode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_LAST_USED_VERSION, -1);
    }

    public static void loadFromPreferences(ColorGameActivity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);

        loadGameSettingsFromPreferences(preferences, activity);
        loadUiSettingsFromPreferences(preferences, activity);

        // delete compat contents
        preferences.edit()
                .remove(COMPAT_KEY_EDIT_COLOR)
                .remove(COMPAT_KEY_IS_EDIT_RGB)
                .remove(COMPAT_KEY_IS_GAME_PERSISTED)
                .remove(COMPAT_KEY_STEP_SIZE)
                .remove(COMPAT_KEY_TARGET_COLOR)
                .apply();
    }

    private static void loadUiSettingsFromPreferences(SharedPreferences preferences, ColorGameActivity activity) {
        boolean startEditRgb = activity.getResources().getBoolean(R.bool.start_edit_rgb);
        final boolean editRgb;
        if(preferences.contains(COMPAT_KEY_IS_EDIT_RGB))
            editRgb = preferences.getBoolean(COMPAT_KEY_IS_EDIT_RGB,startEditRgb);
        else
            editRgb = preferences.getBoolean(KEY_EDIT_RGB, startEditRgb);
        activity.setEditRgb(editRgb);

        boolean startShowValues = activity.getResources().getBoolean(R.bool.start_show_values);
        activity.binding.setShowValues(preferences.getBoolean(KEY_SHOW_VALUES, startShowValues));

        boolean startShowButtons = activity.getResources().getBoolean(R.bool.start_show_buttons);
        activity.binding.setShowButtons(preferences.getBoolean(KEY_SHOW_BUTTONS, startShowButtons));
    }

    private static void loadGameSettingsFromPreferences(SharedPreferences preferences, ColorGameActivity activity) {
        int startTargetColor = ResourcesCompat.getColor(activity.getResources(), R.color.start_target_color, activity.getTheme());
        final int targetColor;
        if(preferences.contains(COMPAT_KEY_TARGET_COLOR))
            targetColor = preferences.getInt(COMPAT_KEY_TARGET_COLOR, startTargetColor);
        else
            targetColor = preferences.getInt(KEY_TARGET_COLOR, startTargetColor);
        activity.binding.setTargetColor(targetColor);

        int startCurrentColor = ResourcesCompat.getColor(activity.getResources(), R.color.start_current_color, activity.getTheme());
        final int currentColor;
        if(preferences.contains(COMPAT_KEY_EDIT_COLOR))
            currentColor = preferences.getInt(COMPAT_KEY_EDIT_COLOR, startCurrentColor);
        else
            currentColor = preferences.getInt(KEY_CURRENT_COLOR, startCurrentColor);
        activity.binding.getCurrentColor().setRgb(currentColor);

        Difficulty startDifficulty = Difficulty.EASY;
        final Difficulty difficulty;
        if(preferences.contains(COMPAT_KEY_STEP_SIZE)) {
            int stepSize = preferences.getInt(COMPAT_KEY_STEP_SIZE, -1);
            difficulty = Difficulty.ofStepSize(stepSize, startDifficulty);
        } else {
            int difficultyIndex = preferences.getInt(KEY_DIFFICULTY_INDEX, startDifficulty.ordinal());
            difficulty = Difficulty.ofIndex(difficultyIndex, startDifficulty);
        }

        activity.binding.setDifficulty(difficulty);
    }
}

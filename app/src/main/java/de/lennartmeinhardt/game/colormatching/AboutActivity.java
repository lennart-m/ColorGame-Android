package de.lennartmeinhardt.game.colormatching;


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * This shows the game's "about" message.
 *
 * @author Lennart Meinhardt
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        String aboutTextHtml = getString(R.string.about_game);
        Spanned aboutTextFormatted;
        if(Build.VERSION.SDK_INT < 24)
            aboutTextFormatted = Html.fromHtml(aboutTextHtml);
        else
            aboutTextFormatted = Html.fromHtml(aboutTextHtml, 0);

        // Set the html-text for the "about game" text view. Do it here as it contains html code (for the colors)
        ((TextView) findViewById(R.id.about_game)).setText(aboutTextFormatted);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Send an email to me (the author).
     */
    public void onSendEmail(View view) {
        ShareCompat.IntentBuilder.from(this)
                .setType("message/rfc822")
                .addEmailTo(getString(R.string.email_address))
                .setSubject(getString(R.string.email_subject))
                .startChooser();
    }

    /*
     * Open a browser to view the source code.
     */
    public void onViewSourceCode(View view) {
        String url = getString(R.string.source_code_address);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}

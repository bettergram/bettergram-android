package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import io.bettergram.messenger.R;

public class SplashActivity extends Activity {

    private View layout02, layout01;
    private EditText emailEdit;
    private Button signUpButton;
    private CheckBox termsCheckbox;
    private ImageView overlayImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        overlayImage = findViewById(R.id.overlayImage);
        layout01 = findViewById(R.id.layout01);
        layout02 = findViewById(R.id.layout02);
        emailEdit = findViewById(R.id.emailEdit);
        signUpButton = findViewById(R.id.signUpButton);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        termsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            signUpButton.setEnabled(isChecked);
        });
        overlayImage.animate()
                .translationY(overlayImage.getHeight())
                .alpha(0.0f)
                .setDuration(3500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        overlayImage.setVisibility(View.GONE);
                        layout01.setVisibility(View.VISIBLE);
                        layout02.setVisibility(View.VISIBLE);
                    }
                });
    }
}

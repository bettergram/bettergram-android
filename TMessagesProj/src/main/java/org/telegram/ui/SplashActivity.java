package org.telegram.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import io.bettergram.messenger.R;
import io.bettergram.service.MailChimpService;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.AlertDialog;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.service.MailChimpService.EXTRA_SUBSCRIBE_EMAIL;
import static io.bettergram.service.MailChimpService.EXTRA_SUBSCRIBE_NEWSLETTER;

public class SplashActivity extends Activity {

    private View layout02, layout01;
    private EditText emailEdit;
    private Button signUpButton;
    private CheckBox termsCheckbox, newsletterCheckbox;
    private ImageView overlayImage;

    private AlertDialog progressDialog;

    private boolean subscribeNewsletter;

    private SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mailchimp_subscribed", MODE_PRIVATE);

    /**
     * Receives data from {@link MailChimpService}
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progressDialog.dismiss();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {

                String json = bundle.getString(MailChimpService.RESULT);

                if (isEmpty(json)) {
                    json = bundle.getString(MailChimpService.ERROR);
                    try {
                        JSONObject errors = new JSONObject(json);
                        String title = errors.keys().next();
                        String message = errors.getString(title);

                        createAlertDialog(title, message)
                                .setPositiveButton(
                                        context.getString(android.R.string.ok),
                                        (dialog, which) -> dialog.dismiss()
                                )
                                .show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        JSONObject object = new JSONObject(json);
                        String email = object.getString("email_address");
                        preferences.edit().putString("mailchimp_subscribed_email", email).apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    createAlertDialog(getString(R.string.success), getString(R.string.please_check_email))
                            .setPositiveButton(
                                    context.getString(android.R.string.ok),
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        AndroidUtilities.runOnUIThread(() -> {
                                            Intent intent1 = new Intent(SplashActivity.this, IntroActivity.class);
                                            intent1.setData(getIntent().getData());
                                            startActivity(intent1);
                                            finish();
                                        }, 1000);
                                    }
                            )
                            .show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressDialog = new AlertDialog(this, 1);
        progressDialog.setMessage(LocaleController.getString("Please wait...", R.string.please_wait));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        overlayImage = findViewById(R.id.overlayImage);
        layout01 = findViewById(R.id.layout01);
        layout02 = findViewById(R.id.layout02);
        emailEdit = findViewById(R.id.emailEdit);
        signUpButton = findViewById(R.id.signUpButton);
        newsletterCheckbox = findViewById(R.id.newsletterCheckbox);
        newsletterCheckbox.setOnCheckedChangeListener((buttonView, subscribeNewsletter) -> {
            this.subscribeNewsletter = subscribeNewsletter;
        });
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
        signUpButton.setOnClickListener(v -> {
            String email = emailEdit.getEditableText().toString();
            if (!isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Intent intent = new Intent(this, MailChimpService.class);
                intent.putExtra(EXTRA_SUBSCRIBE_NEWSLETTER, subscribeNewsletter);
                intent.putExtra(EXTRA_SUBSCRIBE_EMAIL, email);
                startService(intent);
                progressDialog.show();
            } else {
                createAlertDialog(getString(R.string.invalid_email), getString(R.string.invalid_email_msg)).show();
            }
        });
        registerReceiver(this);
    }

    private AlertDialog.Builder createAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        return builder;
    }

    public void startService(Activity activity) {
        Intent intent = new Intent(activity, MailChimpService.class);
        activity.startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this);
    }

    /**
     * Register {@link BroadcastReceiver} of {@link MailChimpService}
     */
    public void registerReceiver(Activity activity) {
        activity.registerReceiver(receiver, new IntentFilter(MailChimpService.NOTIFICATION));
    }

    /**
     * Unregister {@link BroadcastReceiver} of {@link MailChimpService}
     */
    public void unregisterReceiver(Activity activity) {
        activity.unregisterReceiver(receiver);
    }
}

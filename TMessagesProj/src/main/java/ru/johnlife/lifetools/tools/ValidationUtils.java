package ru.johnlife.lifetools.tools;

import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

/**
 * Created by Yan Yurkin
 * 23 November 2017
 */

public class ValidationUtils {
    private static final Pattern namePattern = Pattern.compile("^[A-Za-z ]+$");
    private static final Pattern phonePattern = Pattern.compile("^\\+855[0-9]{8,9}$");
    private static final Pattern imeiPattern = Pattern.compile("^[0-9]{14,15}$");
    private static final Pattern IdNumberPattern = Pattern.compile("^[0-9A-Z\\s]+$");
    private static final Pattern emailPattern = Pattern.compile("^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$");

    public interface Validator {
        boolean isValid();
    }

    public static boolean validate(TextView view, Validator validator, String error) {
        boolean valid = validator.isValid();
        if (!valid) {
            view.setError(error);
        }
        return valid;
    }

    public static boolean validate(Spinner view, Validator validator, String error){
        boolean valid = validator.isValid();
        TextView selectedView = (TextView) view.getSelectedView();
        if (!valid && null != selectedView) {
            selectedView.setError(error);
        }
        return valid;
    }

    public static boolean validate(Validator validator, Runnable actionOnInvalid) {
        boolean valid = validator.isValid();
        if (!valid) {
            actionOnInvalid.run();
        }
        return valid;

    }

    public static boolean validateNonEmpty(TextView view) {
        if (view.getText().toString().isEmpty()) {
            view.setError("This field cannot be empty");
            return false;
        }
        return true;
    }

    public static boolean validateName(TextView view) {
        return validateWithPattern(view, ValidationUtils.namePattern, "This should be valid name");
    }

    public static boolean validatePhone(TextView view) {
        return validateWithPattern(view, phonePattern, "This should be a phone in +85599999999 format");
    }

    public static boolean validateEmail(TextView view) {
        return validateWithPattern(view, emailPattern, "This should be a valid email address");
    }

    public static boolean validateIMEI(TextView view) {
        return validateWithPattern(view, imeiPattern, "This should be a valid IMEI");
    }

    public static boolean validateIDNumber(TextView view) {
        return validateWithPattern(view, IdNumberPattern, "This should be valid ID Number");
    }

    private static boolean validateWithPattern(TextView view, Pattern pattern, String error) {
        if (!validateNonEmpty(view)) return false;
        if (!pattern.matcher(view.getText()).matches()) {
            view.setError(error);
            return false;
        }
        return true;
    }

    public static boolean isValidIMEI(String imei) {
        return imeiPattern.matcher(imei).matches();
    }

}

package com.score.cbook.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.score.cbook.exceptions.ExceedAmountException;
import com.score.cbook.exceptions.InvalidAccountException;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.exceptions.InvalidMsgException;
import com.score.cbook.exceptions.InvalidPasswordException;
import com.score.cbook.exceptions.InvalidPhoneNumberException;
import com.score.cbook.exceptions.LessAmountException;
import com.score.cbook.exceptions.MisMatchFieldException;
import com.score.cbook.exceptions.MisMatchPhoneNumberException;

/**
 * Utility class to handle activity related common functions
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class ActivityUtil {

    private static ProgressDialog progressDialog;

    /**
     * Hide keyboard
     * Need to hide soft keyboard in following scenarios
     * 1. When starting background task
     * 2. When exit from activity
     * 3. On button submit
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getApplicationContext().getSystemService(activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showProgressDialog(Context context, String message) {
        progressDialog = ProgressDialog.show(context, null, message, true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);

        progressDialog.show();
    }

    public static void cancelProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }

    public static void isValidRegistrationFields(String phone, String confirmPhone, String password, String confirmPassword) throws InvalidPhoneNumberException, InvalidPasswordException, MisMatchFieldException,MisMatchPhoneNumberException {
        if (phone.isEmpty()) {
            throw new InvalidPhoneNumberException();
        }

        if (phone.length() != 10 || phone.length() != 10) {
            throw new InvalidPhoneNumberException();
        } else if (phone.length() == 10) {
            String pattern = "^(0)[0-9]*$";
            if (!phone.matches(pattern)) {
                throw new InvalidPhoneNumberException();
            }
        }

        if (password.isEmpty() || password.length() < 8)
            throw new InvalidPasswordException();

        else if (password.length() >= 8) {
            String pattern = "^(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
            if (!password.matches(pattern)) {
                throw new InvalidPasswordException();
            }
        }

        if (!password.equals(confirmPassword))
            throw new MisMatchFieldException();

        if (!phone.equals(confirmPhone))
            throw new MisMatchPhoneNumberException();

    }

    public static void isValidPasswordFields(String currentPassword, String newPassword, String newConfirmPassword) throws InvalidPasswordException, MisMatchFieldException {
        if (currentPassword.isEmpty())
            throw new InvalidPasswordException();

        if (newPassword.isEmpty() || newPassword.length() < 8)
            throw new InvalidPasswordException();

        else if (newPassword.length() >= 8) {
            String pattern = "^(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
            if (!newPassword.matches(pattern)) {
                throw new InvalidPasswordException();
            }
        }

        if (currentPassword.equalsIgnoreCase(newPassword))
            throw new InvalidPasswordException();

        if (!newPassword.equals(newConfirmPassword))
            throw new MisMatchFieldException();
    }

    public static void isValidLoginFields(String givenAccount, String givenPassword, String account, String password) throws InvalidInputFieldsException, MisMatchFieldException {
        if (givenAccount.isEmpty() || givenPassword.isEmpty())
            // empty fields
            throw new InvalidInputFieldsException();

        if (!givenPassword.equalsIgnoreCase(password))
            // invalid username/password
            throw new MisMatchFieldException();
    }

    public static void isValidUsername(String currUsername, String newUsername) throws InvalidInputFieldsException {
        if (currUsername.isEmpty() || newUsername.isEmpty()) {
            throw new InvalidInputFieldsException();
        }

        if (currUsername.length() < 5 || newUsername.length() < 5) {
            throw new InvalidInputFieldsException();
        }
    }

    public static void isValidAccount(String account, String confirmAccount) throws InvalidInputFieldsException, MisMatchFieldException, InvalidAccountException {
        if (account.isEmpty() || confirmAccount.isEmpty()) {
            throw new InvalidInputFieldsException();
        }

        if (account.length() != 12 || confirmAccount.length() != 12) {
            throw new InvalidInputFieldsException();
        } else if (account.length() == 12) {
            String pattern = "^(0|1)[0-9]*$";
            if (!account.matches(pattern)) {
                throw new InvalidAccountException();
            }
        }

        if (!account.equalsIgnoreCase(confirmAccount)) {
            throw new MisMatchFieldException();
        }

    }

    public static void isValidRedeem(String acc, String confirmAcc) throws InvalidInputFieldsException, InvalidAccountException, MisMatchFieldException {
        if (acc.isEmpty() || confirmAcc.isEmpty())
            throw new InvalidInputFieldsException();

        if (acc.length() != 12 || confirmAcc.length() != 12) {
            throw new InvalidInputFieldsException();
        } else if (acc.length() == 12) {
            String pattern = "^(0|1)[0-9]*$";
            if (!acc.matches(pattern)) {
                throw new InvalidAccountException();
            }
        }

        if (!acc.equals(confirmAcc)) throw new MisMatchFieldException();
    }

    public static void isValidGift(String amount, String msg) throws InvalidInputFieldsException, InvalidMsgException, LessAmountException, ExceedAmountException {
        if (amount.isEmpty()) throw new InvalidInputFieldsException();
        if (msg.isEmpty()) throw new InvalidMsgException();
        if (Integer.parseInt(amount) > 10000) throw new ExceedAmountException();
        if (Integer.parseInt(amount) < 100) throw new LessAmountException();
    }

    public static boolean isNewGift(String preAmount, String amount) {
        return !preAmount.equalsIgnoreCase(amount);
    }

}

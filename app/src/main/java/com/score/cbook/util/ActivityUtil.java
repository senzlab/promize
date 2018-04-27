package com.score.cbook.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.score.cbook.exceptions.InvalidAccountException;
import com.score.cbook.exceptions.InvalidAmountException;
import com.score.cbook.exceptions.InvalidInputFieldsException;
import com.score.cbook.exceptions.InvalidMsgException;
import com.score.cbook.exceptions.InvalidPasswordException;
import com.score.cbook.exceptions.MisMatchFieldException;

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

    /**
     * Create and show custom progress dialog
     * Progress dialogs displaying on background tasks
     * <p/>
     * So in here
     * 1. Create custom layout for message dialog
     * 2, Set messages to dialog
     *
     * @param context activity context
     * @param message message to be display
     */
    public static void showProgressDialog(Context context, String message) {
        progressDialog = ProgressDialog.show(context, null, message, true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);

        progressDialog.show();
    }

    /**
     * Cancel progress dialog when background task finish
     */
    public static void cancelProgressDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }

    /**
     * Validate input fields of registration form,
     * Need to have
     * 1. non empty valid phone no
     * 2. non empty username
     * 3. non empty passwords
     * 4. two passwords should be match
     *
     * @return valid or not
     */
    public static void isValidRegistrationFields(String phone, String password, String confirmPassword) throws InvalidAccountException, InvalidPasswordException, MisMatchFieldException {
        if (phone.isEmpty()) {
            throw new InvalidAccountException();
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

    }

    /**
     * Validate input fields of registration form,
     * Need to have
     * 1. non empty valid passwords
     * 2. non empty passwords
     * 3. two passwords should be match
     *
     * @return valid or not
     */
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

        if (!newPassword.equals(newConfirmPassword))
            throw new MisMatchFieldException();

    }

    /**
     * validate input fields of login form
     *
     * @return valid of not
     */
    public static void isValidLoginFields(String givenAccount, String givenPassword, String account, String password) throws InvalidInputFieldsException, MisMatchFieldException {
        if (givenAccount.isEmpty() || givenPassword.isEmpty())
            // empty fields
            throw new InvalidInputFieldsException();

        if (!givenPassword.equalsIgnoreCase(password))
            // invalid username/password
            throw new MisMatchFieldException();

    }

    public static void isValidAccount(String account, String confirmAccount) throws InvalidInputFieldsException, MisMatchFieldException {
        if (account.isEmpty() || confirmAccount.isEmpty()) {
            throw new InvalidInputFieldsException();
        }

        if (account.length() != 12 || confirmAccount.length() != 12) {
            throw new InvalidInputFieldsException();
        } else if (account.length() == 12) {
            String pattern = "^(0|1)[0-9]*$";
            if (!account.matches(pattern)) {
                throw new InvalidInputFieldsException();
            }
        }

        if (!account.equalsIgnoreCase(confirmAccount)) {
            throw new MisMatchFieldException();
        }

    }

    public static void isValidUsername(String currUsername, String newUsername) throws InvalidInputFieldsException {
        if (currUsername.isEmpty() || newUsername.isEmpty()) {
            throw new InvalidInputFieldsException();
        }

        if (currUsername.length() < 5 || newUsername.length() < 5) {
            throw new InvalidInputFieldsException();
        }

    }

    public static void isValidRedeem(String acc, String confirmAcc) throws InvalidInputFieldsException, InvalidAccountException {
        if (acc.isEmpty() || confirmAcc.isEmpty())
            throw new InvalidInputFieldsException();

        if (acc.length() != 12 || confirmAcc.length() != 12) {
            throw new InvalidInputFieldsException();
        } else if (acc.length() == 12) {
            String pattern = "^(0|1)[0-9]*$";
            if (!acc.matches(pattern)) {
                throw new InvalidInputFieldsException();
            }
        }

        if (!acc.equals(confirmAcc)) throw new InvalidAccountException();
    }

    public static void isValidGift(String amount, String msg) throws InvalidInputFieldsException, InvalidAmountException, InvalidMsgException {
        if (amount.isEmpty()) throw new InvalidInputFieldsException();
        if (msg.isEmpty()) throw new InvalidMsgException();
        if (Integer.parseInt(amount) > 10000) throw new InvalidAmountException();
    }

    public static boolean isNewGift(String preAmount, String amount) {
        return !preAmount.equalsIgnoreCase(amount);
    }

}

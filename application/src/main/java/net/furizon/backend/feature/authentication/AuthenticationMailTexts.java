package net.furizon.backend.feature.authentication;

public class AuthenticationMailTexts {

    public static final String SUBJECT_PW_RESET = "Password reset link for your account";
    public static final String SUBJECT_PW_CHANGED = "The password of your account has been changed";
    public static final String SUBJECT_EMAIL_CONFIRM = "Confirm your account";
    public static final String SUBJECT_TOO_MANY_LOGIN_ATTEMPTS = "Account disabled - Too many login attempts";
    public static final String SUBJECT_MEMBERSHIP_FATAL_ERROR = "Membership card FATAL ERROR";
    public static final String SUBJECT_MEMBERSHIP_WARNING = "Membership card warning";

    public static final String TEMPLATE_PW_RESET = "password_reset.jte";
    public static final String TEMPLATE_PW_CHANGED = "password_changed.jte";
    public static final String TEMPLATE_EMAIL_CONFIRM = "email_confirm.jte";
    public static final String TEMPLATE_TOO_MANY_LOGIN_ATTEMPTS = "too_many_login_attempts.jte";
    public static final String TEMPLATE_MEMBERSHIP_CARD_FATAL_ERROR = "membership_card_fatal_error.jte";
    public static final String TEMPLATE_MEMBERSHIP_CARD_WARNING = "membership_card_warning.jte";
}

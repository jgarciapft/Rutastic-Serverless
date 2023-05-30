package resources.users.validators;

public class UsernameValidators {

    public static final String USERNAME_REGEX = "[a-zA-Z0-9_-]+";

    public static boolean usernameIsValid(String username) {
        return username != null
                && !username.trim().isEmpty()
                && username.length() <= 30
                && username.matches(USERNAME_REGEX);
    }

}

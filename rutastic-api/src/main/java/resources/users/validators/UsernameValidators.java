package resources.users.validators;

public class UsernameValidators {

    public static boolean usernameIsValid(String username) {
        return username != null && !username.trim().isEmpty() && username.length() <= 30;
    }

}

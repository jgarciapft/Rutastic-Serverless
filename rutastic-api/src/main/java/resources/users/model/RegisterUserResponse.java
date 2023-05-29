package resources.users.model;

import resources.model.ResponseWithFlags;

public class RegisterUserResponse extends ResponseWithFlags<Integer> {

    public static class Flags {
        public static final int INFO_USER_REGISTERED_SUCCESSFULLY = 10;
        public static final int ERROR_INVALID_USER = 20;
        public static final int ERROR_USER_ALREADY_EXISTS = 30;
        public static final int ERROR_UNABLE_TO_REGISTER = 40;
    }

}

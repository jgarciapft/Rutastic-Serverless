package resources.users.model;

import resources.model.ResponseWithFlags;

public class DeleteUserResponse extends ResponseWithFlags<Integer> {

    public static class Flags {
        public static final int INFO_USER_DELETED = 10;
        public static final int ERROR_USER_NOT_FOUND = 20;
        public static final int ERROR_UNAUTHORIZED = 30;
        public static final int ERROR_USER_DELETION_UNSUCCESSFUL = 40;
    }

}

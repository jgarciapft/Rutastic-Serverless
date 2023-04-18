angular.module('Rutastic')
    // Persistent Controller in charge of the navbar. Persistent across the use of the app if user doesn't refresh the page
    .controller('headerController', ['usersFactory', function (usersFactory) {

        let headerVM = this;

        headerVM.loggedUser = undefined; // Currently logged user

        headerVM.functions = {
            // Sign out the current user
            signOut: function () {
                usersFactory.doSignOut();
            }
        }

        // Observe forever the logged user from the users factory
        usersFactory.observerCallbacks.push(loggedUserUpdatedCallback);

        function loggedUserUpdatedCallback() {
            headerVM.loggedUser =
                usersFactory.loggedCognitoUser !== undefined ? usersFactory.loggedCognitoUser.username : undefined;
        }
    }]);
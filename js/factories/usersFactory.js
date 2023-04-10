import {Auth} from "@aws-amplify/auth";

angular.module('Rutastic')
    // Services related to users resource
    .factory('usersFactory', ['$http', function ($http) {

        // LOCAL VARs

        let restBaseUrl = 'https://8m4aoe3so9.execute-api.us-east-1.amazonaws.com/dev/usuarios';

        let cognitoIDP = new AWS.CognitoIdentityServiceProvider({region: 'eu-west-1'})

        // FACTORY PROPERTIES

        let usersFactory = {
            observerCallbacks: [],
            loggedCognitoUser: undefined, // Holds the logged user within this app
            // Enforceables password policies as configured in the Amazon Cognito User Pool
            passwordPolicies: {
                minLenght: 8,
                mustContainNumber: true,
                mustContainUppercase: true,
                mustContainLowercase: true,
                mustContainSymbols: false
            }
        }

        // FACTORY INTERFACE

        /**
         * Retrieve all registered usernames
         *
         * @return {HttpPromise|Promise|PromiseLike<T>|Promise<T>} A promise which resolves to an array of user model
         * objects, with all the personal info cleared, just the username left
         */
        usersFactory.getAllUsernames = function () {
            return $http
                .get(restBaseUrl)
                .then(response => response.data);
        };

        /**
         * Retrieve a list of the users who are authors of the top monthly routes, ordered by descending number
         * of top monthly routes. For an user to be taken into account at least one of their routes need to have
         * received a kudo rating
         *
         * @return {HttpPromise|Promise|PromiseLike<T>|Promise<T>} A promise which resolves to an array of the top users
         */
        usersFactory.getTop5UsersByTopRoutes = function () {
            return $http
                .get(`${restBaseUrl}?estadistica=top5UsuariosPorTopRutas`)
                .then(response => response.data);
        }

        /**
         * Retrieve a list of users ordered by descending average kudo rating of the routes they're authors of.
         * For an user to be taken into account at least one of their routes need to have received a kudo rating
         *
         * @return {HttpPromise|Promise|PromiseLike<T>|Promise<T>} A promise which resolves to an array of the top users
         */
        usersFactory.getTop5UsersByAvgKudos = function () {
            return $http
                .get(`${restBaseUrl}?estadistica=top5UsuariosPorMediaKudos`)
                .then(response => response.data);
        }

        /**
         * Register a new user of this app
         *
         * @param newUser The new user
         * @return {Promise<ISignUpResult | void>} A promise that resolves to an ISignUpResult object containing the new
         * Cognito user
         */
        usersFactory.registerNewUser = function (newUser) {
            return Auth.signUp({
                username: newUser.username,
                password: newUser.password,
                attributes: {
                    email: newUser.email
                }
            });
        }

        /**
         * Submit the verification code sent to a newly created user
         *
         * @param username The newly created user
         * @param code His verification code
         * @return {Promise<any>}
         */
        usersFactory.confirmNewUser = function (username, code) {
            return Auth.confirmSignUp(username, code)
                // After confirmation register the new user on the REST API
                .then(() => $http.post(restBaseUrl, {username: username}))
        }

        /**
         * Attemp to log in an user provided his credentials
         *
         * @param username
         * @param password
         * @return {Promise<any>} A promise that resolves to the username of the newly logged user
         */
        usersFactory.doSignIn = function (username, password) {
            return Auth.signIn(username, password)
                .then(cognitoUser => {
                    usersFactory.loggedCognitoUser = cognitoUser;
                    notifyUserObservers();
                    return cognitoUser.username;
                })
        }

        /**
         * Signs out the currently logged user
         */
        usersFactory.doSignOut = function () {
            return Auth.signOut(usersFactory.loggedCognitoUser.username)
                .then(() => {
                    usersFactory.loggedCognitoUser = undefined;
                    notifyUserObservers();
                })
        }

        usersFactory.getJWTIdToken = function () {
            return Auth.currentSession()
                .then(session => session.idToken.jwtToken);
        }

        /**
         * Change the currently logged user's email
         *
         * @param newEmail
         * @return {Promise<string>}
         */
        usersFactory.updateEmail = function (newEmail) {
            return Auth.updateUserAttributes(usersFactory.loggedCognitoUser, {
                'email': newEmail
            }).then(function (status) {
                if (status === 'SUCCESS') usersFactory.loggedCognitoUser.attributes.email = newEmail;
                return status;
            })
        }

        /**
         * Change the currently logged user's password
         *
         * @param oldPassword
         * @param newPassword
         * @return {Promise<"SUCCESS">}
         */
        usersFactory.changePassword = function (oldPassword, newPassword) {
            return Auth.changePassword(usersFactory.loggedCognitoUser, oldPassword, newPassword);
        };

        /**
         * Delete the profile of the currently logged user
         *
         * @return A promise
         */
        usersFactory.deleteSelf = function () {
            return Auth.currentSession()
                .then(session => {

                    // Delete ourselves from the REST API
                    $http.delete(`${restBaseUrl}/${usersFactory.loggedCognitoUser.username}`,
                        {headers: {Auth: session.idToken.jwtToken}});

                    // Delete ourselves from the Cognito User Pool
                    let params = {
                        AccessToken: session.accessToken.jwtToken
                    }
                    cognitoIDP.deleteUser(params, function (err, data) {
                        if (err) console.log(err, err.stack); // an error occurred
                    });

                    usersFactory.doSignOut();

                    notifyUserObservers();
                });
        }

        /**
         * Checks a password against the current enforceable password policies. If the password is invalid it returns
         * a reason
         *
         * @param password
         * @return {string|number} 0 if the password is valid, a reason of why it's invalid otherwise
         */
        usersFactory.checkPasswordAgainstPolicies = function (password) {
            if (password.length < usersFactory.passwordPolicies.minLenght) {
                return 'La contraseña debe tener al menos 8 caracteres';
            } else if (usersFactory.passwordPolicies.mustContainLowercase && password.search(/[a-z]+/) === -1) {
                return 'La contraseña debe contener minúsculas';
            } else if (usersFactory.passwordPolicies.mustContainUppercase && password.search(/[A-Z]+/) === -1) {
                return 'La contraseña debe contener mayúsculas';
            } else if (usersFactory.passwordPolicies.mustContainNumber && password.search(/[0-9]+/) === -1) {
                return 'La contraseña debe contener al menos número';
            } else if (usersFactory.passwordPolicies.mustContainSymbols && password.search(/[!@#$%^&*]+/) === -1) {
                return 'La contraseña debe contener al menos uno de los siguientes símbolos (!@#$%^&*)';
            } else {
                return 0;
            }
        }

        // If the user refreshes the page, attempt to sign him in again

        Auth.currentAuthenticatedUser()
            .then(cognitoUser => {
                usersFactory.loggedCognitoUser = cognitoUser;
                notifyUserObservers();
            });

        // Logged user observer interface

        function notifyUserObservers() {
            angular.forEach(usersFactory.observerCallbacks, function (callback) {
                callback();
            })
        }

        return usersFactory;
    }]);
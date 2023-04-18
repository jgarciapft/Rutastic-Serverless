angular.module('Rutastic')
    .factory('kudoEntriesFactory', ['$http', 'usersFactory', function ($http, usersFactory) {

        let restBaseUrl = 'https://8m4aoe3so9.execute-api.us-east-1.amazonaws.com/dev/kudos'

        // FACTORY INTERFACE

        let kudoEntriesFactory = {
            /**
             * Retrieve all kudo entries associated with an user
             *
             * @param username Username of the user
             * @return {HttpPromise|Promise|PromiseLike<T>|Promise<T>} A promise which resolves to the array of
             * kudo entries
             */
            getKudoEntriesOfUser: function (username) {
                return usersFactory.getJWTIdToken()
                    .then(jwtIDToken => $http
                        .get(`${restBaseUrl}/${username}`, {headers: {Auth: jwtIDToken}}))
                    .then(response => response.data);
            },
            /**
             * Retrieve all kudo entries associated for the logged user, if any
             *
             * @return {HttpPromise|Promise|PromiseLike<T>|Promise<T>} A promise which resolves to the array of
             * kudo entries
             */
            getKudoEntriesOfLoggedUser: function () {
                if (usersFactory.loggedCognitoUser !== undefined) // Check that there's a logged user first
                    return kudoEntriesFactory.getKudoEntriesOfUser(usersFactory.loggedCognitoUser.username);
            },
            /**
             * Get the kudo entry (if any) associated to an user and a route
             *
             * @param username Username of the user
             * @param routeId ID of the route
             * @return {HttpPromise|Promise|PromiseLike<T>|Promise<T>} A promise which resolves to the requested kudo entry
             */
            getKudoEntryOfUserForRoute: function (username, routeId) {
                return usersFactory.getJWTIdToken()
                    .then(jwtIDToken => $http
                        .get(`${restBaseUrl}/${username}/${routeId}`, {headers: {Auth: jwtIDToken}}))
                    .then(response => response.data);
            }
        };

        return kudoEntriesFactory;
    }])
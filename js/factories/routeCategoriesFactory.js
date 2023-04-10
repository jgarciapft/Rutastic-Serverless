angular.module('Rutastic')
    .factory('routeCategoriesFactory', ['$http', function ($http) {

        let restBaseUrl = 'https://8m4aoe3so9.execute-api.us-east-1.amazonaws.com/dev/categoriasruta';

        return {
            /**
             * Retrieve all the registered route categories
             *
             * @return {HttpPromise|Promise|PromiseLike<T>|Promise<T>} A promise which resolves to the response object,
             * which contains the collection of route categories
             */
            getAllRouteCategories: function () {
                return $http
                    .get(restBaseUrl)
                    .then(response => response);
            }
        }
    }])
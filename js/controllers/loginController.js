angular.module('Rutastic')
    .controller('loginController', ['$location', '$routeParams', '$scope', 'usersFactory',
        function ($location, $routeParams, $scope, usersFactory) {

            let loginVM = this;

            // PROPERTIES

            loginVM.username = $routeParams.username;
            loginVM.password = undefined;
            loginVM.errorMessage = '';
            loginVM.errorUnconfirmedUser = 0;

            // FUNCTIONS

            loginVM.functions = {
                // Attempt to login an user
                submitLoginForm: function () {
                    usersFactory.doSignIn(loginVM.username, loginVM.password)
                        .then(() => {
                            $location.path('/');
                            $scope.$apply();
                            loginVM.errorMessage = '';
                            loginVM.errorUnconfirmedUser = 0;
                        })
                        .catch(err => {
                            if (err.code === 'NotAuthorizedException') {
                                loginVM.errorMessage = 'Combinación de usuario y contraseña incorrecta o el usuario no existe';
                                loginVM.errorUnconfirmedUser = 0;
                            } else if (err.code === 'UserNotConfirmedException') {
                                loginVM.errorMessage = 'Todavía no has verificado tu cuenta';
                                loginVM.errorUnconfirmedUser = 1;
                            }
                            $scope.$apply();
                        });
                }
            }
        }
    ]);
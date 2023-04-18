angular.module('Rutastic')
    .controller('accountVerificationController', ['$location', '$routeParams', '$scope', '$timeout', 'usersFactory',
        function ($location, $routeParams, $scope, $timeout, usersFactory) {

            let verificationVM = this;

            // PROPERTIES

            verificationVM.username = $routeParams.username;
            verificationVM.verificationCode = undefined;
            verificationVM.accountVerified = 0;
            verificationVM.errorMessage = '';

            // FUNCTIONS

            verificationVM.functions = {
                // Attempt to verify a newly created account with a verification code
                submitVerificationForm: function () {
                    usersFactory.confirmNewUser(verificationVM.username, verificationVM.verificationCode)
                        .then(() => {
                            verificationVM.accountVerified = 1;
                            verificationVM.errorMessage = ''; // Reset the error message on valid code
                            $scope.$apply();

                            // Redirect to login after 2 seconds
                            $timeout(function () {
                                $location.path('/Login/' + verificationVM.username);
                                verificationVM.accountVerified = 0;
                            }, 2000);
                        })
                        .catch(err => {
                            // If the code was invalid
                            if (err.name === 'CodeMismatchException') {
                                verificationVM.errorMessage = 'El código de verificación no es válido';
                                verificationVM.verificationCode = undefined;
                                $scope.$apply();
                            } else {
                                console.log(err);
                            }
                        });
                }
            }
        }
    ]);
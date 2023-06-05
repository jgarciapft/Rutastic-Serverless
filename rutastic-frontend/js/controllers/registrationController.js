angular.module('Rutastic')
    .controller('registrationController', ['$location', '$scope', 'usersFactory', function ($location, $scope, usersFactory) {

        let registrationVM = this;

        // PROPERTIES

        registrationVM.user = {} // Holds data of a potential new user
        registrationVM.errorMessage = '' // Message shown to the user in case an error arises during registration

        // FUNCTIONS

        registrationVM.functions = {
            /**
             * Submission handle for the registration form. Try registering a new user and redirect the user to
             * the account validation page
             */
            submitRegistrationForm: function () {

                // Validate username

                const username = registrationVM.user.username;
                if (username.length < 3 || username.length > 30) {
                    registrationVM.errorMessage = 'El nombre de usuario debe tener entre 3 y 30 caracteres';
                    return;
                }

                if (!/^[a-zA-Z0-9_-]+$/.test(username)) {
                    registrationVM.errorMessage = 'El nombre de usuario solo puede contener letras, números, guiones y guiones bajos';
                    return;
                }

                // Check all enforced password policies before trying to register a new user

                let checksAgainstPolicies = usersFactory.checkPasswordAgainstPolicies(registrationVM.user.password);
                if (checksAgainstPolicies === 0) {
                    // Password is valid, try registering the new user
                    usersFactory
                        .registerNewUser(registrationVM.user)
                        .then(function () {

                            // On successful registration redirect the user to the verification

                            $location.path(`/Verificar/${username}`);
                            $scope.$apply();
                            registrationVM.errorMessage = ''; // On successful registration clear the error message
                        }, function () {
                            registrationVM.errorMessage = 'Compruebe que el email es válido y que ha proporcionado un nombre de usuario y contraseña. Puede que el nombre de usuario o correo ya esté registrado';
                            $scope.$apply();
                        });
                } else {
                    registrationVM.errorMessage = checksAgainstPolicies;
                }
            }
        }
    }
    ]);
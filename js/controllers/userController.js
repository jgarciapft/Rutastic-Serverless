angular.module('Rutastic')
    .controller('userController',
        ['$routeParams', '$scope', '$location', 'usersFactory', function ($routeParams, $scope, $location, usersFactory) {
            let userVM = this;

            userVM.loggedUser = usersFactory.loggedCognitoUser !== undefined ? usersFactory.loggedCognitoUser.username : undefined;
            userVM.requestedUser = $routeParams.username;
            userVM.originalEmail = usersFactory.loggedCognitoUser !== undefined ? usersFactory.loggedCognitoUser.attributes.email : undefined
            userVM.newEmail = undefined // New user's email
            userVM.currentPassword = undefined // Current user's password
            userVM.newPassword = undefined // New user's password
            userVM.editionErrorMessage = '' // Message shown to the user in case any error arises during profile edition
            userVM.passwordChangeErrorMessage = '' // Message shown to the user in case any error arises during password change
            userVM.deletionErrorMessage = '' // Message shown to the user in case any error arises during profile deletion

            userVM.functions = {
                /**
                 * Form submission handler for user profile edition
                 */
                submitEditionForm: function () {

                    if (userVM.newEmail !== userVM.originalEmail) {
                        usersFactory
                            .updateEmail(userVM.newEmail)
                            .then(function (status) {
                                // On successful edition update values
                                if (status === 'SUCCESS') {
                                    alert('Correo electrónico actualizado correctamente');
                                    userVM.originalEmail = userVM.newEmail;
                                    userVM.editionErrorMessage = ''; // On successful edition clear any error message
                                    $scope.$apply();
                                }
                            }, function () {
                                userVM.editionErrorMessage = 'Compruebe que el nuevo correo sea válido. Puede que el correo ya esté en uso por otro usuario';
                                $scope.$apply();
                            });
                    }
                },
                submitChangePasswordForm: function () {
                    // Check all enforced password policies before trying to change the current password
                    let checksAgainstPolicies = usersFactory.checkPasswordAgainstPolicies(userVM.newPassword);
                    if (checksAgainstPolicies === 0) {
                        usersFactory.changePassword(userVM.currentPassword, userVM.newPassword)
                            .then(status => {
                                if (status === 'SUCCESS') {
                                    alert('La contraseña ha sido cambiada con éxito');
                                    userVM.currentPassword = '';
                                    userVM.newPassword = '';
                                    userVM.passwordChangeErrorMessage = '';
                                    $scope.$apply();
                                }
                            })
                            .catch(() => {
                                userVM.passwordChangeErrorMessage = 'La contraseña actual no es válida';
                                $scope.$apply();
                            })
                    } else {
                        userVM.passwordChangeErrorMessage = checksAgainstPolicies;
                    }
                },
                /**
                 * Form submission handler for user profile deletion
                 */
                submitDeletionForm: function () {
                    usersFactory
                        .deleteSelf()
                        .then(() => {
                            // On successful user profile deletion return to the landing page
                            alert('Su cuenta ha sido eliminada con éxito');
                            $location.path('/');
                            $scope.$apply();
                            userVM.deletionErrorMessage = ''; // On successful deletion clear any error message
                        })
                        .catch(() => {
                            userVM.deletionErrorMessage = 'Algo fue mal eliminando su perfil. Pruebe de nuevo más tarde';
                            $scope.$apply();
                        });
                }
            }
        }])
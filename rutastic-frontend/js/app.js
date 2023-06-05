import angular from 'angular';
import ngRoute from 'angular-route';
import Amplify from '@aws-amplify/core';
import moment from "moment";

moment.locale('es');

// Global SPA configuration
export const config = {
    aws: {
        cognito: {
            region: 'us-east-1',
            userPoolId: 'us-east-1_Q6c04CQrJ',
            userPoolWebClientId: '5hipro45ri2j3c7l2drniqi0l6'
        },
        apiGateway: {
            endpoint: 'https://1haxumupa7.execute-api.us-east-1.amazonaws.com/dev'
        }
    }
}

// Initialize Amplify library for authentication with existing Cognito User Pool
Amplify.configure({
    Auth: {
        // Amazon Cognito Region
        region: config.aws.cognito.region,
        // Amazon Cognito User Pool ID
        userPoolId: config.aws.cognito.userPoolId,
        // Amazon Cognito Web Client ID
        userPoolWebClientId: config.aws.cognito.userPoolWebClientId
    }
});

// Initialize AngularJS module for SPA
angular.module('Rutastic', [ngRoute])
    .config(function ($routeProvider) {
        $routeProvider
            // Default route mapped to the landing page
            .when('/', {
                controller: '',
                controllerAs: '',
                templateUrl: 'pages/landingPage.html',
            })
            .when('/Registro', {
                controller: 'registrationController',
                controllerAs: 'registrationVM',
                templateUrl: 'pages/registration.html'
            })
            .when('/Verificar/:username?', {
                controller: 'accountVerificationController',
                controllerAs: 'verificationVM',
                templateUrl: 'pages/verify.html'
            })
            .when('/Login/:username?', {
                controller: 'loginController',
                controllerAs: 'loginVM',
                templateUrl: 'pages/login.html'
            })
            .when('/usuarios/EditarPerfil/:username', {
                controller: 'userController',
                controllerAs: 'userVM',
                templateUrl: 'pages/editProfile.html'
            })
            .when('/rutas/CrearRuta', {
                controller: 'routeHandlerController',
                controllerAs: 'routeHandlerVM',
                templateUrl: 'pages/routeCRUD.html'
            })
            .when('/rutas/EditarRuta/:ID', {
                controller: 'routeHandlerController',
                controllerAs: 'routeHandlerVM',
                templateUrl: 'pages/routeCRUD.html'
            })
            .when('/rutas/FiltrarRutas', {
                controller: 'routeQueryController',
                controllerAs: 'routeQueryVM',
                templateUrl: 'pages/queryRoutes.html'
            })
            .when('/rutas/DetallesRuta/:ID', {
                controller: 'routeDetailsController',
                controllerAs: 'routeDetailsVM',
                templateUrl: 'pages/routeDetails.html'
            })
        ;
    });
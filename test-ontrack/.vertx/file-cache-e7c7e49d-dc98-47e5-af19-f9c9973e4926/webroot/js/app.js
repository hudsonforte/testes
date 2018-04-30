angular.module('OntrackApp', []).config(['$routeProvider', function ($routeProvider) {
    $routeProvider.
        when('/', {templateUrl: '/tpl/listar.html', controller: ListCtrl}).
        when('/novoCliente', {templateUrl: '/tpl/novo.html', controller: AddCtrl}).
        when('/edit/:id', {templateUrl: '/tpl/editar.html', controller: EditCtrl}).
        otherwise({redirectTo: '/'});
}]);

function ListCtrl($scope, $http) {
    $http.get('/clientes').success(function (data) {
        $scope.clientes = data;
    });
}

function AddCtrl($scope, $http, $location) {
    $scope.master = {};
    $scope.activePath = null;

    $scope.insert = function (cliente, AddNewForm) {

        $http.post('/clientes', cliente).success(function () {
            $scope.reset();
            $scope.activePath = $location.path('/');
        });

        $scope.reset = function () {
            $scope.cliente = angular.copy($scope.master);
        };

        $scope.reset();

    };
}

function EditCtrl($scope, $http, $location, $routeParams) {
    var id = $routeParams.id;
    $scope.activePath = null;

    $http.get('/clientes/' + id).success(function (data) {
        $scope.cliente = data;
    });

    $scope.update = function (cliente) {
        $http.put('/clientes/' + id, cliente).success(function (data) {
            $scope.cliente = data;
            $scope.activePath = $location.path('/');
        });
    };

    $scope.remove = function (cliente) {
            $http.delete('/clientes/' + id)
                .success(function(data, status, headers, config) {
                    $scope.activePath = $location.path('/');
                }).
                error(function(data, status, headers, config) {
                    console.log("error");
                    // custom handle error
                });
    };
}
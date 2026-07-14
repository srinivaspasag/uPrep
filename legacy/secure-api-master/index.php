<?php
use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

require 'vendor/autoload.php';

$app = new \Slim\App;

$app->get("/",function(Request $request, Response $response, array $args) {
    $response->getBody()->write('Hello World');
    return $response;
});


$app->post("/instamojo-payment-request", function (Request $request, Response $response, array $args) {
    $headers = $request->getHeaders();
    $xapikey = $request->getHeaderLine('HTTP_X_API_KEY');
    $xauthtoken = $request->getHeaderLine('HTTP_X_AUTH_TOKEN');
    $body = $request->getParsedBody();
    $method = $request->getMethod();
    $curl_resp = callInstaMojoApi($xapikey,$xauthtoken,$body,$method);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($curl_resp));
});

$app->post("/get-payment-status", function (Request $request, Response $response, array $args) {
    $headers = $request->getHeaders();
    $xapikey = $request->getHeaderLine('HTTP_X_API_KEY');
    $xauthtoken = $request->getHeaderLine('HTTP_X_AUTH_TOKEN');
    $body = $request->getParsedBody();
    $method = "GET";
    $curl_resp = callInstaMojoApi($xapikey,$xauthtoken,$body,$method);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($curl_resp));
});

$app->post("/access-token", function (Request $request, Response $response, array $args) {
    $headers = $request->getHeaders();
    $body = $request->getParsedBody();
    $method = $request->getMethod();
    $curl_resp = callInstaMojoApi("","",$body,$method);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($curl_resp));
});

function callInstaMojoApi($apikey,$authToken,$bodyParams,$methodType){
    if($apikey != "" && $authToken !== ""){
        $header = array(
        'x-api-key:'.$apikey,
        'x-auth-token:'.$authToken
        );
    }
    else{
        $header = array();
    }
    $url = $bodyParams['instamojo_url'];
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL,$url);
    if($methodType === "GET"){
    }
    else {
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $bodyParams);
    }
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HEADER, 'Content-Type: application/x-www-form-urlencoded');
    if(sizeof($header) > 0){
        curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
    }
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
    $response = curl_exec($ch);
    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $response = json_decode($response , true);
    $response['httpcode'] = $httpcode;
    curl_close($ch);
    return $response;
}

$app->run();
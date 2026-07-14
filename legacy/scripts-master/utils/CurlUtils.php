<?php
class CurlUtils
{
    function curlExecution($url, $params , $methodType){
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL,$url);
        if($methodType === "GET"){
        }
        else {
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
        }
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HEADER, 'Content-Type: application/x-www-form-urlencoded');
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        $response = curl_exec($ch);
        $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $response = json_decode($response , true);
        $response['httpcode'] = $httpcode;
        curl_close($ch);
        return $response;
    }
}
?>
<?php

require_once ("../utils/CurlUtils.php");

$url = "3.7.93.20:19012/members/getMemberProfile";
$curl = new CurlUtils();
$row = 1;
$dump = array();
$params = array(
    "orgId"=>"58c244d9e4b0d17e65cab555",
    "targetUserId"=>"",
    "callingUserId"=>"",
    "userId"=>"",
    "callingApp"=>"cmds-app",
    "callingAppId"=>"cmds-app"
);
$file="members.csv";

if (($handle = fopen($file, "r")) !== FALSE) {
    while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
        $num = count($data);
        $row++;
        $userId = $data[0];
        array_push($dump, array("userId" => $userId));
    }
    fclose($handle);
}

$count = 0;
foreach ($dump as $user) {
    $params['targetUserId'] = $user['userId'];
    $params['userId'] = $user['userId'];
    $params['callingUserId'] = $user['userId'];
    $response = $curl->curlExecution($url, $params, 'POST');
    $httpCode = $response['httpcode'];
    if ($httpCode !== 200) {
        die("Error is came");
    }
    if(!empty($response['errorCode'])) {
        echo "Error in userId: ".$user['userId']."<br />";
        echo "Error is came: ".$response['errorCode']."<br />";
        echo "Error is came: ".$response['errorMessage']."<br />";
        continue;
    }
    $count++;
    echo "Done for ".$count." users, going to next user<br />";
}

<!--Steps Required to run this script 
    1.Change URL to prod or qa
    2.Get userId and orgMemberID by running script(getUserIdAndOrgMemberId.js) inside mongo-script folder
    3.Change orgId  to required Organization
    4.Get endTime from currentMillis.
    5.Change type param accordingly (type is param in url ex:localhost:8086/addProgramsToStudents.php?type=test)
    6.Change sectionId accordingly.
-->
<?php

require_once ("utils/CurlUtils.php");
#Change url to prod or qa
$url = "http://localhost:19012/members/updateEndDateMapping";
$curl = new CurlUtils();
$row = 1;
$dump = array();
if(!isset($_GET['type'])) {
    die("Type no there");
}
$type = $_GET['type'];
$file = '';
$params = array(
    "sectionId" => "",
    "targetOrgMemberId"=>"",
    "orgId"=>"584e44e8c92e5dca741bae2a",
    "targetUserId"=>"",
    "endTime"=>1542133800000
    );

if($type == 'updateJee') {
    $file = "data/updateEndDateMapping.csv";
    $params["sectionId"] = "584e89b5c92e3fb3237fa623";
}
else {
    die("Type is bad");
}
if (($handle = fopen($file, "r")) !== FALSE) {
    while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
        $num = count($data);
        $row++;
        $userId = $data[0];
        $orgMemberId = $data[1];
        array_push($dump, array("userId" => $userId, "orgMemberId" => $orgMemberId));
    }
    fclose($handle);
}


$count = 0;
foreach ($dump as $user) {
    $params['targetOrgMemberId'] = $user['orgMemberId'];
    $params['targetUserId'] = $user['userId'];
    // $params["sellerReferenceNo"] = "lp".$type.$count;
    $response = $curl->curlExecution($url, $params, 'POST');
    $httpCode = $response['httpcode'];
    if ($httpCode !== 200) {
        die("Error found");
    }
    if(!empty($response['errorCode'])) {
        echo "Error in userId: ".$user['userId']."<br />";
        echo "ErrorCode is : ".$response['errorCode']."<br />";
        echo "ErrorMessage is : ".$response['errorMessage']."<br />";
        continue;
    }
    $count++;
    echo "Done for ".$count." users, going to next user<br />";
}
?>
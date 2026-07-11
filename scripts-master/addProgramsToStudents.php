<!--Steps Required to run this script 
    1.Change URL to prod or qa
    2.Get userId and orgMemberID by running script(getUserIdAndOrgMemberId.js) inside mongo-script folder
    3.Change orgId and contentSrc.id to required Organization
    4.Get userId of superAdmin of that particular organization and change callingUserId and userId accordingly.
    5.Change point of sale param accordingly.
    6.Change type param accordingly (type is param in url ex:localhost:8086/addProgramsToStudents.php?type=test)
    7.Change sectionId programId and centerId accordingly.
    8.Change Seller Reference Number to start string with point of sale param value.
-->
<?php
#Script to addProgramsToStudents (365 days access)
require_once ("utils/CurlUtils.php");
#The tabappRouter controller to do the curl request
#Change url to prod or qa
$url = "http://localhost:19002/tabapprouter/addOrgMemberMapping";
#Creating object to execute curl
$curl = new CurlUtils();
$row = 1;
$dump = array();
if(!isset($_GET['type'])) {
    die("Type no there");
}
$type = $_GET['type'];
$file = '';
#Params required to pass to the controller
#Make sure to pass the orgId of specific organization
#Also make sure to get userId of super admin of that particular organization
$params = array("callingApp"=>"TabApp",
    "callingUserId"=>"584e44eec92e5dca741bae2d",
    "sectionIds[0]" => "",
    "callingAppId"=>"TabApp",
    "targetOrgMemberId"=>"",
    "orgId"=>"584e44e8c92e5dca741bae2a",
    "contentSrc.id"=>"584e44e8c92e5dca741bae2a",
    "programId"=>"",
    "targetProfile"=>"STUDENT",
    "targetUserId"=>"",
    "userId"=>"584e44eec92e5dca741bae2d",
    "contentSrc.type"=>"ORGANIZATION",
    "centerId"=>"",
    "profile"=>"SUPER_ADMIN",
    "sellerReferenceNo"=>"",
    "packageDays"=>365,
    "pointOfSale"=>"lp");
#Get userId and orgMemberId from user via URL based on type
if($type == 'test') {
    #Generate this file using the mongo-script
    #This file contains userId and orgMemberId of students
    $file = "data/testStudent.csv";
    #Getting the programId, sectionId and centerId of particular program that needs to be assigned to set of students
    $params["sectionIds[0]"] = "584e89b5c92e3fb3237fa623";
    $params["programId"] = "584e894fc92e3fb3237fa61e";
    $params["centerId"] = "584e47ebc92e5dca741bae3b";
}
else {
    die("Type is bad");
}
#Reads data from CSV and parses data to an array
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
#Doing curl request for each single user from the dump array
foreach ($dump as $user) {
    $params['targetOrgMemberId'] = $user['orgMemberId'];
    $params['targetUserId'] = $user['userId'];
    $params["sellerReferenceNo"] = "lp".$type.$count;
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
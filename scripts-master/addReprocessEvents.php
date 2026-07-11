<!--Steps Required to run this script 
    1.Change URL to prod or qa
    2.Get cmdsVideoIds that needs to be converted from mongo.
-->
<?php

require_once ("utils/CurlUtils.php");
#Change url to prod or qa
$url = "http://localhost:19014/cmdsVideos/convertAgain/";
$params=array();
$curl = new CurlUtils();
$row = 1;
$dump = array();
#This file should contain a list of cmds video Ids to convert
$file = 'data/reprocessevents.csv';
if (($handle = fopen($file, "r")) !== FALSE) {
    while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
        $num = count($data);
        $row++;
        $cmdsVideoId = $data[0];
        array_push($dump, array("cmdsVideoId" => $cmdsVideoId));
    }
    fclose($handle);
}
$count = 0;
foreach ($dump as $cmdsVideo) {
    $newurl = $url;
    $newurl = $url.$cmdsVideo['cmdsVideoId'];
    $response = $curl->curlExecution($newurl, $params, 'POST');
    $httpCode = $response['httpcode'];
    if ($httpCode !== 200) {
        die("Error found");
    }
    if(!empty($response['errorCode'])) {
        echo "Error in cmdsVideoId: ".$cmdsVideo['cmdsVideoId']."<br />";
        echo "ErrorCode is : ".$response['errorCode']."<br />";
        echo "ErrorMessage is : ".$response['errorMessage']."<br />";
        continue;
    }
    $count++;
    echo "Done for ".$count." cmdsVideos, going to next cmdsVideo<br />";
}
?>
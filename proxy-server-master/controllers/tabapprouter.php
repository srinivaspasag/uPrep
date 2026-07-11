<?php
use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

$app->get('/hello/{name}', function (Request $request, Response $response) {
    $name = $request->getAttribute('name');
    $response->getBody()->write("Hello, $name");

    return $response;
});

$app->post('/getContentLinks', function (Request $request, Response $response) {
    $url = "https://cmds.learnpedia.in/tabapprouter/getContentLinks";
    $targetType = $request->getParam("target_type");
    $targetId = $request->getParam("target_id");
    $start = $request->getParam("start");
    $addedAfter = $request->getParam("addedAfter");
    $size = $request->getParam("size");

    $dataBase = new DbUtils();

    $countQuery = getCountQuery($addedAfter, $targetId, $targetType);
    $countResult = $dataBase->db->query($countQuery);
    if(!$countResult) {
        die("Error in Query ".$countQuery."\r\n".$dataBase->db->error);
    }
    $row = $countResult->fetch_row();
    $totalHits = $row[0];

    $returnResult = ['totalHits' => $totalHits, 'serverTime' => 0, 'latestContent' => $addedAfter, "cumulativeErrorCode" => null, 'list' => array()];

    $selectQuery = getSelectQuery($addedAfter, $targetId, $targetType, $start, $size);
    $dbResult = $dataBase->db->query($selectQuery);
    if(!$dbResult) {
        die("Error in Query ".$selectQuery."\r\n".$dataBase->db->error);
    }

    while($row = $dbResult->fetch_assoc()) {
        array_push($returnResult['list'], json_decode($row['data']));
    }
    $returnResult['latestContent'] = array_values(array_slice($returnResult['list'], -1))[0];
    $returnResult['serverTime'] = round(microtime(true) * 1000);
    $result = ['errorCode' => '', 'errorMessage' => '', 'result' => $returnResult];
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($result));
});

$app->post('/getEntityLeaderBoard', function (Request $request, Response $response) {
    $testId = $request->getParam("entity_id");
    $start = $request->getParam("start");
    $size = $request->getParam("size");

    $dataBase = new DbUtils();

    $rankListCountQuery = getStudentsCountForTest($testId);
    $rankListCountQueryResult = $dataBase->db->query($rankListCountQuery);
    if(!$rankListCountQueryResult) {
        die("Error in Query ".$rankListCountQuery."\r\n".$dataBase->db->error);
    }
    $row = $rankListCountQueryResult->fetch_row();
    $totalHits = $row[0];

    $rankListQuery = getRankListQuery($testId, $start, $size);
    $rankListQueryResult = $dataBase->db->query($rankListQuery);
    if(!$rankListQueryResult) {
        die("Error in Query ".$rankListQuery."\r\n".$dataBase->db->error);
    }    
    $returnResult = ['totalHits' => $totalHits, 'cumulativeErrorCode' => null, 'info' => "", 'list' => array()];
    while($row = $rankListQueryResult->fetch_assoc()) {
        array_push($returnResult['list'], json_decode($row['data']));
    }
    $result = ['errorCode' => '', 'errorMessage' => '', 'result' => $returnResult];
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($result));
});


$app->post('/getUserEntityRank', function (Request $request, Response $response) {
    $userId = $request->getParam("userId");
    $testId = $request->getParam("entity_id");

    $dataBase = new DbUtils();

    $rankQuery = getRankQuery($userId, $testId);
    $rankQueryResult = $dataBase->db->query($rankQuery);
    if(!$rankQueryResult) {
        die("Error in Query ".$rankQuery."\r\n".$dataBase->db->error);
    }    
    $rank;
    $user;
    while($row = $rankQueryResult->fetch_assoc()) {
        $data = json_decode($row['data'],true);
        $rank = $data['rank'];
        $user = $data['user'];
    }
    $returnResult = ['rank' => $rank, 'user' => $user];
    $result = ['errorCode' => '', 'errorMessage' => '', 'result' => $returnResult];
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($result));
});


$app->post("/updateApk",function(Request $request, Response $response){
    $req_version = $request->getParam("version");
    $directory = "cache/Apkuploads";
    $files = glob($directory."/*.apk");
    if(sizeof($files) === 0){
        $res = array('errorMessage' => 'No apk files available','errorCode'=>'MISSING_APK','result' => '');
    }
    else{
        $application_name = basename($files[0],".apk");
        $filename = explode("-", $application_name);
        $version = $filename[1];
        $apkdetails = array('version'=>$version,'url'=>$files[0]);
        if($req_version < $version){
            $res = array('errorMessage' => '','errorCode'=>'','result' =>$apkdetails);
        }else{
            $res = array('errorMessage' => 'Version already upto date','errorCode'=>'NO_UPDATES','result'=>'');
        }
    }
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});

$app->post('/getContents', function (Request $request, Response $response) {
    $url = "https://cmds.learnpedia.in/tabapprouter/getContents";
    $testId = $request->getParam("entity_id");
    $directory = "cache/tests";
    $file = $testId.".json";
    $filePath = $directory."/".$file;

    if(file_exists($filePath) && filesize($filePath) > 0){
        $result = file_get_contents($filePath);
        return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write($result);
    }elseif(empty($testId)){
        $res = array('errorMessage' => '', 'errorCode' => 'NO_BODY_PARAMETERS', 'result' => '');
        return $response->withStatus(401)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    }else{
        // $params = $request->getBody()->getContents();
        // $curl = new CurlUtils();
        // if(!is_dir($directory)) {
        //     mkdir($directory, 0777, true);
        // }
        // $contents = $curl->createFileByCurlExecution($url,$filePath,$params, true);
        // $content = json_decode($contents, true);
        // if(!empty($content['errorCode'])){
        //     unlink($filePath);
        // }elseif (empty($content['errorCode']) && $content["result"]['totalHits'] === 0) {
        //     unlink($filePath);
        // }
        $res = array('errorMessage' => '', 'errorCode' => 'TEST_NOT_FOUND', 'result' => '');
        return $response->withStatus(401)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    }
});

$app->post("/checkConnection" , function (Request $request, Response $response){
    $url = "https://cmds.learnpedia.in/login";
    $params='';
    $curl = new CurlUtils();
    $resp = $curl->curlExecution($url,$params,"GET");
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($resp));
});

$app->post('/syncTabletAnalytics', function (Request $request, Response $response) {
    $testId = $request->getParam("entityId");
    $userId = $request->getParam("userId");
    $deviceId = $request->getParam('deviceId');
    $data = json_encode($request->getBody()->getContents());
    $database = new DbUtils();
    $insertData = [
        'userId' => escapeString($userId),
        'testId' => escapeString($testId),
        'deviceId' => escapeString($deviceId),
        'data' => $data,
        'time_created'=>round(microtime(true) * 1000),
        'synced' =>0
    ];
    $insertResult = $database->insertToTable($insertData, ENTITY_USER_ATTEMPTS, "IGNORE");
    if($insertResult !== true) {
        die("Data no inserted");
    }
    // $directory = "cache/testsubmissions/".$testId;
    // $file = $userId.".txt";
    // $filePath = $directory."/".$file;

    // if(file_exists($filePath) && filesize($filePath) > 0){
    //     $res = array('errorMessage' => '', 'errorCode' => 'MULTI_ATTEMPTS_NOT_ALLOWED', 'result' => '');
    //     return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    // }elseif(empty($testId) || empty($userId)){
    //     $res = array('errorMessage' => '', 'errorCode' => 'NO_BODY_PARAMETERS', 'result' => '');
    //     return $response->withStatus(401)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    // }else{
        // $params = $request->getBody()->getContents();
        // mkdir($directory, 0777, true);
        // $curl = new CurlUtils();
        // $curl->createFile($filePath,$params);
        $result = array('processed' => true);
        $res = array('errorMessage' => '', 'errorCode' => '', 'result' => $result);
        return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});

$app->get('/totalTestsSubmissions', function (Request $request, Response $response){
    // $directory = "cache/testsubmissions";
    // $tests = glob($directory."/*", GLOB_ONLYDIR);
    $log = Logger::getLogger('myLogger');
    $params = $request->getQueryParams();
    $testsCount = 0;
    $list = array();
    $database = new DbUtils();
    $queryParams = [
        0 => 'test_name',
        1 => 'testId',
        2 => 'last_attempt_time'
    ];
    $sql_query = "SELECT COUNT(*) AS test_count, test_name,testId, MAX(time_created) AS last_attempt_time FROM ".ENTITY_USER_ATTEMPTS." INNER JOIN ".TEST_NAME_TABLE." ON user_entity_attempts.testId = test_name.test_id GROUP BY testId";
    $totalTestsSubmissionsQuery = populateDataTableParams($params,$queryParams,$sql_query);
    $dbResult = $database->db->query($totalTestsSubmissionsQuery);
    $filteredCountQuery = populateDataTableFilterParams($params,$queryParams,$sql_query);
    $filteredCountResult = $database->db->query($filteredCountQuery);
    $recordsTotal = mysqli_num_rows($filteredCountResult);
    if(!$dbResult) {
        die("Error in Query ".$totalTestsSubmissionsQuery."\r\n".$database->db->error);
    }
    if(mysqli_num_rows($dbResult) > 0){
            while($row = $dbResult->fetch_assoc()) {
            $testsCount++;
            $temp = array('testId'=>$row['testId'],'testName'=>$row['test_name'],'count'=>$row['test_count'],
                'last_atttempt_time'=>$row['last_attempt_time']);
            array_push($list,$temp);
            // array_push($returnResult['list'], json_decode($row['data']));
        }
    }
    // foreach ($tests as $test) {
    //     $users = [];
    //     $userNames = [];
        // $testId = basename($test);
        // $count = count(glob($test."/*.txt"));
        // if ($count > 0) {
        //     $testsCount ++;
        //     foreach (glob($test."/*.txt") as $file) {
        //         array_push($users,basename($file,".txt"));
        //     }
        //     $fetchUserQuery = getNames($users);
        //     // $userNames = $database->db->query($fetchUserQuery);
        //     $log->info("Query is ".$fetchUserQuery);
        //     $userNameQuery = $database->db->query($fetchUserQuery);
        //     while ($row = $userNameQuery->fetch_assoc()) {
        //         array_push($userNames,$row['studentname']);
        //         // $log->info("UserName is ".var_dump($userNames));
        //     }
        //     $testNameQuery = getTestNameQuery($testId);
        //     $testName = $database->db->query($testNameQuery);
        //     $testName = $testName->fetch_assoc();
        //     $temp = array('testId' => $testId, 'testName' => $testName['test_name'], 'count' => $count,'userNames'=>$userNames);
        //     array_push($list, $temp);
        // }
    // }
    $res = array("totalTests" => $testsCount, "tests" => $list,"recordsFiltered"=>$recordsTotal,"recordsTotal"=>$recordsTotal);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});

$app->post('/submitTest/{testId}', function (Request $request, Response $response){
    $url = "https://cmds.learnpedia.in/tabapprouter/syncTabletAnalytics";
    $testId = $request->getAttribute("testId");
    // $directory = "cache/testsubmissions/".$testId;
    $res = syncProxyAnalytics($testId,$url);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    // if(is_dir($directory)){
    //     $filePaths = glob($directory."/*.txt");
    //     $intialCount = count($filePaths);
    //     $curl = new CurlUtils();
    //     foreach ($filePaths as $filePath) {
    //         $params = file_get_contents($filePath);
    //         $resp = ($curl->curlExecution($url, $params,"POST"));
    //         if($resp['httpcode'] === 200){
    //             if(empty($resp['errorCode']) || $resp['errorCode'] == 'MULTI_ATTEMPTS_NOT_ALLOWED'){
    //                 unlink($filePath);
    //                 $submittedCount++;
    //             }
    //         }
    //     }
    //     $submits = array('totalSubmits' => $submittedCount, 'initialCount' => $intialCount);
    //     $res = array('errorMessage' => '', 'errorCode' => '', 'result' => $submits);
    //     return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    // }else{
    //     $res = array('errorMessage' => '', 'errorCode' => 'INVALID_DIRECTORY', 'result' => '');
    //     return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    // }
});

function syncProxyAnalytics($testId,$url){
    $log = Logger::getLogger('myLogger');
    $submittedCount = 0;
    if($testId != null){
        $getTestSubmissions = getTestSubmissions($testId);
    }
    else{
        $getTestSubmissions = getTestSubmissions(null);
    }
    $database = new DbUtils();
    $dbResult = $database->db->query($getTestSubmissions);
    if(!$dbResult) {
        die("Error in Query ".$getTestSubmissions."\r\n".$database->db->error);
    }
    $curl = new CurlUtils();
    $initialCount = (int) mysqli_num_rows($dbResult);
    $log->info("Initial Count of users fetched to sync proxy analytics ".$initialCount);
    while($row = $dbResult->fetch_assoc()) {
        $log->info("Inside while of syncProxyAnalytics");
        $params = $row['data'];
        $testId = $row['testId'];
        $userId = $row['userId'];
        // var_dump($params);
        $resp = ($curl->curlExecution($url, $params, "POST"));
        // $log->info("Query is ".$fetchUserQuery);
        // var_dump($resp);
        if($resp['httpcode'] === 200){
            if(empty($resp['errorCode']) || $resp['errorCode'] == 'MULTI_ATTEMPTS_NOT_ALLOWED'){
                $log->info("userId is".$userId);
                $log->info("TestId is".$testId);
                $log->info("errorCode received is".$resp['errorCode']);
                $submittedCount++;
                updateSyncInUserEntityAttempts($userId,$testId);
            }
        }
        // array_push($returnResult['list'], json_decode($row['data']));
    }
    $submits = array('totalSubmits' => $submittedCount, 'initialCount' => $initialCount);
    $res = array('errorMessage' => '', 'errorCode' => '', 'result' => $submits);
    return $res;
}

function updateSyncInUserEntityAttempts($userId,$testId){
    $database = new DbUtils();
    $updateQuery = updateSyncInUserEntityAttemptsQuery($userId,$testId);
    $updateResult = $database->db->query($updateQuery);
    if(!$updateResult) {
        die("Error in Query ".$updateQuery."\r\n".$database->db->error);
    }
    return $updateResult;
}

$app->post('/submitAllTests', function (Request $request, Response $response){
    $url = "https://cmds.learnpedia.in/tabapprouter/syncTabletAnalytics";
    $res = syncProxyAnalytics(null,$url);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    // $directory = "cache/testsubmissions";
    // $tests = glob($directory."/*", GLOB_ONLYDIR);
    // $curl = new CurlUtils();
    // foreach ($tests as $test) {
    //     $filePaths = glob($test."/*.txt");
    //     $totalCount += count($filePaths);
    //     foreach ($filePaths as $filePath) {
    //         $params = file_get_contents($filePath);
    //         $resp = ($curl->curlExecution($url, $params,"POST"));
    //         if($resp['httpcode'] === 200){
    //             if(empty($resp['errorCode']) || $resp['errorCode'] == 'MULTI_ATTEMPTS_NOT_ALLOWED'){
    //                 unlink($filePath);
    //                 $submittedCount++;
    //             }
    //         }
    //     }
    // }
    // $submits = array('totalSubmits' => $submittedCount, 'initialCount' => $totalCount);
    // $res = array('errorMessage' => '', 'errorCode' => '', 'result' => $submits);
    // return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});


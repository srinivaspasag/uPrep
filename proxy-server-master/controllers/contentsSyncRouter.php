<?php
use \Psr\Http\Message\ServerRequestInterface as Request;
use \Psr\Http\Message\ResponseInterface as Response;

$app->post('/authenticateAdmin', function (Request $request, Response $response) {
    $url = "https://cmds.learnpedia.in/tabapprouter/authenticate";
    $curl = new CurlUtils();
    $resp = $curl ->curlExecution($url, LOGINPARAMS, 'POST');

    $httpCode = $resp['httpcode'];
    if ($httpCode !== 200) {
        return $response->withStatus($httpCode)->withHeader('Content-Type', 'text/plain')->write('Error');
    }

    if(!empty($resp['errorCode'])) {
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json')->write(json_encode($resp));
    }

    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($resp));

});

$app->post("/syncOrgInfo",function(Request $request,Response $response){
    $url = "https://cmds.learnpedia.in/tabapprouter/getOrgInfo";
    $curl = new CurlUtils();
    $params = ORG_INFO_PARAMS;
    $directory = "cache/";
    $file = "orgInfo.json";
    $filePath = $directory.$file;
    if(file_exists($filePath) && filesize($filePath) > 0){
        unlink($filePath);
    }
    if(!is_dir($directory)) {
        mkdir($directory, 0777, true);
    }
    $contents = $curl->createFileByCurlExecution($url, $filePath, $params, false);
    $content = json_decode($contents, true);
    if(!empty($content['errorCode'])){
        if(file_exists($filePath)){
            unlink($filePath);
        }
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json')->write(json_encode($content));
    }
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($content));
});

$app->post('/getOrgInfo',function(Request $request, Response $response){
    $directory = "cache/";
    $file = "orgInfo.json";
    $filePath = $directory.$file;
    if(file_exists($filePath) && filesize($filePath) > 0){
        $result = file_get_contents($filePath);
        return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write($result);
    }
    else{
        $res = array('errorMessage' => '', 'errorCode' => 'ORG_INFO_NOT_FOUND', 'result' => '');
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
    }
});

$app->post('/syncUsers',function(Request $request, Response $response){
    $url = "https://cmds.learnpedia.in/tabapprouter/getAllUserData";
    $params['callingApp'] = 'TabApp';
    $params['callingAppId'] = 'TabApp';
    $params['orgId'] = ORGID;
    $database = new DbUtils();
    $targetUserId = $request->getParam("targetUserId");
    if($targetUserId != null){
        $params['targetUserId'] = $targetUserId;
    }
    else{
        $constructedQuery = getLastUpdated();
        $getLastUpdatedQuery = $database->db->query($constructedQuery);
        if($getLastUpdatedQuery && $getLastUpdatedQuery->num_rows > 0){
            $getLastUpdated = $getLastUpdatedQuery->fetch_assoc();
            $params['lastUpdated'] = (int)($getLastUpdated['last_updated']);
        }
        else{
            //Do not set lastUpdated value.
            // $params['lastUpdated'] = 0;
        }
    }
    // $params['targetUserId'] = "5ae959ffe4b05eaa8fd28f85";
    $curl = new CurlUtils();
    $result = $curl->curlExecution($url, $params, 'POST');
    $users = $result['result']['users'];
    $usersSynced = 0;
    $jsResult = [];
    $jsResult['error'] = array();
    foreach ($users as $user) {
        # code...
        $jsonValue = json_encode($user);
        $jsonValue = $database->db->real_escape_string($jsonValue);
        $insertData = [
            'memberId' => escapeString($user['memberId']),
            'username' => escapeString($user['username']),
            'userId' => escapeString($user['id']),
            'usersalt' => escapeString($user['salt']),
            'userpassword' => escapeString($user['password']),
            'studentname' => escapeString($user['orgProfile']['info']['firstName']." ". $user['orgProfile']['info']['lastName']),
            'last_updated' => escapeString($user['orgProfile']['info']['lastUpdated']),
            'info'=> escapeString($jsonValue)
        ];
        $insertResult = $database->insertToTable($insertData, USER_TABLE, "REPLACE");
        if($insertResult !== true) {
            $jsResult['error'] = $database->db->error;
            $jsResult['usersSynced'] = $usersSynced;
            return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($jsResult));
        }
        else{
            $usersSynced++;
        }
    }
    $jsResult['usersSynced'] = $usersSynced;
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($jsResult));
});

$app->get("/getUsers",function(Request $request, Response $response){
    $log = Logger::getLogger('myLogger');
    $reqParams = $request->getQueryParams();
    $database = new DbUtils();
    $data = [];
    $queryParams = [
        0 => 'studentname',
        1 => 'memberId',
        2 => 'userId'
    ];
    $sql_query = "SELECT memberId,studentname,userId FROM ".USER_TABLE."";
    $constructedQuery = populateDataTableParams($reqParams,$queryParams,$sql_query);
    // $log->info("query is".$constructedQuery);
    // $userResp = array("data"=>$constructedQuery);
    $dbResult = $database->db->query($constructedQuery);
    $filteredCountQuery = populateDataTableFilterParams($reqParams,$queryParams,$sql_query);
    $filteredCountResult = $database->db->query($filteredCountQuery);
    $recordsTotal = mysqli_num_rows($filteredCountResult);
    $userResp = [];
    if(!$dbResult) {
        die("Error in Query ".$selectQuery."\r\n".$database->db->error);
    }
    if(mysqli_num_rows($dbResult) > 0){
        while($row = $dbResult->fetch_assoc()) {
                $data[] = $row;
        }
    }
    $userResp = array("data"=>$data,"recordsTotal"=>$recordsTotal,"recordsFiltered"=>$recordsTotal);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($userResp));
});

$app->post("/authenticate",function(Request $request, Response $response){
    $password = $request->getParam("password");
    $useGlobalUsername = $request->getParam("useGlobalUsername");
    $database = new DbUtils();
    $username = "";
    if($useGlobalUsername){
        $username = $request->getParam("username");
    }
    else{
        $memberId = $request->getParam("memberId");
        $username = ORGID.":".$memberId;
    }
    $constructedQuery = getUser($username);
    $userInfoQuery = $database->db->query($constructedQuery);
    if($userInfoQuery && $userInfoQuery->num_rows > 0){
        $userInfo = $userInfoQuery->fetch_assoc();
        $validateCredentials = validateUserCredentials($userInfo['usersalt'],$userInfo['userpassword'],$username,$password);
        if($validateCredentials){
            $res = array('errorMessage' => '', 'errorCode' => '', 'result' => json_decode($userInfo['info']),true);
        }
        else{
            $res = array('errorMessage' => 'Please check your credentials.', 'errorCode' => 'INCORRECT_PASSWORD','result'=>'');
        }
    }
    else{
        $res = array('errorMessage' => 'User not found in proxy.', 'errorCode' => 'USER_NOT_FOUND', 'result' => '');
    }
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});

function validateUserCredentials($usersalt,$hashedPassword,$username,$userpassword){
    $saltedPassword = $usersalt."/vdntu/".$userpassword;
    $sha256Password = hash("sha256",$saltedPassword,true);
    $encryptedPassword = base64_encode($sha256Password);
    if($hashedPassword === $encryptedPassword ){
        return true;
    }
    else{
        return false;
    }
}

$app->post('/syncProgram', function (Request $request, Response $response) {
    $log = Logger::getLogger('myLogger');

    $url = "https://cmds.learnpedia.in/tabapprouter/getContentLinks";
    $targetId = $request->getParam("targetId");
    $userId = $request->getParam("userId");
    $log->info("Inside Sync Program. TargetId is ".$targetId);
    $params = CONTENT_LINK_PARAMS;
    $params['userId'] = $userId;
    $params['target.id'] = $targetId;
    $params['target.type'] = "SECTION";
    $params['callingUserId'] = $userId;
    $params['targetUserId'] = $userId;

    $database = new DbUtils();
    $constructedQuery = getAddedAfterQuery($targetId, "SECTION");
    $log->info("Query -> ".$constructedQuery);
    $addedAfter = $database->db->query($constructedQuery);
    if($addedAfter && $addedAfter->num_rows > 0) {
        $timeCreated = $addedAfter->fetch_assoc();
        $log->info("Added After value ".$timeCreated['time_created']);
        $params['addedAfter'] = 1 + $timeCreated['time_created'];
    }
    else {
        $params['addedAfter'] = 0;
    }

    $curl = new CurlUtils();
    $start = 0;
    $size = $params['size'];

    $contentFound = 0;
    $moduleIds = array();
    $failed = 0;
    $jsResult = [];
    $jsResult['error'] = array();
    while (true) {
        $params['start'] = $start;
        $result = $curl->curlExecution($url, $params, 'POST');
        $result = $result['result'];
        $list = $result['list'];

        $listSize = sizeof($list);
        if($listSize === 0) {
            break;
        }
        else {
            $contentFound += $listSize;
        }

        foreach ($list as $value) {
            $timeCreated = $value['lastUpdated'];
            $type = $value['content']['type'];
            $contentId = $value['content']['id'];

            if($type == "MODULE") {
                array_push($moduleIds, $value['content']['id']);
            }
            $jsonValue = json_encode($value);
            $jsonValue = $database->db->real_escape_string($jsonValue);
            $insertData = ['time_created' => $timeCreated, 'content_id' => escapeString($contentId), 'content_type' => escapeString($type), 'data' => escapeString($jsonValue),
                'target_type' => "'SECTION'", 'target_id' => escapeString($targetId)];
            $insertResult = $database->insertToTable($insertData, CONTENT_LINKS_TABLE);
            if($insertResult !== true) {
                $failed++;
                array_push($jsResult['error'], $database->db->error);
            }
            if ($type == "TEST") {
                $name = $value['content']['name'];
                $nameValues = array('test_id' => escapeString($contentId), 'test_name' => escapeString($name));
                $nameInsertResult = $database->insertToTable($nameValues, TEST_NAME_TABLE);
                if($nameInsertResult !== true) {
                    array_push($jsResult['error'], $database->db->error);
                }
            }
        }

        $start = $start + $size;
    }

    $params['target.type'] = "MODULE";
    unset($params['size']);
    $params['start'] = 0;
    foreach ($moduleIds as $moduleId) {
        $params['target.id'] = $moduleId;
        $result = $curl->curlExecution($url, $params, 'POST');
        $result = $result['result'];
        $list = $result['list'];

        $listSize = sizeof($list);
        if($listSize === 0) {
            break;
        }
        else {
            $contentFound += $listSize;
        }
        foreach ($list as $value) {
            $timeCreated = $value['lastUpdated'];
            $type = $value['content']['type'];
            $contentId = $value['content']['id'];
            $jsonValue = json_encode($value);
            $jsonValue = $database->db->real_escape_string($jsonValue);
            $insertData = ['time_created' => $timeCreated, 'content_id' => escapeString($contentId), 'content_type' => escapeString($type), 'data' => escapeString($jsonValue),
                'target_type' => "'MODULE'", 'target_id' => escapeString($moduleId), 'parent_id' => escapeString($targetId)];
            $insertResult = $database->insertToTable($insertData, CONTENT_LINKS_TABLE);
            if($insertResult !== true) {
                $failed++;
                array_push($jsResult['error'], $database->db->error);
            }
            if ($type == "TEST") {
                $name = $value['content']['name'];
                $nameValues = array('test_id' => escapeString($contentId), 'test_name' => escapeString($name));
                $nameInsertResult = $database->insertToTable($nameValues, TEST_NAME_TABLE);
                if($nameInsertResult !== true) {
                    array_push($jsResult['error'], $database->db->error);
                }
            }
        }
    }

    unset($database);
    $jsResult['newContentSynced'] = $contentFound;
    $jsResult['failed'] = $failed;
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($jsResult));
});

$app->post('/syncTests', function (Request $request, Response $response) {
    /**
     * @var Request $request
     * @var Response $response
     */
    $database = new DbUtils();
    $sectionId = $request->getParam("targetId");
    $userId = $request->getParam("userId");

    $params = CONTENT_LINK_PARAMS;
    $params['userId'] = $userId;
    $params['callingUserId'] = $userId;
    $params['targetUserId'] = $userId;
    $params['type'] = "QUESTION";

    $testIds = downloadTests($sectionId, $database, $params);
    
    $totalTests = $testIds['totalTests'];
    $successfullTestsCount = $testIds['successfullTestsCount'];
    $result['totalTests'] = $totalTests;
    $result['successfullTests'] = $successfullTestsCount; 
    $res = array('errorMessage' => '', 'errorCode' => '', 'result' => $result);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});

$app->post('/showTests', function (Request $request, Response $response) {
    $database = new DbUtils();
    $sectionId = $request->getParam("targetId");
    $testQuery = getTestIdsQuery($sectionId);
    $testIds = $database->db->query($testQuery);
    if(!$testIds) {
        die("Error in Query ".$testQuery."\r\n".$database->db->error);
    }
    $info = [];
    while($row = $testIds->fetch_assoc()) {
        $testData = json_decode($row['data'],true);
        $testInfo["testName"] = $testData["content"]["name"];
        $testInfo["testId"] = $testData["content"]["id"];
        array_push($info,$testInfo);
    }
    $res = array('errorMessage' => '', 'errorCode' => '', 'result' => $info);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});

$app->get("/getTestUsers",function(Request $request, Response $response){
    $database = new DbUtils();
    $params = $request->getQueryParams();
    $testId = $request->getParam("testId");
    $queryParams = [
        0 => 'studentname',
        1 => 'memberId',
        2 => 'deviceId',
        3 => 'synced'
    ];
    $data = [];
    $sql_query = "SELECT studentname,memberId,synced,testId,deviceId FROM ".USER_TABLE." INNER JOIN ".ENTITY_USER_ATTEMPTS." ON users.userId = user_entity_attempts.userId WHERE user_entity_attempts.testId =".escapeString($testId)."";
    $totalUsersQuery = populateDataTableParams($params,$queryParams,$sql_query);
    $dbResult = $database->db->query($totalUsersQuery);
    $filteredCountQuery = populateDataTableFilterParams($params,$queryParams,$sql_query);
    $filteredCountResult = $database->db->query($filteredCountQuery);
    $recordsTotal = mysqli_num_rows($filteredCountResult);
    $testUserResp = [];
    if(!$dbResult) {
        die("Error in Query ".$totalUsersQuery."\r\n".$database->db->error);
    }
    if(mysqli_num_rows($dbResult) > 0){
        while($row = $dbResult->fetch_assoc()) {
                $data[] = $row;
        }
    }
    $testUserResp = array("data"=>$data,"recordsTotal"=>$recordsTotal,"recordsFiltered"=>$recordsTotal);
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($testUserResp));
});

$app->post('/syncRankList', function (Request $request, Response $response) {
    /**
     * @var Request $request
     * @var Response $response
     */
    $log = Logger::getLogger('myLogger');
    $log->info("Inside sync rank list");
    $database = new DbUtils();
    $testId = $request->getParam("testId");
    $userId = $request->getParam("userId");

    $params = RANK_LIST_PARAMS;
    $params['userId'] = $userId;
    $params['callingUserId'] = $userId;
    $params['targetUserId'] = $userId;
    $params['entity.id'] = $testId;
    $params['start'] = 0;
    $params['size'] = 1;

    $resp = getRankListByCurl($params);

    $httpCode = $resp['httpcode'];
    if ($httpCode !== 200) {
        return $response->withStatus($httpCode)->withHeader('Content-Type', 'text/plain')->write('Error');
    }

    if(!empty($resp['errorCode'])) {
        return $response->withStatus(400)->withHeader('Content-Type', 'application/json')->write(json_encode($resp));
    }

    // LOGIC STARTS
    // Get count of students taken this test
    $countQuery = getStudentsCountForTest($testId);
    $log->info("The count query is ".$countQuery);
    $countResult = $database->db->query($countQuery);
    if(!$countResult) {
        $log->info("Error in Query ".$countQuery."\r\n".$database->db->error);
        die("Error in Query ".$countQuery."\r\n".$database->db->error);
    }
    $row = $countResult->fetch_row();
    $totalHits = $row[0];
    // If count of students less than totalhits then check for all entries in db w.r.t testId and userId, 
    // then delete them and hit API again with no size and start params and replace them with new entries
    if($totalHits != $resp["result"]['totalHits']){
        $log->info("totalHits mismatch, So entering it into DB. the totalHits from API is ".$resp["result"]['totalHits']);
        $params['size'] = 0;
        $secResp = getRankListByCurl($params);
        $secHttpCode = $secResp['httpcode'];
        if ($secHttpCode !== 200) {
            return $response->withStatus($secHttpCode)->withHeader('Content-Type', 'text/plain')->write('Error');
        }

        if(!empty($secResp['errorCode'])) {
            return $response->withStatus(400)->withHeader('Content-Type', 'application/json')->write(json_encode($secResp));
        }

        $log->info("successfull API call");

        // delete all entries w.r.t testId and userId
        $deleteQuery = deleteStudentTestEntries($testId);
        $deleteQueryResult = $database->db->query($deleteQuery);
        if(!$deleteQueryResult) {
            die("Error in Query ".$deleteQuery."\r\n".$database->db->error);
        }
        
        // Add new Entries
        $lists = $secResp["result"]['list'];
        foreach ($lists as $list) {
            $studentUserId = $list["user"]["userId"];
            $score = $list["info"]["measures"]["score"];
            $timeTaken = $list["info"]["measures"]["timeTaken"];
            $nameValues = array('test_id' => escapeString($testId), 'user_id' => escapeString($studentUserId),
                                'data' => escapeString(json_encode($list)), 'score' => escapeString($score),
                                'time_taken' => escapeString($timeTaken)
                            );
            $nameInsertResult = $database->insertToTable($nameValues, TEST_RANK_LIST);
        }

    }
    $res = array('errorMessage' => '', 'errorCode' => '', 'result' => "Success");
    return $response->withStatus(200)->withHeader('Content-Type', 'application/json')->write(json_encode($res));
});

function getRankListByCurl($params) {
    $url = "https://cmds.learnpedia.in/tabapprouter/getEntityLeaderBoard";
    $curl = new CurlUtils();
    $resp = $curl ->curlExecution($url, $params, 'POST');
    return $resp;
}

function downloadTest($testId, $params){
    $curl = new CurlUtils();
    $url = "https://cmds.learnpedia.in/tabapprouter/getContents";
    $directory = "cache/tests";
    $file = $testId.".json";
    $filePath = $directory."/".$file;

    if(file_exists($filePath) && filesize($filePath) > 0){
        return 0;
    }elseif(empty($testId)){
        return 0;
    }else{
        if(!is_dir($directory)) {
            mkdir($directory, 0777, true);
        }

        $contents = $curl->createFileByCurlExecution($url, $filePath, $params, true);
        $content = json_decode($contents, true);
        if(!empty($content['errorCode'])){
            if(file_exists($filePath)){
                unlink($filePath);
            }
            return 0;
        }elseif (empty($content['errorCode']) && $content["result"]['totalHits'] === 0) {
            if(file_exists($filePath)){
                unlink($filePath);
            }
            return 0;
        }
    }
    return 1;
}


function downloadTests($sectionId, $database, $params){
    /**
     * @var mysqli_result $resultSet
     * @var mysqli_result $testIds
     * @var mysqli_result $moduleTestResultSet
     */
    $successfullTestsCount = 0;
    $totalTests = 0;

    // Get top level Tests
    $testQuery = getTestIdsQuery($sectionId);
    $testIds = $database->db->query($testQuery);
    if(!$testIds) {
        die("Error in Query ".$testQuery."\r\n".$database->db->error);
    }
    while($row = $testIds->fetch_assoc()) {
        $totalTests++;
        $testData = json_decode($row['data'],true);

        $info = $testData["content"]["info"];
        $info = json_decode($info, true);
        $qIds = array();
        foreach( $info['metadata'] as $subject) {
            foreach ($subject['qIds'] as $qid) {
                array_push($qIds, $qid);
            }
        }
        $count = 0;
        // adding question ids 
        foreach ($qIds as $value) {
            $params["ids[".$count."]"] = $value;
            $count++;
        }
        $successfullTestsCount += downloadTest($row['content_id'], $params);
    }
    unset($testIds);

    return $testsDataList = array('totalTests' => $totalTests, 'successfullTestsCount' => $successfullTestsCount);
}

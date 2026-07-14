<?php
const CONTENT_LINKS_TABLE = "content_links";
const TEST_NAME_TABLE = "test_name";
const TEST_RANK_LIST = "test_rank_list";
const ENTITY_USER_ATTEMPTS = "user_entity_attempts";
const USER_TABLE = "users";
const CREATE_CONTENT_LINKS = "CREATE TABLE IF NOT EXISTS ".CONTENT_LINKS_TABLE."(
   id INT(7) NOT NULL AUTO_INCREMENT,
   time_created BIGINT(10),
   target_id VARCHAR(30) NOT NULL,
   target_type VARCHAR(30) NOT NULL,
   parent_id VARCHAR(30) DEFAULT NULL,
   content_id VARCHAR(30) NOT NULL,
   content_type VARCHAR(20) NOT NULL,
   data TEXT,
   PRIMARY KEY(id)
)";

const CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS ".USER_TABLE."(
    memberId VARCHAR(30) NOT NULL,
    username VARCHAR(50) NOT NULL,
    studentname VARCHAR(100) NOT NULL,
    userId VARCHAR(30) NOT NULL,
    userpassword VARCHAR(50) NOT NULL,
    usersalt VARCHAR(50) NOT NULL,
    info TEXT,
    last_updated BIGINT(10),
    PRIMARY KEY(userId)
)";
const CREATE_ENTITY_USER_ATTEMPTS = "CREATE TABLE IF NOT EXISTS ".ENTITY_USER_ATTEMPTS."(
    userId VARCHAR(30) ,
    testId VARCHAR(30) NOT NULL,
    synced BOOLEAN NOT NULL default 0,
    deviceId VARCHAR(50) ,
    data TEXT,
    time_created BIGINT(10),
    PRIMARY KEY(userId,testId)
)";
const CREATE_TEST_NAME = "CREATE TABLE IF NOT EXISTS ".TEST_NAME_TABLE."(
    test_id VARCHAR(30) NOT NULL,
    test_name VARCHAR(1000),
    PRIMARY KEY(test_id)
)";
const CREATE_TEST_RANK_LIST = "CREATE TABLE IF NOT EXISTS ".TEST_RANK_LIST."(
    id INT(7) NOT NULL AUTO_INCREMENT,
    test_id VARCHAR(30) NOT NULL,
    user_id VARCHAR(30) NOT NULL,
    data TEXT,
    score FLOAT(8),
    time_taken BIGINT(10),
    PRIMARY KEY(id)
)";

const ALTER_ENTITY_USER_ATTEMPTS = "ALTER TABLE ".ENTITY_USER_ATTEMPTS." ADD COLUMN IF NOT EXISTS deviceId VARCHAR(50)";

function getAddedAfterQuery($target_id, $target_type){
    $query = "SELECT time_created FROM ".CONTENT_LINKS_TABLE." WHERE target_id = '";
    $query = $query.$target_id."' AND target_type = '".$target_type."' ORDER BY time_created DESC LIMIT 1";
    return $query;
}

function getTestSubmissions($testId){
    $query = "SELECT data,userId,testId FROM ".ENTITY_USER_ATTEMPTS."";
    if($testId != null){
        $query .= " WHERE testId=".escapeString($testId)." AND synced=0";
    }
    else{
        $query .= " WHERE synced=0";
    }
    return $query;
}

function getUser($username){
    $query = "SELECT usersalt,userpassword,info FROM ".USER_TABLE." WHERE username= ".escapeString($username)." LIMIT 1";
    return $query;
}

function getLastUpdated(){
    $query = "SELECT last_updated FROM ".USER_TABLE." ORDER BY last_updated DESC LIMIT 1";
    return $query;
}

function getTotalTestsSubmission(){
    $query = "SELECT COUNT(*) AS test_count, test_name,testId FROM ".ENTITY_USER_ATTEMPTS." INNER JOIN ".TEST_NAME_TABLE." ON user_entity_attempts.testId = test_name.test_id AND synced=0 GROUP BY testId";
    return $query;
}

function updateSyncInUserEntityAttemptsQuery($userId,$testId){
    $query = "UPDATE ".ENTITY_USER_ATTEMPTS." SET synced = 1 WHERE userId = ".escapeString($userId)." AND testId = ".escapeString($testId)."";
    return $query;
}

function getAllUsers($params){
    $search_condition = $query = "";
    $sql_query = "SELECT memberId,studentname,userId FROM ".USER_TABLE."";
    $search_condition .= $sql_query;
    if(isset($params['draw'])){
        $columns = array(0 => 'studentname',
            1 => 'memberId'
        );
        if(!empty($params['search']['value'])){
            $search_condition .= " WHERE ";
            $search_condition .= " ( studentname LIKE '%".$params['search']['value']."%' ";    
            $search_condition .= " OR memberId LIKE '%".$params['search']['value']."%' )";
        }
        if(isset($search_condition) && $search_condition != ""){
            $query .= $search_condition;
        }
        $query .=  " ORDER BY ". $columns[$params['order'][0]['column']]."   ".$params['order'][0]['dir']."  LIMIT ".$params['start']." ,".$params['length']." ";
    }
    else{
        $query .= " ORDER BY studentname LIMIT ".$params['start']." ,".$params['length']." ";
    }
    return $query;
}

function getAllUsersFilter($params){
    $search_condition = $query = "";
    $sql_query = "SELECT memberId,studentname,userId FROM ".USER_TABLE."";
    $query .= $sql_query;
    if( !empty($params['search']['value']) ) {
        $search_condition .= " WHERE ";
        $search_condition .= " ( studentname LIKE '%".$params['search']['value']."%' ";    
        $search_condition .= " OR memberId LIKE '%".$params['search']['value']."%' )";
    }
    if(isset($search_condition) && $search_condition != '') {
        $query .= $search_condition;
    }
    return $query;
}

function getCountQuery($addedAfter, $targetId, $targetType, $contentType = '') {
    $query = "SELECT COUNT(*) AS COUNT FROM ".CONTENT_LINKS_TABLE." WHERE time_created > ".$addedAfter." AND target_id = ".escapeString($targetId)." AND target_type = ".escapeString($targetType)." ";
    if(!empty($contentType)) {
        $query = $query."AND content_type = '".$contentType."' ";
    }
    return $query;
}

function getRankListQuery($testId, $start, $size) {
    $query = "SELECT * FROM ".TEST_RANK_LIST." WHERE test_id = ".escapeString($testId)." LIMIT ".$start.",".$size;
    return $query;
}

function getRankQuery($userId, $testId) {
    $query = "SELECT * FROM ".TEST_RANK_LIST." WHERE test_id = ".escapeString($testId)." AND user_id = ".escapeString($userId);
    return $query;
}

function getStudentsCountForTest($testId) {
    $query = "SELECT COUNT(*) AS COUNT FROM ".TEST_RANK_LIST." WHERE test_id = ".escapeString($testId);
    return $query;
}

function getNames($users){
    $query = "SELECT studentname FROM ".USER_TABLE." WHERE userId IN('".implode("','",$users)."')";
    return $query;
}

function deleteStudentTestEntries($testId) {
    $query = "DELETE FROM ".TEST_RANK_LIST." WHERE test_id = ".escapeString($testId);
    return $query;
}

function getSelectQuery($addedAfter, $targetId, $targetType, $start = 0, $size = 50, $contentType = '') {
    if(empty($size)) {
        $size = 50;
    }
    $query = "SELECT * FROM ".CONTENT_LINKS_TABLE." WHERE time_created > ".$addedAfter." AND target_id = ".escapeString($targetId)." AND target_type = ".escapeString($targetType)." ";
    if(!empty($contentType)) {
        $query = $query."AND content_type = ".escapeString($contentType)." ";
    }
    $query = $query."ORDER BY time_created ASC LIMIT ".$start.", ".$size;
    return $query;
}

function escapeString($string) {
    return "'".$string."'";
}

function populateDataTableParams($params,$values,$sql_query) {
        $log = Logger::getLogger('myLogger');
        $search_condition = $query = "";
        $keyString = '';
        $valueString = '';
        $first = true;
        foreach($values as $key => $val){
            if($first) {
                $first = false;
            }
            else {
                $keyString = $keyString.',';
                $valueString = $valueString.',';
            }
            $keyString = $keyString.$key;
            $valueString = $valueString.$val;
        }
        // $log->info("Inside populateDataTableParams.".$valueString);
        // $sql_query = "SELECT ".$valueString." FROM ".$table."";
        // $log->info("sql_query is".$sql_query);
        $search_condition .= $sql_query;
        $third = true;
        if(isset($params['draw'])){
            $columns =explode(",",$valueString);
            if(!empty($params['search']['value'])){
                $search_condition .= " HAVING ";
                $second = true;
                for($i=0;$i<count($values);$i++) {
                    if($second){
                        $search_condition .= " ( ".$values[$i]." LIKE '%".$params['search']['value']."%' ";
                        $second = false;
                    }
                    else{
                        $search_condition .= " OR ".$values[$i]." LIKE '%".$params['search']['value']."%' ";
                    }
                }
                $search_condition .= " ) ";
            }
            if(isset($search_condition) && $search_condition != ""){
                $query .= $search_condition;
            }
            $log->error("Inside here");
            $query .=  " ORDER BY ". $columns[$params['order'][0]['column']]."   ".$params['order'][0]['dir']."  LIMIT ".$params['start']." ,".$params['length']." ";
        }
        // else{
        //     $query .= " ORDER BY studentname LIMIT ".$params['start']." ,".$params['length']." ";
        // }
        $log->info("Full query is ".$query);
        return $query;
}

function populateDataTableFilterParams($params, $values, $sql_query){
    $log = Logger::getLogger('myLogger');
    $search_condition = $query = "";
    $keyString = '';
    $valueString = '';
    $first = true;
    foreach($values as $key => $val){
        if($first) {
            $first = false;
        }
        else {
            $keyString = $keyString.',';
            $valueString = $valueString.',';
        }
        $keyString = $keyString.$key;
        $valueString = $valueString.$val;
    }
    // $log->info("Inside populateDataTableParams.".$valueString);
    // $sql_query = "SELECT ".$valueString." FROM ".$table."";
    // $log->info("sql_query is".$sql_query);
    $query .= $sql_query;
    if( !empty($params['search']['value']) ) {
        $search_condition .= " HAVING ";
        $second = true;
        for($i=0;$i<count($values);$i++) {
            if($second){
                $search_condition .= " ( ".$values[$i]." LIKE '%".$params['search']['value']."%' ";
                $second = false;
            }
            else{
                $search_condition .= " OR ".$values[$i]." LIKE '%".$params['search']['value']."%' ";
            }
        }
        $search_condition .= " ) ";
    }
    if(isset($search_condition) && $search_condition != '') {
        $query .= $search_condition;
    }
    return $query;
}

function getTestIdsQuery($targetId){
    $query = "SELECT * FROM content_links WHERE (target_id = ".escapeString($targetId)." OR parent_id = ".escapeString($targetId).") AND content_type = 'TEST' ORDER BY 'time_created'";
    return $query;
}

function getModuleIdsQuery($targetId){
    $query = "SELECT content_id FROM ".CONTENT_LINKS_TABLE." WHERE target_id = ".escapeString($targetId)." AND content_type = 'MODULE'";
    return $query;
}

function getModuleTestsQuery($moduleIds){
    $Ids = null;
    if (!is_array($moduleIds)) {
        $Ids = "= ".escapeString($moduleIds);
    }else if(sizeof($moduleIds) > 1){
        $Ids = "IN ('".implode("', '", $moduleIds)."') ";
    }else{
        $Ids = "= '".$moduleIds[0]."'";
    }
    $query = "SELECT * FROM content_links WHERE target_id ".$Ids." AND content_type = 'TEST'";
    return $query;
}

function insertTestNameQuery($testId, $testName){
    $query = "INSERT INTO ".TEST_NAME_TABLE." (test_id, test_name) VALUES (".escapeString($testId).",".escapeString($testName).")";
    return $query;
}

function getTestNameQuery($testId){
    $query = "SELECT test_name FROM ".TEST_NAME_TABLE." WHERE test_id = '".$testId."' LIMIT 1";
    return $query;
}


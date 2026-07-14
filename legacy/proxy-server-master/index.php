<?php

require 'vendor/autoload.php';
$app = new \Slim\App;
require_once 'utils/dbQueries.php';
require_once 'controllers/tabapprouter.php';
require_once 'controllers/contentsSyncRouter.php';
require_once 'utils/CurlUtils.php';
require_once 'utils/DbUtils.php';
require_once 'userConfig.php';
include('log4php/Logger.php');
Logger::configure('config.xml');

$app->run();

$database = new DbUtils();

$tableCreateUserAttempts = $database->db->query(CREATE_ENTITY_USER_ATTEMPTS);
if(!$tableCreateUserAttempts) {
    die("Table not created for user_entity_attempts");
}

$tableCreateContentLinks = $database->db->query(CREATE_CONTENT_LINKS);
if(!$tableCreateContentLinks) {
    die("Table not created for content_links");
}

$tableCreateTests = $database->db->query(CREATE_TEST_NAME);
if(!$tableCreateTests) {
    die("Table not created for test_name");
}

$tableCreateRankList = $database->db->query(CREATE_TEST_RANK_LIST);
if(!$tableCreateRankList) {
    die("Table not created for rank list");
}

$tableCreateUsers = $database->db->query(CREATE_USER_TABLE);
if(!$tableCreateUsers) {
    die("Table not created for users");
}

//Alter Query to add DeviceId.
$alterTableUserAttempts = $database->db->query(ALTER_ENTITY_USER_ATTEMPTS);
if(!$alterTableUserAttempts){
    die("Table column cannot be altered");
}
?>
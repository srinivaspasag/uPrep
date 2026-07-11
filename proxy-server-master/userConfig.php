<?php
define ('USERNAME', 'admin.lalittutorials@gmail.com');
define('PASSWORD', 'learn123');
define('ORGID', '5930ffeae4b085173199e08b');

const LOGINPARAMS =  [
    "username" => USERNAME,
    "password" => PASSWORD,
    "orgId" => ORGID,
    "useGlobalUsername" => true
];

const CONTENT_LINK_PARAMS = [
    "callingApp"=>"TabApp",
    "addContent"=>"true",
    "orderBy"=>"lastUpdated",
    "sortOrder"=>"ASC",
    "versionCode"=>"81",
    "linkType"=>"ADDED",
    "mac"=>"98:0c:a5:86:64:c4",
    "callingAppId"=>"TabApp",
    "size"=>"50",
    "orgId"=> ORGID,
    "android_id"=>"d8ac3e0ef369c55c",
    "start"=>"0",
    "deviceType"=>"MOBILE",
    "memberId"=>"SUPER_ADMIN",
    "addedAfter"=>"0",
    "deviceId"=>"98:0c:a5:86:64:c4",
    "addAnswer"=>true
];

const ORG_INFO_PARAMS = [
    "callingApp" => "TabApp",
    "callingAppId" => "TabApp",
    "slug" => "lalit",
    "orgCmdsURL" => 'https://cmds.learnpedia.in/org/lalit'
];

const RANK_LIST_PARAMS = [    
    "entity.type"=>"TEST",
    "orgId"=>ORGID,
    "contentSrc.id"=>ORGID,
    "contentSrc.type"=>"ORGANIZATION",
    "memberId"=>"SUPER_ADMIN"  
];
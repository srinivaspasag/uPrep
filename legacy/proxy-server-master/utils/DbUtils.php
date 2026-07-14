<?php

class DbUtils
{
    public $db;
    public $database = 'proxy_server';

    function __construct() {
        $this->db = $this->connect_db();
    }

    function __destruct() {
        $this->db->close();
    }

    function connect_db() {
        $server = 'localhost';
        $user = 'raghu';
        $pass = 'groot';

        $connection = new mysqli($server, $user, $pass);
        if(mysqli_connect_errno()){
            exit('connection failed: '. mysqli_connect_error());
        }

        $sql = "CREATE DATABASE IF NOT EXISTS ".$this->database;

        if ($connection->query($sql) !== true) {
            die('Error: ' . $connection->error);
        }
        $connection->select_db($this->database);
        return $connection;
    }

    function createTable($table, $fields) {
        $query = 'CREATE TABLE IF NOT EXISTS '.$table.'( ';
        $query = $query.'pri_key INT(7) NOT NULL AUTO_INCREMENT';
        foreach ($fields as $key=>$val) {
            $query = $query.', '.$key.' '.$val.' ';
        }
        $query = $query.', PRIMARY KEY (pri_key)';
        $query = $query.')';
        $this ->db->query($query);
    }

    function insertToTable($values, $table, $constraint=null) {
        if(null === $constraint){
            $constraint = "";
        }
        if($constraint === "REPLACE"){
            $query = "REPLACE INTO ".$table." ";
        }
        else{
            $query = "INSERT ".$constraint." INTO ".$table." ";
        }
        $keyString = '';
        $valueString = '';
        $first = true;
        foreach ($values as $key => $val) {
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
        $query = $query.'('.$keyString.') VALUES ('.$valueString.');';
        $dbResult = $this->db->query($query);
        return $dbResult;
    }
}
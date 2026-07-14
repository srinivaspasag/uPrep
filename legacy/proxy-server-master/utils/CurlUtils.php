<?php
class CurlUtils
{
    function getImages($imagesList){
        $directory = "cache/img";
        if (!is_dir($directory)) {
            mkdir($directory, 0777, true);
        }
        if(!is_array($imagesList)) {
            $name = explode("/", $imagesList);
            $fileName = $name[sizeof($name) - 1];
            $this->grab_image($imagesList, $directory."/".$fileName);
        }
        else {
            foreach ($imagesList as $image) {
                $name = explode("/", $image);
                $fileName = $name[sizeof($name) - 1];
                $this->grab_image($image, $directory."/".$fileName);
            }
        }
    }

    function grab_image($url,$saveto){
        if(!file_exists($saveto)){
            $ch = curl_init ($url);
            curl_setopt($ch, CURLOPT_HEADER, 0);
            curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
            curl_setopt($ch, CURLOPT_BINARYTRANSFER,1);
            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
            curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
            $raw=curl_exec($ch);
            curl_close ($ch);
            $fp = fopen($saveto,'x');
            fwrite($fp, $raw);
            fclose($fp);
        }
    }

    function curlExecution($url, $params , $methodType){
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL,$url);
        if($methodType === "GET"){
        }
        else {
            curl_setopt($ch, CURLOPT_POST, true);
            curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
        }
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_HEADER, 'Content-Type: application/x-www-form-urlencoded');
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        $response = curl_exec($ch);
        $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        $response = json_decode($response , true);
        $response['httpcode'] = $httpcode;
        curl_close($ch);
        return $response;
    }

    function createFile($filePath, $data){
        $fp = fopen($filePath, "w") or die("Unable to open file!");
        fwrite($fp, $data);
        fclose($fp);
    }

    function createFileByCurlExecution($url, $filePath, $params, $changeData){
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_URL,$url);
        curl_setopt($ch, CURLOPT_POST, true);  // tell curl you want to post something
        curl_setopt($ch, CURLOPT_POSTFIELDS, $params);
        curl_setopt($ch, CURLOPT_TIMEOUT, 10000);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true); // return the output in string format
        curl_setopt($ch, CURLOPT_HEADER, 'Content-Type: application/x-www-form-urlencoded');
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        $contents = curl_exec($ch);
        curl_close($ch);
        $content = json_decode($contents, true);
        if (!empty($content['errorCode'])) {
            return $contents;
        } else {
            if($changeData){
                $contents = $this->changeImageUrls($content);
                if($contents === null){
                    die(preg_last_error());
                }else{
                    $contents = json_encode($contents);
                }                
            }
            $fp = fopen($filePath, "w") or die("Unable to open file!");
            fwrite($fp, $contents);
            fclose($fp);
            return $contents;
        }
    }

    function changeImageUrls($content){
        $lists = $content['result']['list'];
        $regex = "@https?://imglearn.learnpedia.in/viewer/view/(solution|question|cmdsquestion)/img/([0-9a-f\-]+\.(sol|qus)\.img\.conv\.original\.(jpg|png))@";
        // $regex = "@(https((?!https).)*?img/(.*?).jpg)|(https((?!https).)*?img/(.*?).png)@";
        $replaceRegex = "@https?://imglearn.learnpedia.in/viewer/view/(solution|question|cmdsquestion)/img/@";
        // $replaceRegex = "@(https((?!https).)*?img/)@";
        if (!is_null($lists) || is_array($lists) || is_object($lists))
        {
            foreach ($lists as $key => $list) {
                $desc = $list['desc'];
                $descFlag = preg_match_all($regex, $desc, $urls_desc);
                $info = $list['info'];
                $infoFlag = preg_match_all($regex, $info, $urls_info);
                $name = $list['name'];
                $nameFlag = preg_match_all($regex, $name, $urls_name);
                $imagesUrls = array_merge((array)$urls_desc[0], (array)$urls_info[0], (array)$urls_name[0]);

                $this->getImages($imagesUrls);

                // replace code

                $desc = preg_replace($replaceRegex, "learnImg/tabapprouter/cache/img/", $desc);
                $info = preg_replace($replaceRegex, "learnImg/tabapprouter/cache/img/", $info);
                $name = preg_replace($replaceRegex, "learnImg/tabapprouter/cache/img/", $name);

                $list['desc'] = $desc;
                $list['info'] = $info;
                $list['name'] = $name;

                $content['result']['list'][$key] = $list;
            }
        }
        return $content;
    }
}
?>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Panel</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
    <link rel="stylesheet" href="public/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="public/css/proxyServerUtils.css"/>
</head>
<body>
<div class="wrapper">
    <div class="text-center bg-primary stickyHeader">
        <img src="public/img/institutelogo.png" class="instituteLogo">
        <span class="adminPanel">Admin Panel</span>
    </div>
    <ol class="breadcrumb container">
        <span class="qLink">Quick Links -></span>
        <li class="breadcrumb-item "><a href="fetchprograms.php">Programs</a></li>
        <li class="breadcrumb-item"><a href="users.php">Users</a></li>
        <li class="breadcrumb-item"><a href="submittests.php">SubmitTests</a></li>
        <li class="breadcrumb-item active">Upload Apk</li>
    </ol>
    <div class="container">
        <h4>Please Upload APK here</h4>
        <form class="well" action="handle_upload.php" method="post" enctype="multipart/form-data">
            <div class="form-group">
                <label for="file">Select a file to upload (.apk file only)</label>
                <input type="file" name="file" required>
                <p class="help-block">Please upload apk here</p>
                <?php
                  $directory = "cache/Apkuploads";
                  $files = glob($directory."/*.apk");
                  if(sizeof($files) === 0){
                  }
                  else{
                    $application_name = basename($files[0],".apk");
                    echo "APK version already available: ".$application_name;
                  }
                ?>
            </div>
            <input type="submit" class="btn btn-md btn-primary" value="Upload">
        </form>
    </div>
    <footer class="footer">
        <div class="container text-center">
            <span class="text-muted">2013-<?php echo date("Y"); ?> &copy; Learnpedia Edutech Pvt Ltd. ALL Rights Reserved</span>
        </div>
    </footer>
</div>
</body>
</html>
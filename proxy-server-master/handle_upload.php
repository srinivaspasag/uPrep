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
        <li class="breadcrumb-item active"><a href="fileupload.php">Upload Apk</a></li>
    </ol>
    <div class="container">
        <?php
        if ($_SERVER['REQUEST_METHOD'] === 'POST') {
            if (isset($_FILES['file'])) {
                $file = $_FILES['file'];
                $file_name = $file['name'];
                $file_tmp = $file['tmp_name'];
                $file_error = $file['error'];
                $file_ext = explode(".", $file_name);
                $file_ext = strtolower(end($file_ext));
                $allowed = "apk";
                if (strcmp($file_ext, $allowed) === 0) {
                    if ($file_error == 0 && preg_match("/[A-Za-z]+-([0-9]{2,5})/", $file_name)) {
                        $directory = "cache/Apkuploads";
                        if(is_dir($directory)){
                        }
                        else{
                            mkdir($directory, 0777, true);
                        }
                        $file_destination = $directory . "/" . $file_name;
                        $files = glob($directory . "/*.apk");
                        foreach ($files as $file) {
                            if (is_file($file)) {
                                unlink($file);
                            }
                        }
                        $uploaded_file = move_uploaded_file($file_tmp, $file_destination);
                        if ($uploaded_file) {
                            echo "<div class='alert alert-success'>File uploaded successfully</div>";
                        } else {
                            echo "<div class='alert alert-danger'>File could not upload</div><a href='fileupload.php'>Go Back</a>";
                        }
                    } else {
                        echo("<div class='alert alert-danger'>Some error occured while uploading file</div><a href='fileupload.php'>Go Back</a>");
                    }
                } else {
                    echo "<div class='alert alert-danger'>File extension not allowed</div><a href='fileupload.php'>Go Back</a>";
                }
            } else {
                echo "<div class='alert alert-danger'>Please upload files that are less than 50MB</div><a href='fileupload.php'>Go Back</a>";
            }
        } else {
            header("Location: fileupload.php");
        }
        ?>
    </div>
    <footer class="footer">
        <div class="container text-center">
            <span class="text-muted">2013-<?php echo date("Y"); ?> &copy; Learnpedia Edutech Pvt Ltd. ALL Rights Reserved</span>
        </div>
    </footer>
</div>
</body>
</html>

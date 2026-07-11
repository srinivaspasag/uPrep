<!DOCTYPE html>
<html>
<head>
    <title>Admin Panel</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
    <link rel="stylesheet" href="public/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="public/css/datatables.bootstrap.min.css">
    <link rel="stylesheet" href="public/css/proxyServerUtils.css"/>
    <link rel="stylesheet" href="public/css/users.css"/>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/limonte-sweetalert2/6.11.4/sweetalert2.min.css" />
    <script src="public/js/jquery-3.2.1.min.js" type="text/javascript"></script>
    <script src="public/js/bootstrap.min.js" type="text/javascript"></script>
    <script type="text/javascript" src="public/js/jquery.dataTables.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/limonte-sweetalert2/6.11.4/sweetalert2.all.min.js"></script>
    <script type="text/javascript" src="public/js/proxyServerUtils.js"></script>
    <script type="text/javascript" src="public/js/users.js"></script>
    <script type="text/javascript" src="public/js/datatables.bootstrap.min.js"></script>
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
            <li class="breadcrumb-item active">Users</li>
            <li class="breadcrumb-item"><a href="submittests.php">SubmitTests</a></li>
            <li class="breadcrumb-item"><a href="fileupload.php">Upload Apk</a></li>
            <div class="syncUserInfo floatRight">
                <button class="btn btn-info btn-sm boldy">Sync User Info.</button>
            </div>
        </ol>
        <div class="container tableContainer" style="overflow: auto;">
            <div class="overlay">
                <div class="loadingText"></div>
                <div class="loader" style="display: none;"></div>
            </div>
            <div id="userData">
                <div class="">
                    <table id="user-table" class="table table-bordered" style="width: 100%;">
                        <thead class="thead-default">
                            <tr>
                                <th>Name</th>
                                <th>MemberId </th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
            <input type="hidden" id="userId">
        </div>
        <footer class="footer">
            <div class="container text-center">
              <span class="text-muted">2013-<?php echo date("Y"); ?> &copy; Learnpedia Edutech Pvt Ltd. ALL Rights Reserved</span>
          </div>
      </footer>
  </div>
  <script type="text/javascript">
    users.init();
</script>
</body>
</html>
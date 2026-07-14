<?php
use PHPMailer\PHPMailer\PHPMailer;
require 'vendor/autoload.php';
$mail = new PHPMailer;

$name = $email = $phone = $classcomp = "";

if ($_SERVER["REQUEST_METHOD"] == "POST") {
  $name = test_input($_POST["name"]);
  $email = test_input($_POST["email"]);
  $phone = test_input($_POST["phone"]);
  $classcomp = test_input($_POST["classcomp"]);
}

function test_input($data) {
  $data = trim($data);
  $data = stripslashes($data);
  $data = htmlspecialchars($data);
  return $data;
}


// $mail->isSMTP();
// $mail->SMTPDebug = 2;
$mail->Host = 'smtp.gmail.com';
// $mail->SMTPSecure = 'tls';
$mail->Port = 587;
// $mail->SMTPAuth = true;
$mail->Username = 'info@learnpedia.in';
$mail->Password = 'lpepl@2018';
$mail->setFrom('info@uprep.in', 'UPrep');
$mail->addReplyTo('info@uprep.in', 'UPrep');
$mail->addAddress('naveen.m@uprep.in', 'Naveen');
$mail->Subject = 'Get in Touch - Foundation';
$mail->message = 
//$mail->msgHTML(file_get_contents('foundation.html'), __DIR__);
$mail->Body = "Name:  ".$name."\n"."Mobile:  ".$phone."\n"."Email:  ".$email."\n"."Class Completed:  ".$classcomp;
//$mail->addAttachment('test.txt');
if (!$mail->send()) {
    echo 'Mailer Error: ' . $mail->ErrorInfo;
} else {
    echo 'The email message was sent.';
}

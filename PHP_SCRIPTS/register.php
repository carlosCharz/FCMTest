<?php 
    if (isset($_POST["token"])) {
        
           $token = $_POST["token"];
           $conn = mysqli_connect("127.0.0.1", "root", "admin1234", "fcmtest") or die("Error connecting");
           $q = "INSERT INTO users (token) VALUES ( '$token') "
              ." ON DUPLICATE KEY UPDATE token = '$token';";
              
      mysqli_query($conn, $q) or die(mysqli_error($conn));
      mysqli_close($conn);
    }
 ?>
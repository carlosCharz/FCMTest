<?php 

    if (isset($_POST["token"])) {
        
           $token=$_POST["token"];
           $conn = mysqli_connect("localhost","root","","fcmtest") or die("Error connecting");
           $q="DELETE FROM users where token = '$token'";
              
      mysqli_query($conn,$q) or die(mysqli_error($conn));
      mysqli_close($conn);
    }
 ?>
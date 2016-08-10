<?php 
    if (isset($_POST["token"])) {
        
           $_uv_Token=$_POST["token"];
           $conn = mysqli_connect("localhost","root","","fcmtest") or die("Error connecting");
           $q="INSERT INTO users (token) VALUES ( '$_uv_Token') "
              ." ON DUPLICATE KEY UPDATE token = '$_uv_Token';";
              
      mysqli_query($conn,$q) or die(mysqli_error($conn));
      mysqli_close($conn);
    }
 ?>
<?php 
    $conn = mysqli_connect("localhost","root","","fcmtest") or die("Error connecting");
    $q="DELETE FROM users";
              
    mysqli_query($conn,$q) or die(mysqli_error($conn));
    mysqli_close($conn);
 ?>
<?php 
	$conn = mysqli_connect("127.0.0.1", "root", "admin1234", "fcmtest");
	$sql = " select * from users;";
	$result = mysqli_query($conn, $sql);
	$tokens = array();

	while($row = mysqli_fetch_array($result)){
    	array_push($tokens, array(
        	'id' => $row['id'],
        	'token' => $row['token']
    	));
	}

	echo json_encode(array('result' => $tokens));

	mysqli_close($conn);
 ?>
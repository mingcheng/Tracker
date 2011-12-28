<?php 

if (PHP_SAPI != "cli") {
    exit; 
}

$database_file = realpath(isset($argv[1]) ? $argv[1] : null);
if (!is_file($database_file)) {
    exit;
}

try {
    $db = new PDO("sqlite:$database_file");
} catch(PDOExpception $e) {
    echo $e->getMessage();
}

//"<when>2011-12-03T07:49:46.712-08:00</when> \n <gx:coord>120.107078 30.282008 0</gx:coord>"
$track_template = "<when>%s</when>\n<gx:coord>%s %s %s</gx:coord>\n";

$sql = "SELECT DISTINCT * from location WHERE del = 0 ORDER BY time";

$coordinates = array();
foreach($db->query($sql) as $row) {
    array_push($coordinates, sprintf($track_template, date("c", $row['time']/1000), $row["longitude"], $row["latitude"], $row["altitude"]));
}

$coordinates = trim(implode("", $coordinates));

$kml_tempalte = file_get_contents("template.kml");


printf($kml_tempalte, 'name', 'name', 'desp', $coordinates);

$db = null;

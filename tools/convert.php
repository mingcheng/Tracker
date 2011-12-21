<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2" 
    xmlns:gx="http://www.google.com/kml/ext/2.2" 
    xmlns:kml="http://www.opengis.net/kml/2.2" 
    xmlns:atom="http://www.w3.org/2005/Atom">
    <Document>
        <name>%s</name>
        <Style id="redLine">
            <LineStyle>
                <color>7f0000ff</color>
                <width>4</width>
            </LineStyle>
        </Style>

<?php

try {
    $db = new PDO("sqlite:loc.db");
} catch(PDOExpception $e) {
    echo $e->getMessage();
}

$sql = "SELECT * from location WHERE del = 0 ORDER BY time DESC";

$coordinates = array();
foreach($db->query($sql) as $row) {
    array_push($coordinates, sprintf("%s,%s,%s \n", $row["longitude"], $row["latitude"], $row["altitude"]));
}


$coordinates_template = "
        <Placemark>
            <name>%s</name>
            <description><![CDATA[%s]]></description>
            <styleUrl>#redLine</styleUrl>
            <MultiGeometry>
                <LineString>
                    <coordinates> %s </coordinates>
                </LineString>
            </MultiGeometry>
        </Placemark>
";

$coordinates = sprintf($coordinates_template, "title", "desption", trim(implode("", $coordinates)));


echo $coordinates;


$db = null;
?>
    </Document>
</kml>

package com.gracecode.gpsrecorder.util;

import android.database.Cursor;

import java.io.*;

public class KMLHelper {
    private final static String TAG = KMLHelper.class.getName();

    private final static String KML_FILE_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\" \n" +
        "    xmlns:gx=\"http://www.google.com/kml/ext/2.2\" \n" +
        "    xmlns:kml=\"http://www.opengis.net/kml/2.2\" \n" +
        "    xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" +
        "    <Document>\n" +
        "        <name>%s</name>\n" +
        "        <description><![CDATA[%s]]></description>\n" +
        "        <Style id=\"redLine\">\n" +
        "            <LineStyle>\n" +
        "                <color>7f0000ff</color>\n" +
        "                <width>4</width>\n" +
        "            </LineStyle>\n" +
        "        </Style>\n" +
        "%s\n" +
        "    </Document>\n" +
        "</kml>";

    private final static String COORDINATES_TEMPLATE = "<Placemark>\n" +
        "            <name>%s</name>\n" +
        "            <description><![CDATA[%s]]></description>\n" +
        "            <styleUrl>#redLine</styleUrl>\n" +
        "            <MultiGeometry>\n" +
        "                <LineString>\n" +
        "                    <coordinates>%s</coordinates>\n" +
        "                </LineString>\n" +
        "            </MultiGeometry>\n" +
        "        </Placemark>";

//    private final static String POINT_TEMPLATE = "<Placemark>\n" +
//        "            <name>在第一路口向右转，朝新月路行进 目的地在左侧</name>\n" +
//        "            <description>行驶 140&amp;#160;米</description>\n" +
//        "            <LookAt>\n" +
//        "                <longitude>120.21091</longitude>\n" +
//        "                <latitude>30.2076</latitude>\n" +
//        "                <altitude>0</altitude>\n" +
//        "                <heading>327.902161</heading>\n" +
//        "                <tilt>45</tilt>\n" +
//        "                <range>100</range>\n" +
//        "            </LookAt>\n" +
//        "            <Point>\n" +
//        "                <coordinates>120.21091,30.2076,0</coordinates>\n" +
//        "            </Point>\n" +
//        "        </Placemark>";

    protected Cursor data;
    protected String name;
    protected String description;

    protected String kmlXMLString;
    private String multiGeometryCoordinatesString;
    private String coordinatesString;

    public KMLHelper(String name, String description, Cursor data) {
        this.data = data;
        this.name = name;
        this.description = description;
    }

    public int getCount() {
        return data.getCount();
    }

    private void makeMultiGeometryCoordinatesString() {
        for (data.moveToFirst(), multiGeometryCoordinatesString = ""; !data.isAfterLast(); data.moveToNext()) {
            Double latitude = data.getDouble(data.getColumnIndex("latitude"));
            Double longitude = data.getDouble(data.getColumnIndex("longitude"));
            Double altitude = data.getDouble(data.getColumnIndex("altitude"));

            multiGeometryCoordinatesString += String.format("%f,%f,%f \n", longitude, latitude, altitude);
        }
    }

    private void makeCoordinatesString() {
        makeMultiGeometryCoordinatesString();
        coordinatesString = String.format(COORDINATES_TEMPLATE, name, description, multiGeometryCoordinatesString);
    }


    private void makeEntryKMLString() {
        if (coordinatesString == null) {
            makeCoordinatesString();
        }

        kmlXMLString = String.format(KML_FILE_TEMPLATE, name, description, coordinatesString);
    }


    public String getEntryKMLString() {
        if (kmlXMLString == null) {
            makeEntryKMLString();
        }
        return kmlXMLString;
    }

    public void saveKMLFile(File fileKml) throws IOException {
        FileWriter fw = new FileWriter(fileKml);
        fw.write(getEntryKMLString());
        fw.close();
    }

    public static String getFileContent(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuffer sbContent = new StringBuffer();
        String sLine = "";

        while ((sLine = br.readLine()) != null) {
            String s = sLine.toString() + "\n";
            sbContent = sbContent.append(s);
        }

        fis.close();
        isr.close();
        br.close();

        return sbContent.toString();
    }
}

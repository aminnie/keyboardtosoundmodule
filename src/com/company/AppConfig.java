package com.company;

import java.io.*;
import java.util.Properties;

public class AppConfig {

    Properties configProps = new Properties();

    String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    String configPath = rootPath + "config.xml";

    public boolean loadProperties() {

        try {
            configProps.loadFromXML(new FileInputStream(configPath));

            // get the property value and print it out
            System.out.println("Config: In device is " +  configProps.getProperty("indevice"));
            System.out.println("Config: Out device is " +  configProps.getProperty("outdevice"));
        }
        catch (FileNotFoundException ex) {
            System.out.println("File not found exception: " + configPath);
            return false;
        }
        catch (IOException ex) {
            System.out.println("File read exception: " + configPath);
            return false;
        }

        return true;
    }

    public boolean saveProperties() {

        try {
            configProps.storeToXML(new FileOutputStream(configPath), "Saved to XML file");

            // get the property value and print it out
            System.out.println("Config: App Name " +  configProps.getProperty("appname"));
            System.out.println("Config: App Version " +  configProps.getProperty("appversion"));
            System.out.println("Config: App Date " +  configProps.getProperty("appdate"));
            System.out.println("Config: In device is " +  configProps.getProperty("indevice"));
            System.out.println("Config: Out device is " +  configProps.getProperty("outdevice"));


        }
        catch (IOException ex) {
            System.out.println("Config File write failed: " + configPath);
            return false;
        }

        return true;
    }

    // Get selected In Midi device - Keyboard
    public String getInDevice() {
        return configProps.getProperty("indevice");
    }

    // Get selected Out Midi device - Sound Module
    public String getOutDevice() {
        return configProps.getProperty("outdevice");
    }

    // Get selected Out Midi device - Sound Module
    public String getSoundModule(int idx) {
        String strmodule;

        switch (idx) {
            case 1:
                strmodule = configProps.getProperty("sndmodfil1");
            default:
                strmodule = configProps.getProperty("sndmodfil0");
        }

        return strmodule;
    }

    public String getSongsFile() {
        return configProps.getProperty("songsfile");
    }

}

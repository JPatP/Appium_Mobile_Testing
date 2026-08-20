// https://www.javadoc.io/doc/io.appium/java-client/latest/io/appium/java_client/android/options/UiAutomator2Options.html
package com.pat.appium;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class AppiumBasicsTest {
     @Test
    public void Test() throws MalformedURLException, URISyntaxException {

         AppiumServiceBuilder service = new AppiumServiceBuilder();
         UiAutomator2Options options = new UiAutomator2Options();
         options.setDeviceName("Pixel_7");
         options.setApp("C:\\Users\\gonza\\IdeaProjects\\AppiumMobileTests\\src\\main\\java\\resources\\ApiDemos-debug.apk");
         //
         // emulator -avd Pixel_7 -verbose
        // Android Driver
         AndroidDriver driver = new AndroidDriver(new URI("http://127.0.0.1:4723").toURL(), options);
         // new AndroidDriver(url, options) — this sends an HTTP request to the Appium server (running on 127.0.0.1:4723) asking it to start a new automation session.
     }
}


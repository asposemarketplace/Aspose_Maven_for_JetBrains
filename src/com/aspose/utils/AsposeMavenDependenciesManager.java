/**
 * Copyright (c) Aspose 2002-2014. All Rights Reserved.
 *
 * LICENSE: This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not,
 * see http://opensource.org/licenses/gpl-3.0.html
 *
 * @author Adeel Ilyas <adeel.ilyas@aspose.com>
 *
 */
package com.aspose.utils;

import com.aspose.maven.apis.artifacts.Metadata;
import com.aspose.wizards.maven.AsposeMavenModuleBuilderHelper;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.*;


public class AsposeMavenDependenciesManager {



    public AsposeMavenDependenciesManager() {

    }

    public String readURLContents(String Url) throws MalformedURLException, IOException {
        URL url = new URL(Url);
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        String body = new String(baos.toByteArray(), encoding);
        return body;
    }


    public Metadata getProductMavenDependency(String productMavenRepositoryUrl) {
        final String mavenMetaDataFileName = "maven-metadata.xml";
        Metadata data = null;

        try {
            String productMavenInfo;
            productMavenInfo = readURLContents(productMavenRepositoryUrl + mavenMetaDataFileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(com.aspose.maven.apis.artifacts.ObjectFactory.class);
            Unmarshaller unmarshaller;
            unmarshaller = jaxbContext.createUnmarshaller();
            AsposeConstants.println(productMavenInfo);
            data = (Metadata) unmarshaller.unmarshal(new StreamSource(new StringReader(productMavenInfo)));

            String remoteArtifactFile = productMavenRepositoryUrl + data.getVersion() + "/" + data.getArtifactId() + "-" + data.getVersion();

            AsposeConstants.println(remoteArtifactFile);
            if (!remoteFileExists(remoteArtifactFile + ".jar")) {
                AsposeConstants.println("Not Exists");
                data.setClassifier(getResolveSupportedJDK(remoteArtifactFile));
            } else {
                AsposeConstants.println("Exists");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            data = null;
        }
        return data;
    }

    public static String getResolveSupportedJDK(String ProductURL) {
        String supportedJDKs[] = {"jdk17", "jdk16", "jdk15", "jdk14", "jdk18"};
        String classifier = null;
        for (String jdkCheck : supportedJDKs) {
            AsposeConstants.println(ProductURL + "-" + jdkCheck + ".jar");
            if (remoteFileExists(ProductURL + "-" + jdkCheck + ".jar")) {
                AsposeConstants.println("Exists");
                classifier = jdkCheck;
                break;
            } else {
                AsposeConstants.println("Not Exists");
            }
        }
        return classifier;
    }

    public static boolean remoteFileExists(String URLName) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con =
                    (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }






    /**
     * @return
     */
    public static boolean isInternetConnected() {
        try {
            InetAddress address = InetAddress.getByName(AsposeConstants.INTERNTE_CONNNECTIVITY_PING_URL);
            if (address == null) {
                return false;
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    public boolean retrieveAsposeMavenDependencies(@NotNull ProgressIndicator progressIndicator) {
    try {
        AsposeMavenModuleBuilderHelper.getAsposeProjectMavenDependencies().clear();
        progressIndicator.setText("Retrieving Aspose Maven Dependencies latest artifacts...");

        for (AsposeJavaAPI component : AsposeMavenProject.getApiList().values()) {

            if (component.is_selected()) {
                Metadata productMavenDependency = getProductMavenDependency(component.get_mavenRepositoryURL());
                if (productMavenDependency != null) {
                    AsposeMavenModuleBuilderHelper.getAsposeProjectMavenDependencies().add(productMavenDependency);
                }
            }
        }
    } catch (Exception rex) {
        return false;
    }
        if (!AsposeMavenModuleBuilderHelper.getAsposeProjectMavenDependencies().isEmpty()) {

            return true;
        } else {
            return false;
        }
    }
}

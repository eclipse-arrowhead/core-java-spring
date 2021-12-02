/********************************************************************************
* Copyright (c) 2021 Bosch.IO GmbH[ and others]
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package eu.arrowhead.core.hbconfmgr.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class PropertiesExtractor {
    private static Properties properties;
    static {
        properties = new Properties();
        final URL url = PropertiesExtractor.class.getClassLoader().getResource("mockserver.properties");
        try{
            properties.load(new FileInputStream(url.getPath()));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getProperty(final String key){
        return properties.getProperty(key);
    }
}
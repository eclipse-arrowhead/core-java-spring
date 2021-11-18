/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common.log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.arrowhead.common.CoreCommonConstants;

public class JDBCConnectionFactoryForLog4J2 {
	
	//=================================================================================================
	// members
	
	private static Properties props;
	private static DataSource dataSource;

	static {
		try {
			init();
		} catch (final IOException ex) {
			// this class' purpose to configure logging so in case of exceptions we can't use logging
			System.out.println(ex.getMessage()); //NOSONAR no logging at this point
			ex.printStackTrace(); //NOSONAR no logging at this point
		}
	}

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static Connection getConnection() throws SQLException {
		if (dataSource == null) {
			final HikariConfig config = new HikariConfig();
			config.setJdbcUrl(props.getProperty(CoreCommonConstants.DATABASE_URL));
			config.setUsername(props.getProperty(CoreCommonConstants.DATABASE_USER));
			config.setPassword(props.getProperty(CoreCommonConstants.DATABASE_PASSWORD));
			config.setDriverClassName(props.getProperty(CoreCommonConstants.DATABASE_DRIVER_CLASS));
			
			dataSource = new HikariDataSource(config);
		}
		
		return dataSource.getConnection();
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private static void init() throws IOException {
		InputStream propStream = null;
		
		try {
			final File propertiesFile = new File(CoreCommonConstants.APPLICATION_PROPERTIES);
			if (!propertiesFile.exists()) {
				propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(CoreCommonConstants.APPLICATION_PROPERTIES);
			} else {
				propStream = new FileInputStream(propertiesFile);
			}
			
			final Properties temp = new Properties();
			temp.load(propStream);
	
			props = temp;
		} finally {
			if (propStream != null) {
				propStream.close();
			}
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private JDBCConnectionFactoryForLog4J2() {
		throw new UnsupportedOperationException();
	}
}
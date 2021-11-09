/********************************************************************************
 * Copyright (c) 2021 {Lulea University of Technology}
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 
 *
 * Contributors: 
 *   {Lulea University of Technology} - implementation
 *   Arrowhead Consortia - conceptualization 
 ********************************************************************************/
package eu.arrowhead.core.configuration.database.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ConfigurationListResponseDTO;
import eu.arrowhead.common.dto.shared.ConfigurationRequestDTO;
import eu.arrowhead.common.dto.shared.ConfigurationResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;


@Service
public class ConfigurationDBService {
	
	//=================================================================================================
	// members
	
	private static final String INVALID_FORMAT_ERROR_MESSAGE = " has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING;
	
	@Value("${spring.datasource.url}")
	private String url;
	@Value("${spring.datasource.username}")
	private String user;
	@Value("${spring.datasource.password}")
	private String password;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;

	private static final Logger logger = LogManager.getLogger(ConfigurationDBService.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	private Connection getConnection() throws SQLException {
	  return DriverManager.getConnection(url, user, password);
	}


	//-------------------------------------------------------------------------------------------------
	private void closeConnection(final Connection conn) throws SQLException {
	  conn.close();
	}

	
	//-------------------------------------------------------------------------------------------------
	public ConfigurationResponseDTO getConfigForSystem(String systemName) {
		logger.debug("getConfigForSystem:");

		if (systemName == null) {
			return null;
		}
		systemName = systemName.toLowerCase().trim();
		if (systemName.equals("")) {
			return null;
		}

		ConfigurationResponseDTO ret = null;
		Connection conn = null;
	
		try {
			conn = getConnection();
			final String sql = "SELECT id, contentType, data, created_at, updated_at FROM configuration_data WHERE systemName=? ORDER BY id DESC LIMIT 1;";
			final PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, systemName);
	
			final ResultSet rs = stmt.executeQuery();
			
			// fetch the information
			if (rs.next()) {
				ret = new ConfigurationResponseDTO();
				ret.setId(rs.getLong(1));
				ret.setSystemName(systemName);
				ret.setContentType(rs.getString(2));
				ret.setData(rs.getString(3));
				ret.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(Utilities.parseDBLocalStringToUTCZonedDateTime(rs.getString(4))));
				ret.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(Utilities.parseDBLocalStringToUTCZonedDateTime(rs.getString(5))));
			}
			
			rs.close();
			stmt.close();
		} catch (final SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (final SQLException e) {
				logger.debug(e.toString());
			}
			
		}
	
		return ret;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ConfigurationResponseDTO setConfigForSystem(final String systemName, final ConfigurationRequestDTO conf) {
		logger.debug("getConfigForSystem:");

		if (systemName == null || conf == null) {
			return null;
		}
		
		if (systemName.equals("")) {
			return null;
		}
		
		if (!cnVerifier.isValid(systemName)) {
			logger.debug("System name{}", INVALID_FORMAT_ERROR_MESSAGE);
			return null;
		}

		Connection conn = null;
		try {
			conn = getConnection();

			final String sql = "INSERT INTO configuration_data(systemName, fileName, contentType, data) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE fileName=?, contentType=?, data=?;";
			final PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, systemName.toLowerCase().trim());
			stmt.setString(2, conf.getFileName());
			stmt.setString(3, conf.getContentType());
			stmt.setString(4, conf.getData());

			stmt.setString(5, conf.getFileName());
			stmt.setString(6, conf.getContentType());
			stmt.setString(7, conf.getData());
			
			stmt.executeUpdate();
			stmt.close();
		} catch (final SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (final SQLException e) {
				logger.debug(e.toString());
			}
			
		}

		return getConfigForSystem(systemName);
	}

	//-------------------------------------------------------------------------------------------------
	public ConfigurationListResponseDTO getAllConfigurations() {
		logger.debug("getAllConfigurations:");

		final ConfigurationListResponseDTO ret = new ConfigurationListResponseDTO();

		Connection conn = null;
		try {
			conn = getConnection();

			final String sql = "SELECT id, systemName, contentType, data, created_at, updated_at FROM configuration_data;";
			final PreparedStatement stmt = conn.prepareStatement(sql);
	
			final ResultSet rs = stmt.executeQuery();
			
			final List<ConfigurationResponseDTO> data = new ArrayList<ConfigurationResponseDTO>();
			
			while (rs.next()) {
				final ConfigurationResponseDTO entry = new ConfigurationResponseDTO();
				entry.setId(rs.getLong(1));
				entry.setSystemName(rs.getString(2));
				entry.setContentType(rs.getString(3));
				entry.setData(rs.getString(4));
				entry.setCreatedAt(Utilities.convertZonedDateTimeToUTCString(Utilities.parseDBLocalStringToUTCZonedDateTime(rs.getString(5))));
				entry.setUpdatedAt(Utilities.convertZonedDateTimeToUTCString(Utilities.parseDBLocalStringToUTCZonedDateTime(rs.getString(6))));
				data.add(entry);
			}
			ret.setData(data);
			ret.setCount(data.size());
			
			rs.close();
			stmt.close();
		} catch (final SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (final SQLException e) {
				logger.debug(e.toString());
			}
			
		}
		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	public ConfigurationResponseDTO deleteConfigForSystem(final String systemName) {
		logger.debug("deleteConfigForSystem:");

		if (systemName == null) {
			return null;
		}
		if (systemName.equals("")) {
			return null;
		}

		// check if config exists
		final ConfigurationResponseDTO ret = getConfigForSystem(systemName.toLowerCase().trim());
		if (ret == null) {
			return null;
		}

		Connection conn = null;
		try {
			conn = getConnection();

			final String sql = "DELETE FROM configuration_data WHERE systemName=?;";
			final PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, systemName.toLowerCase().trim());

			stmt.executeUpdate();
			stmt.close();

		} catch (final SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (final SQLException e) {
				logger.debug(e.toString());
			}
			
		}

		return ret;
	}
}
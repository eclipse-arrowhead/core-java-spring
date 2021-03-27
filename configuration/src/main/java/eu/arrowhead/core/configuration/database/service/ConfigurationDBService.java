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

import java.util.Optional;
import java.util.List;
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.dto.shared.ConfigurationListResponseDTO;
import eu.arrowhead.common.dto.shared.ConfigurationRequestDTO;
import eu.arrowhead.common.dto.shared.ConfigurationResponseDTO;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.InvalidParameterException;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Array;
import java.sql.SQLException;


@Service
public class ConfigurationDBService {
	//=================================================================================================
	// members
	
	@Value("${spring.datasource.url}")
	private String url;
	@Value("${spring.datasource.username}")
	private String user;
	@Value("${spring.datasource.password}")
	private String password;

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
	public ConfigurationResponseDTO getConfigForSystem(final String systemName) {
		logger.debug("getConfigForSystem:");

		ConfigurationResponseDTO ret = null;
		
		Connection conn = null;
	
		try {
			conn = getConnection();
			String sql = "SELECT id, contentType, data, created_at, updated_at FROM configuration_data WHERE systemName=? ORDER BY id DESC LIMIT 1;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, systemName);
	
			ResultSet rs = stmt.executeQuery();
			
			// fetch the information
			if (rs.next()) {
				ret = new ConfigurationResponseDTO();
				ret.setId(rs.getLong(1));
				ret.setSystemName(systemName);
				ret.setContentType(rs.getString(2));
				ret.setData(rs.getString(3));
				ret.setCreatedAt(rs.getString(4));
				ret.setUpdatedAt(rs.getString(5));
			}
			
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (SQLException e) {
				logger.debug(e.toString());
			}
			
		}
	
		return ret;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ConfigurationResponseDTO setConfigForSystem(final String systemName, final ConfigurationRequestDTO conf) {
		logger.debug("getConfigForSystem:");

		Connection conn = null;

		try {
			conn = getConnection();

			String sql = "INSERT INTO configuration_data(systemName, fileName, contentType, data) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE fileName=?, contentType=?, data=?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, systemName);
			stmt.setString(2, conf.getFileName());
			stmt.setString(3, conf.getContentType());
			stmt.setString(4, conf.getData());

			stmt.setString(5, conf.getFileName());
			stmt.setString(6, conf.getContentType());
			stmt.setString(7, conf.getData());
			
			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (SQLException e) {
				logger.debug(e.toString());
			}
			
		}

		return getConfigForSystem(systemName);
	}

	//-------------------------------------------------------------------------------------------------
	public ConfigurationListResponseDTO getAllConfigurations() {
		logger.debug("getAllConfigurations:");

		ConfigurationListResponseDTO ret = new ConfigurationListResponseDTO();

		Connection conn = null;
		try {
			conn = getConnection();

			String sql = "SELECT id, systemName, contentType, data, created_at, updated_at FROM configuration_data;";
			PreparedStatement stmt = conn.prepareStatement(sql);
	
			ResultSet rs = stmt.executeQuery();
			
			List<ConfigurationResponseDTO> data = new ArrayList<ConfigurationResponseDTO>();
			// fetch the information
			if (rs.next()) {
				ConfigurationResponseDTO entry = new ConfigurationResponseDTO();
				entry.setId(rs.getLong(1));
				entry.setSystemName(rs.getString(2));
				entry.setContentType(rs.getString(3));
				entry.setData(rs.getString(4));
				entry.setCreatedAt(rs.getString(5));
				entry.setUpdatedAt(rs.getString(6));
				data.add(entry);
			}
			ret.setData(data);
			ret.setCount(data.size());
			
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (SQLException e) {
				logger.debug(e.toString());
			}
			
		}
		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	public ConfigurationResponseDTO deleteConfigForSystem(final String systemName) {
		logger.debug("deleteConfigForSystem:");

		// check if config exists
		ConfigurationResponseDTO ret = getConfigForSystem(systemName);
		if (ret == null) {
			return null;
		}

		Connection conn = null;
		try {
			conn = getConnection();

			String sql = "DELETE FROM configuration_data WHERE systemName=?;";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, systemName);

			stmt.executeUpdate();
			stmt.close();

		} catch (SQLException e) {
			logger.debug(e.toString());
			throw new ArrowheadException(CoreCommonConstants.DATABASE_OPERATION_EXCEPTION_MSG);
		} finally {
			try {
				closeConnection(conn);
			} catch (SQLException e) {
				logger.debug(e.toString());
			}
			
		}

		return ret;
	}
}

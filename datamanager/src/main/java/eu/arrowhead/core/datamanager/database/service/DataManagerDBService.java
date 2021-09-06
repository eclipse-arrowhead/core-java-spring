/********************************************************************************
 * Copyright (c) 2020 {Lulea University of Technology}
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
package eu.arrowhead.core.datamanager.database.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.verifier.CommonNamePartVerifier;


@Service
public class DataManagerDBService {
	
	//=================================================================================================
	// members

	public static final int MAX_ALLOWED_TIME_DIFF  = 1000; // milliseconds
	
	private static final String INVALID_FORMAT_ERROR_MESSAGE = " has invalid format. Name must match with the following regular expression: " + CommonNamePartVerifier.COMMON_NAME_PART_PATTERN_STRING;
	
	@Value("${spring.datasource.url}")
	private String url;
	@Value("${spring.datasource.username}")
	private String user;
	@Value("${spring.datasource.password}")
	private String password;
	
	@Autowired
	private CommonNamePartVerifier cnVerifier;
	
	@Value(CoreCommonConstants.$USE_STRICT_SERVICE_DEFINITION_VERIFIER_WD)
	private boolean useStrictServiceDefinitionVerifier;

	private static final Logger logger = LogManager.getLogger(DataManagerDBService.class);
	
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
	private int serviceToID(final String systemName, final String serviceName, final Connection conn) {
		logger.debug("serviceToID for {}/{}", systemName, serviceName);

		int id = -1;

		PreparedStatement stmt;
		try {
			final String sql = "SELECT id FROM dmhist_services WHERE system_name=? AND service_name=? LIMIT 1;";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, systemName);
			stmt.setString(2, serviceName);
			final ResultSet rs = stmt.executeQuery();

			rs.next();
			id  = rs.getInt("id");

			rs.close();
			stmt.close();
		} catch (final Exception e) {
			logger.debug("serviceToID: " + e.toString());
			id = -1;
		}

		return id;
	}

	//-------------------------------------------------------------------------------------------------
	public ArrayList<String> getAllHistorianSystems() {
		logger.debug("getAllHistorianSystems");

		final ArrayList<String> ret = new ArrayList<String>();
		Connection conn = null;

		try {
			conn = getConnection();
			final String sql = "SELECT DISTINCT(system_name) FROM dmhist_services;";
			final PreparedStatement stmt = conn.prepareStatement(sql);

			final ResultSet rs = stmt.executeQuery();
			while (rs.next() == true) {
				ret.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch (final SQLException e) {
			logger.debug(e.toString());
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
	public boolean addServiceForSystem(final String systemName, final String serviceName, final String serviceType) {
		logger.debug("addServiceForSystem for {}/{}", systemName, serviceName);
		Assert.isTrue(!Utilities.isEmpty(serviceType), "Service type is blank");

		Connection conn = null;
		try {
			final String validatedSystemName = validateSystemName(systemName);
			final String validatedServiceName = validateServiceName(serviceName);
			
			conn = getConnection();
			int id = serviceToID(validatedSystemName, validatedServiceName, conn);
			if (id != -1) {
				return false; // already exists
			} else {
				final String sql = "INSERT INTO dmhist_services(system_name, service_name, service_type) VALUES(?, ?, ?);";
				final PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				stmt.setString (1, validatedSystemName);
				stmt.setString (2, validatedServiceName);
				stmt.setString (3, serviceType);
				stmt.executeUpdate();
				final ResultSet rs = stmt.getGeneratedKeys();
				rs.next();
				id = rs.getInt(1);
				rs.close();
				stmt.close();
		    }
		  } catch (final Exception e) {
			  logger.debug("addServiceForSystem: " + e.toString());
			  return false;
		  } finally {
			  try {
				  closeConnection(conn);
			  } catch (final SQLException se) {
				  logger.debug("addServiceForSystem: " + se.toString());
			  }
		  }
	
		  return true;
	}
	
	//-------------------------------------------------------------------------------------------------
	public ArrayList<String> getServicesFromSystem(final String systemName) {
		logger.debug("getServicesFromSystem for {}", systemName);

		final String validatedSystemName = validateSystemName(systemName);
		final ArrayList<String> ret = new ArrayList<String>();
		Connection conn = null;
		try {
			conn = getConnection();
			final String sql = "SELECT DISTINCT(service_name) FROM dmhist_services WHERE system_name=?;";
			final PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, validatedSystemName);

			final ResultSet rs = stmt.executeQuery();
			while (rs.next() == true) {
				ret.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch(final SQLException db){
			logger.error(db.toString());
		} finally {
			try {
				closeConnection(conn);
			} catch(final SQLException db) {
				logger.debug("getServicesFromSystem:" + db.toString());
			}
		}

		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean createEndpoint(final String systemName, final String serviceName) {
		logger.debug("createEndpoint for {}/{}", systemName, serviceName);
		
		final String validatedSystemName = validateSystemName(systemName);
		final String validatedServiceName = validateServiceName(serviceName);

		Connection conn = null;
		try {
			conn = getConnection();
			int id = serviceToID(validatedSystemName, validatedServiceName, conn);
			if (id != -1) {
				return true;
		    } else {
		    	final String sql = "INSERT INTO dmhist_services(system_name, service_name) " + "VALUES(?,?);";
		    	final PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		    	stmt.setString (1, validatedSystemName);
		    	stmt.setString (2, validatedServiceName);
		    	stmt.executeUpdate();
		    	final ResultSet rs = stmt.getGeneratedKeys();
		    	rs.next();
		    	id = rs.getInt(1);
		    	rs.close();
		    	stmt.close();
		    }
		} catch (final Exception db) {
			logger.debug("createEndpoint: " + db.toString());
			return false;
		} finally {
			try {
				closeConnection(conn);
			} catch(final Exception e){
				logger.debug("createEndpoint: " + e.toString());
			}
		}

		return true;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean updateEndpoint(final String systemName, final String serviceName, final Vector<SenML> message) {
		logger.debug("updateEndpoint for {}/{}", systemName, serviceName);
		
		final String validatedSystemName = validateSystemName(systemName);
		final String validatedServiceName = validateServiceName(serviceName);

		boolean ret = true;

		final double bt = message.get(0).getBt();
		final double maxTs = getLargestTimestamp(message);
		final double minTs = getSmallestTimestamp(message);

		Connection conn = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(false);
			final int sid = serviceToID(validatedSystemName, validatedServiceName, conn);
			if (sid != -1) {
				String sql = "INSERT INTO dmhist_messages(sid, bt, mint, maxt, msg) VALUES(?, ?, ?, ?, ?)";
				PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				stmt.setLong(1, sid);
				stmt.setDouble(2, bt);
				stmt.setDouble(3, minTs);
				stmt.setDouble(4, maxTs);
				stmt.setString(5, message.toString());

				int mid = stmt.executeUpdate();
				ResultSet rs = stmt.getGeneratedKeys();
				rs.next();
				mid = rs.getInt(1);
				rs.close();
				stmt.close();

				// that was the entire message, now insert each individual JSON object in the message
				final String bu = message.get(0).getBu();
				for (final SenML m : message) {
					double t = 0;
					if (m.getT() != null) {
						if (m.getT() < SenML.RELATIVE_TIMESTAMP_INDICATOR) {
							t = m.getT() + bt;
						}
					} else {
						t = bt;
					}

					if (m.getU() == null) {
						m.setU(bu);
					}

					if (m.getN() != null) {
						sql = "INSERT INTO dmhist_entries(sid, mid, n, t, u, v, vs, vb) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
						stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
						stmt.setInt(1, sid);
						stmt.setInt(2, mid);
						stmt.setString(3, m.getN());
						stmt.setDouble(4, t);
						stmt.setString(5, m.getU());
						if (m.getV() == null) {
							stmt.setNull(6, java.sql.Types.DOUBLE);
						} else {
							stmt.setDouble(6, m.getV());
						} 
						stmt.setString(7, m.getVs());
						if (m.getVb() != null) {
							stmt.setBoolean(8, m.getVb());
						} else {
							stmt.setNull(8, java.sql.Types.BOOLEAN);
						}
						stmt.executeUpdate();
						rs = stmt.getGeneratedKeys();
						rs.close();
						stmt.close();
					}
				}
				conn.commit();
			} else {
				ret = false;
			}
		} catch (final SQLException e) {
			logger.debug("Database store error: " + e.toString());

			try {
				conn.rollback();
			} catch(final SQLException err) {
				logger.debug("Database store error: " + err.toString());
			}
			ret = false;
		} finally {
			try{
				closeConnection(conn);
			} catch(final Exception e){
				logger.debug("Database error: " + e.toString());
			}
		}

		return ret;
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	public Vector<SenML> fetchMessagesFromEndpoint(final String systemName, final String serviceName, double from, double to, final int count) {
		logger.debug("fetchMessagesFromEndpoint for "+ systemName + "/"+serviceName);
		
		final String validatedSystemName = validateSystemName(systemName);
		final String validatedServiceName = validateServiceName(serviceName);

		Connection conn = null;

		try {
			conn = getConnection();
			final int serviceId = serviceToID(validatedSystemName, validatedServiceName, conn);
			if (serviceId == -1) {
				logger.debug("fetchMessagesFromEndpoint: service doesn't exist");
				return null;
			}

			if (from < 0.0) {
				from = 0.0;                                       //1970-01-01
			}
			if (to <= 0.0) {
				to = MAX_ALLOWED_TIME_DIFF + (long)(System.currentTimeMillis() / 1000.0); // current timestamp - not ok to insert data that is created in the future (excl. minor clock drift)
			}

			String sql = "";
			PreparedStatement stmt = null;
			sql = "SELECT id FROM dmhist_messages WHERE sid=? AND bt >=? AND bt <=? ORDER BY bt DESC LIMIT ?;";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, serviceId);
			stmt.setDouble(2, from);
			stmt.setDouble(3, to);
			stmt.setInt(4, count);

			final Vector<SenML> messages = new Vector<SenML>();
			final SenML hdr = new SenML();
			hdr.setBn(serviceName);
			messages.add(hdr);
			final double bt = 0;
			final String bu = null;
			final ResultSet messageListRs = stmt.executeQuery();
			while (messageListRs.next() == true) {
				final int mid = messageListRs.getInt("id");

				final String sql2 = "SELECT * FROM dmhist_entries WHERE sid=? AND mid=? AND t>=? AND t <=? ORDER BY t DESC;";
				final PreparedStatement stmt2 = conn.prepareStatement(sql2);
				stmt2.setInt(1, serviceId);
				stmt2.setInt(2, mid);
				stmt2.setDouble(3, from);
				stmt2.setDouble(4, to);

				final ResultSet rs2 = stmt2.executeQuery();
				while (rs2.next() == true ) {
					final SenML msg = new SenML();
					msg.setT((double)rs2.getLong("t"));
					msg.setN(rs2.getString("n"));
					msg.setU(rs2.getString("u"));
					final double v = rs2.getDouble("v");
					if (!rs2.wasNull()) {
						msg.setV(v);
					}

					msg.setVs(rs2.getString("vs"));
					final Boolean foo = rs2.getBoolean("vb");
					if (!rs2.wasNull()) {
						msg.setVb(rs2.getBoolean("vb"));
					}

					messages.add(msg);
				}
				rs2.close();
			}
			stmt.close();
			if (messages.size() == 1) {
				return messages;
			}

			//recalculate a bt time and update all relative timestamps
			final double startbt = ((SenML)messages.get(1)).getT();
			((SenML)messages.firstElement()).setBt(startbt);
			((SenML)messages.firstElement()).setT(null);
			((SenML)messages.get(1)).setT(null);
			for (final SenML m : messages) {
				if (m.getT() != null) {
					m.setT(m.getT() - startbt);
				}
			}

			return messages;
		} catch (final SQLException e) {
			logger.debug(e.toString());
		} finally {
			try {
				closeConnection(conn);
			} catch(final Exception e) {
				logger.debug("Database error: " + e.toString());
			}
		}

		return null;
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unused")
	public Vector<SenML> fetchSignalsFromEndpoint(final String systemName, final String serviceName, double from, double to, final Vector<Integer> counts, final Vector<String> signals) {
		logger.debug("fetchSignalsFromEndpoint for "+ systemName + "/"+serviceName);
		
		final String validatedSystemName = validateSystemName(systemName);
		final String validatedServiceName = validateServiceName(serviceName);

		Connection conn = null;

		try {
			conn = getConnection();
			final int serviceId = serviceToID(validatedSystemName, validatedServiceName, conn);
			if (serviceId == -1) {
				logger.debug("fetchEndpoint: service doesn't exist");
				return null;
			}

			if (from < 0.0) {
				from = 0.0;                                       // not before 1970-01-01
			}
			if (to <= 0.0) {
				to = MAX_ALLOWED_TIME_DIFF + (long)(System.currentTimeMillis() / 1000.0); // current timestamp - not ok to insert data that is created in the future (excl. minor clock drift)
			}

			final Vector<SenML> messages = new Vector<SenML>();
			final SenML hdr = new SenML();
			hdr.setBn(serviceName);
			messages.add(hdr);

			for (int index = 0; index < signals.size(); index++) {
				final String signalName = signals.get(index);
				final int signalCount = counts.get(index);
				PreparedStatement stmt = null;
				final String sql = "SELECT * FROM dmhist_entries WHERE sid=? AND n=? AND t>=? AND t<=? ORDER BY t DESC LIMIT ?;";
				stmt = conn.prepareStatement(sql);
				stmt.setInt(1, serviceId);
				stmt.setString(2, signalName);
				stmt.setDouble(3, from);
				stmt.setDouble(4, to);
				stmt.setInt(5, signalCount);

				final ResultSet rs = stmt.executeQuery();

				int dataLeft = signalCount;
				while (rs.next() == true && dataLeft > 0) {
					final SenML msg = new SenML();
					msg.setT((double)rs.getLong("t"));
					msg.setN(rs.getString("n"));
					msg.setU(rs.getString("u"));
					final double v = rs.getDouble("v");
					if (!rs.wasNull()) {
						msg.setV(v);
					}

					msg.setVs(rs.getString("vs"));
					final Boolean foo = rs.getBoolean("vb");
					if (!rs.wasNull()) {
						msg.setVb(rs.getBoolean("vb"));
					}

					messages.add(msg);
					dataLeft--;
				}

				rs.close();
				stmt.close();
			}

			if (messages.size() == 1) {
				return messages;
			}

			//recalculate a bt time and update all relative timestamps
			final double startbt = ((SenML)messages.get(1)).getT();
			((SenML)messages.firstElement()).setBt(startbt);
			((SenML)messages.firstElement()).setT(null);
			((SenML)messages.get(1)).setT(null);
			for (final SenML m : messages) {
				if (m.getT() != null) {
					m.setT(m.getT() - startbt);
				}
			}

			return messages;
		} catch (final SQLException e) {
			logger.debug("SQl error: " + e.toString());
		} finally {
			try {
				closeConnection(conn);
			} catch(final Exception e) {
				logger.debug("Database error: " + e.toString());
			}
		}

		return null;
	}


	//=================================================================================================
	// assistant methods
  
	//-------------------------------------------------------------------------------------------------
	// returns largest (newest) timestamp value
	private double getLargestTimestamp(final Vector<SenML> msg) {
		final double bt = msg.get(0).getBt();
		double max = bt;
		for (final SenML m : msg) {
			if (m.getT() == null) {
				continue;
			}
			if (m.getT() > SenML.RELATIVE_TIMESTAMP_INDICATOR) { // absolute
				if (m.getT() > max ) {
					max = m.getT();
				}
			} else {                      // relative
				if (m.getT()+bt > max ) {
					max = m.getT() + bt;
				}
			}
		}

		return max;
	}

	//-------------------------------------------------------------------------------------------------
	// returns smallest (oldest) timestamp value
	private double getSmallestTimestamp(final Vector<SenML> msg) {
		final double bt = msg.get(0).getBt();
		double min = bt;
		for (final SenML m : msg) {
			if (m.getT() == null) {
				continue;
			}
			if (m.getT() > SenML.RELATIVE_TIMESTAMP_INDICATOR) { // absolute
				if (m.getT() < min ) {
					min = m.getT();
				}
			} else {                      // relative
				if (m.getT()+bt < min ) {
					min = m.getT() + bt;
				}
			}
		}
		
		return min;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String validateSystemName(final String systemName) {
		Assert.isTrue(!Utilities.isEmpty(systemName), "System name is blank");
		Assert.isTrue(cnVerifier.isValid(systemName), "System name" + INVALID_FORMAT_ERROR_MESSAGE);
		
		return systemName.toLowerCase().trim();
	}
	
	//-------------------------------------------------------------------------------------------------
	private String validateServiceName(final String serviceName) {
		Assert.isTrue(!Utilities.isEmpty(serviceName), "Service name is blank");
		
		if (useStrictServiceDefinitionVerifier) {
			Assert.isTrue(cnVerifier.isValid(serviceName), "Service name" + INVALID_FORMAT_ERROR_MESSAGE);
		}
		
		return serviceName.toLowerCase().trim();
	}
}
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
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.dto.shared.SenML;
import eu.arrowhead.common.exception.InvalidParameterException;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Array;
import java.sql.SQLException;


@Service
public class DataManagerDBService {
	//=================================================================================================
	// members

  public static final int MAX_ALLOWED_TIME_DIFF  = 1000; //milliseconds
	
	@Value("${spring.datasource.url}")
	private String url;
	@Value("${spring.datasource.username}")
	private String user;
	@Value("${spring.datasource.password}")
	private String password;

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

	  int id=-1;

	  PreparedStatement stmt;
	  try {
	    String sql = "SELECT id FROM dmhist_services WHERE system_name=? AND service_name=? LIMIT 1;";
	    stmt = conn.prepareStatement(sql);
	    stmt.setString(1, systemName);
	    stmt.setString(2, serviceName);
	    ResultSet rs = stmt.executeQuery();

	    rs.next();
	    id  = rs.getInt("id");

	    rs.close();
	    stmt.close();
	  } catch(Exception e) {
      logger.debug("serviceToID: " + e.toString());
	    id = -1;
	  }

	  return id;
	}

	//-------------------------------------------------------------------------------------------------
	public ArrayList<String> getAllHistorianSystems() {
    logger.debug("getAllHistorianSystems");

	  ArrayList<String> ret = new ArrayList<String>();
	  Connection conn = null;

	  try {
	    conn = getConnection();
	    String sql = "SELECT DISTINCT(system_name) FROM dmhist_services;";
	    PreparedStatement stmt = conn.prepareStatement(sql);

	    ResultSet rs = stmt.executeQuery();
	    while(rs.next() == true) {
	      ret.add(rs.getString(1));
	    }
	    rs.close();
	    stmt.close();
	  } catch (SQLException e) {
	    logger.debug(e.toString());
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
	public boolean addServiceForSystem(final String systemName, final String serviceName, final String serviceType) {
    logger.debug("addServiceForSystem for {}/{}", systemName, serviceName);

	  Connection conn = null;
	  try {
	    conn = getConnection();
	    int id = serviceToID(systemName, serviceName, conn);
	    if (id != -1) {
	      return false; //already exists
	    } else {
	      String sql = "INSERT INTO dmhist_services(system_name, service_name, service_type) VALUES(?, ?, ?);";
	      PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	      stmt.setString (1, systemName);
	      stmt.setString (2, serviceName);
	      stmt.setString (3, serviceType);
	      stmt.executeUpdate();
	      ResultSet rs = stmt.getGeneratedKeys();
	      rs.next();
	      id = rs.getInt(1);
	      rs.close();
	      stmt.close();
	    }

	  } catch (Exception e) {
      logger.debug("addServiceForSystem: " + e.toString());
	    return false;
	  } finally {
	    try {
	      closeConnection(conn);
	    } catch (SQLException se) {
        logger.debug("addServiceForSystem: " + se.toString());
      }

	  }

	  return true;
	}

	//-------------------------------------------------------------------------------------------------
	public ArrayList<String> getServicesFromSystem(final String systemName) {
    logger.debug("getServicesFromSystem for {}", systemName);

	  ArrayList<String> ret = new ArrayList<String>();
	  Connection conn = null;
	  try {
	    conn = getConnection();
	    String sql = "SELECT DISTINCT(service_name) FROM dmhist_services WHERE system_name=?;";
	    PreparedStatement stmt = conn.prepareStatement(sql);
	    stmt.setString(1, systemName);

	    ResultSet rs = stmt.executeQuery();
	    while(rs.next() == true) {
	      ret.add(rs.getString(1));
	    }
	    rs.close();
	    stmt.close();
	  } catch(SQLException db){
	    logger.error(db.toString());
	  }

	  finally {
	    try {
	      closeConnection(conn);
	    } catch(SQLException db) {
	      logger.debug("getServicesFromSystem:" + db.toString());
	    }

	  }

	  return ret;
	}


	//-------------------------------------------------------------------------------------------------
	public boolean createEndpoint(final String systemName, final String serviceName) {
    logger.debug("createEndpoint for {}/{}", systemName, serviceName);

	  Connection conn = null;
	  try {
	    conn = getConnection();
	    int id = serviceToID(systemName, serviceName, conn);
	    if (id != -1) {
	      return true;
	    } else {
	      String sql = "INSERT INTO dmhist_services(system_name, service_name) " + "VALUES(?,?);";
	      PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	      stmt.setString (1, systemName);
	      stmt.setString (2, serviceName);
	      stmt.executeUpdate();
	      ResultSet rs = stmt.getGeneratedKeys();
	      rs.next();
	      id = rs.getInt(1);
	      rs.close();
	      stmt.close();

	    }

	  } catch (Exception db) {
	    logger.debug("createEndpoint: " + db.toString());
	    return false;
	  } finally {
	    try{
	      closeConnection(conn);
	    } catch(Exception e){
        logger.debug("createEndpoint: " + e.toString());
	    }

	  }

	  return true;
	}

	//-------------------------------------------------------------------------------------------------
	public boolean updateEndpoint(final String systemName, final String serviceName, final Vector<SenML> message) {
    logger.debug("updateEndpoint for {}/{}", systemName, serviceName);

	  boolean ret = true;

	  double bt = message.get(0).getBt();
	  double maxTs = getLargestTimestamp(message);
	  double minTs = getSmallestTimestamp(message);

	  Connection conn = null;
	  try {
	    conn = getConnection();
      conn.setAutoCommit(false);
	    int sid = serviceToID(systemName, serviceName, conn);
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
	      String bu = message.get(0).getBu();
        for (SenML m : message) {
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
    } catch (SQLException e) {
      logger.debug("Database store error: " + e.toString());

      try {
        conn.rollback();
      } catch(SQLException err) {
        logger.debug("Database store error: " + err.toString());
      }
      ret = false;
    } finally {
      try{
        closeConnection(conn);
      } catch(Exception e){
        logger.debug("Database error: " + e.toString());
      }

    }

    return ret;
  }

  //-------------------------------------------------------------------------------------------------
  public Vector<SenML> fetchMessagesFromEndpoint(final String systemName, final String serviceName, double from, double to, final int count) {
    logger.debug("fetchMessagesFromEndpoint for "+ systemName + "/"+serviceName);

    Connection conn = null;

    try {
      conn = getConnection();
      int serviceId = serviceToID(systemName, serviceName, conn);
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

      Vector<SenML> messages = new Vector<SenML>();
      SenML hdr = new SenML();
      hdr.setBn(serviceName);
      messages.add(hdr);
      double bt = 0;
      String bu = null;
      ResultSet messageListRs = stmt.executeQuery();
      while(messageListRs.next() == true) {
        int mid = messageListRs.getInt("id");

        String sql2 = "SELECT * FROM dmhist_entries WHERE sid=? AND mid=? AND t>=? AND t <=? ORDER BY t DESC;";
        PreparedStatement stmt2 = conn.prepareStatement(sql2);
        stmt2.setInt(1, serviceId);
        stmt2.setInt(2, mid);
        stmt2.setDouble(3, from);
        stmt2.setDouble(4, to);

        ResultSet rs2 = stmt2.executeQuery();
        while(rs2.next() == true ) {
          SenML msg = new SenML();
          msg.setT((double)rs2.getLong("t"));
          msg.setN(rs2.getString("n"));
          msg.setU(rs2.getString("u"));
          double v = rs2.getDouble("v");
          if (!rs2.wasNull()) {
            msg.setV(v);
			    }

			    msg.setVs(rs2.getString("vs"));
			    Boolean foo = rs2.getBoolean("vb");
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
	    double startbt = ((SenML)messages.get(1)).getT();
	    ((SenML)messages.firstElement()).setBt(startbt);
	    ((SenML)messages.firstElement()).setT(null);
	    ((SenML)messages.get(1)).setT(null);
	    for (SenML m : messages) {
	      if (m.getT() != null) {
          m.setT(m.getT() - startbt);
	      }
	    }

	    return messages;

	  } catch (SQLException e) {
		  logger.debug(e.toString());
	  } finally {
		  try {
			  closeConnection(conn);
		  } catch(Exception e){
		  }
	  }

	  return null;
	}

	//-------------------------------------------------------------------------------------------------
	public Vector<SenML> fetchSignalsFromEndpoint(String systemName, String serviceName, double from, double to, final Vector<Integer> counts, final Vector<String> signals) {
		logger.debug("fetchSignalsFromEndpoint for "+ systemName + "/"+serviceName);

		Connection conn = null;

	  try {
	    conn = getConnection();
	    int serviceId = serviceToID(systemName, serviceName, conn);
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

      Vector<SenML> messages = new Vector<SenML>();
      SenML hdr = new SenML();
      hdr.setBn(serviceName);
      messages.add(hdr);

      for (int index = 0; index < signals.size(); index++) {
        String signalName = signals.get(index);
        int signalCount = counts.get(index);
        PreparedStatement stmt = null;
        String sql = "SELECT * FROM dmhist_entries WHERE sid=? AND n=? AND t>=? AND t<=? ORDER BY t DESC LIMIT ?;";
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, serviceId);
        stmt.setString(2, signalName);
        stmt.setDouble(3, from);
        stmt.setDouble(4, to);
        stmt.setInt(5, signalCount);

        ResultSet rs = stmt.executeQuery();

        int dataLeft = signalCount;
        while(rs.next() == true && dataLeft > 0) {
          SenML msg = new SenML();
          msg.setT((double)rs.getLong("t"));
          msg.setN(rs.getString("n"));
          msg.setU(rs.getString("u"));
          double v = rs.getDouble("v");
	      if (!rs.wasNull()) {
          msg.setV(v);
	      }

	      msg.setVs(rs.getString("vs"));
	      Boolean foo = rs.getBoolean("vb");
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
	    double startbt = ((SenML)messages.get(1)).getT();
	    ((SenML)messages.firstElement()).setBt(startbt);
	    ((SenML)messages.firstElement()).setT(null);
	    ((SenML)messages.get(1)).setT(null);
	    for (SenML m : messages) {
	      if (m.getT() != null) {
          m.setT(m.getT()-startbt);
	      }
	    }

	    return messages;

	  } catch (SQLException e) {
	    logger.debug("SQl error: " + e.toString());
	  } finally {
	    try {
	      closeConnection(conn);
	    } catch(Exception e){
	    }

	  }

	  return null;
	}


	//=================================================================================================
	// assistant methods
  
	//-------------------------------------------------------------------------------------------------
	//returns largest (newest) timestamp value
	private double getLargestTimestamp(final Vector<SenML> msg) {
	  double bt = msg.get(0).getBt();
	  double max = bt;
	  for (SenML m : msg) {

	    if (m.getT() == null) {
	      continue;
	    }
	    if (m.getT() > SenML.RELATIVE_TIMESTAMP_INDICATOR) { // absolute
	      if (m.getT() > max ) {
          max = m.getT();
	      }
	    } else {                      //relative
	      if (m.getT()+bt > max ) {
          max = m.getT() + bt;
	      }
	    }
	  }

	  return max;
	}

	//-------------------------------------------------------------------------------------------------
	//returns smallest (oldest) timestamp value
	private double getSmallestTimestamp(final Vector<SenML> msg) {
	  double bt = msg.get(0).getBt();
	  double min = bt;
	  for (SenML m : msg) {

	    if (m.getT() == null) {
	      continue;
	    }
	    if (m.getT() > SenML.RELATIVE_TIMESTAMP_INDICATOR) { // absolute
	      if (m.getT() < min ) {
          min = m.getT();
	      }
	    } else {                      //relative
	      if (m.getT()+bt < min ) {
          min = m.getT() + bt;
	      }
	    }
	  }

	  return min;
	}

}

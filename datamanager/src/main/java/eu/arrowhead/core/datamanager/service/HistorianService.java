package eu.arrowhead.core.datamanager.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.dto.shared.SenML;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.*; 
import java.util.Vector;
import java.util.Properties;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.Gson;


@Service
public class HistorianService {
	 private static Connection connection = null;
	 private static String dbAddress;
	 private static String dbUser;
	 private static String dbPassword;
	 private static Properties props = null;

	 private static final Logger logger = LogManager.getLogger(DataManagerDriver.class);

	 static {

	}

	/**
	 * @fn static boolean Init(Properties props)
	 * @brief 
	 *
	 */
	public static boolean Init(Properties propss){
	  props = propss;

	  try {
	    Class.forName("com.mysql.cj.jdbc.Driver");
	  } catch (ClassNotFoundException e) {
	    logger.debug("Where is your MySQL JDBC Driver?");
	    return false;
	  }

	  try {
	    connection = getConnection();
	    connection.close();
	  } catch (SQLException e) {
	    logger.debug("Connection Failed! Check output console");
	    System.exit(-1);
	    return false;
	  }

	  return true;
	}


	/**
	 * @fn private static Connection getConnection()
	 * @brief 
	 *
	 */
	private static Connection getConnection() throws SQLException {
	  Connection conn = DriverManager.getConnection(props.getProperty("spring.datasource.url"), props.getProperty("spring.datasource.username"), props.getProperty("spring.datasource.password"));

	  return conn;
	}


	/**
	 * @fn private static void closeConnection(Connection conn)
	 * @brief 
	 *
	 */
	private static void closeConnection(Connection conn) throws SQLException {
	  conn.close();
	}


	/**
	 * @fn static int serviceToID(String serviceName, Connection conn)
	 * @brief Returns the database ID of a specific service
	 *
	 */
	static int serviceToID(String serviceName, Connection conn) {
	  int id=-1;

	  Statement stmt = null;
	  try {
	    stmt = conn.createStatement();
	    String sql;
	    sql = "SELECT id FROM dmhist_services WHERE service_name='"+serviceName+"' LIMIT 1;";
	    ResultSet rs = stmt.executeQuery(sql);

	    rs.next();
	    id  = rs.getInt("id");

	    rs.close();
	    stmt.close();
	  }catch(SQLException se){
	    id = -1;
	  }catch(Exception e){
	    id = -1;
	  }

	  return id;
	}


	/**
	 * @fn static ArrayList<String> getSystems()
	 *
	 */
	public static ArrayList<String> getSystems(){
	  ArrayList<String> ret = new ArrayList<String>();
	  Connection conn = null;
	  try {
	    conn = getConnection();
	    Statement stmt = conn.createStatement();
	    String sql = "SELECT DISTINCT(service_name) FROM dmhist_services;";

	    ResultSet rs = stmt.executeQuery(sql);
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
	    } catch (SQLException e) {}

	  }

	  return ret;
	}


	/**
	 * @fn public static boolean addServiceForSystem(String systemName, String serviceName, String serviceType)
	 *
	 */
	public static boolean addServiceForSystem(String systemName, String serviceName, String serviceType){
	  Connection conn = null;
	  try {
	    conn = getConnection();
	    int id = serviceToID(serviceName, conn);
	    if (id != -1) {
	      return false; //already exists
	    } else {
	      Statement stmt = conn.createStatement();
	      String sql = "INSERT INTO dmhist_services(system_name, service_name, service_type) VALUES(\""+systemName+"\", \""+serviceName+"\", \""+serviceType+"\");"; //bug: check name for SQL injection!
	      int mid = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
	      ResultSet rs = stmt.getGeneratedKeys();
	      rs.next();
	      id = rs.getInt(1);
	      rs.close();
	      stmt.close();

	    }

	  } catch (SQLException e) {
	    return false;
	  } finally {
	    try {
	      closeConnection(conn);
	    } catch (SQLException e) {}
	  
	  }

	  return true;
	}


	/**
	 * @fn
	 *
	 */
	public static ArrayList<String> getServicesFromSystem(String systemName){
	  ArrayList<String> ret = new ArrayList<String>();
	  Connection conn = null;
	  try {
	    conn = getConnection();
	    Statement stmt = conn.createStatement();
	    String sql = "SELECT DISTINCT(service_name) FROM dmhist_services WHERE system_name='"+systemName+"';";

	    ResultSet rs = stmt.executeQuery(sql);
	    while(rs.next() == true) {
	      ret.add(rs.getString(1));
	    }
	    rs.close();
	    stmt.close();
	  }catch(SQLException db){
	  } finally {
	    try {
	      connection.close();
	    }catch(SQLException db){}

	  }


	  return ret;
	}


	/**
	 * @fn static boolean createEndpoint(String name)
	 *
	 */
	public static boolean createEndpoint(String systemName, String serviceName) {
	  Connection conn = null;
	  try {
	    conn = getConnection();
	    int id = serviceToID(serviceName, conn);
	    if (id != -1) {
	      return true; //already exists
	    } else {
	      Statement stmt = conn.createStatement();
	      String sql = "INSERT INTO dmhist_services(system_name, service_name) VALUES(\""+systemName+"\", \""+serviceName+"\");"; //bug: check name for SQL injection!
	      int mid = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
	      ResultSet rs = stmt.getGeneratedKeys();
	      rs.next();
	      id = rs.getInt(1);
	      rs.close();
	      stmt.close();

	    }

	  } catch (SQLException e) {
	    return false;
	  } finally {
	    try{
	      closeConnection(conn);
	    } catch(Exception e){}
	  
	  }

	  return true;
	}


	/**
	 * @fn static boolean updateEndpoint(String serviceName, Vector<SenML> msg)
	 *
	 */
	public static boolean updateEndpoint(String serviceName, Vector<SenML> msg) {
	  boolean ret = false;

	  double maxTs = maxTs(msg);
	  double minTs = minTs(msg);
	  //logger.debug("bt(msg): "+(msg.get(0).getBt())+", minTs(msg): "+minTs+", maxTs(msg): " + maxTs);

	  Connection conn = null;
	  try {
	    conn = getConnection();
	    int sid = serviceToID(serviceName, conn);
	    if (sid != -1) {
	      Statement stmt = conn.createStatement();
	      String sql = "INSERT INTO dmhist_messages(sid, bt, mint, maxt, msg, datastored) VALUES("+sid+", "+msg.get(0).getBt()+","+minTs+", "+maxTs+", '"+msg.toString()+"',NOW());"; //how to escape?
	      int mid = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
	      ResultSet rs = stmt.getGeneratedKeys();
	      rs.next();
	      mid = rs.getInt(1);
	      rs.close();
	      stmt.close();

	      // that was the entire message, now insert each individual JSON object in the message
	      double bt = msg.get(0).getBt();
	      String bu = msg.get(0).getBu();
	      for (SenML m : msg) {
		double t = 0;
		if (m.getT() != null) {
		  if (m.getT() < 268435456) //if relative ts, update it
		    t = m.getT() + bt;
		} else
		  t = bt;

		if (m.getU() == null)
		  m.setU(bu);

		String n = m.getN();
		String unit = null;
		if (m.getU() != null)
		  unit = "'"+m.getU()+"'";
		String value = null;
		if (m.getV() != null)
		  value = "'"+m.getV()+"'";
		String stringvalue = null;
		if (m.getVs() != null)
		  stringvalue = "'"+m.getVs()+"'";
		String boolvalue = null;
		if (m.getVb() != null)
		  boolvalue = "'"+m.getVb()+"'";

		if (n != null) {
		  sql = "INSERT INTO dmhist_entries(sid, mid, n, t, u, v, sv, bv) VALUES("+sid+", "+mid+", '"+n+"', " + t +", "+unit+", "+value+", "+stringvalue+", "+boolvalue+");";
		  stmt = conn.createStatement();
		  stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		  rs = stmt.getGeneratedKeys();
		  rs.close();
		  stmt.close();
		}

	      }

	    } else {
	      ret = false;
	    }
	  } catch (SQLException e) {
	    ret = false;
	  } finally {
	    try{
	      closeConnection(conn);
	    } catch(Exception e){}
	  
	  }

	  return ret;
	}


	/**
	 * @fn static Vector<SenML> fetchEndpoint(String serviceName, int count, Vector<String> signals)
	 *
	 */
	public static Vector<SenML> fetchEndpoint(String serviceName, long from, long to, int count, Vector<String> signals) {
	  Connection conn = null;
	  try {
	    conn = getConnection();
	    int id = serviceToID(serviceName, conn);
	    if (id == -1)
	      return null;

	    String signalss = "";
	    if (signals != null) {
	      for (String sig: signals) {
		signalss += ("'"+sig + "',");
	      }
	      signalss = signalss.substring(0, signalss.length()-1); //remove last ',' XXX. remove/detect escape characters 
	    }

	    if (from == -1)
	      from = 0;                                       //1970-01-01
	    if (to == -1)
	      to = 1000 + (long)(System.currentTimeMillis() / 1000.0); // now()

	    Statement stmt = conn.createStatement();

	    String sql = "";
	    if (signals != null)
	      sql = "SELECT * FROM dmhist_entries WHERE sid="+id+" AND n IN ("+signalss+") AND t >= "+from+" AND t <= "+to+" ORDER BY t DESC;";
	    else
	      sql = "SELECT * FROM dmhist_entries WHERE sid="+id+" AND t >= "+from+" AND t <= "+to+" ORDER BY t DESC;";
	    ResultSet rs = stmt.executeQuery(sql);

	    Vector<SenML> messages = new Vector<SenML>();
	    SenML hdr = new SenML();
	    hdr.setBn(serviceName);
	    messages.add(hdr);
	    double bt = 0;
	    String bu = null;
	    while(rs.next() == true && count > 0) {
	      SenML msg = new SenML();
	      msg.setT((double)rs.getLong("t"));
	      msg.setN(rs.getString("n"));
	      msg.setU(rs.getString("u"));
	      msg.setV(rs.getDouble("v"));

	      messages.add(msg);
	      count--;
	    }

	    rs.close();
	    stmt.close();

	    // if no data, was found, just return the header element
	    if (messages.size() == 1)
	      return messages;

	    //recalculate a bt time and update all relative timestamps
	    double startbt = ((SenML)messages.get(1)).getT();
	    ((SenML)messages.firstElement()).setBt(startbt);
	    ((SenML)messages.firstElement()).setT(null);
	    ((SenML)messages.get(1)).setT(null);
	    for (SenML m : messages) {
	      if (m.getT() != null)
		m.setT(m.getT()-startbt);
	    }

	    return messages;

	} catch (SQLException e) {
	  logger.debug(e.toString());
	} finally {
	  try {
	    closeConnection(conn);
	  } catch(Exception e){}

	}

	return null;
	}


	//returns largest (newest) timestamp value
	private static double maxTs(Vector<SenML> msg) {
		double bt = msg.get(0).getBt();
		double max = bt;
		for (SenML m : msg) {

			if (m.getT() == null)
				continue;
			if (m.getT() > 268435456) { // absolute
				if (m.getT() > max )
					max = m.getT();
			} else {                      //relative
				if (m.getT()+bt > max )
					max = m.getT() + bt;
			}
		}

		return max;
	}

	  //returns smallest (oldest) timestamp value
	  private static double minTs(Vector<SenML> msg) {
	    double bt = msg.get(0).getBt();
	    double min = bt;
	    for (SenML m : msg) {

	      if (m.getT() != null) {
		if ((m.getT() + bt) < min )
		  min = m.getT() + bt;
	      }
	    }

	    return min;
	}
}

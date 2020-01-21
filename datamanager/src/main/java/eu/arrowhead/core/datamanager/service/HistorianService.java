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

	 static {

	}

	/**
	 * @fn static boolean Init(Properties props)
	 * @brief 
	 *
	 */
	public static boolean Init(Properties propss){
	  props = propss;

	  System.out.println("HistorianServce::Init()");

	  try {
	    Class.forName("com.mysql.cj.jdbc.Driver");
	  } catch (ClassNotFoundException e) {
	    System.out.println("Where is your MySQL JDBC Driver?");
	    e.printStackTrace();
	    return false;
	  }

	  System.out.println("MySQL JDBC Driver Registered!");
	  try {
	    connection = getConnection();
	    //checkTables(connection, props.getProperty("spring.datasource.database"));
	    connection.close();
	  } catch (SQLException e) {
	    System.out.println("Connection Failed! Check output console");
	    e.printStackTrace();
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

	  //System.out.println("serviceToID('"+serviceName+"')");
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
	    //se.printStackTrace();
	  }catch(Exception e){
	    id = -1;
	    //e.printStackTrace();
	  }

	  //System.out.println("serviceToID('"+serviceName+"')="+id);
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
	    System.err.println(e.toString());
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
	    //System.out.println("addServiceForSystem: found " + id);
	    if (id != -1) {
	      return false; //already exists
	    } else {
	      Statement stmt = conn.createStatement();
	      String sql = "INSERT INTO dmhist_services(system_name, service_name, service_type) VALUES(\""+systemName+"\", \""+serviceName+"\", \""+serviceType+"\");"; //bug: check name for SQL injection!
	      //System.out.println(sql);
	      int mid = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
	      ResultSet rs = stmt.getGeneratedKeys();
	      rs.next();
	      id = rs.getInt(1);
	      rs.close();
	      stmt.close();
	      //System.out.println("addServiceForSystem: created " + id);

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
	    //System.out.println(sql);

	    ResultSet rs = stmt.executeQuery(sql);
	    while(rs.next() == true) {
	      //System.out.println("---"+rs.getString(1));
	      ret.add(rs.getString(1));
	    }
	    rs.close();
	    stmt.close();
	  }catch(SQLException db){
	    //System.out.println(db.toString());
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
	    //System.out.println("createEndpoint: found " + id);
	    if (id != -1) {
	      return true; //already exists
	    } else {
	      Statement stmt = conn.createStatement();
	      String sql = "INSERT INTO dmhist_services(system_name, service_name) VALUES(\""+systemName+"\", \""+serviceName+"\");"; //bug: check name for SQL injection!
	      //System.out.println(sql);
	      int mid = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
	      ResultSet rs = stmt.getGeneratedKeys();
	      rs.next();
	      id = rs.getInt(1);
	      rs.close();
	      stmt.close();
	      //System.out.println("createEndpoint: created " + id);

	    }

	  } catch (SQLException e) {
	    //System.out.println("createEndpoint:: "+e.toString());
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
	  System.out.println("bt(msg): "+(msg.get(0).getBt())+", minTs(msg): "+minTs+", maxTs(msg): " + maxTs);

	  Connection conn = null;
	  try {
	    conn = getConnection();
	    int sid = serviceToID(serviceName, conn);
	    if (sid != -1) {
	      Statement stmt = conn.createStatement();
	      String sql = "INSERT INTO dmhist_messages(sid, bt, mint, maxt, msg, datastored) VALUES("+sid+", "+msg.get(0).getBt()+","+minTs+", "+maxTs+", '"+msg.toString()+"',NOW());"; //how to escape?
	      System.out.println(sql);
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
		System.out.println("m: " + m.toString());
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
		  System.out.println(sql);
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
	    //System.out.println(e.toString());
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
	    //System.out.println("Got id of: " + id);
	    if (id == -1)
	      return null;

	    String signalss = "";
	    if (signals != null) {
	      for (String sig: signals) {
		signalss += ("'"+sig + "',");
	      }
	      signalss = signalss.substring(0, signalss.length()-1); //remove last ',' XXX. remove/detect escape characters 
	      System.out.println("Signals: '" + signalss + "'");
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
	    System.out.println(sql);
	    ResultSet rs = stmt.executeQuery(sql);

	    Vector<SenML> messages = new Vector<SenML>();
	    SenML hdr = new SenML();
	    hdr.setBn(serviceName);
	    messages.add(hdr);
	    double bt = 0;
	    String bu = null;
	    while(rs.next() == true && count > 0) {
	      //Gson gson = new Gson();
	      //SenML[] smlarr = gson.fromJson(rs.getString("msg"), SenML[].class);
	      //System.out.println("fetch() " + rs.getString("msg"));
	      SenML msg = new SenML();
	      msg.setT((double)rs.getLong("t"));
	      msg.setN(rs.getString("n"));
	      msg.setU(rs.getString("u"));
	      msg.setV(rs.getDouble("v"));

	      System.out.println("\t: " + msg.toString());
	      messages.add(msg);
	      count--;

	      /*for (SenML m : smlarr) {
		  if (m.getBt() != null) {
		    bt = m.getBt();

		    if (((SenML)messages.firstElement()).getBt() == null)
		      ((SenML)messages.firstElement()).setBt(bt);
		  }
		  if (m.getBu() != null) {
		    bu = m.getBu();

		    if (((SenML)messages.firstElement()).getBu() == null)
		      ((SenML)messages.firstElement()).setBu(bu);
		  }

		  System.out.println("  got " + m.getN());
		  // check if m contains a value in signals
		  if (signals.contains(m.getN())) {
		    if (m.getT() != null) {
		      if (m.getT() < 268435456) // if less than 2**28, it is relative
		        m.setT(bt+m.getT());
		    } else {
		      m.setT(bt);
		    }
		    messages.add(m);
		    count--;
		  }
		}*/
	    }

	    rs.close();
	    stmt.close();

	    //update bn fields (i.e. remove if the same as the first
	    /*String startbn = ((SenML)messages.firstElement()).getBn();
	    for (int i = 1; i< messages.size(); i++) {
	      SenML m = (SenML)messages.get(i);
	      System.out.println("startbn: "+ startbn+"\tm.Bn: "+m.getBn());
	      if (startbn.equals(m.getBn()))
		m.setBn(null);
	    }*/

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
	      System.out.println("\t" + m.toString());
	    }

	    // update unit tags: XXX do it another way! loop and check if all messages have the same u
	    /*String startbu = ((SenML)messages.firstElement()).getBu();
	    if (startbu != null) {
	      for (SenML m : messages) {
		try {
		  if (m.getU().equals(startbu)){
		    m.setU(null);
		  }
		} catch(Exception e){
		}
	      }
	    }*/

	    return messages;

	} catch (SQLException e) {
	  System.err.println(e.toString());
	} finally {
	  try {
	    closeConnection(conn);
	  } catch(Exception e){}

	}

	return null;
	}


	/**
	 * @fn static Vector<SenML> fetchEndpoint(String serviceName, long from, long to, int count)
	 * @brief
	 * @param name
	 * @param count
	 * @return
	 */
	/*public static Vector<SenML> fetchEndpoint(String serviceName, long from, long to, int count) {
	  Connection conn = null;

	  try {
	    conn = getConnection();
	    int id = serviceToID(serviceName, conn);
	    //System.out.println("Got id of: " + id);
	    if (id == -1) {
	      return null;
	    }
	    Statement stmt = conn.createStatement();
	    String sql = "SELECT * FROM dmhist_messages WHERE sid="+id+" ORDER BY datastored DESC LIMIT "+count+";";
	    System.out.println(sql);
	    ResultSet rs = stmt.executeQuery(sql);

	    String msg = "";
	    Vector<SenML> messages = new Vector<SenML>(); 
	    while(rs.next() == true && count > 0) {
	      msg = rs.getString("msg");
	      System.out.println("###\n"+ msg + "###");
	      Gson gson = new Gson();
	      SenML[] smlarr = gson.fromJson(msg, SenML[].class);

	      System.out.println("fetch() " + msg);
	      for (SenML m : smlarr)
		messages.add(m);
	      
	      count--;
	    }
	    rs.close();
	    stmt.close();

	    // if no data, was found, just return the header element
	    if (messages.size() <= 1)
	      return messages;

	    //recalculate a bt time and update all relative timestamps
	    double startbt = ((SenML)messages.get(0)).getBt();
	    //((SenML)messages.firstElement()).setBt(startbt);
	    ((SenML)messages.firstElement()).setT(null);
	    for (SenML m : messages) {
	      m.setBn(null);
	      System.out.println("\t" + m.toString());
	      if (m.getBt() != null) {
		m.setT(m.getBt());
		m.setBt(null);
	      }
	      if (m.getT() != null)
		m.setT(m.getT()-startbt);
	    }
	    ((SenML)messages.firstElement()).setBn(serviceName);

	    return messages;

	  } catch (SQLException e) {
	    System.err.println(e.toString());
	  } finally {
	    try{
	      closeConnection(conn);
	    } catch(Exception e){}

	  }

	    return null;
	  }*/

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

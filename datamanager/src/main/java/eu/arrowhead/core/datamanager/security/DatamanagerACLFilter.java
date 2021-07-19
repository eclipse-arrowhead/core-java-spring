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
package eu.arrowhead.core.datamanager.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.exception.AuthException;


@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class DatamanagerACLFilter {
	
	//=================================================================================================
	// members

	private final Logger logger = LogManager.getLogger(DatamanagerACLFilter.class);

    @Autowired
    private ApplicationContext context;

    @Value("${acl.file}")
    private String aclFileName;

    private ArrayList<AclRule> rules = null;

    private static final boolean USE_DEFAULTS = true;

    public static final String DM_HIST_OP = CommonConstants.DATAMANAGER_URI + CommonConstants.OP_DATAMANAGER_HISTORIAN;
    public static final String DM_PROXY_OP = CommonConstants.DATAMANAGER_URI + CommonConstants.OP_DATAMANAGER_PROXY;
    public static final String DM_HIST_OP_WS = DM_HIST_OP + "/ws";

    public static final String METHOD_GET  = "GET";
    public static final String METHOD_PUT  = "PUT";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DEL  = "DELETE";

    public static final String ACL_METHODS = "gpPd";
    public static final String ACL_METHOD_GET  = "g";
    public static final String ACL_METHOD_PUT  = "p";
    public static final String ACL_METHOD_POST = "P";
    public static final String ACL_METHOD_DEL  = "d";

    public static final String ACL_LINE_COMMENT1 = "#";
    public static final String ACL_LINE_COMMENT2 = ";";
    public static final String ACL_SYS_WILDCARD  = "$SYS";
    public static final String ACL_SRV_WILDCARD  = "*";

    public static final String ACL_SYS_DELIM  = ":";
    public static final String ACL_RULE_DELIM  = ",";
    public static final String ACL_PATH_DELIM  = "@";
    public static final String ACL_PATH_SEPARATOR  = "/";
    public static final String ACL_DEFAULT_RULE = "$SYS: gpPd@$SYS/*";
        
	//=================================================================================================
	// methods

    public DatamanagerACLFilter() {
        
    }

    @PostConstruct
    public void init() {
        final boolean res = loadFile(aclFileName);
        if (res==false) {
            logger.info("Could not load ACL file!");

            if (USE_DEFAULTS) {
                logger.info("Loading defaults ...");
                final String[] defaults = new String[1];
                defaults[0] = ACL_DEFAULT_RULE;
                load(defaults);
            } else {
                logger.info("Exiting ...");
                final int exitCode = 0;
                SpringApplication.exit(context, () -> exitCode);
                System.exit(exitCode);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
    public boolean checkRequest(final String systemCN, final String operation, final String path) throws Exception {
        logger.debug("checkRequest for CN: {}, op: {} for '{}'", systemCN, operation, path);

        String endPath = "";
        if (path.contains(DM_HIST_OP_WS)) {
            final int idx = path.indexOf(DM_HIST_OP_WS) + DM_HIST_OP_WS.length() + 1;
            if (idx < path.length()) {
            	endPath = path.substring(idx);
            }
        } else if (path.contains(DM_HIST_OP)) {
            final int idx = path.indexOf(DM_HIST_OP) + DM_HIST_OP.length() + 1;
            if (idx < path.length()) {
            	endPath = path.substring(idx);
            }
        } else if (path.contains(DM_PROXY_OP)){
            final int idx = path.indexOf(DM_PROXY_OP)+ DM_PROXY_OP.length() + 1;
            if (idx < path.length()) {
            	endPath = path.substring(idx);
            }
        }

        logger.debug("endPath: {}", endPath);
        if (endPath.isEmpty()) {
        	return true;
        }
        
        final String[] targetPath = endPath.split(ACL_PATH_SEPARATOR);
        String op = "";

        switch (operation.toUpperCase().trim()) {
            case METHOD_GET:
                op = ACL_METHOD_GET;
                break;
            case METHOD_PUT:
                op = ACL_METHOD_PUT;
                break;
            case METHOD_POST:
                op = ACL_METHOD_POST;
                break;
            case METHOD_DEL:
                op = ACL_METHOD_DEL;
                break;
            default:
                throw new AuthException("Unknown method");
        }

        for (final AclRule rule: rules) {
            if (rule.systemName.equalsIgnoreCase(systemCN)) {
                for (final AclEntry acl: rule.acls) {
                    final String[] pathParts = acl.path.split(ACL_PATH_SEPARATOR);
                    final String pathSystem = pathParts[0].trim();
                    final String pathService = pathParts[1].trim();

                    if (acl.path.equals(endPath)) {
                        if (acl.operations.contains(op)) {
                            return true;
                        }
                    } else if (pathSystem.equals(targetPath[0]) && pathService.equals(ACL_SRV_WILDCARD)) {
                        if (acl.operations.contains(op)) {
                            return true;
                        }
                    } else if(pathSystem.equals(ACL_SRV_WILDCARD) && pathService.equals(ACL_SRV_WILDCARD)) {
                        if (acl.operations.contains(op)) {
                            return true;
                        }
                    } 

                }
            } else if (rule.systemName.equals(ACL_SYS_WILDCARD)) {
                for (final AclEntry acl: rule.acls) {
                    final String[] pathParts = acl.path.split(ACL_PATH_SEPARATOR);
                    String pathSystem = "";
                    String pathService = "";
                    if (pathParts.length == 2) {
                        pathSystem = pathParts[0].trim();
                        pathService = pathParts[1].trim();
                    }

                    if (targetPath.length == 1) {
                        if ((pathSystem.equals(ACL_SYS_WILDCARD) && targetPath[0].equalsIgnoreCase(systemCN))) {
                            if (acl.operations.contains(op)) {
                                return true;
                            }
                        }
                    } else {
                        if ((pathSystem.equals(ACL_SYS_WILDCARD) && targetPath[0].equalsIgnoreCase(systemCN)) && (pathService.equals(ACL_SRV_WILDCARD) || pathService.equalsIgnoreCase(targetPath[1]))) {
                            if (acl.operations.contains(op)) {
                                return true;
                            }
                        }
                    }

                }
            }

        }
        
        return false;
    }


    //-------------------------------------------------------------------------------------------------
    public boolean load(final String[] lines) {
        rules = new ArrayList<AclRule>();

        try {
            for (String line: lines) {
                line = line.trim();
                if (line.startsWith(ACL_LINE_COMMENT1) || line.startsWith(ACL_LINE_COMMENT2) || line.equals("")) {
                    continue;
                }

                final String[] parts = line.split(ACL_SYS_DELIM);
                final AclRule r = new AclRule(parts[0].trim());
                final String rulesData[] = parts[1].trim().split(ACL_RULE_DELIM);
                for (final String rule : rulesData) {
                    r.addACLEntries(rule.trim());
                }
                rules.add(r);
            }
        } catch (final Exception e) {
            logger.info("Misformed ACL file!");
            rules = new ArrayList<AclRule>();

            return false;
        }

        return true;
    }


    //-------------------------------------------------------------------------------------------------
    public boolean loadFile(final String filename) {
        final ArrayList<String> lines = new ArrayList<String>();

        try {
            final Scanner myReader = new Scanner(new File(filename));
            while (myReader.hasNextLine()) {
              final String line = myReader.nextLine().trim();
              lines.add(line);
            }
            myReader.close();

            return load(lines.toArray(new String[lines.size()]));
        } catch (final FileNotFoundException e) {
            logger.debug("Could not load ACL file!");
            logger.debug(e);
        }
        return false;
    }

    //-------------------------------------------------------------------------------------------------
    private static class AclEntry {
        String operations;
        String path;

        public AclEntry() {

        }
    }

    //-------------------------------------------------------------------------------------------------
    private static class AclRule {
        String systemName;
        ArrayList<AclEntry> acls = new ArrayList<AclEntry>();

        
        public AclRule(final String systemName){
            this.systemName = systemName;
        }


        private void addACLEntries(final String entryString) throws Exception{
            final String[] parts = entryString.split(ACL_RULE_DELIM);

            for (final String part : parts) {
                addACLEntry(part);
            }
        }
        
        private void addACLEntry(final String entryString) throws Exception {
            final String[] parts = entryString.split(ACL_PATH_DELIM);
            final String operations = parts[0].trim().toLowerCase();
            final String path = parts[1].trim();

            // allow CRUD only operations
            for (int i = 0; i < operations.length(); i++) {
                final String op = "" + operations.charAt(i);
                if (ACL_METHODS.indexOf(op) == -1) {
                    throw new Exception("illegal operation: " + op);
                }
            }
            final AclEntry acl = new AclEntry();
            acl.operations = operations;
            acl.path = path;
            acls.add(acl);
        }

    }
}
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
import java.util.Scanner;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import org.springframework.beans.factory.annotation.Value;
import eu.arrowhead.common.SecurityUtilities;
import eu.arrowhead.common.dto.shared.CertificateType;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.SpringApplication;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.AuthException;

import java.util.Map;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class DatamanagerACLFilter {

    private final Logger logger = LogManager.getLogger(DatamanagerACLFilter.class);

    @Autowired
    private ApplicationContext context;

    @Value("${acl.file}")
    private String aclFileName;

    ArrayList<AclRule> rules = null;

    //=================================================================================================
    // members
        
	//=================================================================================================
	// assistant methods

    public DatamanagerACLFilter() {
        
    }

    @PostConstruct
    public void init() {
        System.out.println("ACL file name: " + aclFileName);
        boolean res = loadFile(aclFileName);
        if(res==false) {
            logger.info("Could not load ACL file!");
            
            int exitCode = 0;
            SpringApplication.exit(context, () -> exitCode);
            System.exit(exitCode);
        }
    }


    //-------------------------------------------------------------------------------------------------
    public boolean checkRequest(final String systemCN, final String operation, final String path) throws Exception {

        logger.info("\n\nsystemName: "+  systemCN+ " path: " + path);
        logger.info("Method: " + operation);

        String endPath = "";
        if (path.contains("/datamanager/historian")) {
            endPath = path.substring(path.indexOf("/datamanager/historian") + 23);
        } else if (path.contains("datamanager/proxy")){
            endPath = path.substring(path.indexOf("/datamanager/proxy")+ 19);
        }
        System.out.println("End of path is: " + endPath);

        final String[] targetPath = endPath.split("/");
        String op = "";

        switch(operation.trim()) { //XXX fixme
            case "GET":
                op = "g";
                System.out.println("op is g");
                break;
            case "PUT":
                op ="p";
                System.out.println("op is p");
                break;
            case "POST":
                op ="P";
                System.out.println("op is P");
                break;
            case "DELETE":
                op ="d";
                System.out.println("op is d");
                break;
            default:
                throw new AuthException("Unknown method");
        }

        // check all rules
        for(AclRule rule: rules) {

            // only check rules that matches systemName or $SYS constant
            if(rule.systemName.equals(systemCN)) {
                System.out.println("Found matching system name: " + rule.systemName);

                for(AclEntry acl: rule.acls) {
                    System.out.println("ACL-path: " + acl.path);
                    final String[] pathParts = acl.path.split("/");
                    final String pathSystem = pathParts[0].trim();
                    final String pathService = pathParts[1].trim();

                    // check hard coded rule
                    if(acl.path.equals(endPath)) {
                        System.out.println("Found matching path: " + endPath);
                        if(acl.operations.contains(op)) {
                            System.out.println("\tFound allowed operation0: " + operation);
                            return true;
                        }
                    } else if(pathSystem.equals(targetPath[0]) && pathService.equals("*")) { // match aname/*
                        if(acl.operations.contains(op)) {
                            System.out.println("\tFound allowed operation1: " + operation);
                            return true;
                        }
                    } else if(pathSystem.equals("*") && pathService.equals("*")) { // match */*
                        if(acl.operations.contains(op)) {
                            System.out.println("\tFound allowed operation2: " + operation);
                            return true;
                        }
                    } else {
                        System.out.println("No match for endpath: " + endPath + " for rule: " + acl.path + " ops: " + acl.operations);
                    }

                }
            } else if(rule.systemName.equals("$SYS")) {
                System.out.println("Found matching system name: $SYS(" + systemCN + ")");

                for(AclEntry acl: rule.acls) {
                    System.out.println("ACL-path: " + acl.path);
                    final String[] pathParts = acl.path.split("/");
                    String pathSystem = "";
                    String pathService = "";
                    if (pathParts.length == 2) {
                        pathSystem = pathParts[0].trim();
                        pathService = pathParts[1].trim();
                    }

                    System.out.println("pathSystem: " + pathSystem);
                    System.out.println("pathService: " + pathService);
                    if(targetPath.length == 1) {
                        if((pathSystem.equals("$SYS") && targetPath[0].equals(systemCN))) {
                            if(acl.operations.contains(op)) {
                                System.out.println("\tFound allowed operationS-1: " + operation);
                                return true;
                            }
                        }
                    } else {

                        System.out.println("targetPath[1]: " + targetPath[1]);
                        System.out.println("!\n");

                        if((pathSystem.equals("$SYS") && targetPath[0].equals(systemCN)) && (pathService.equals("*") || pathService.equals(targetPath[1]))) {
                            if(acl.operations.contains(op)) {
                                System.out.println("\tFound allowed operationS0: " + operation);
                                return true;
                            }
                        }
                    }

                }
            } //else if *

        }
        System.out.println("No auth rule found!\n");
        return false;
    }
    //-------------------------------------------------------------------------------------------------
    public boolean load(final String[] lines) {
        rules = new ArrayList<AclRule>();

        try {
            for(String line: lines) {
                line = line.trim();
                if(line.startsWith("#") || line.startsWith(";") || line.equals("")) {
                    continue;
                }

                System.out.println(line);
                String[] parts = line.split(":");
                System.out.println("\tSystem is " + parts[0].trim());
                System.out.println("\tRule is " + parts[1].trim());

                final AclRule r  = new AclRule(parts[0].trim());
                final String rulesData[] = parts[1].trim().split(",");
                for (String rule : rulesData) {
                    r.addACLEntries(rule.trim());
                }

                rules.add(r);
            }

        } catch(Exception e) {
            System.out.println("Misformad ACL file!");
            rules = new ArrayList<AclRule>();

            return false;
        }
        return true;
    }


    //-------------------------------------------------------------------------------------------------
    public boolean loadFile(final String filename) {
        final ArrayList<String> lines = new ArrayList<String>();

        try {
            Scanner myReader = new Scanner(new File(filename));
            while (myReader.hasNextLine()) {
              String line = myReader.nextLine().trim();
              lines.add(line);
            }
            myReader.close();

            return load(lines.toArray(new String[lines.size()]));

        } catch (FileNotFoundException e) {
            logger.debug("Could not load ACL file!");
            e.printStackTrace();
        }
        return false;
    }

    class AclEntry {
        String operations;
        String path;

        public AclEntry() {

        }
    }

    class AclRule {
        String systemName;
        ArrayList<AclEntry> acls = new ArrayList<AclEntry>();

        
        public AclRule(final String systemName){
            this.systemName = systemName;
        }


        private void addACLEntries(final String entryString) throws Exception{
            final String[] parts = entryString.split(",");

            for (String part : parts) {
                addACLEntry(part);
            }
            
        }

        
        private void addACLEntry(final String entryString) throws Exception {
            System.out.println("\t\t<" + entryString.trim() + ">");
            String[] parts = entryString.split("@");
            final String operations = parts[0].trim().toLowerCase();
            final String path = parts[1].trim();

            // validate CRUD only operations
            for (int i = 0; i < operations.length(); i++) {
                String op = "" + operations.charAt(i);
                if("gpPd".indexOf(op) == -1) {
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
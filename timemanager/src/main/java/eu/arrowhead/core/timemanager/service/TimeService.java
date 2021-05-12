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
package eu.arrowhead.core.timemanager.service;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.CoreCommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.internal.DTOConverter;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.timemanager.database.service.TimeManagerDBService;

import java.util.List;
import java.util.ArrayList; 
import java.util.Iterator; 
import java.util.Vector;


@Service
public class TimeService {

    //=================================================================================================
    // members
    //

    private final Logger logger = LogManager.getLogger(TimeService.class);

    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public TimeService() {

    }

    //=================================================================================================
    // assistant methods

}

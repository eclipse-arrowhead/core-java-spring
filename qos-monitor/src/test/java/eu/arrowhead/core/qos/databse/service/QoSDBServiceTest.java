package eu.arrowhead.core.qos.databse.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.database.entity.System;
import eu.arrowhead.common.database.entity.QoSIntraMeasurement;
import eu.arrowhead.common.database.entity.QoSIntraPingMeasurement;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementPingRepository;
import eu.arrowhead.common.database.repository.QoSIntraMeasurementRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogDetailsRepository;
import eu.arrowhead.common.database.repository.QoSIntraPingMeasurementLogRepository;
import eu.arrowhead.common.database.repository.SystemRepository;
import eu.arrowhead.common.dto.shared.QoSMeasurementType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.core.qos.database.service.QoSDBService;

@RunWith(SpringRunner.class)
public class QoSDBServiceTest {

	//=================================================================================================
	// members
	@InjectMocks
	private QoSDBService qoSDBService;

	@Mock
	private QoSIntraMeasurementRepository qoSIntraMeasurementRepository;

	@Mock
	private QoSIntraMeasurementPingRepository qoSIntraMeasurementPingRepository;

	@Mock
	private QoSIntraPingMeasurementLogRepository qoSIntraPingMeasurementLogRepository;

	@Mock
	private QoSIntraPingMeasurementLogDetailsRepository qoSIntraPingMeasurementLogDetailsRepository;

	@Mock
	private SystemRepository systemRepository;

	private static final String LESS_THAN_ONE_ERROR_MESSAGE= " must be greater than zero.";
	private static final String NOT_AVAILABLE_SORTABLE_FIELD_ERROR_MESSAGE = " sortable field  is not available.";
	private static final String NOT_IN_DB_ERROR_MESSAGE = " is not available in database";

	//=================================================================================================
	// methods

	//=================================================================================================
	// Tests of updateCountStartedAt

	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testUpdateCountStartedAt() {

		final List<QoSIntraPingMeasurement> measurementList = getQosIntraPingMeasurementListForTest();

		//TODO Implement testing logic here
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private List<QoSIntraPingMeasurement> getQosIntraPingMeasurementListForTest() {

		final int sizeOfMeasurementList = 3;
		final List<QoSIntraPingMeasurement> qoSIntraPingMeasurementList = new ArrayList<>(sizeOfMeasurementList);

		final QoSIntraMeasurement measurement = getQoSIntraMeasurementForTest();

		for (int i = 0; i < sizeOfMeasurementList; i++) {

			final QoSIntraPingMeasurement pingMeasurement = new QoSIntraPingMeasurement();
			pingMeasurement.setMeasurement(measurement);
			pingMeasurement.setAvailable(true);
			pingMeasurement.setMaxResponseTime(1);
			pingMeasurement.setMinResponseTime(1);
			pingMeasurement.setMeanResponseTimeWithoutTimeout(1);
			pingMeasurement.setMeanResponseTimeWithTimeout(1);
			pingMeasurement.setJitterWithoutTimeout(1);
			pingMeasurement.setJitterWithTimeout(1);
			pingMeasurement.setLostPerMeasurementPercent(0);
			pingMeasurement.setCountStartedAt(ZonedDateTime.now());
			pingMeasurement.setLastAccessAt(ZonedDateTime.now());
			pingMeasurement.setSent(35);
			pingMeasurement.setSentAll(35);
			pingMeasurement.setReceived(35);
			pingMeasurement.setReceivedAll(35);
		}

		return qoSIntraPingMeasurementList;
	}

	//-------------------------------------------------------------------------------------------------
	private QoSIntraMeasurement getQoSIntraMeasurementForTest() {

		final System system = getSystemForTest();
		final QoSIntraMeasurement measurement = new QoSIntraMeasurement(
				system, 
				QoSMeasurementType.PING, 
				ZonedDateTime.now());

		return measurement;
	}

	//-------------------------------------------------------------------------------------------------
	private System getSystemForTest() {

		final System system = new System(
				"testSystem",
				"address",
				12345,
				"authenticationInfo");

		return system;
	}
}

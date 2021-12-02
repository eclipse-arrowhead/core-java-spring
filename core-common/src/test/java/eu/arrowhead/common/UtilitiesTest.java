/********************************************************************************
 * Copyright (c) 2019 AITIA
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   AITIA - implementation
 *   Arrowhead Consortia - conceptualization
 ********************************************************************************/

package eu.arrowhead.common;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.exception.UnavailableServerException;

@RunWith(SpringRunner.class)
public class UtilitiesTest {

	//=================================================================================================
	// members

	private static final String OS_NAME = "os.name";
	private static final String WINDOWS_PREFIX = "win";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testToPrettyJSONInputNull() {
		final String result = Utilities.toPrettyJson(null);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testToPrettyJSONNotJSON() {
		final String result = Utilities.toPrettyJson("abc");
		Assert.assertEquals("abc", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testToPrettyJSONValidObject() {
		final String osName = System.getProperty(OS_NAME).toLowerCase();
		final String expected =  osName.startsWith(WINDOWS_PREFIX) ? "{\r\n  \"a\" : 1\r\n}" : "{\n  \"a\" : 1\n}";
		
		final String result = Utilities.toPrettyJson("{ \"a\": 1 }");
		Assert.assertEquals(expected, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testToPrettyJSONValidList() {
		final String osName = System.getProperty(OS_NAME).toLowerCase();
		final String expected =  osName.startsWith(WINDOWS_PREFIX) ? "[ {\r\n  \"a\" : 1\r\n} ]" : "[ {\n  \"a\" : 1\n} ]";
		
		final String result = Utilities.toPrettyJson("[{ \"a\": 1 }]");
		Assert.assertEquals(expected, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testToJSONNullObject() {
		final String result = Utilities.toJson(null);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testToJSONValidObject() {
		final ErrorMessageDTO dto = new ErrorMessageDTO("test", 11, ExceptionType.GENERIC, null);
		final String osName = System.getProperty(OS_NAME).toLowerCase();
		final String expected = osName.startsWith(WINDOWS_PREFIX) ? "{\r\n  \"errorMessage\" : \"test\",\r\n  \"errorCode\" : 11,\r\n  \"exceptionType\" : \"GENERIC\"\r\n}" : 
																	"{\n  \"errorMessage\" : \"test\",\n  \"errorCode\" : 11,\n  \"exceptionType\" : \"GENERIC\"\n}";

		final String result = Utilities.toJson(dto);
		Assert.assertEquals(expected, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFromJSONNullString() {
		final ErrorMessageDTO result = Utilities.fromJson(null, ErrorMessageDTO.class);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFromJSONNullClass() {
		final Object result = Utilities.fromJson("does not matter", null);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testFromJSONConvertFailed() {
		Utilities.fromJson("wrong JSON", ErrorMessageDTO.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testFromJSONConvertSuccess() {
		final ErrorMessageDTO result = Utilities.fromJson("{ \"exceptionType\": \"AUTH\" }", ErrorMessageDTO.class);
		Assert.assertEquals(ExceptionType.AUTH, result.getExceptionType());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testText2MapNull() {
		Assert.assertNull(Utilities.text2Map(null));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testText2MapEmpty() {
		final Map<String,String> result = Utilities.text2Map("    ");
		Assert.assertEquals(0, result.size());
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testText2MapOne() {
		final Map<String,String> result = Utilities.text2Map("abc = def");
		Assert.assertEquals(1, result.size());
		Assert.assertEquals("def", result.get("abc"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testText2MapTwoOneWOValue() {
		final Map<String,String> result = Utilities.text2Map("abc =, def= ghi   ");
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("", result.get("abc"));
		Assert.assertEquals("ghi", result.get("def"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testText2MapDecoding() {
		final Map<String,String> result = Utilities.text2Map("ab%3D = def,  ghi = i%2Cj");
		Assert.assertEquals(2, result.size());
		Assert.assertEquals("def", result.get("ab="));
		Assert.assertEquals("i,j", result.get("ghi"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMap2TextNull() {
		Assert.assertNull(Utilities.map2Text(null));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMap2TextEmpty() {
		final String result = Utilities.map2Text(Collections.<String,String>emptyMap());
		Assert.assertEquals("", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMap2TextNormal() {
		final Map<String,String> map = new HashMap<String,String>(2);
		map.put("abc", "def");
		map.put("ghi", "jkl");
		final String result = Utilities.map2Text(map);
		Assert.assertTrue(result.contains("abc=def"));
		Assert.assertTrue(result.contains("ghi=jkl"));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testMap2TextEncoding() {
		final Map<String,String> map = new HashMap<String,String>(1);
		map.put("ab=", "d,f");
		final String result = Utilities.map2Text(map);
		Assert.assertEquals("ab%3D=d%2Cf", result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateHttpStatusFromArrowheadExceptionBradbury() {
		final ArrowheadException ex = new ArrowheadException("Fahrenheit", 451);
		final HttpStatus result = Utilities.calculateHttpStatusFromArrowheadException(ex);
		Assert.assertEquals(HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testCalculateHttpStatusFromArrowheadExceptionInternalServerError() {
		final ArrowheadException ex = new ArrowheadException("Does not matter.");
		final HttpStatus result = Utilities.calculateHttpStatusFromArrowheadException(ex);
		Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertStringToRelayTypeWithNullString() {
		final RelayType convertedType = Utilities.convertStringToRelayType(null);
		Assert.assertEquals(RelayType.GENERAL_RELAY, convertedType);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertStringToRelayTypeWithBlankString() {
		final RelayType convertedType = Utilities.convertStringToRelayType("");
		Assert.assertEquals(RelayType.GENERAL_RELAY, convertedType);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertStringToRelayTypeWithInvalidString() {
		final RelayType convertedType = Utilities.convertStringToRelayType("InvalidString");
		Assert.assertNull(convertedType);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertStringToRelayTypeWithValidGatekeeperTypeString() {
		final RelayType convertedType = Utilities.convertStringToRelayType("GATEKEEPER_RELAY");
		Assert.assertEquals(RelayType.GATEKEEPER_RELAY, convertedType);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertStringToRelayTypeWithValidGatewayTypeString() {
		final RelayType convertedType = Utilities.convertStringToRelayType("GATEWAY_RELAY");
		Assert.assertEquals(RelayType.GATEWAY_RELAY, convertedType);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertStringToRelayTypeWithValidGeneralTypeString() {
		final RelayType convertedType = Utilities.convertStringToRelayType("GENERAL_RELAY");
		Assert.assertEquals(RelayType.GENERAL_RELAY, convertedType);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertStringToRelayTypeWithValidButLowercaseStringWithWhiteSpaces() {
		final RelayType convertedType = Utilities.convertStringToRelayType(" general_relay ");
		Assert.assertEquals(RelayType.GENERAL_RELAY, convertedType);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCertCNFromSubjectOk() {
		final String result = Utilities.getCertCNFromSubject("cn=abc.def.gh");
		Assert.assertEquals("abc.def.gh", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCertCNFromSubjectNotOk() {
		final String result = Utilities.getCertCNFromSubject("abc.def.gh");
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetCertCNFromSubjectNullParameter() {
		final String result = Utilities.getCertCNFromSubject(null);
		Assert.assertNull(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetSystemCertFromKeyStoreNullKeyStore() throws KeyStoreException {
		Utilities.getSystemCertFromKeyStore(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ServiceConfigurationError.class)
	public void testGetSystemCertFromKeyStoreNotInitializedKeyStore() throws KeyStoreException {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		Utilities.getSystemCertFromKeyStore(keystore);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ServiceConfigurationError.class)
	public void testGetSystemCertFromKeyStoreEmptyKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(null, null);
		Utilities.getSystemCertFromKeyStore(keystore);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetPrivateKeyNullKeyStore() throws KeyStoreException {
		Utilities.getPrivateKey(null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetPrivateKeyNullKeyPass() throws KeyStoreException {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		Utilities.getPrivateKey(keystore, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ServiceConfigurationError.class)
	public void testGetPrivateKeyNotInitializedKeyStore() throws KeyStoreException {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		Utilities.getPrivateKey(keystore, "abc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ServiceConfigurationError.class)
	public void testGetPrivateKeyEmptyKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		final KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(null, null);
		Utilities.getPrivateKey(keystore, "abc");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetPublicKeyFromBase64EncodedStringNull() {
		Utilities.getPublicKeyFromBase64EncodedString(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetPublicKeyFromBase64EncodedStringEmpty() {
		Utilities.getPublicKeyFromBase64EncodedString("  ");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetPublicKeyFromBase64EncodedStringNotBase64() {
		Utilities.getPublicKeyFromBase64EncodedString(";");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testGetPublicKeyFromBase64EncodedStringNotAKey() {
		Utilities.getPublicKeyFromBase64EncodedString("bm90IGEga2V5");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testGetPublicKeyFromPEMFileInputStreamNull() {
		Utilities.getPublicKeyFromPEMFile(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testGetPublicKeyFromPEMFileIOException() throws IOException {
		final InputStream is = InputStream.nullInputStream();
		is.close();
		Utilities.getPublicKeyFromPEMFile(is);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testGetPublicKeyFromPEMFileOk() throws IOException {
		final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		final PublicKey publicKey = Utilities.getPublicKeyFromPEMFile(is);
		Assert.assertEquals("RSA", publicKey.getAlgorithm());
		Assert.assertEquals("X.509", publicKey.getFormat());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidGood() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("service.cloud.operator.arrowhead.eu");
		Assert.assertTrue(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidWrongFormat() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("service;cloud;operator;arrowhead;eu");
		Assert.assertFalse(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidTooShort() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("cloud.operator.arrowhead.eu");
		Assert.assertFalse(result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidWrongSuffix() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("service.cloud.operator.arrowhead.hu");
		Assert.assertFalse(result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidWrongMasterName() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("service.cloud.operator.arowhead.eu");
		Assert.assertFalse(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidCompareWithCloudCNGood() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("service.cloud.operator.arrowhead.eu", "cloud.OPERATOR.arrowhead.eu");
		Assert.assertTrue(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidCompareWithCloudCNWrongFormat() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("service;cloud;operator;arrowhead;eu", "cloud.operator.arrowhead.eu");
		Assert.assertFalse(result);
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testIsKeyStoreCNArrowheadValidCompareWithCloudCNDifferentCloud() {
		final boolean result = Utilities.isKeyStoreCNArrowheadValid("service.cloud2.operator.arrowhead.eu", "cloud.OPERATOR.arrowhead.eu");
		Assert.assertFalse(result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertZonedDateTimeToUTCStringNull() {
		Assert.assertNull(Utilities.convertZonedDateTimeToUTCString(null));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testConvertZonedDateTimeToUTCStringNotNull() {
		final ZonedDateTime time = ZonedDateTime.of(2019, 6, 18, 14, 31, 10, 0, ZoneId.of("+3"));
		final String result = Utilities.convertZonedDateTimeToUTCString(time);
		Assert.assertEquals("2019-06-18T11:31:10Z", result);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testParseUTCStringToLocalZonedDateTimeNull() {
		Assert.assertNull(Utilities.parseUTCStringToLocalZonedDateTime(null));
	}

	//-------------------------------------------------------------------------------------------------
	@Test
	public void testParseUTCStringToLocalZonedDateTimeEmpty() {
		Assert.assertNull(Utilities.parseUTCStringToLocalZonedDateTime("  "));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DateTimeParseException.class)
	public void testParseUTCStringToLocalZonedDateTimeIllFormed() {
		Utilities.parseUTCStringToLocalZonedDateTime("2019/06/18 12:45:31");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testParseUTCStringToLocalZonedDateTimeWellFormed() {
		final String timeStr = "2019-06-18T12:45:31Z";
		final Instant instant = Instant.parse(timeStr);
		LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		int offset = TimeZone.getDefault().getOffset(instant.toEpochMilli());
		ldt = ldt.plus(offset, ChronoUnit.MILLIS);
		
		final ZonedDateTime result = Utilities.parseUTCStringToLocalZonedDateTime(timeStr);
		Assert.assertEquals(ldt.getDayOfMonth(), result.getDayOfMonth());
		Assert.assertEquals(ldt.getHour(), result.getHour());
		Assert.assertEquals(ldt.getMinute(), result.getMinute());
		Assert.assertEquals(ldt.getSecond(), result.getSecond());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateExceptionFromErrorMessageDTOParamNull() {
		Utilities.createExceptionFromErrorMessageDTO(null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testCreateExceptionFromErrorMessageDTOExceptionTypeNull() {
		Utilities.createExceptionFromErrorMessageDTO(new ErrorMessageDTO());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateExceptionFromErrorMessageDTOArrowheadException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.ARROWHEAD, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testCreateExceptionFromErrorMessageDTOAuthException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.AUTH, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = BadPayloadException.class)
	public void testCreateExceptionFromErrorMessageDTOBadPayloadException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.BAD_PAYLOAD, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testCreateExceptionFromErrorMessageDTOInvalidParameterException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.INVALID_PARAMETER, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = DataNotFoundException.class)
	public void testCreateExceptionFromErrorMessageDTODataNotFoundException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.DATA_NOT_FOUND, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = ArrowheadException.class)
	public void testCreateExceptionFromErrorMessageDTOGenericException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.GENERIC, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = TimeoutException.class)
	public void testCreateExceptionFromErrorMessageDTOTimeoutException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.TIMEOUT, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = UnavailableServerException.class)
	public void testCreateExceptionFromErrorMessageDTOUnavailableServerException() {
		final ErrorMessageDTO error = new ErrorMessageDTO("error", 0, ExceptionType.UNAVAILABLE, "origin");
		Utilities.createExceptionFromErrorMessageDTO(error);
	}
}
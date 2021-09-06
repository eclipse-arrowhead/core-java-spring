package eu.arrowhead.common.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.processor.model.AddressDetectionResult;
import eu.arrowhead.common.verifier.NetworkAddressVerifier;

@RunWith(SpringRunner.class)
public class NetworkAddressDetectorTest {
	
	//=================================================================================================
	// members
	
	@InjectMocks
	private NetworkAddressDetector networkAddressDetector;
	
	@Spy
	private NetworkAddressPreProcessor networkAddressPreProcessor;

	@Spy
	private NetworkAddressVerifier networkAddressVerifier;
	
	private static final String HEADER_FORWARDED = "forwarded";
	private static final String HEADER_X_FORWARDED_FOR = "x-forwarded-for";
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_FlagFalse() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", false);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertTrue(result.isSkipped());
		assertFalse(result.isDetectionSuccess());
		assertTrue(Utilities.isEmpty(result.getDetectedAddress()));
		assertEquals("Address detection process was skipped", result.getDetectionMessage());
		
		verify(networkAddressPreProcessor, never()).normalize(anyString());
		verify(networkAddressVerifier, never()).verify(anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_RequestNull() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		
		final AddressDetectionResult result = networkAddressDetector.detect(null);
		
		assertTrue(result.isSkipped());
		assertFalse(result.isDetectionSuccess());
		assertTrue(Utilities.isEmpty(result.getDetectedAddress()));
		assertEquals("Address detection process was skipped", result.getDetectionMessage());
		
		verify(networkAddressPreProcessor, never()).normalize(anyString());
		verify(networkAddressVerifier, never()).verify(anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_RetriveFromConnector_1() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final String remoteAddr = "170.132.0.10";
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals(remoteAddr, result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(remoteAddr));
		verify(networkAddressVerifier, times(1)).verify(eq(remoteAddr));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_RetriveFromConnector_2() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final String remoteAddr = NetworkAddressVerifier.IPV4_PLACEHOLDER;
		final String localAddr = "170.132.0.10";
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
		when(requestMock.getLocalAddr()).thenReturn(localAddr);
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals(localAddr, result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(remoteAddr));
		verify(networkAddressVerifier, times(1)).verify(eq(localAddr));
		verify(networkAddressPreProcessor, times(1)).normalize(eq(localAddr));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_RetriveFromConnector_3() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final String remoteAddr = "127.0.0.1";
		final String localAddr = "170.132.0.10";
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
		when(requestMock.getLocalAddr()).thenReturn(localAddr);
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals(localAddr, result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(remoteAddr));
		verify(networkAddressVerifier, times(1)).verify(eq(localAddr));
		verify(networkAddressPreProcessor, times(1)).normalize(eq(localAddr));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_RetriveFromConnector_4() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final String remoteAddr = NetworkAddressVerifier.IPV6_LOOPBACK;
		final String localAddr = "170.132.0.10";
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
		when(requestMock.getLocalAddr()).thenReturn(localAddr);
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals(localAddr, result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(remoteAddr));
		verify(networkAddressVerifier, times(1)).verify(eq(localAddr));
		verify(networkAddressPreProcessor, times(1)).normalize(eq(localAddr));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_RetriveFromConnector_5() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final String remoteAddr = NetworkAddressVerifier.IPV6_UNSPECIFIED;
		final String localAddr = "170.132.0.10";
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
		when(requestMock.getLocalAddr()).thenReturn(localAddr);
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals(localAddr, result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(remoteAddr));
		verify(networkAddressVerifier, times(1)).verify(eq(localAddr));
		verify(networkAddressPreProcessor, times(1)).normalize(eq(localAddr));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_RetriveFromConnector_6() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final String remoteAddr = " ";
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
		when(requestMock.getHeaders(anyString())).thenReturn(Collections.emptyEnumeration());
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertFalse(result.isSkipped());
		assertFalse(result.isDetectionSuccess());
		assertTrue(Utilities.isEmpty(result.getDetectedAddress()));
		assertEquals("Network address detection was unsuccessful.", result.getDetectionMessage());
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(remoteAddr));
		verify(networkAddressVerifier, never()).verify(anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testDetect_RetriveFromConnector_7() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final String remoteAddr = "170.132.0.10";
		final Set<String> proxyList = (HashSet<String>) ReflectionTestUtils.getField(networkAddressDetector, "filterProxyAddressSet");
		proxyList.add(remoteAddr);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		when(requestMock.getRemoteAddr()).thenReturn(remoteAddr);
		when(requestMock.getHeaders(anyString())).thenReturn(Collections.emptyEnumeration());
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		
		assertFalse(result.isSkipped());
		assertFalse(result.isDetectionSuccess());
		assertTrue(Utilities.isEmpty(result.getDetectedAddress()));
		assertEquals("Network address detection was unsuccessful.", result.getDetectionMessage());
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq(remoteAddr));
		verify(networkAddressVerifier, never()).verify(anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderForwarded_1() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "for=170.132.0.10";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("170.132.0.10", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("170.132.0.10"));
		verify(networkAddressVerifier, times(1)).verify(eq("170.132.0.10"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderForwarded_2() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "For=\"[2001:db8:cafe::17]:4711\"";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("2001:0db8:cafe:0000:0000:0000:0000:0017", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("2001:db8:cafe::17"));
		verify(networkAddressVerifier, times(1)).verify(eq("2001:0db8:cafe:0000:0000:0000:0000:0017"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderForwarded_3() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "for=192.0.2.60;proto=http;by=203.0.113.43";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("192.0.2.60", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("192.0.2.60"));
		verify(networkAddressVerifier, times(1)).verify(eq("192.0.2.60"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderForwarded_4() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "for=192.0.2.43, for=198.51.100.17";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("198.51.100.17", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("198.51.100.17"));
		verify(networkAddressVerifier, times(1)).verify(eq("198.51.100.17"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderForwarded_5() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "for=192.0.2.43, for=\"[2001:db8:cafe::17]\"";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("2001:0db8:cafe:0000:0000:0000:0000:0017", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("2001:db8:cafe::17"));
		verify(networkAddressVerifier, times(1)).verify(eq("2001:0db8:cafe:0000:0000:0000:0000:0017"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderForwarded_6() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "for=12.34.56.78;host=example.com;proto=https, for=23.45.67.89;host=other.com;proto=http";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("23.45.67.89", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("23.45.67.89"));
		verify(networkAddressVerifier, times(1)).verify(eq("23.45.67.89"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderForwarded_7() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "for=unknown";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		when(requestMock.getHeaders(eq(HEADER_X_FORWARDED_FOR))).thenReturn(Collections.emptyEnumeration());
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertFalse(result.isDetectionSuccess());
		assertTrue(Utilities.isEmpty(result.getDetectedAddress()));
		assertEquals("Network address detection was unsuccessful.", result.getDetectionMessage());
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("unknown"));
		verify(networkAddressVerifier, never()).verify(anyString());
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testDetect_DetectByHeaderForwarded_8() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "for=170.132.0.10, for=23.45.67.89";
		final Set<String> proxyList = (HashSet<String>) ReflectionTestUtils.getField(networkAddressDetector, "filterProxyAddressSet");
		proxyList.add("23.45.67.89");
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.enumeration(List.of(headerValue)));
		when(requestMock.getHeaders(eq(HEADER_X_FORWARDED_FOR))).thenReturn(Collections.emptyEnumeration());
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("170.132.0.10", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("23.45.67.89"));
		verify(networkAddressPreProcessor, times(1)).normalize(eq("170.132.0.10"));
		verify(networkAddressVerifier, never()).verify(eq("23.45.67.89"));
		verify(networkAddressVerifier, times(1)).verify(eq("170.132.0.10"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testDetect_DetectByHeaderXForwardedFor_1() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "170.132.0.10, 23.45.67.89";
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.emptyEnumeration());
		when(requestMock.getHeaders(eq(HEADER_X_FORWARDED_FOR))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertTrue(result.isDetectionSuccess());
		assertEquals("23.45.67.89", result.getDetectedAddress());
		assertTrue(Utilities.isEmpty(result.getDetectionMessage()));
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("23.45.67.89"));
		verify(networkAddressPreProcessor, never()).normalize(eq("170.132.0.10"));
		verify(networkAddressVerifier, times(1)).verify(eq("23.45.67.89"));
		verify(networkAddressVerifier, never()).verify(eq("170.132.0.10"));
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	@Test
	public void testDetect_DetectByHeaderXForwardedFor_2() {
		ReflectionTestUtils.setField(networkAddressDetector, "useDetector", true);
		final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
		final String headerValue = "170.132.0.10, 23.45.67.89";
		final Set<String> proxyList = (HashSet<String>) ReflectionTestUtils.getField(networkAddressDetector, "filterProxyAddressSet");
		proxyList.add("23.45.67.89");
		proxyList.add("170.132.0.10");
		when(requestMock.getHeaders(eq(HEADER_FORWARDED))).thenReturn(Collections.emptyEnumeration());
		when(requestMock.getHeaders(eq(HEADER_X_FORWARDED_FOR))).thenReturn(Collections.enumeration(List.of(headerValue)));
		
		final AddressDetectionResult result = networkAddressDetector.detect(requestMock);
		assertFalse(result.isSkipped());
		assertFalse(result.isDetectionSuccess());
		assertTrue(Utilities.isEmpty(result.getDetectedAddress()));
		assertEquals("Network address detection was unsuccessful.", result.getDetectionMessage());
		
		verify(networkAddressPreProcessor, times(1)).normalize(eq("23.45.67.89"));
		verify(networkAddressPreProcessor, times(1)).normalize(eq("170.132.0.10"));
		verify(networkAddressVerifier, never()).verify(anyString());
	}
}

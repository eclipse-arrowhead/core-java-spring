package eu.arrowhead.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.arrowhead.common.dto.RelayType;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;

public class Utilities {
	
	//=================================================================================================
	// members
	
	private static final int SERVICE_CN_NAME_LENGTH = 5;
	@SuppressWarnings("unused")
	private static final int CLOUD_CN_NAME_LENGTH = 4;
	@SuppressWarnings("unused")
	private static final int AH_MASTER_CN_NAME_LENGTH = 2;
	
	private static final String AH_MASTER_SUFFIX = "eu";
	private static final String AH_MASTER_NAME = "arrowhead";
	
	private static final String KEY_FACTORY_ALGORHITM_NAME = "RSA";
	private static final KeyFactory keyFactory;
	private static final Pattern PEM_PATTERN = Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*");
	
	private static final Logger logger = LogManager.getLogger(Utilities.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String dateTimePattern = "yyyy-MM-dd HH:mm:ss";
	static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
	
	static {
	    mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		try {
			keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORHITM_NAME);
		} catch (final NoSuchAlgorithmException ex) {
			logger.fatal("KeyFactory.getInstance(String) throws NoSuchAlgorithmException, code needs to be changed!");
			throw new ServiceConfigurationError("KeyFactory.getInstance(String) throws NoSuchAlgorithmException, code needs to be changed!", ex);
		}
	}
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static boolean isEmpty(final String str) {
		return str == null || str.isBlank();
	}
	
	//-------------------------------------------------------------------------------------------------
	@Nullable
	public static String stripEndSlash(final String uri) {
	    if (uri != null && uri.endsWith("/")) {
	    	return uri.substring(0, uri.length() - 1);
	    }
	    
	    return uri;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static String convertZonedDateTimeToUTCString(final ZonedDateTime time) {
		if (time == null) {
			return null;
		}
		
		final LocalDateTime localDateTime = LocalDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC);
		return dateTimeFormatter.format(localDateTime);
	}
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
	public static ZonedDateTime parseUTCStringToLocalZonedDateTime(final String timeStr) throws DateTimeParseException {
		if (isEmpty(timeStr)) {
			return null;
		}
		
		final TemporalAccessor tempAcc = dateTimeFormatter.parse(timeStr);
		final ZonedDateTime parsedDateTime = ZonedDateTime.of(tempAcc.get(ChronoField.YEAR),
															  tempAcc.get(ChronoField.MONTH_OF_YEAR),
															  tempAcc.get(ChronoField.DAY_OF_MONTH),
															  tempAcc.get(ChronoField.HOUR_OF_DAY),
															  tempAcc.get(ChronoField.MINUTE_OF_HOUR),
															  tempAcc.get(ChronoField.SECOND_OF_MINUTE),
															  0,
															  ZoneOffset.UTC);
														
		final ZoneOffset offset = OffsetDateTime.now().getOffset();
		return ZonedDateTime.ofInstant(parsedDateTime.toInstant(), offset);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static Direction calculateDirection(final String direction, final String origin) {
		logger.debug("calculateDirection started ...");
		final String directionStr = direction != null ? direction.toUpperCase().trim() : "";
		Direction validatedDirection;
		switch (directionStr) {
			case CommonConstants.SORT_ORDER_ASCENDING:
				validatedDirection = Direction.ASC;
				break;
			case CommonConstants.SORT_ORDER_DESCENDING:
				validatedDirection = Direction.DESC;
				break;
			default:
				throw new BadPayloadException("Invalid sort direction flag", org.apache.http.HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		return validatedDirection;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ValidatedPageParams validatePageParameters(final Integer page, final Integer size, final String direction, final String origin) {
		int validatedPage;
		int validatedSize;

		if (page == null && size == null) {
			validatedPage = -1;
			validatedSize = -1;
		} else {
			if (page == null || size == null) {
				throw new BadPayloadException("Defined page or size could not be with undefined size or page.", org.apache.http.HttpStatus.SC_BAD_REQUEST, origin);
			} else {
				validatedPage = page;
				validatedSize = size;
			}
		}

		final Direction validatedDirection = calculateDirection(direction, origin);
		
		return new ValidatedPageParams(validatedPage, validatedSize, validatedDirection);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Nullable
	public static String toPrettyJson(final String jsonString) {
		try {
			if (jsonString != null) {
				final String jsonString_ = jsonString.trim();
				if (jsonString_.startsWith("{")) {
					final Object tempObj = mapper.readValue(jsonString_, Object.class);
					return mapper.writeValueAsString(tempObj);
				} else {
					final Object[] tempObj = mapper.readValue(jsonString_, Object[].class);
					return mapper.writeValueAsString(tempObj);
				}
			}
	    } catch (final IOException ex) {
	    	// it seems it is not a JSON string, so we just return untouched
	    }
		
	    return jsonString;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Nullable
	public static String toJson(final Object object) {
		if (object == null) {
			return null;
		}
		
		try {
			return mapper.writeValueAsString(object);
		} catch (final JsonProcessingException ex) {
			throw new ArrowheadException("The specified object cannot be converted to text.", ex);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Nullable
	public static <T> T fromJson(final String json, final Class<T> parsedClass) {
		if (json == null || parsedClass == null) {
			return null;
		}
		
	    try {
	    	return mapper.readValue(json, parsedClass);
	    } catch (final IOException ex) {
	      throw new ArrowheadException("The specified string cannot be converted to a(n) " + parsedClass.getSimpleName() + " object.", ex);
	    }
	}
	
	//-------------------------------------------------------------------------------------------------
	@Nullable
	public static Map<String,String> text2Map(final String text) {
		if (text == null) {
			return null;
		}
		
		final Map<String,String> result = new HashMap<>();
		if (!isEmpty(text.trim())) {
			final String[] parts = text.split(",");
			for (final String part : parts) {
				final String[] pair = part.split("=");
				result.put(URLDecoder.decode(pair[0].trim(), StandardCharsets.UTF_8), pair.length == 1 ? "" : URLDecoder.decode(pair[1].trim(), StandardCharsets.UTF_8));
			}
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------------------------------
	@Nullable
	public static String map2Text(final Map<String,String> map) {
		if (map == null) {
			return null;
		}
		
		final StringBuilder sb = new StringBuilder();
		for (final Entry<String,String> entry : map.entrySet()) {
			final String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
			final String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
			sb.append(key).append("=").append(value).append(", ");
		}
		
		return map.isEmpty() ? "" : sb.substring(0, sb.length() - 2);
	}
	
	//-------------------------------------------------------------------------------------------------
	/**
	 * 
	 * @param scheme default: http
	 * @param host default: 127.0.0.1
	 * @param port default: 80
	 * @param queryParams default: null
	 * @param path default: null
	 * @param pathSegments default: null
	 */
	public static UriComponents createURI(final String scheme, final String host, final int port, final MultiValueMap<String, String> queryParams, final String path, final String... pathSegments) {
		final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		builder.scheme(scheme == null ? CommonConstants.HTTP : scheme)
			   .host(host == null ? CommonConstants.LOCALHOST : host)
			   .port(port <= 0 ? CommonConstants.HTTP_PORT : port);
		
		if (queryParams != null) {
			builder.queryParams(queryParams);
		}
		
		if (pathSegments != null && pathSegments.length > 0) {
			builder.pathSegment(pathSegments);
		}
		
		if (!Utilities.isEmpty(path)) {
			builder.path(path);
		}
		
		return builder.build();
	}
	
	//-------------------------------------------------------------------------------------------------
	public static UriComponents createURI(final String scheme, final String host, final int port, final String path) {
		return createURI(scheme, host, port, null, path);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static HttpStatus calculateHttpStatusFromArrowheadException(final ArrowheadException ex) {
		Assert.notNull(ex, "Exception is null.");
		
		HttpStatus status = HttpStatus.resolve(ex.getErrorCode());
	    if (status == null) {
	    	switch (ex.getExceptionType()) {
	    	case AUTH:
	    		status = HttpStatus.UNAUTHORIZED;
			    break;
	        case BAD_PAYLOAD:
	        case INVALID_PARAMETER:
	        	status = HttpStatus.BAD_REQUEST;
	          	break;
	        case DATA_NOT_FOUND:
	        	status = HttpStatus.NOT_FOUND;
	        	break;
	        case UNAVAILABLE:
	        case TIMEOUT:
	        	status = HttpStatus.GATEWAY_TIMEOUT;
	        	break;
	        default:
	    		status = HttpStatus.INTERNAL_SERVER_ERROR;
	    	}
	    }
	    
		return status;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static RelayType convertStringToRelayType(final String str) {
		if (isEmpty(str)) {
			return RelayType.GENERAL_RELAY;
		}
				
		try {
			return RelayType.valueOf(str.toUpperCase().trim());			
		} catch (final IllegalArgumentException ex) {
			return null;
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	@Nullable
	public static String getCertCNFromSubject(final String subjectName) {
		if (subjectName == null) {
			return null;
		}
		
	    try {
	    	// Subject is in LDAP format, we can use the LdapName object for parsing
	    	final LdapName ldapname = new LdapName(subjectName);
	    	for (final Rdn rdn : ldapname.getRdns()) {
	    		// Find the data after the CN field
	    		if (CommonConstants.COMMON_NAME_FIELD_NAME.equalsIgnoreCase(rdn.getType())) {
	    			return (String) rdn.getValue();
	    		}
	    	}
	    } catch (final InvalidNameException ex) {
	    	logger.warn("InvalidNameException in getCertCNFromSubject: {}", ex.getMessage());
	    	logger.debug("Exception", ex);
	    }

	    return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static String getCloudCommonName(final String cloudOperator, final String cloudName) {
		Assert.isTrue(!isEmpty(cloudOperator), "Cloud operator is null or blank.");
		Assert.isTrue(!isEmpty(cloudName), "Cloud name is null or blank.");
		
		return (cloudName.trim() + "." + cloudOperator.trim() + ".arrowhead.eu").toLowerCase();  
	}
	
	//-------------------------------------------------------------------------------------------------
	public static X509Certificate getFirstCertFromKeyStore(final KeyStore keystore) {
		Assert.notNull(keystore, "Key store is not defined.");
		
        try {
            final Enumeration<String> enumeration = keystore.aliases();
            final String alias = enumeration.nextElement();
            return (X509Certificate) keystore.getCertificate(alias);
        } catch (final KeyStoreException | NoSuchElementException ex) {
        	logger.error("Getting the first cert from key store failed...", ex);
            throw new ServiceConfigurationError("Getting the first cert from keystore failed...", ex);
        }
    }
	
	//-------------------------------------------------------------------------------------------------
	public static PrivateKey getPrivateKey(final KeyStore keystore, final String keyPass) {
		Assert.notNull(keystore, "Key store is not defined.");
		Assert.notNull(keyPass, "Password is not defined.");
		
	    PrivateKey privateKey = null;
	    String element;
	    try {
	    	final Enumeration<String> enumeration = keystore.aliases();
	    	while (enumeration.hasMoreElements()) {
	    		element = enumeration.nextElement();
	    		privateKey = (PrivateKey) keystore.getKey(element, keyPass.toCharArray());
	    		if (privateKey != null) {
	    			break;
	    		}
	    	}
	    } catch (final KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException ex) {
	    	logger.error("Getting the private key from key store failed...", ex);
	    	throw new ServiceConfigurationError("Getting the private key from key store failed...", ex);
		}

	    if (privateKey == null) {
	    	throw new ServiceConfigurationError("Getting the private key failed, key store aliases do not identify a key.");
	    }
	    
	    return privateKey;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static PublicKey getPublicKeyFromBase64EncodedString(final String encodedKey) {
		Assert.isTrue(!isEmpty(encodedKey), "Encoded key is null or blank");
		
		final byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
		return generatePublicKeyFromByteArray(keyBytes);
	}

	//-------------------------------------------------------------------------------------------------
	public static PublicKey getPublicKeyFromPEMFile(final InputStream is) {
		Assert.notNull(is, "Input stream is null");
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final byte[] buf = new byte[1024];
			for (int read = 0; read != -1; read = is.read(buf)) {
				baos.write(buf, 0, read);
			}
			
		    final String pem = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
		    baos.close();
		    final String encoded = PEM_PATTERN.matcher(pem).replaceFirst("$1");
		    final byte[] keyBytes = Base64.getMimeDecoder().decode(encoded);
		    
		    return generatePublicKeyFromByteArray(keyBytes);
		} catch (final IOException ex) {
		      throw new ArrowheadException("IOException occurred during PEM file loading from input stream.", ex);
		} 
	}
	
	//-------------------------------------------------------------------------------------------------
	public static boolean isKeyStoreCNArrowheadValid(final String commonName) {
		if (isEmpty(commonName)) {
			return false;
		}
		
        final String[] cnFields = commonName.split("\\.", 0);
        return cnFields.length == SERVICE_CN_NAME_LENGTH && cnFields[cnFields.length - 1].equals(AH_MASTER_SUFFIX) && cnFields[cnFields.length - 2].equals(AH_MASTER_NAME);
    }
	
	//-------------------------------------------------------------------------------------------------
	public static boolean isKeyStoreCNArrowheadValid(final String clientCN, final String cloudCN) {
		if (isEmpty(clientCN) || isEmpty(cloudCN)) {
			return false;
		}
		
	    final String[] clientFields = clientCN.split("\\.", 2); // valid clientFields contains clientServiceName, cloudName.operator.arrowhead.eu
	    
	    return clientFields.length >= 2 && cloudCN.equalsIgnoreCase(clientFields[1]);
	}
	
	//-------------------------------------------------------------------------------------------------
	public static String getDatetimePattern() { return dateTimePattern; }
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private Utilities() {
		throw new UnsupportedOperationException();
	}
	
	//-------------------------------------------------------------------------------------------------
	private static PublicKey generatePublicKeyFromByteArray(final byte[] keyBytes) {
		try {
			return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
		} catch (final InvalidKeySpecException ex) {
		      logger.error("getPublicKey: X509 keyspec could not be created from the decoded bytes.");
		      throw new AuthException("Public key decoding failed due wrong input key", ex);
		}
	}
	
	//=================================================================================================
	// nested classed
	
	//-------------------------------------------------------------------------------------------------
	public static class ValidatedPageParams {
		
		//=================================================================================================
		// members
		
		private final int validatedPage;
		private final int validatedSize;
		private final Direction validatedDirecion;
		
		//=================================================================================================
		// methods
		
		//-------------------------------------------------------------------------------------------------
		public ValidatedPageParams(final int validatedPage, final int validatedSize, final Direction validatedDirection) {
			this.validatedPage = validatedPage;
			this.validatedSize = validatedSize;
			this.validatedDirecion = validatedDirection;
		}

		//-------------------------------------------------------------------------------------------------
		public int getValidatedPage() { return validatedPage; }
		public int getValidatedSize() { return validatedSize; }
		public Direction getValidatedDirecion() { return validatedDirecion; } 
	}
}
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
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import eu.arrowhead.common.dto.internal.ChoreographerSessionStepStatus;
import eu.arrowhead.common.dto.internal.QoSMeasurementAttribute;
import eu.arrowhead.common.dto.internal.RelayType;
import eu.arrowhead.common.dto.shared.ChoreographerSessionStatus;
import eu.arrowhead.common.dto.shared.ErrorMessageDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.exception.TimeoutException;
import eu.arrowhead.common.exception.UnavailableServerException;

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

	private static final String MAC_ADDRESS_PATTERN_STRING = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
	public static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile(MAC_ADDRESS_PATTERN_STRING);

	private static final Logger logger = LogManager.getLogger(Utilities.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;

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
	public static boolean isEmpty(final Map<?,?> map) {
		return map == null || map.isEmpty();
	}
	
	//-------------------------------------------------------------------------------------------------
	public static boolean isEmpty(final Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

    //-------------------------------------------------------------------------------------------------
    public static boolean notEmpty(final String str) {
        return StringUtils.hasText(str);
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

		return dateTimeFormatter.format(time.toInstant());
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
	public static ZonedDateTime parseUTCStringToLocalZonedDateTime(final String timeStr) throws DateTimeParseException {
		if (isEmpty(timeStr)) {
			return null;
		}

		final TemporalAccessor tempAcc = dateTimeFormatter.parse(timeStr);

		return ZonedDateTime.ofInstant(Instant.from(tempAcc), ZoneId.systemDefault());
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("squid:RedundantThrowsDeclarationCheck")
	public static ZonedDateTime parseDBLocalStringToUTCZonedDateTime(final String timeStr) throws DateTimeParseException {
		if (isEmpty(timeStr)) {
			return null;
		}
		
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		final TemporalAccessor tempAcc = formatter.parse(timeStr);
		final ZonedDateTime parsedDateTime = ZonedDateTime.of(tempAcc.get(ChronoField.YEAR),
															  tempAcc.get(ChronoField.MONTH_OF_YEAR),
															  tempAcc.get(ChronoField.DAY_OF_MONTH),
															  tempAcc.get(ChronoField.HOUR_OF_DAY),
															  tempAcc.get(ChronoField.MINUTE_OF_HOUR),
															  tempAcc.get(ChronoField.SECOND_OF_MINUTE),
															  0,
															  ZoneId.systemDefault());
		
		return ZonedDateTime.ofInstant(parsedDateTime.toInstant(), ZoneOffset.UTC);
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
	 * @param scheme default: http
	 * @param host default: 127.0.0.1
	 * @param port default: 80
	 * @param queryParams default: null
	 * @param path default: null
	 * @param pathSegments default: null
	 */
	public static UriComponents createURI(final String scheme, final String host, final int port, final MultiValueMap<String, String> queryParams, final String path, final String... pathSegments) {
		final UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		builder.scheme(Utilities.isEmpty(scheme) ? CommonConstants.HTTP : scheme.trim())
			   .host(Utilities.isEmpty(host) ? CommonConstants.LOCALHOST : host.trim())
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
		return createURI(scheme, host, port, null, path, (String[]) null);
	}

	//-------------------------------------------------------------------------------------------------
	public static UriComponents createURI(final String scheme, final String host, final int port, final String path, final String... queryParams) {
		if (queryParams == null || queryParams.length == 0) {
			return createURI(scheme, host, port, path);
		}
		if (queryParams.length % 2 != 0) {
			throw new InvalidParameterException("queryParams variable arguments conatins a key without value");
		}

		final LinkedMultiValueMap<String, String> query = new LinkedMultiValueMap<>();

		int count = 1;
		String key = "";
		for (final String vararg : queryParams) {
			if (count % 2 != 0) {
				query.putIfAbsent(vararg, new ArrayList<>());
				key = vararg;
			} else {
				query.get(key).add(vararg);
			}
			count++;
		}

		return createURI(scheme, host, port, query, path);
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
	public static QoSMeasurementAttribute convertStringToQoSMeasurementAttribute(final String str) {
		if (isEmpty(str)) {
			throw new InvalidParameterException("Attribute string is null or empty");
		}
				
		try {
			return QoSMeasurementAttribute.valueOf(str.toUpperCase().trim());			
		} catch (final IllegalArgumentException ex) {
			throw new InvalidParameterException("Unkown attribute string: " + str);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ChoreographerSessionStatus convertStringToChoreographerSessionStatus(final String str) {
		if (isEmpty(str)) {
			throw new InvalidParameterException("Status string is null or empty");
		}
				
		try {
			return ChoreographerSessionStatus.valueOf(str.toUpperCase().trim());			
		} catch (final IllegalArgumentException ex) {
			throw new InvalidParameterException("Unkown status string: " + str);
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ChoreographerSessionStepStatus convertStringToChoreographerSessionStepStatus(final String str) {
		if (isEmpty(str)) {
			throw new InvalidParameterException("Status string is null or empty");
		}
				
		try {
			return ChoreographerSessionStepStatus.valueOf(str.toUpperCase().trim());			
		} catch (final IllegalArgumentException ex) {
			throw new InvalidParameterException("Unkown status string: " + str);
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
	public static X509Certificate getSystemCertFromKeyStore(final KeyStore keystore) {
		Assert.notNull(keystore, "Key store is not defined.");

        try {
            // the first certificate is not always the end certificate. java does not guarantee the order
			final Enumeration<String> enumeration = keystore.aliases();
            while (enumeration.hasMoreElements()) {
                final Certificate[] chain = keystore.getCertificateChain(enumeration.nextElement());

                if (Objects.nonNull(chain) && chain.length >= 3) {
                    return (X509Certificate) chain[0];
                }
            }
            throw new ServiceConfigurationError("Getting the first cert from keystore failed...");
        } catch (final KeyStoreException | NoSuchElementException ex) {
        	logger.error("Getting the first cert from key store failed...", ex);
            throw new ServiceConfigurationError("Getting the first cert from keystore failed...", ex);
        }
    }

	//-------------------------------------------------------------------------------------------------
	public static X509Certificate getCloudCertFromKeyStore(final KeyStore keystore) {
		Assert.notNull(keystore, "Key store is not defined.");

		try {
		    // debian installation with new certificates have a different alias
            // i.e. the format is {cloudname}.{cloudoperator}." + AH_MASTER_NAME + "." + AH_MASTER_SUFFIX
			final Enumeration<String> enumeration = keystore.aliases();
			while (enumeration.hasMoreElements()) {
				final String alias = enumeration.nextElement();
                final X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);

                if (isCloudCertificate(certificate)) {
                    return certificate;
                }
			}

			final String errorMsg = "Getting the cloud cert from keystore failed. " +
					"Cannot find alias in the following format: {cloudname}.{cloudoperator}." + AH_MASTER_NAME + "." + AH_MASTER_SUFFIX;
			logger.error(errorMsg);
			throw new ServiceConfigurationError(errorMsg);
		} catch (final KeyStoreException | NoSuchElementException ex) {
			logger.error("Getting the cloud cert from keystore failed...", ex);
			throw new ServiceConfigurationError("Getting the cloud cert from keystore failed...", ex);
		}
	}

	//-------------------------------------------------------------------------------------------------
	public static X509Certificate getRootCertFromKeyStore(final KeyStore keystore) {
		Assert.notNull(keystore, "Key store is not defined.");

		try {
            // debian installation with new certificates have a different alias
			final Enumeration<String> enumeration = keystore.aliases();
			while (enumeration.hasMoreElements()) {
				final String alias = enumeration.nextElement();
                final X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
                final String commonName = getCertCNFromSubject(certificate.getSubjectDN().getName());
                Assert.notNull(commonName, "Certificate without commonName is not allowed");
                final String[] cnParts = commonName.split("\\.");
				if (cnParts.length == 2 && cnParts[0].equals(AH_MASTER_NAME) && cnParts[1].equals(AH_MASTER_SUFFIX)) {
					return (X509Certificate) keystore.getCertificate(alias);
                }
			}

			final String errorMsg = "Getting the root cert from keystore failed. " +
					"Cannot find CN in the following format: " + AH_MASTER_NAME + "." + AH_MASTER_SUFFIX;
			logger.error(errorMsg);
			throw new ServiceConfigurationError(errorMsg);
		} catch (final KeyStoreException | NoSuchElementException ex) {
			logger.error("Getting the root cert from keystore failed...", ex);
			throw new ServiceConfigurationError("Getting the root cert from keystore failed...", ex);
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
				// the first certificate is not always the end certificate. java does not guarantee the order
				final Certificate[] chain = keystore.getCertificateChain(element);
				if(Objects.isNull(chain) || chain.length < 3) {
					continue;
				}

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
    public static PrivateKey getCloudPrivateKey(final KeyStore keystore, final String keyPass) {
        Assert.notNull(keystore, "Key store is not defined.");
        Assert.notNull(keyPass, "Password is not defined.");

        try {
			final Enumeration<String> storedAliases = keystore.aliases();
			while (storedAliases.hasMoreElements()) {
				final String alias = storedAliases.nextElement();
				final X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);
				if (isCloudCertificate(certificate)) {
					final PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, keyPass.toCharArray());
					if (privateKey != null) {
						logger.debug("Found cloud private key with alias: " + alias);
						return privateKey;
					}
				}
            }
        } catch (final KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException ex) {
            logger.error("Getting the private key from key store failed...", ex);
            throw new ServiceConfigurationError("Getting the private key from key store failed...", ex);
        }

		logger.error("Getting the private key failed, key store aliases do not identify a key.");
		throw new ServiceConfigurationError("Getting the private key failed, key store aliases do not identify a key.");
	}

	//-------------------------------------------------------------------------------------------------
	public static PrivateKey getCloudPrivateKey(final KeyStore keystore, final String cloudCommonName, final String keyPass) {
		Assert.notNull(keystore, "Key store is not defined.");
		Assert.notNull(cloudCommonName, "CloudCommonName is not defined.");
		Assert.notNull(keyPass, "Password is not defined.");

		try {
			// Try to find private key with common name as the alias
			PrivateKey privateKey = (PrivateKey) keystore.getKey(cloudCommonName, keyPass.toCharArray());
			if (privateKey != null) {
				return privateKey;
			}

			// The cloud common name is not the alias in debian installation with own certificates
			final String[] cnParts = cloudCommonName.split("\\.");
			if (cnParts.length == 4 && cnParts[2].equals(AH_MASTER_NAME) && cnParts[3].equals(AH_MASTER_SUFFIX)) {
				final String cloudName = cnParts[0];
				privateKey = (PrivateKey) keystore.getKey(cloudName, keyPass.toCharArray());
				if (privateKey != null) {
					return privateKey;
				}
			}

			// Try to find the private key based on the common name
			logger.warn("Cannot find cloud private key based on alias. Trying to find based on common name...");
			return getCloudPrivateKey(keystore, keyPass);

		} catch (final KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException ex) {
			logger.error("Getting the cloud private key from key store failed...", ex);
			throw new ServiceConfigurationError("Getting the cloud private key from key store failed...", ex);
		}
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
	private static boolean isCloudCommonName(final String commonName) {
		Assert.notNull(commonName, "Empty commonName is not allowed");
		final String[] cnParts = commonName.split("\\.");
		return (cnParts.length == 4 && cnParts[2].equals(AH_MASTER_NAME) && cnParts[3].equals(AH_MASTER_SUFFIX));
	}

	//-------------------------------------------------------------------------------------------------
	private static boolean isCloudCertificate(final X509Certificate certificate) {
		final String commonName = getCertCNFromSubject(certificate.getSubjectDN().getName());
		Assert.notNull(commonName, "Certificate without commonName is not allowed");
		return isCloudCommonName(commonName);
	}

	//-------------------------------------------------------------------------------------------------
	public static void createExceptionFromErrorMessageDTO(final ErrorMessageDTO dto) {
		Assert.notNull(dto, "Error message object is null.");
		Assert.notNull(dto.getExceptionType(), "Exception type is null.");
		
		switch (dto.getExceptionType()) {
	    case ARROWHEAD:
	    	throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case AUTH:
	        throw new AuthException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case BAD_PAYLOAD:
	        throw new BadPayloadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    case INVALID_PARAMETER:
	    	throw new InvalidParameterException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case DATA_NOT_FOUND:
            throw new DataNotFoundException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case GENERIC:
            throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case TIMEOUT:
        	throw new TimeoutException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        case UNAVAILABLE:
	        throw new UnavailableServerException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
	    default:
	    	logger.error("Unknown exception type: {}", dto.getExceptionType());
	    	throw new ArrowheadException(dto.getErrorMessage(), dto.getErrorCode(), dto.getOrigin());
        }
    }

    //-------------------------------------------------------------------------------------------------
    public static boolean isValidMacAddress(final String macAddress) {
        Assert.notNull(macAddress, "MAC address must not be null");
        final Matcher matcher = MAC_ADDRESS_PATTERN.matcher(macAddress);
        return matcher.matches();
    }

    //-------------------------------------------------------------------------------------------------
    public static String lowerCaseTrim(final String text) {
        if (isEmpty(text)) {
            return text;
        }

        return text.toLowerCase().trim();
    }

    //-------------------------------------------------------------------------------------------------
    public static String firstNotNullIfExists(final String first, final String second) {
        return Utilities.notEmpty(first) ? first : second;
    }

    //-------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static <T> T firstNotNullIfExists(final T first, final T second) {
        if (first instanceof String && second instanceof String) {
            return (T) firstNotNullIfExists((String) first, (String) second);
        }
        return Objects.nonNull(first) ? first : second;
    }

    //-------------------------------------------------------------------------------------------------
    public static String firstNotNullIfExists(final String... args) {
        for (final String candidate : args) {
            if (Utilities.notEmpty(candidate)) {
                return candidate;
            }
        }

        return null;
    }

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
}
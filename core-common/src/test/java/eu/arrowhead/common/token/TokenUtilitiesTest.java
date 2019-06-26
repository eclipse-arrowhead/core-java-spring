package eu.arrowhead.common.token;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.InvalidParameterException;
import eu.arrowhead.common.token.TokenUtilities.TokenInfo;

@RunWith(SpringRunner.class)
public class TokenUtilitiesTest {
	
	//=================================================================================================
	// members
	
	private PublicKey authPublicKey;
	private PrivateKey privateKey;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	@Before
	public void setUp() throws Exception {
		final InputStream publicKeyInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/authorization.pub");
		authPublicKey = Utilities.getPublicKeyFromPEMFile(publicKeyInputStream);
		
		final KeyStore keystore = KeyStore.getInstance("PKCS12");
		keystore.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("certificates/provider.p12"), "123456".toCharArray());
		privateKey = Utilities.getPrivateKey(keystore, "123456");
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testValidateTokenAndExtractTokenInfoPublicKeyNull() {
		TokenUtilities.validateTokenAndExtractTokenInfo(null, null, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = IllegalArgumentException.class)
	public void testValidateTokenAndExtractTokenInfoPrivateKeyNull() {
		TokenUtilities.validateTokenAndExtractTokenInfo(null, authPublicKey, null);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateTokenAndExtractTokenInfoTokenNull() {
		TokenUtilities.validateTokenAndExtractTokenInfo(null, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = InvalidParameterException.class)
	public void testValidateTokenAndExtractTokenInfoTokenEmpty() {
		TokenUtilities.validateTokenAndExtractTokenInfo("\r\n", authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenProcessingFailed() {
		TokenUtilities.validateTokenAndExtractTokenInfo("abc", authPublicKey, getInvalidPrivateKey());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMissingConsumerInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.RBgxB4vvXF0DkbjRiPIhAP9mZsz0zyGN0uIDPsZxFaI9HT8ppFHLrazg_sVzHWsQ5insvWY5gg3Zk6WgBST-GJ1GVtpyniQqbk0-Uh-td_NQ7dXm6XnQqDvRn3kq4TOViOpI8WvlyvVG3XsDq0-KegIvrjjb0KZMYedAgaShdHas2OKhMtXU0fk_uNoPyGdgI07PWi-kscvO-dp0Mk2VM39Dx4fnM7wBo3E1SisvUuEeE2-3Q99OcHju9GvCjfo7UboMBsTcVq6bcY9F3DwEaea6VUGdMBAoAFCZSbPjPGOjujXCm_m86wG2WoqavM-UfWIvyzfHXVV_GE5THCqpKA.Naf-m0ubeJqpFUlR50DtTg.L-Z7JhVAu-2TlrMHE6IdJFMjT0dNtTGQEjqrfVQRWweVetXlNWURmgMZrnBMIJxWTrQnkjgBZkrRMEIaL7bEcaxpNHFOxV-4Bhf8FoJjZUoP-I4TlSLDiwsDmNaFToL_XbTEA8wtt7s76gE6phLmTqsZZHq3jfTxaafb9CYKEDZJYSfnaD5RVGMlT-GOFmhMPStb9dpNTGpJzswqfZlGWmTKBuNDzR08eLXCnejWpeVsZUUhB9WgulrLd-Q_9NTAJOZatM5sBw7F3m0kWzMLQkhF6zMxGuDum249N0A43u8-BwIv773LLGmCNNxEgf1VsMvhgbv4LmqM_IJv0nLgnPsQ44NuU_WP7HlW-x3SQE1StNxUJ6xrq6KGh4o4adqVx1JxlJNPJA_VGiTX_MmG-hUvLnK1fc2cuvm8821epI5rQ1TtdZB2egpJ2778f4hKJze-GO2IzeM5ck2yeJr6pIMKfYw1LZ0c39FWMH4W1aKCVmp9yvBl-_Fk4COMHKliVXRoYCawcleXrxYgIq6Nvagwxef2uALw-8g7yYdAvwrVid3j4vATWeEknqtIwFBQIXQk3JKDjpPIvIJSsg-sRCREWDXKpD7PYx_fMHXoxo1e6KpeV2FLTEGfeZDlbeSXsiiC47Fd_9uByAZoVbcvBq6vaM-opY6QMweA6gAIN9oEKcC2Lh5e6SN0urIEbpHNIgorj-5XtcIdOobFHsaFmw.rEAP1q3Oo-qYFKVtT_chtwMGgB3PIqnRpgZCBg4u5lw";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMalformedConsumerInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.H7keycRsxxMkLgh6b8hPUd4pOxAa1im9l5Dc95xX4FP1o05uho9BHM0YJqZ4A2G19u3PtJAtWD0T-8zBA29jZaORHVJypuEekxlCsO_ylQqcfQVDCPUsfe4QEqA41XhQq9XAJTsp7gOjgI7Dm4qnK6Pm97vzejQHUelD74Xe9HHYnUohfOE07W4JfIurFORtrvlqbG2V0lPk4oRIR3Yo19pIavRkQ9l_QN7QF0B-lxP39sqheE3mxJg2dXOItP4Xm4B0USYOrYZXkPk8mosncZFxVHd0yBjWsqoF0zXeDfae_fg6uHaClaRXp9s02mMw_20cpItjB_o-ldyq3qM39A.nrnxBwIHjKHp7r3c1kW6ug.bfzi5MnorazK6GUHMy6Dhqdj5hkJ0hCS5JnAAt3wB83G-iwLYOx9gVkXH9qWTthtyDsrjQcCZGskPeRKqYMxn1szSwG_T1hR6vyzfkJo5aP0OmEITgwMmWCjrpXowVDTCw8ZnG99EuprC-mmrQjIeUXegwPn3AFKFW2YYho5A3oQhIwwTFtmPLuYGdogoGv0JsTn0X2pB4tpa4d9eoRuhUzEsWR6reIwcstdaGAYkmtOKP9T3rC7yYtS7L9pKAxBjJoGPHDsWWnJJ8nnFNSH1bk7rjt0fxGczJZ8tozIN9tUkoV4RZ6gSNiv5atZ2F-DXuTFoMD-twjPS_BhvtXAEBB_GJq4KZrdiBGrOwpVgx6i1lzazgTpZIdaEJgg6D55YcQiytBI82SlsSF6POVTIjYkXyrLXgYCICHd7oT2STTXasQLTGR33D7BvxtXRjfoJDxD6ez4n5iayn8adUMhsxZLeztK59boaAcS5IinuZdPDpzayvvP63RKN3dPW4tHkg02hw9aHT2a25-rkopIeJj3HIvxxEMD-28uHPhseKqsS_wlDzgfvskVlaq3k6lw7SeulPvjxi1-uZjeq-aFWFUiK5duaC3TVFvWQEHm3E7FJECy6P9YRk_P8_j00Q2ISVKk8eYcyPoOzFPBkJzj6tUjE6843Z02uNcSP-c62mm-iywSsrZsN-D9Nr8745HmZteAyMql4QX-tIubT90wWpCx2q9CbVAqewPZs1J68aU.8mqeKdIsB1DO-1hQww14QPQEWWHw4MmFQhyodFl4SNQ";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMissingServiceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.W8EZx1BLt9u-P0WnXB-Y7MIsCqTLJAcSwZY1VsZ0C4SpPyM7OTIjV7cEP3oruc86fOz42zQlDV39cpr1vc1WcHqgJmAV8kaHFRmFrG--kHq36hEVT3WEpsBa9LdZ_CV-1WUACFI-NWMGHSr2u00ddoNc9fV5gWfFPjKdUZHyT4hVwHmmoy46kaDnA04UbUnQ5puF_d4Jkr-OJNE2HAXisZM9oA-MIncciCJtMQYgVbXKxvANTgyWRcSS1qVCwY5rYhSjzhrXKfImKyWoOglWTkCSHmeJrPOFU-1Ccx1x6t5PS2Yl0bcweZJ5BEO89NFLfFoP4D1PP3BW9XE1LMAZWA.K6u1QRiHy_awc7KVeQJl4w.pmkMb-Wn7UWuEjzU-KsaHI0BOYMKAYDqpiZ-nq1ungO33QgjgGFsPQWTInM39fzzYiA2doqkmTp-33lqQqFCMPlsuZhaNyLpbU16s0OW2kSOlJeTc_i-OFxWT9LQkklgMiy_IgNBwpI4kUfcB_JoAj8fAiMicGR2ZGg_CYP2a0MfMd7lLuaG0AKPyGsYmyH0qlDJYlu4YYUB2Exmi3HuGAzOlufbneWvEtfIe6c4_sRv91grM7BjPmBSpxYlR-JG2Vj9M9B-4bjkmpNpyrvMOXBVQCM2zfV804MDIJeyCcC0Uaxm5P9algbRuOH_XSI2O8Q72lq0hhVSVyCsrvRnMu0qDWq0z5sqkUE1zjqBydvKZjRWOeVZhjMbBZexWC79j8rgJRKRu9waC7QggAMBFkocKRwO4_BM9Y1vpCxoQeHVSh_RY1G6Bgyw3EkLo68SLC_CFTDZJKx-SiCOGRLgttXITIthQXr67Xmkm1Eq39JVU_K1QFxXBvx-SqAeGc60yhrDbb2822RSdNVEIiHr5Og-_bHZxZU9pxqwx-amiFZF-dJuJgFAY99Qxd6p3anYyE8WGf0tZY3DYC4QqZLrLLOLbxsu774mpzrsie8h7feQiUqkZj6I1udQECiN5B79b5Y3XO2zPDfzJx6Z6d6X6SAAZ5H-qqfG49uqrYwKaMobEAgrAGq-yjPT7wmVlWg4jUDcGIyG2AlN35xYERQG3DIMJlO1C_zj6v5PaWOXLBU.avGIFGVpACfOilozGlCPX_u9EgGvoHUwqdzkeVrA1Og";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMalformedServiceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.iaqHK7o4BMpNB0ibMg1ykywNN4BbM4sI2oAYnR_TXEqFCkxtt8hIeydphEWSIu0V1OG11_WdO1m-toJshbCAB-oJUlj8Hh4qwjpu5lLp95iP8JftHI2kOiKcPF-tG5PZBedj2tIn0JgcprlN4PKJO_0Gs_bxb46BrV_H_OxcpUL8rVnOko__rB-k96zRK4kdEiN4m1AuxKK2DurY8vv1AxEzBMLFHhrYEsUqxfvFDyG7fpZfCb3P-JCTadWWZ5xF7ynvcaP7QAfGj5AV6jBwhC3oS-p_PQMnmiZrN_NSACFr5ROHVxstZAbsEceFI89YP4yD2UNTabUIiXyRp-fd-Q.KuJAInOYErIiNQVkGROdOQ.5Zkd6_M530p-RXAljmADdKdxygpOKapWWzT4PnHwUWN2B1_PkDW9cBPSanHdy4NM_2tvrGm4uOZ5LslMvuzVLg3v2PjKi7fxqpJaBvjRlFsNHuPGM4Vnh8nF9IMezGO8XIAWjm6CWcYfjKk6r5Ghvqv2Hu8v8rZc7bQuRaA6wqrvVvqAEXp8Pvb876uz3IkVt_bK2XNXQCPZAlA1LgCTQDZojbhCL4KFatsChwNtI6jeEKBLW01QPUI1Rk1G0N-b5R97iY5f3V31UWQIBo9Uyeus2oyNASHXmpELYIVuZCaDJ7g6enQVtKrCVJf1pOjinCwCO2CV2hEP5cHQrGGnK6iiDgNchyVZtFJwBqHAhwKQZAUVEx2xiV8PJuZa7oDgXGJp56CqFrRIXDLLToyIUVOv1i2zTYS6gh6FoCIZqQyAOXR88zczX1C3JBHoPB_5BAZDH0DG6ChdyoS4mVKTI9ya3AZEAzB6AY6Ybzla3WU_hYZuIVfxpEvZHMiS72u3KkzO26KErKQUoFXqx6h0H8qrEZeFJOIL1me9L46SLCfD83ugYU6hmHo3VuuTKYU1hmnMZ3byTcHANiQTSnjVFWYt0Boz1pyrMrNCE5vH-0uOYsl1F2TU1AsM6OyWibK9ajWuL5xM9PAn3hdOpkz8EBh76aDnQNrrYNlLVPvtcI05L4toxtvsh4OSeUDFa308D9uJdMr-jCix_FiH65CGSz97XEECM7en7VCHQjgk6h1Zb4cxJD9AfWodOb5FFmNb._qKrQYp-DDzaVvkH-84FsAbG9QQdsd7bBZ-qntKl-4E";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMalformedExpirationTime() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.Zba3848Ab-9DTIRM76OXfL6YSIc5PJjunhSvcxZBT_R-bBGCeX5ggjn3kz2iWfp45sctjlDCagdPc31T4ssK_jbAfXBheYulU3CHgZkUhQXin8-iohd3bpM8ZxJKWMwqmKeLk6ijv_h5PIWF9-PaQZ9QoGIdk5aYKOIKOdv6GRrjj09RdJJ4BsJ1cOU5bezeokHrt0ZBjjnaa4x2NKftu_KQZgpmcy0kG4yFbBCaRRVRmPjptT7b5c8wgoq3CfRU-3qES-CqC6PF_1CoK9cnNAIs79F2ED3ytmeD0abDKVbR5ETPiu5FdoTWnuxuaaYddJZlkOKmhVwULhdW3g3fbw.Ke8q_qAlyJC6tghyIc48Vg.idxHHvWbc62AV41UjJt-u5Z_b1VT472jzMCYkCCpk4skYvACSTtw5c7wbQejhhkJQIPg6ClZ0gBUM4l9CiIpnxRbsAf_sOhrVo4Y9HjnNC9auHwGS9p0B2OBPccKQn8BAu5EoEkhjB3VOqAN39ULpdKDoJkGz59nU9xlf1LWvwj4bK1sjO_GHAWZHqxjdsHcPypgqjCc_ui3Je-PJoQPVa9-JuS5_Nc1PQaYdMR-EkZjxBgA7lzkQLCCNDoOZ10FRP5IRhbf0X7zW6CbnRMExtEz-DE4akYEwqvb1QeY2nxajzgQB2vyJ6FG3Zrh1MIEtfHdB5-IUk-LGB6PJi35K5KwGzDTN_FBJGy7DiF3DIv2dsxbMW6flu3EYgn40fn1neCfU8O6EWpjX9Im8_ENFernF8UHNa9pH8tA2F81vYQxsYREy9lXg_0h-mhVzof3L_Ka-ZahEjvNligIVKNMRvNUhqWtrLx4hp3VRkxCfSTk3x2eRmTTJwAA467yHXn9r83IE2uxhtxKVQESiUG72PuLNpAhKpu41jsws-4vnz5qjCJflryNfnwTUUfKCiZc3LElimu1y9v8B6pUKJnLJMDLcfLNrt8m1trEg5J1hT42--SjVTPtl5GiSpLSzPJ2nlWR1CNdFQkzDejiA0P6eiGho-a4aF7KvojxUHdWVO0ddAM9HxneGw9n_qLPO00mhRJIGUfhqkqE1yqfdHgXFImzSQF62rNOevkgtHWBBIkzHC_sgpY__3ZSbVGptEYnzug3byf2gdpBiit1m4Ze5A.7YI9_SFJpYPYCic5l3K8EktCr8_IvRWOliN9EckPilM";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoExpirated() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.eBb-3IBceyijx1c2jCaJjwBczl4rL0zZm6oITKdvAK-odRZt80sejK2ECU6EPoNBG82KgcBYSYgsx906ZWrLHAYlW1CM2FVYoAy-SlLYmR1XSy-h2XPKLHjc0fFiX1TPzYZ-3BWN3TT-K7zrCd9W8aMBPlAPpqBJGZJ6ccrIPHVM6ObmHZ_6N4aA7ZaRfeJPY1lDbPj7_gIz_pbNskBVUCbY9KfyLfQCpqhbHzCA8NAg7_xKz8yfTSp1UsmVhdbd6oSbHZfqAwtw-ziaXQ0dmiCUpJI3Xd_t3kDxxt3umCTJYgjH7qczOqiF2Z94pzxh3ESZO_3jmYPsroqCS5CAOg.DpOCaW3jIZJEiAUsHA0P2Q.q-L4WwQWgueA7zaog8PdgD3ycYX2PCf4_WmY-umJsnr9TqwulW9i6kMDuwdg6y4KcM274Z5k6M2TEqxakFvgqkVRgP3TDhg3ZXzqDG44_ICNXlEFUddbPx8500Y3jQQVriHdR2yZ9FGg19pAsa-bsTlV1goS5GRDFYPSOLkxShGvD9QpSB-xaxBq5j-IxhrY52iconMF3hrMg9jC5JBD5OCR9YvzXHam-F3BO9N6-EDG0KsccQIJ-tzoQLZOpa_gxTHuQUhCzMnf8dVFaH-z2wc9rkMP9vneqNyV7uP8F0RB0dHG7w6YYPv5PwR4FVGVC3oQDSGnIqtixpJm8Y9MVlxd_M2-LElQ8Pod9PgfEhVV99MZ04bE0zxjEQU6esLuw2bDieYGGEa0kF2juRJYeuHLH2QmQ4K6vkdokUd-HDyX5rJfTy3_St-NLk12BzxEtHox--eY6vvEGZ0PY8iKIVSuQlzUjz0UmB-IWt7P_v1quEvK0t9FeTXCZNMPC1fWPompyyNSna38tb9CD9nCL31V1K5G7EOKvnAy5M25ic0_EGjXC0z8jeKLP78r9Tz8rY7B4hefpVXtNrHObKYXgEaL0EtpQ1d8BppCQNLfZ9DDsV-Yel_ylztVfFds2q1qPp1VEiThLL2xQv-zTtZfQsGBgNza9b743Dy9VueCIgMPGjGtJMSywisrh-0-ixxFCub03IwQusU_WSm99L5QDEVqHJwFNMngAah-PgxPxDG7kKUZInyyg9TwCFBABoQFo0uX3OJtKYzUyunP_FJH_w.Gc-n3zC71Ab5LoD5HSKWN7f9LodGDDGY4LaPGmSTDVc";
		final TokenInfo tokenInfo = TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
		Assert.assertTrue(tokenInfo.getEndOfValidity() != null && System.currentTimeMillis() > tokenInfo.getEndOfValidity().longValue());
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidateTokenAndExtractTokenInfoEverythingIsOk() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.CcwXpakkpOwganOeZLQcmeB3WZ-UZxZ7oZ1MbunWKetGty7NoQJRwfMBpD9HeK58-2glvRXMnirwRi7x-HeIj2FA4wKM7ptRvQQirtM4xUsUMyPSVgRWeRuvVN8UUf4L_O_M6fi0HgDKPE5aCq1x_mkCULvSo9SiVyePTddByWscYBRn-kHIZK9va_xt9GR9DTlAw-ooqv04z_rAo83iTuNZLob_LY6kYDvzcfHxE38vnPKDCzks6AHXLk0wU5XEqmiDBQmUtlc-Gzo43P4LR-ZopT5p0WUajP5HxPwrdGw9K88wSqWAYi3zKh2REd8NQCmnVobwg9VFXpwmW9S1MA.uDMJ5zVdMoa-Qa_mE-2vMQ.a56lUBtFO8R5AZS0FNIK_ABDXeX8nZWfSJra3fqlwKc9i8euMC9drd6cMnU9NUiRmMm7YOB_XQ97FrHvz6VkzljbVJIYxNO-xF-6p6h2hj2MYvO11Wnk8r9s8gh1JFbP2iU07ceZXCxDYlaRUNFc69Fn-ZRx8NS4WhDlqQtg5k6uw-qS5J5uOp-5zUtksRlz1Minda5oky-FHBMJD3aiUGrAFhziGV8ftax-89krnYEgqSJcUNUlm1rvLXdaaGBFHQ1KE8iXkS8ujgCexvwzXlBjLcqx1_gMJPojC-xZ10u9_7q4VVwpCiVNSrIb0m764vr4bjSs8wEQGVN5ACxADAh3flY2z-yrvfcHCZE4wbYSZeesRNkJ3EhETAqoPqM2yZx2ztY5u3dHmexQtFeSXbZalGUTbwu9HPUbJQJUR-4ux3dSeQxP6HQg_cvDMTGTb4LMZy08i6Nx8pbBXX9wZojdL38P20SQuFDYEcyED7PQtMdu1Ov4No9iXelhxqBrdldN461_AM-tPbdzQrXi4c3Iv8V-S-nrUTYVXIJB3vw5OL1ZfP0Bu3kD0_3GuJEfGluWJDCP2j-eic67LjGpfu7f6H5ik99t5lRhJovxvqo4dDZLuVCJlxgY20azSxICPcldnLxsFCSzG8CYrazPvMktBE1mmxkpA8NUPGsDcEM5XtSjHvpyafwaVVwETCRFCdQelUcGJmVv_ZCzXo7tqeqmAUYDdYlitb1ZllWjCmE.iE76xWS5HQWamApKUT3WBdq1rqBn6qrmStAz6zj2a8U";
		final TokenInfo tokenInfo = TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
		Assert.assertEquals("consumer", tokenInfo.getConsumerName());
		Assert.assertEquals("testservice", tokenInfo.getService());
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("serial")
	private PrivateKey getInvalidPrivateKey() {
		return new PrivateKey() {
			public String getFormat() { return null; }
			public byte[] getEncoded() { return null; }
			public String getAlgorithm() { return null;	}
		};
	}
}
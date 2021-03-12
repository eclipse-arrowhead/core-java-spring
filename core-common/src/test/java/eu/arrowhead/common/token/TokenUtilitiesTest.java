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
	public void testValidateTokenAndExtractTokenInfoMissingConsumerInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.irtdYktHvjiQQeUYXH1Qm9zitPz0P0X3l7wEumHs1ixmAxZ5p8Rxxjay7-KyDzSahkuUyiQKVSBs9rWVuDUUyxibhsfy6YYHO5miJezsenC1hjqvIY-UezpWomoMFfN1Pg2zHaDSgtXeExp6UPkpXzbxnMRD17n4LtBdzI01oZf1D28wzD3X06OaPZkb_QlFVbmCmgm3jNi6Iyl5qxKgl6_cZ9E_CjcKHUDKXyUV1relepIpj0pntSjOtjbdrnPf-J8TT8_Q64IsvKZgoktK48MBMSHBNYMOeXjs6wBSbgDbTzOx3YuhdmRNGEnscRTpBds5mXVk4Siboxh6ITZusw.VljdRYMiQljpUxE7I8DaOQ.DmV9GnMV0Iyqtbb2iq-ccKqLlNilYoQB8nqUz23M9yT_Mk1JQQRFM-FOsLJx2zsTT6AXoy1cofGEbXwoDTjVvSnOJsoRNCrRxglk6eOEnuG-EavDz6rZnuiSs4fGUlFCA9mz4PBZYpurceOhhkXBnlOz34Cdo6wbS6t9lA5WZxIioP_kVolUwtQtNYKUKT-pP53YZq5stjCK1lDgh7hFWYtfxb_2p9WgTSS1Y3X6jLO5nTcGYOvFq5tRJuIv_cm6hUy6bezEFcbKwGvj_uMpleU_aoSlzxG3WvbsSD2zYFGHjmI-4niOGYQCIe14Enxv70tVoEgLlQneyoxRtmNPZ41KJ-5r51XyF6y1zVex3YC21JEqLZw_JMJzVMvZfuD0UiGxspWYFlJdA7_1GAOEiVwVVBOO97cWYwhyJm4MTHBTZoVBjXEPBdV-oZeXqdPZJIgfA9SGngntN7F15kPAjN-cM281vtX6CrG8-nPRY8EpLGO3_MaNgSY-PAeuT-eCluAhkWHUkhLwzp_PkvJxWhIk1xoMrXhmJG3FLS4uk1EVErYhr5GocOidjQRrbbVIJbmQFvm7yECCu9gzKniyO4PiGXjurhBewbQ5ltKDzVgR6O18Inz0J4QBdLSMklzRe9NHGScnUC2-T-Iri_HP9OzMENWWMCLP0qPF0-y3XglR4W3AArb8oECSoRmAzfQfZ_Z5jQ1fL5_-kXXaME6IHQ.UWnOacnwBILpoa01Udp8uGtKhB9oDIfhb90C7bGxDxY";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoMalformedConsumerInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.JdyiF3gct9ovKQf6DRa3Qk6Jv1okiCCQR34WF5Dut0qtZqVp4IGuChi5bTWmFxHF3yMs521bGI5CJPqmD5t9RdE5RW5q_IIEl4uNLIjmacK-v9lGWnbJFDeTjVOBn0kuSTtLfmWh-iQMn7lXKHMEgSylDHszLcRt6gKoGUIpHBGMTCjzP1mZMRvJM74HtgPnGc9rXu6f781GnYI07yJdUk18ukuVaL6c3MJ9DrhybqtcDgSpwyx-78VSVb9KgMt52Or5y_vVSjg-4VDr4oydTFJGWBst-tAFmnDnTcqe_YIDN-mJqdPnLDSZTvkUEaMiF6oJAl6MC7RyMpvGM1l0CQ.98HwURVTZXAJGTXK1_xqhA.Coq6JfKdx_8yybuW4Rm-NJC49f1qtu79SHp31WnpBsRxQYj6hIo_B-Ij6gI65AvST_o4yGRM6PrgXG0QI6DZsaUM2UTEzn2KUgReMy0-NNHk5F1QF9g6nPtxT5Z60d09vpvW68GCwHyVe2bTLIDyAQPcD9lZASRf15YZDPiMowuwJLExljS4JDptgFp2muqErqBNaDq0bEpZoWsHdsbNG9VJtk1eEtBR4EiM_y0tfbOUEe5z2HR2cH8hSeGofQHQz9izGPgifj5kQZEmb4f9XaqC4wKtuNhmS6Zwn89YocNuSwaaq1EVLgpzZ3U4jptNJtqJVRRU1dylFKrCOgqLA_Ynn3x88VY7fEtjdFv3h7WNV-Sj0AhlXMQrY_SnKKNQv_kZCCGi4ecr4LbyN3p_oHmg0XMQkHdZmOPasyd9BNxr7jpDQY9RnOtQRno42ZZ2gAU9LrBn49nfxJ--w1tn_MayHRcL_Ib0bCJkmfhAccQZdzTfkTLrnoFsb3ihMO3P3Bdwpj6_0cA-R18F-YcmEQY-UzCPcXDPiH1aFZCGBvL4_bRNK2CWzA2RkC7-wvESZNuc1ZNyyWA8xSaXlFgXURoBLtzAGef-tAbBbNWcX4R-q-6bHVMBWHJ6utMLfXsJ4ULuYlzIFEiGD-rxPmo_KmG481hnQqUaLnGRqS-NuBYEHXmKm-e7p4j_MEnb6vM2YV5Wd2VWsd0M9c0_3_nILA.ugaQCi4-pCvuQqxY1GhceTwHaXsHlF_8K9IbU7PSTSw";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoMissingServiceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.xEYfM0gfFilrL5mot1dsiS3KcCH71qcW1JOHnJHipN79Ge4baHKP3e2mlaH-8vVaNuQkcgOBsbPVsFhLplzUgOQIhG69e7z4j1OX6zULb3wSmYNk7E9RS2PSeHWecibhjUJtnRoR9E363cXy2553Dm6KM4rpVHe4cZHcMU0QPc20LwQgU-eW4KwBuLrIzZ-EJanORMTu3whIYoiBY4JRSvY1C1UhApBlCwc9jZhs_D5-7_AZYAApNiOYbToqnOHXEZalVNyS5QzHWW3MBYVwB7_Cvp009gfwpDVbuYdOpwJkuaI53cjrdsg3Oro_V-xGrOvogcH54Vqp4xzGWA0Zkg.QCbMl34XnRY7wAjqepNCiA.TQhFLNbhp-LessE2Zmu3UB-6P1-qiaPPotSPGU8_3RDQJGIjtwaRGiWSIvajdf6I8ys6wthqIDfkhxx5ZhOHnsE_549q5zeBHaisNjs7fKXmOKV535f4cIu-zUklrkg1fxRf1dgP7IPuN2Ogp0uvyGJpPigP6SobBObzmIzJKbQdn8NOUJH4XiU_ZiGvE8c3eu4ycYEmcvz33UOczTez2kzKeqrmtkxNkXAVFc0r3-tducnlnpKwVC9B-MTXWJbA4yeJBNV1nry1dNlBEwB9bNYRrT1HtaKYZ47itJBkajCxSrb1p5FHNtg4htJRAJvDgr1HVDXr5EwVUhb61m3eItZDXqpXtHJRzG3fQMUX_YxdJw_UpeZnJgD4O0RAecFsk039JwdJDgan5tCo0sLKRf7ac4wG6Knxzh1je29imiLmCxu0VmaYTJ3oru_MrFAFBf0G7FUqT6a3J0roXZttcS4xKm-XJcSz83aijBnZWNDRNwpkUAmMw7hC23_gdxeMRXWR9koBvXSq_mORDO0v_jszu78qhYbW87_r4qCWDxaWIh0u4R7JvLBSqGNF_Dg8pmw6qUBBIh8Di4icmrm7A2UCC-4HHxhRLfVIU6dAfAyNMjtDPrZ03_LsoPNuUARE63q1i06h9giM9YBiPqMUameoY52UtgOfSb6hqnSSum5ehTdjn5hL-V6dlDEEfImnn73kBC5etJ4edDh3n41DchGxvZ1YKIsiDXjIZFxbGuQ.z1dSVCTCBv0kcjjlgd_o-jwK-o7Pa-0dHTFVb0Wp7K4";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoMalformedServiceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.RkZIgphj_Z78LDpgMR-7Af6eZj8gWHyt0fD3tVdgIxPSYCApakxXhmYo2p4zcy2U_vKt3XVyUd_s4hnTND--7c9TuIHaAr-7mM3uMAOXjggAPLcYlW6wX7ZDboPj-Cy1mLdCjnKLd_rMJnLE8SDbvBPwVEu3aw2TSvNvSTCBvD4KAqN2O9MZTU9aNn3b8zEIVEGgJsO1bdW8wKT0jlAOXNA9pDEBKPv0uQszRlFop4Iq4l0rAJSacgtWWojT0Azt8uKYFuScMzLKXYbZbfDyBgaHzJUl3DPx2p3NFvec9Ew5A8P0AEqBnOI6K5x1rMkNn7BWcv33GkIw4Qq3NLZPsA.gdm_vdqyaEl4ApQ99lMj7Q.vWuRfSlXZeADfQ8bc3l73mMX4fo2AZn8RbJHZQ6gHSkTrObjlqQ_4nmzs2P-3XlU2TGPsWqjNjAvGsB6w_hJO0m0ZRlGWpT7aDCwVS7DeQLSf4zanNlEjRgC3mC78W8vri2AdnLxvkB-yWCTKiGDHsoiCknG1vWEvRfjEW22aXUGo_XPNKaUSwGEQw3Q5pQ9JhUx0IEPOmwaXq7vejWw5kbneUaIuIIRV--EuKl7DZzP4EclOVa5MqZ_F2lpZE_Hlu2a0tjQwYtPdXqOnqHrolpa2ezCv8qFqHosnvn6Us0hlHRPYX6Uum_M-34iSqg1ahMMNSz_hLyvGoXQh2xTp0BISyjYhvZ0RY-LuNj7Nxh-PgWt4OB4wVacWFY5Cnb9BRWHkK153jMkcLB80nOq5mczNKWs5-Gb9Z69ihetWN8-PiOfIISbVyvzDIX_Me5kvyKbUT6gZ5wuLb_S7oD_a2B-dbSKyP4zuctAG3GYzxXFxtds8eYUdfe9wgBxuWg-sJE2mukcUX-nzmPimJVQ2X2vFwsyhs7s4yJWMpri-QzPRIRMLGPMYnQH_ijHoGWCN5id8HRTXOSom0JwDfMQdqjFI2f4rorp8waFRsxml1Y43Eg2QCnhM4iUbt0ff26QUy6p3NxbS9tqwG5axMw2-11IcejHeR5hvtFdmCSUThCU55MwAEHyU-eQxN1iiP9GtbR9h5yENr0w-dBmnrJh3irupOJKHu0VIqrMs-CLi97w_F-Xd8xrjCNXOayy3TP2.IvpFOzSrM3G7JUdoyLnSgpT_LntaSssLFEFVLiqNXVA";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoMissingInterfaceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.CzoeUE843XvOY01orjNsWHjKqFsHAueUkJZpuvi1bbrXiO26OqsPppjjs6IlqKs6gZl6_J16G6c7ut7tZfuzFYfFBnkmYSsYkLKjkYoXLij4EZO9imumG59owvYaytzo0mvJBKb5w1XavP-God_y1RRKkRjLBh1cb8JenKmnOA1HLBsBY5K_wt36U2KC2A3EiwkRS7mzfletlL-Ahy9La2kjYBckH2grzfkCZOKoxCYo8AQ6JgTWTARj4YgLWf040IdqS7QJy0ZsToyAjEGsZCFlbUuWNVL7jMexyleI6wOwtHP2odRPY9PBAvRCLhANKeDzS0i9TYHJNs4AIENyZg.-2rqZ67-qL6athQbwBev2w.AJ1cVTATabiGsBljvuMNnhvcK8DI-IBSB23Xo2Cj7K64j1ows0FNwj_jqLP_xnk9U3OBnWETH8pI-P7nuWRWm4r7ReGgJN2u3_N2k8N0EN5lSDwRlH-VakcpQ_TZZ9dCydACQeC-9Ot3BEefHkykAkbFF8Zwm8wrwcaKDmWhIp6llQn4uqYTABPJmQwfLyyQbCRvcZLU2sez3IxodcrRdMdPtp8l9JzBM9oVyhVMVU1MjrwGekEmQToJ9ucPGoCFB5LUM5riuipcjwtgd5fcROX0z1PK7d2E6lHvhVI9N8XSF3gvL7q704pSY6YItbNJXF7VMOvRURJeeQ6R5QUC0x7UEocJs7SrfW_GBzH0SDkPCoaDbpNJYvtcL-UBcddg2svlZJFkZCJ0A_qYnsMzlkxl_mCEz8fjmvlzVC8MMSY-EZ4cOqJotOzxwF762qs93LFSjQzwdolUsZHJlyuSqHNw88K3lBwljuB80Tm5lLzaZL8MiEeQWi049JnEsBz4_SI6VZebncHlCR1FgSReIdow2BWuVYSX3GNCK5dJ3t-rrspNLjtah4vKrLvapDBirC4QEA6r_UtnkGzjhy6FBr6vnqwTpRP84J9DSI6As-l9R0mDyeTTUgCvVPKlq-7aDwK7QcHBNR0w4yLRZjpD1fKY7cMTxfc8wgsFi7QELitPe4TTB8btReRewXFVzDK_ehvYfxuMgxvA69SoOL1LSGX47CFHMEFAXpa9d_tClfP5icQLTHecLA10sFUnhIDVRr7_HCJv-yy5P-b-n4uM3Q.hJrM-TfMNlz8KxM8O39IwydDzP3wU7NmyUSw3tZJyjs";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoMalformedInterfaceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.Zpl1Q7Slx7iWqNeUnEeUErQKjNYFDgmJD320PNjbQUM2W7NmMehjzQdEvHib1N8p6UVWexyGcLQUmeo8q2BdekiNAMSqy6m4YG3amxUWhL4cde7cNR64q4s5cPe0d-DQPSJ_qyDxSdTz6WCYg0beK4b-QKOLeVezJuiItFdywzhPCsEm8cVtYuOVmpylyR4kIBSFbthlYok9e8UdryiF7vOL-6cEtffwnTGsxkmHsx3DRJPXgX-NmTTX7vKVzGovIwY5LBHJSIrXdmks94TDFrUZiSPw6JSHK7ci7muFT9D1xbJmjJeuxfOcRVcP6autyJy7tv07sbnTozvFxtpcEg.qq6gANxGlm_vfQEEWmVPHA.JTLc-7YC1eyIWKGJUYBJeKgczIL1ihkOyOpyBjsCio5nLE0dOmbORODnQH3ofw3EsYTnL5er2jZmzPDgdCWYARaCE5AEgBuShj8-1bn56ok82p6n5Bf21o0u88Gwhsj8y8_jVIfv0RZjRm-dYkOtzQUFrxTeBw-klE46dV-XSBuIIJUpPt2Cbpomh3LcxSLs7tQojpMCyns-aeP5Z_aB4w-5xWKRUi9R5brC8etXYsdOovq5qjG2mhPmIUunPtpO3JPSe9TtKDzkuG2qNeAQN-WF-Y2uPEU_6Mj48zKGp2sUp9t5Qj51hgBC9jDcJrqHNPRYPulpGkJmHJBxwEzRMFJDHmfzZWKEAeW2SusPUGzSBcf2vCZS7fO2dM5MXS2QufoBJQkdxXbAS1U1sIPyoc2dkEl862HsScg4Gb9GlLGHZvHPkI5JL34ctXatCVJn3ENzc8yA9po2n3Je2eBDpZw4-xyI6wzgWvrnK7usQEqhq31QEITTUECMH5hJfRqGnzCCNLYqUOfzHnvDoWVvzzybRUzHAWBG3LSQew-PRbPSXUHrNdP2m4XjdkqDSsjpQ6TMeS07Noxw8NhfpwDAnD225y66exwkduhGENa9Ew2zltISNv-DTgrg1lBtv-ij_LIshPcKllyfN6dEkyTZsArvzt00yWXGLN7jguUT3yQm2nVhDkwoDhV78vkqsMMADNAphIRJKpFnX2DxDrydG60Yjk6ZSpiJOxrKwvhr53SzUOL7dJQqoOrq_UpvRN_4_R27cs5G6cV16bOMNfx2og.AIcRjtA_2lmj8gCopd7HJ_f1G44-HK1nfwrHPozzBtY";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMalformedExpirationTime() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.LD6wZmSL2ul9kBp-cpltSzvS9yTw94IjhyRCv3ZBVXZVJ-Xyg30FDpCmZtodr1g_1R0biouFDxGZcEmK6lAywPIR7kjh6N69wVtMJfdrtEXsCQe0cK5-UcdlDKzoCOtghl9G6eNLIayBJJafi8VPfzwtqQIFBpHidwu9Arf6tH86CNbsYcmE2YQBjtp1-Aj19oTiGeH6dG5IAoWg9GtE8anxiZy3HiZ_D9Yx7Dh6z8FWPoSGTNIUggjI4iYs0OdYMUQGrEYPMA_qBK2FDPWyZjJs7NnN68JMEDDhdgcWRQAinGS-9PlLJxZIf1ha4WvsEXQZilty2LhkW7P2qiE9Zg.T4q8hxfKuCOpNogAo0pZiA.nemwVqilxecNuV7lLqmzTFwoAU_FPtwldhlQ9t1aIszpMyDBniFEbVilCg0gX5VutaJMTmJaW7wcdpp84b0iATUiT0yYI2TR6PDfqK2uZLBq-iwVFeHW0JcLB6TmNMguiVqm8ZZwFsUmRM8B0dH3J1fpaSg9iXf2Q8315I_e3ZUxRqe96LjKvONm-Xaby8OdgYyIhUm-GHepp6_zD1802L4RFUOl9Yoy0hodDZCBufDkDR1h7wRxQg7NtyfDwzhF3oeC_gWxh69TbFu4Ox_Tyeq63ep8vM1-pIlMckvfmgx6IFCfw3nBik4jL3BKMrTA4E4gP5b2stlvH51RV_Xl7fuops00IOX8KOZM-7F_TSbAgq1OdA0JfWLx9Iw5jRdsTymAV1NltYr3pXZ5rQExZXDyt2rg00nFuIfpiMmafBQ-81z-Nu7hLKQTnuZVkaOpHt_qjQV07-nAowVl_ahXt8IZyJn3L3bRWkFciF6vWpuc--lUKuESpWeoRzZNovhPLPrM6C0Dv8LqlNRdswYmMQN3xm-amh7RKkX38XYDiSZEArFCpWojrZVKKV22yg8E--DFKuXa5PfmfDJUZ0UbjYsDviizwnNIpj8Qi2Wh4jElhM7ont6IAqLu8NtfH1lcOSXh8pVlSQ5u9ius2XUPcQ9k9hFBaR5LvOyHfstfALl60umADI_mZLsgvaHhXmTC4ntBfKJoOqyqxe74by1UZii9AqKEFhaB3kE7YPi3G07Dk3jvJNDkGqXvIB3u6nc6wc1sGLbFMYt8ySwRLEKhfq3s73Sx3inD_TYOrnSi7qI.iWVKg9PXSmTTxjX4R2ci1MhgE9A3uTkhdDwa4FV39ms";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoExpirated() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.mFS5R2MmfWzFIaZltLGOsHTgVLLarTaJ45CeP1zS7rfjlVotbTP7VdfB24i12pfABh_afeJb8Ok40erblmd1Qu-afGdJwyFWocllYcQHKVW24KxfvIBGyF15nxZXKOaB9U4GWLo78p5bdnzN7d-NKUzJ-4by8Ch3s-YaNYJA-S4rfp-5rnXm30igqe0B8VU8DtTaSrGcZLG6MfVjzlIVaX2WJu4PfKYwm_XfKDim62tiaM01TROC92xoC2aNaCqlDPcXUcKYujYUR3KCMOdgnf4I3Q895bwzeNll2EiGcS20v4fFjD2esCCiJZVZPzziEF6DC1cWwkITFOvw-h1vtA._VQEXegW4vFKwFdUBiqcIA.CIT5Ie0gq_avJ2eUc_xNTiBc4rmAEZaIfknyAUcHt1tJROIaXoMhb8eVTuoJZ0gcksrhqEk2zsxJN9y7fRXpwHyrjoQ8-8eq0VPguHW3xjdQ42oYQJGEILPtLjv9-aqPP0d5RhsQnLDxupIBAadfhK7wNfj5WjxD_VKzO-RUOEjRVxeTBl396nLjansqgDA9Rp6Pwu57trZYhxvT0fnt0sOCJ21kJPN4jOzwGPoxY_vT9ZYARlBxyBRdyuNb43nWGTQ0_4seWpFQFzupMHlqyNkvkOhNigOUD3G66slSwWi4udBB9imnFLX4UL6yWLIRLNAPEP_IiquRqNJsB34fNfl_j8nXQf0jPizNsA22RxyZ8pNXtoIEe7sjFzdwVO_jMyk55UIHyK1SvRFI7WybalZNlfqdC01t4I1oYhpuIPbsV1bLuEp8r4_Pb4jRcZ3GAZyn4sYyIXeTr-jtjpGeOpjg5MdgzpY-UxI6RrGu5518QwMg9HU6fUhnce0c_ve3J4aqZ-XNNH3QQmxeXiMeM6S3XIZde2ja7KWKZoCD-rCLdiMdJNPzy_5o-Fn4ivh3Y48_U-uXDjKL0OvifRyE8FH-gb5XZBVYC2NVRKC_yN5pozqjA94qcVVbPHoVqJtTyxzK5hwZiP_X7sKSF5s2fCizUg4nNzThv0pEcq10Blz5wkzGXYfHvTK8125LlotHIpjhP-TC3XGDWs5SemZNKWTvu1QN7xlpGLTQWlSRRDHzUGiWC8rDgyX2hfPZjp2lPje3qRtHD3VF128a8EWG9TVZrNOzDWYvfCgPurw37clbo-71VpIqS6AaguuStKu_.tC8gCSwYlfbEP6B5FgUyN-yHIjA95mvanGbG49W8MNQ";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidateTokenAndExtractTokenInfoEverythingIsOk() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.OeJc0QYnXnUj06m-o_aYNrxJHKUSgURPM4OxsHHUjamGt9Ur5YZXb6Mx_5bHjlcImzXlmWm8i-CIbX0mU1OV6PNp3nN9MR8AJtjy4qiJUjeJwJrChgb9teIhVyr_5uFkUpvexPsnY3vWKapNTd9i7aLY8669n8uxxLHHIxKSjrg9WyCcRVXdsRbo-OXXWx7Lq1dHKz1Cdze4Mvnq2BYlh19h0q8mhYNYiZ3m17CahEMV0R7VEoIgcFE11DXg_3CMJHBt-In3nXQm0aTEF6Xrs6M0Ul-eQV9KrL2YCFA44vseYU5Znh08exR6r7FTvh7WVv4tK1Cum4YUXEu0_NSQow.0M_7VXIvyy6-egD7rstkNA.FIenD_Ztsiv-cBkQXzJFPG8I3_1UKRPlG8cYyVJ5mA8vXVBBP6YIL0o8a068aWn0iFshoa6FagI5aK4yETwf-HQD5r-3c5hemtgW0zUpnM2bQx_ek-adHSpKHfOtbNQ3CJGMa9tvgGOV-IskkFeu54hJoYpcczgn4x9aqLDvJbCIFmRPJm0TjxOX4z2MUeq0gMRObLsORxDPlYQs3M4KbyEU6fDc6jbnZSH9z-cj7ALuBPQCt1Gl1ea34ShxUf1U3gkbO4iyHQxxyVf9WsdtHxI2daNuYnvovfzL2VJr7Vo2NbkmVFn8_JIRsk45UVryN85VXxkNwNRt2TyfPF9LfLp4jDJZXoRLMo-ckKZaYHM0iCAEuL8R2KInAW_G9KKGA2RqiYzlRiir__A9tF4M83cWV5DAee27E61MO_2uzJ6b-lKBhoLVaBc1fwEEJ34Sq8kO9DYDofOx4LqkWVRwGy8Xn32-DHXJ_2LzTcztQ3wAOQpZRi1O5M0PrCq9ClrsRMbLr3iYBco3XwRPSGpxSfDST9uN1Cn52qcIInNEFSkOZROsjvbABxfUxJth2gTtponuhnPuJDX8L9AB9GoFFbt9OmY-7XJClDf_UFiENpw3lEw3vluQc4t8pzBluYkZCDnIOFRoC3_aVzHzLk3YZ2XI2QBMGslrg7RgBahXU6YDdTlvvQgOXUkMg-30HejomBfxJgzDdFC4fU2sjeJfgMGlswCSvNBheMkku-aYRqsCdhKHQWVMsHmEZHuxbDS1A9tRqXLCQTmdP6hyp77zNA.x_GsHPy1adNEii_gSROMVElbbRegGxJFOWbJMqdi8VA";
		final TokenInfo tokenInfo = TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
		Assert.assertEquals("consumer", tokenInfo.getConsumerName());
		Assert.assertEquals("testservice", tokenInfo.getService());
		Assert.assertEquals("HTTP-SECURE-JSON", tokenInfo.getInterfaceName());
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
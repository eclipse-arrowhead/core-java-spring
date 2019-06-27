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
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.irtdYktHvjiQQeUYXH1Qm9zitPz0P0X3l7wEumHs1ixmAxZ5p8Rxxjay7-KyDzSahkuUyiQKVSBs9rWVuDUUyxibhsfy6YYHO5miJezsenC1hjqvIY-UezpWomoMFfN1Pg2zHaDSgtXeExp6UPkpXzbxnMRD17n4LtBdzI01oZf1D28wzD3X06OaPZkb_QlFVbmCmgm3jNi6Iyl5qxKgl6_cZ9E_CjcKHUDKXyUV1relepIpj0pntSjOtjbdrnPf-J8TT8_Q64IsvKZgoktK48MBMSHBNYMOeXjs6wBSbgDbTzOx3YuhdmRNGEnscRTpBds5mXVk4Siboxh6ITZusw.VljdRYMiQljpUxE7I8DaOQ.DmV9GnMV0Iyqtbb2iq-ccKqLlNilYoQB8nqUz23M9yT_Mk1JQQRFM-FOsLJx2zsTT6AXoy1cofGEbXwoDTjVvSnOJsoRNCrRxglk6eOEnuG-EavDz6rZnuiSs4fGUlFCA9mz4PBZYpurceOhhkXBnlOz34Cdo6wbS6t9lA5WZxIioP_kVolUwtQtNYKUKT-pP53YZq5stjCK1lDgh7hFWYtfxb_2p9WgTSS1Y3X6jLO5nTcGYOvFq5tRJuIv_cm6hUy6bezEFcbKwGvj_uMpleU_aoSlzxG3WvbsSD2zYFGHjmI-4niOGYQCIe14Enxv70tVoEgLlQneyoxRtmNPZ41KJ-5r51XyF6y1zVex3YC21JEqLZw_JMJzVMvZfuD0UiGxspWYFlJdA7_1GAOEiVwVVBOO97cWYwhyJm4MTHBTZoVBjXEPBdV-oZeXqdPZJIgfA9SGngntN7F15kPAjN-cM281vtX6CrG8-nPRY8EpLGO3_MaNgSY-PAeuT-eCluAhkWHUkhLwzp_PkvJxWhIk1xoMrXhmJG3FLS4uk1EVErYhr5GocOidjQRrbbVIJbmQFvm7yECCu9gzKniyO4PiGXjurhBewbQ5ltKDzVgR6O18Inz0J4QBdLSMklzRe9NHGScnUC2-T-Iri_HP9OzMENWWMCLP0qPF0-y3XglR4W3AArb8oECSoRmAzfQfZ_Z5jQ1fL5_-kXXaME6IHQ.UWnOacnwBILpoa01Udp8uGtKhB9oDIfhb90C7bGxDxY";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMalformedConsumerInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.JdyiF3gct9ovKQf6DRa3Qk6Jv1okiCCQR34WF5Dut0qtZqVp4IGuChi5bTWmFxHF3yMs521bGI5CJPqmD5t9RdE5RW5q_IIEl4uNLIjmacK-v9lGWnbJFDeTjVOBn0kuSTtLfmWh-iQMn7lXKHMEgSylDHszLcRt6gKoGUIpHBGMTCjzP1mZMRvJM74HtgPnGc9rXu6f781GnYI07yJdUk18ukuVaL6c3MJ9DrhybqtcDgSpwyx-78VSVb9KgMt52Or5y_vVSjg-4VDr4oydTFJGWBst-tAFmnDnTcqe_YIDN-mJqdPnLDSZTvkUEaMiF6oJAl6MC7RyMpvGM1l0CQ.98HwURVTZXAJGTXK1_xqhA.Coq6JfKdx_8yybuW4Rm-NJC49f1qtu79SHp31WnpBsRxQYj6hIo_B-Ij6gI65AvST_o4yGRM6PrgXG0QI6DZsaUM2UTEzn2KUgReMy0-NNHk5F1QF9g6nPtxT5Z60d09vpvW68GCwHyVe2bTLIDyAQPcD9lZASRf15YZDPiMowuwJLExljS4JDptgFp2muqErqBNaDq0bEpZoWsHdsbNG9VJtk1eEtBR4EiM_y0tfbOUEe5z2HR2cH8hSeGofQHQz9izGPgifj5kQZEmb4f9XaqC4wKtuNhmS6Zwn89YocNuSwaaq1EVLgpzZ3U4jptNJtqJVRRU1dylFKrCOgqLA_Ynn3x88VY7fEtjdFv3h7WNV-Sj0AhlXMQrY_SnKKNQv_kZCCGi4ecr4LbyN3p_oHmg0XMQkHdZmOPasyd9BNxr7jpDQY9RnOtQRno42ZZ2gAU9LrBn49nfxJ--w1tn_MayHRcL_Ib0bCJkmfhAccQZdzTfkTLrnoFsb3ihMO3P3Bdwpj6_0cA-R18F-YcmEQY-UzCPcXDPiH1aFZCGBvL4_bRNK2CWzA2RkC7-wvESZNuc1ZNyyWA8xSaXlFgXURoBLtzAGef-tAbBbNWcX4R-q-6bHVMBWHJ6utMLfXsJ4ULuYlzIFEiGD-rxPmo_KmG481hnQqUaLnGRqS-NuBYEHXmKm-e7p4j_MEnb6vM2YV5Wd2VWsd0M9c0_3_nILA.ugaQCi4-pCvuQqxY1GhceTwHaXsHlF_8K9IbU7PSTSw";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMissingServiceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.xEYfM0gfFilrL5mot1dsiS3KcCH71qcW1JOHnJHipN79Ge4baHKP3e2mlaH-8vVaNuQkcgOBsbPVsFhLplzUgOQIhG69e7z4j1OX6zULb3wSmYNk7E9RS2PSeHWecibhjUJtnRoR9E363cXy2553Dm6KM4rpVHe4cZHcMU0QPc20LwQgU-eW4KwBuLrIzZ-EJanORMTu3whIYoiBY4JRSvY1C1UhApBlCwc9jZhs_D5-7_AZYAApNiOYbToqnOHXEZalVNyS5QzHWW3MBYVwB7_Cvp009gfwpDVbuYdOpwJkuaI53cjrdsg3Oro_V-xGrOvogcH54Vqp4xzGWA0Zkg.QCbMl34XnRY7wAjqepNCiA.TQhFLNbhp-LessE2Zmu3UB-6P1-qiaPPotSPGU8_3RDQJGIjtwaRGiWSIvajdf6I8ys6wthqIDfkhxx5ZhOHnsE_549q5zeBHaisNjs7fKXmOKV535f4cIu-zUklrkg1fxRf1dgP7IPuN2Ogp0uvyGJpPigP6SobBObzmIzJKbQdn8NOUJH4XiU_ZiGvE8c3eu4ycYEmcvz33UOczTez2kzKeqrmtkxNkXAVFc0r3-tducnlnpKwVC9B-MTXWJbA4yeJBNV1nry1dNlBEwB9bNYRrT1HtaKYZ47itJBkajCxSrb1p5FHNtg4htJRAJvDgr1HVDXr5EwVUhb61m3eItZDXqpXtHJRzG3fQMUX_YxdJw_UpeZnJgD4O0RAecFsk039JwdJDgan5tCo0sLKRf7ac4wG6Knxzh1je29imiLmCxu0VmaYTJ3oru_MrFAFBf0G7FUqT6a3J0roXZttcS4xKm-XJcSz83aijBnZWNDRNwpkUAmMw7hC23_gdxeMRXWR9koBvXSq_mORDO0v_jszu78qhYbW87_r4qCWDxaWIh0u4R7JvLBSqGNF_Dg8pmw6qUBBIh8Di4icmrm7A2UCC-4HHxhRLfVIU6dAfAyNMjtDPrZ03_LsoPNuUARE63q1i06h9giM9YBiPqMUameoY52UtgOfSb6hqnSSum5ehTdjn5hL-V6dlDEEfImnn73kBC5etJ4edDh3n41DchGxvZ1YKIsiDXjIZFxbGuQ.z1dSVCTCBv0kcjjlgd_o-jwK-o7Pa-0dHTFVb0Wp7K4";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}

	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMalformedServiceInfo() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.RkZIgphj_Z78LDpgMR-7Af6eZj8gWHyt0fD3tVdgIxPSYCApakxXhmYo2p4zcy2U_vKt3XVyUd_s4hnTND--7c9TuIHaAr-7mM3uMAOXjggAPLcYlW6wX7ZDboPj-Cy1mLdCjnKLd_rMJnLE8SDbvBPwVEu3aw2TSvNvSTCBvD4KAqN2O9MZTU9aNn3b8zEIVEGgJsO1bdW8wKT0jlAOXNA9pDEBKPv0uQszRlFop4Iq4l0rAJSacgtWWojT0Azt8uKYFuScMzLKXYbZbfDyBgaHzJUl3DPx2p3NFvec9Ew5A8P0AEqBnOI6K5x1rMkNn7BWcv33GkIw4Qq3NLZPsA.gdm_vdqyaEl4ApQ99lMj7Q.vWuRfSlXZeADfQ8bc3l73mMX4fo2AZn8RbJHZQ6gHSkTrObjlqQ_4nmzs2P-3XlU2TGPsWqjNjAvGsB6w_hJO0m0ZRlGWpT7aDCwVS7DeQLSf4zanNlEjRgC3mC78W8vri2AdnLxvkB-yWCTKiGDHsoiCknG1vWEvRfjEW22aXUGo_XPNKaUSwGEQw3Q5pQ9JhUx0IEPOmwaXq7vejWw5kbneUaIuIIRV--EuKl7DZzP4EclOVa5MqZ_F2lpZE_Hlu2a0tjQwYtPdXqOnqHrolpa2ezCv8qFqHosnvn6Us0hlHRPYX6Uum_M-34iSqg1ahMMNSz_hLyvGoXQh2xTp0BISyjYhvZ0RY-LuNj7Nxh-PgWt4OB4wVacWFY5Cnb9BRWHkK153jMkcLB80nOq5mczNKWs5-Gb9Z69ihetWN8-PiOfIISbVyvzDIX_Me5kvyKbUT6gZ5wuLb_S7oD_a2B-dbSKyP4zuctAG3GYzxXFxtds8eYUdfe9wgBxuWg-sJE2mukcUX-nzmPimJVQ2X2vFwsyhs7s4yJWMpri-QzPRIRMLGPMYnQH_ijHoGWCN5id8HRTXOSom0JwDfMQdqjFI2f4rorp8waFRsxml1Y43Eg2QCnhM4iUbt0ff26QUy6p3NxbS9tqwG5axMw2-11IcejHeR5hvtFdmCSUThCU55MwAEHyU-eQxN1iiP9GtbR9h5yENr0w-dBmnrJh3irupOJKHu0VIqrMs-CLi97w_F-Xd8xrjCNXOayy3TP2.IvpFOzSrM3G7JUdoyLnSgpT_LntaSssLFEFVLiqNXVA";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoTokenMalformedExpirationTime() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.Zk1u_ic7Tpl_pckUq7wIDCmKNb6F_7ydGHHsWNou0JUZKiyzsQQhqJfLwpnKWkkhVC9HtGpLhEy3zeYJ5lHS6D1XxqwotoM5iul8-YBtvYB9EBcG9wQl9XcQMxqVn2uT-trEkbh8O7t8iNRELc_xnQYl1qkQSe5r1G6HnAEcafuI0UCiutrlyDuZDWVSPv-PZCaLISjkkbqhjt9q0iUZFk_ZJaF5CgE6Uz2Rt-5HdA7akTq7C3TyrK90eR9kjFyTfj9AEDOEEUAoUG4rSLfZXFbM5YeG25fRbKm-ulb8mmbSVirvpAkVqBgqckrwFMs7PEQ99tZRL7zMOfn-ieqIUA.zvvDZnHJncmeAbjA7kVWrw.29v954dGfu8oMQyF8Ewye4PINfCcrxyr0UtFxl8XQir1YxRZBJlAvUcdV9zQvelS4lV_7YzVfHwImebV9rzQFapDW-Tk-chw09kwZzcZGOUBTiA0aPiJtLzkBp3Gn6b-JOC6mrgjkNu2Ctf8m0PeFbZI1vYO21p9mpzasO5l7oqPLbliDut2MxxjEq98rERsaJuEqk9TpYnsNhTb0zwtPq0fVU5tFI7nSw9dCEfVETTvvX7xAztEcOMZloxMk_MkkGVG8f5pDPZKVsQF5_v6gx37LPRrl6PBO2UD7uaHqvsXlwRvIyfvIvUlWzSfpol9xQIdKfLh_YwWGsRNc9uYZUuctp8LNo4W1puNIdZ4XlgKXMh7LzZd6yspWqXsipWpWZnXiikbvmvZeC_VuJg0QIM1VtPE92WM-0ZlpoLd0LqrpmVPesc83Vmcnjv2OJeFcuTDD8NBxlhWbDhu8yUHCcfDo2LLvOHcjMgZFEhY2QKmWL9y_vlJxK7gWYbltUkiG5Du4Tp7rwz1Wl5dPUgC6Dv-y5cA3d219UkIPsUNiwysf2QI1QJm081kYezDJPQHLUvPUieTmULyaBVaddJZTGiC_DHIc1cNiknncCUy2jBtWoRSa7B6-ceBNndoK5MsbAKT3vKox5jAAcs54CZiCcRCOn_du6t4cUxi44FqqSrFpRCNkKtgCaFXLWk5yORmXz_y8gW_fUiwv2X5fKI3Yf-ujbpG-4TDJxtxvVbOhI946ZVQh2VP5i2rHWlGT1IH.r14VmjOS-blzhIkm484UfoJ15fsLiMtgH15t1MyP7dA";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test(expected = AuthException.class)
	public void testValidateTokenAndExtractTokenInfoExpirated() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.l14wcjuhBSqeL041cFl_NDaTtA63XyiGewZmAL0SmMVqPxOb84ILSUvFRxuuf4IYeDp4-oaUGBxJtD5ynk2IrDGEMdZC-NtD7c-ZWMCcdZrx8lqUSKu6cGHIS1ap2eSx9jsib9YEdGUYmkke2EimpqKDovjznzUsrYycqPdqMR7kXqrPZINAIU-EU8IR9kHXLZcwao_CJt99-hulnZzND_5xq5PGOCYKMXTkRYQ8z0YhT9L2uYM2QgaM5PfANXcvYU3aaJMN9aCsamQqCvj5ZNOqFmAscCYNcYP-PQS7_hHhoAnGRoj1MvyA4KTaTxHFZtk60QWlwz-qGz_bV-IWWg.ofl7Trt63uHkyDpJTYd-Vg.V8xFuv3hG8BuBqF5jyMZ1_W3rxgbME2Cpv_ngDsX37M1G943nMvmLP93q_fPxa6ccLwRSeD9sqrSRUkdDHt-CuqPIzoJqQtq_eSeI7FtspsXCshkyicob0-tpEKeTtpkQR-svaREJnNjCEs5yTep5eU3ez7ZxTAhrhpbq5nDfgmUZCAMlneE--zEYksVnMHc4XKtahFC63YCo3DWYEfElw60EVIAICQl6n1lv4s1LYK6EWBZGVaOQl_xtnrRM2vsg8CY2UfgcUYscF-idoyz9vevzCM2XUnMAtm0wCspLVry85xFNy1LpL8y2EkU8C5pD3WXFLKmu87hC0Bb_KudV59fv5UdT22XHRw8jd8p6LRZi0kE_Y8-yfuvAk87mgYzPvFktWz6SMFCH0E5C5r9cOYYh37h5ob8lZAqsmcByKyrzoRiL9f2XsL42IXtYxf9dxcxF6tBr7sVB-Wi1uKtsGvxbG39KK-VK2sB6tMek1N5ba9auxa36uFN3R971u5c8p1zMGu8KPFPfUKkNXcP_8ivAq_tuu2Asd3Oj1o9zFTNKNc4wTWvZTcZlNx4_aPwjeOLx-X33LVHKDOo-2hfMmtsRszWd0hAjMOSuEGTRhT9WSLqQgCXqxEtN9Z-kt58k3XxrxxCXgBUFEvThpj3tJFw57O_xTzZZjJf7ePa7HRztdLeTOYQbM1PYrxYw43DtynDah8sJQ2hNBPCVjgBj1NiRXyZdmLTcuCUoX5H_Jsej9gFSLRdnLfvNceQoRBn5FHJnCp3mp6jY_hmloyg7w.TwZmDCqWSvBvdbOD6qGtjQYJYjdVIjxyc24hf7dvmDg";
		TokenUtilities.validateTokenAndExtractTokenInfo(token, authPublicKey, privateKey);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Test
	public void testValidateTokenAndExtractTokenInfoEverythingIsOk() {
		final String token = "eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIiwiY3R5IjoiSldUIn0.zRR8VyqFDZZ5KSByd5jnRKQEHSAmOce99H02ABELKAMnlz-9OekZnJvsIxPP28Vv9CVtf1r_AACylBmVkZtuUoQE5PLlkiYq128rYCiYx7Aa5oCU2hRQIMfW_9KhUDMG5KdF9_9ZMqvt1ZfrP4tRc0b0I_DA20aZpH2-TR60JuX8eISKon131Rif3TmNpqXI6FHsAGvh4h4a9vt2nwCCTyCdqI2-9NJ8Gz-bze5cFEccADtnvcEPA8J7qDqq_bQWAFAECY8YUDKDrXmYbsyXqJ2LXhH_6aBWpq6PxgOq2g9HkHYR1M404sseMDY3rFpMll8pQjikcop0uAi-NE_l1g.EHG0BhIMqdEXJwOONU5CQw.NvgiIqYbD0ML_d9hcahUU3jFLWIMar8RdwQoflQolv-vVoXwqENRnKjQxujLbigM4buzPylLFF7dT8y1-x_SB-H1Eyqj_QSp1TFUAh3t0_8XWUfPztFh71GjvTPzhyoBjEqliayfIO_qBmsn9iI_4p0j7dmKOhj6ICjt0TIygwTUwGA2OK06kNE9YJeYkExE5gYJDvor8kFeBPZXe1Bhfw-8BBygMCrJ-Ej7orjCNpV0evoQ9b7AvQeATlwV7pWj6tn34uvId9X369ySzfyEf4WBaP4f48-ldbj3untQhRj8gKXvWlOXmQqr_DTrra8wPkAJXXVfeT5f3IknVCh2xkcthtPf4g2M9DyGRIGivbkwsQLNRzNSPszOnxg0OUvXoN3oOhtOdVBNcNdjF1C4kiht-krvUGvkf1W1aNc0EX35uIBkL-Z__eUeQwipsyz6r5FrKlk-54yFK0wDavFLy9790fF46m5U1RpaI43Wemt77iuUP9Mn_vV7gL7dEOgjej1Vcumwt_gSTSS5a-qttEwchb3p4sQfekGRxS5hz4ecQs5dbme8RoK9sFqVMyu3e2K7S9iXbHhw0vhWf2hjPSegRWwVF04ojcN1BQnIELfFnJ3QGzA82nz6t31b72_0AfKxw1A6j7r5JW0iuqMroEnC6OdD_7PJcHx0rF89qbS2Z2qahO4MVh9ZB6w2N8oAFloWtQV2SGkQQ6Dp-d6BS49on5utFR4PjdIXs0z0ETk.jasfCemk7jsQR5NFAvIVJ47r4XRBP9uTi90I1_yMg90";
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
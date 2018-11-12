/* 
 * Copyright 2014-2017 UME Framework Group, Apache License Version 2.0 
 */
package org.umeframework.uac.app.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.umeframework.dora.ajax.AjaxRender;
import org.umeframework.dora.cache.CacheManager;
import org.umeframework.dora.message.MessageProperties;
import org.umeframework.dora.service.BaseComponent;
import org.umeframework.dora.service.user.UserCacheService;
import org.umeframework.dora.service.user.UserLoginService;

/** 
 * OAuth2.0用户鉴权服务逻辑实现.<br>
 *
 * @author Yue MA
 */
@RestController
@RequestMapping("oauth2.0/")
public class OAuthController extends BaseComponent {
	/**
	 * JSON格式化工具实例。
	 */
	@Resource(name = "doraAjaxRender")
	private AjaxRender<String> ajaxRender;
	/**
	 * 鉴权Code缓存器。
	 */
	@Qualifier("accessCodeCacheManager")
	@Autowired
	private CacheManager accessCodeCacheManager;
	/**
	 * 用户信息缓存器。
	 */
	@Qualifier("doraUserCacheService")
	@Autowired
	private UserCacheService userCacheService;
	/**
	 * 用户登录服务实例。
	 */
	@Qualifier("doraUserLoginService")
	@Autowired
	private UserLoginService userLoginService;

	@Qualifier("oauthClientConfig")
	@Autowired
	private MessageProperties oauthClientConfig;

	@Qualifier("oauthServerConfig")
	@Autowired
	private MessageProperties oauthServerConfig;

	/**
	 * Get access code
	 * 
	 * @param responseType
	 * @param clientId
	 * @param redirectUri
	 */
	@RequestMapping(value = "authorize", method = RequestMethod.GET, headers = { "content-type=application/x-www-form-urlencoded" })
	@ResponseBody
	public void doGetCode(
	        @RequestParam("response_type") String responseType,
	        @RequestParam("client_id") String clientId,
	        @RequestParam("redirect_uri") String redirectUri) {

		if (oauthClientConfig.get(clientId) != null) {
			String forward = this.oauthServerConfig.get("login.page");
			super.getLogger().info("Forward to " + forward);
			// forward to login page ...
		}
	}

	@RequestMapping(value = "login", method = RequestMethod.POST, headers = { "content-type=application/json" })
	@ResponseBody
	public String doLogin(
	        @RequestParam("response_type") String responseType,
	        @RequestParam("client_id") String clientId,
	        @RequestParam("redirect_uri") String redirectUri,
	        @RequestParam("state") String state,
	        @PathVariable("user") String loginId,
	        @PathVariable("password") String loginPassword,
	        @PathVariable("password") String option) {

		String token = userLoginService.login(loginId, loginPassword, option);
		String code = createCode(token);
		
		Map<String, Object> codeBean = new HashMap<String, Object>();
		codeBean.put("token", token);
		codeBean.put("targetUri", redirectUri);

		accessCodeCacheManager.set(code, 60, codeBean);
		String result = redirectUri + "?code=" + code + "?state=" + state;
		return result;
	}

	@RequestMapping(value = "token", method = RequestMethod.POST, headers = { "application/x-www-form-urlencoded" })
	@ResponseBody
	public String doGetToken(
	        HttpServletRequest request,
	        HttpServletResponse response,
	        @RequestParam("grant_type") String grantType, // authorization_code | refresh_token。
	        @RequestParam("code") String code,
	        @RequestParam("refresh_token") String refreshToken,
	        @RequestParam("redirect_uri") String redirectUri,
	        @RequestParam("client_id") String clientId,
	        @PathVariable("client_secret") String clientSecret,
	        @RequestBody(required = false) String options) {
		
		if (!"authorization_code".equals(grantType)) {
			//TODO: Exception
		}

		Map<String, Object> codeBean = accessCodeCacheManager.get(code);
		if (codeBean == null) {
			//TODO: Exception
		}
		
		String targetUri = (String) codeBean.get("redirect_uri");
		if (!targetUri.trim().toUpperCase().equals(redirectUri.trim().toUpperCase())) {
			//TODO: Exception
		}
		
		String token = (String) codeBean.get("token");
		long expires = userCacheService.getTokenExpire();
		Map<String, Object> result = new HashedMap<String, Object>();
		result.put("access_token", token);
		result.put("expires_in", expires);
		result.put("refresh_token", token);
		result.put("token_type", "bearer");
		String jsonResponse = ajaxRender.render(result);
		return jsonResponse;
	}

	@RequestMapping(value = "rtoken", method = RequestMethod.POST, headers = { "application/x-www-form-urlencoded" })
	@ResponseBody
	public String doRefreshToken(
	        HttpServletRequest request,
	        HttpServletResponse response,
	        @RequestParam("grant_type") String grantType, // refresh_token。
	        @RequestParam("refresh_token") String refreshToken,
	        @RequestParam("redirect_uri") String redirectUri,
	        @RequestParam("client_id") String clientId,
	        @PathVariable("client_secret") String clientSecret,
	        @RequestBody(required = false) String options) {

		if (!"refresh_token".equals(grantType)) {
			//TODO: Exception
		}
		
		if (userCacheService.getUserObject(refreshToken) == null) {
			//TODO: Exception
		}

		String token = userCacheService.refreshToken(refreshToken, null);
		long expires = userCacheService.getTokenExpire();
		Map<String, Object> result = new HashedMap<String, Object>();
		result.put("access_token", token);
		result.put("expires_in", expires);
		result.put("refresh_token", token);
		result.put("token_type", "bearer");
		String jsonResponse = ajaxRender.render(result);
		return jsonResponse;
	}

	protected void verifyClient(String clientId) {
	}

	protected String createCode(String token) {
		return null;
	}

}

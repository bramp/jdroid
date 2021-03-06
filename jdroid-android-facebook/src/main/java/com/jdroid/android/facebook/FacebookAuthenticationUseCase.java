package com.jdroid.android.facebook;

import com.facebook.model.GraphUser;
import com.jdroid.android.usecase.AbstractUseCase;
import com.jdroid.android.utils.AndroidEncryptionUtils;
import com.jdroid.java.exception.UnexpectedException;
import com.jdroid.java.utils.LoggerUtils;
import com.jdroid.java.utils.StringUtils;

import org.slf4j.Logger;

public class FacebookAuthenticationUseCase extends AbstractUseCase {
	
	private static final long serialVersionUID = -2087611650376005398L;
	
	private static final Logger LOGGER = LoggerUtils.getLogger(FacebookAuthenticationUseCase.class);
	
	private FacebookConnector facebookConnector;
	private FacebookUser facebookUser;
	private Boolean loginMode = true;
	
	/**
	 * @see com.jdroid.android.usecase.AbstractUseCase#doExecute()
	 */
	@Override
	protected void doExecute() {
		
		if (loginMode) {
			facebookUser = getFacebookUserFromCache();
			if (facebookUser == null) {
				
				GraphUser fbUser = facebookConnector.executeMeRequest();
				String accessToken = facebookConnector.getAccessToken();
				
				if (fbUser == null) {
					throw new UnexpectedException("Failed to get GraphUser from Facebook");
				}
				
				facebookUser = new FacebookUser();
				facebookUser.setFirstName(fbUser.getFirstName());
				facebookUser.setLastName(fbUser.getLastName());
				facebookUser.setFacebookId(fbUser.getId());
				Object email = fbUser.asMap().get("email");
				if (email != null) {
					facebookUser.setEmail(email.toString());
				}
				sendFacebookLogin(facebookUser, accessToken);
				FacebookPreferencesUtils.saveFacebookUser(accessToken, facebookUser);
			} else {
				LOGGER.debug("facebookUserInfo from cache facebookUserInfo= " + facebookUser);
			}
			afterFacebookLogin();
		} else {
			FacebookPreferencesUtils.cleanFacebookUser();
			afterFacebookLogout();
		}
	}
	
	protected void sendFacebookLogin(FacebookUser facebookUser, String token) {
		// Do Nothing
	}
	
	protected void afterFacebookLogin() {
		// Do Nothing
	}
	
	protected void afterFacebookLogout() {
		// Do Nothing
	}
	
	public void setFacebookConnector(FacebookConnector facebookConnector) {
		this.facebookConnector = facebookConnector;
	}
	
	public FacebookUser getFacebookUser() {
		return facebookUser;
	}
	
	private FacebookUser getFacebookUserFromCache() {
		FacebookUser facebookUser = null;
		String accessToken = facebookConnector.getAccessToken();
		if ((accessToken != null) && StringUtils.isNotBlank(accessToken)) {
			String accessTokenHash = AndroidEncryptionUtils.generateShaHash(accessToken);
			LOGGER.debug(" accessTokenHash=" + accessTokenHash);
			String savedAccessTokenHash = FacebookPreferencesUtils.loadFacebookAccessTokenHashFromPreferences();
			if (accessTokenHash.equals(savedAccessTokenHash)) {
				facebookUser = FacebookPreferencesUtils.loadFacebookUser();
			}
		}
		return facebookUser;
	}
	
	public void setLoginMode(Boolean loginMode) {
		this.loginMode = loginMode;
	}
	
	public Boolean isLoginMode() {
		return loginMode;
	}
	
}

package com.pig4cloud.pig.auth.support;

import cn.dev33.satoken.oauth2.data.loader.SaOAuth2DataLoader;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.hutool.core.util.StrUtil;
import com.pig4cloud.pig.admin.api.entity.SysOauthClientDetails;
import com.pig4cloud.pig.admin.api.feign.RemoteClientDetailsService;
import com.pig4cloud.pig.common.core.util.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 加载 OAuth2 客户端相关数据
 *
 * @author lengleng
 * @date 2024/11/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaOAuth2ClientDataLoaderImpl implements SaOAuth2DataLoader {

	private final RemoteClientDetailsService remoteClientDetailsService;

	/**
	 * 根据 id 获取 Client 信息
	 * @param clientId 应用id
	 * @return ClientModel
	 */
	@Override
	public SaClientModel getClientModel(String clientId) {
		R<SysOauthClientDetails> clientDetailsById = remoteClientDetailsService.getClientDetailsById(clientId);

		SysOauthClientDetails clientDetailsByIdData = clientDetailsById.getData();
		if (Objects.nonNull(clientDetailsByIdData)) {
			SaClientModel saClientModel = new SaClientModel();
			saClientModel.setClientId(clientDetailsByIdData.getClientId())
				.setClientSecret(clientDetailsByIdData.getClientSecret())
				.setContractScopes(List.of(clientDetailsByIdData.getScope()))
				.addAllowGrantTypes(clientDetailsByIdData.getAuthorizedGrantTypes())
				.setAccessTokenTimeout(clientDetailsByIdData.getAccessTokenValidity())
				.setIsNewRefresh(true) // 是否生成新的refreshToken
				.setRefreshTokenTimeout(clientDetailsByIdData.getRefreshTokenValidity());

			if (StrUtil.isNotBlank(clientDetailsByIdData.getWebServerRedirectUri())) {
				saClientModel.setAllowRedirectUris(
						Arrays.stream(clientDetailsByIdData.getWebServerRedirectUri().split(StrUtil.COMMA)).toList());
			}

			return saClientModel;
		}
		return null;
	}

}

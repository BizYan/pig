package com.pig4cloud.pig.auth.support.handle;

import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.request.RequestAuthModel;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.granttype.handler.SaOAuth2GrantTypeHandlerInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.pig4cloud.pig.admin.api.dto.UserDTO;
import com.pig4cloud.pig.admin.api.dto.UserInfo;
import com.pig4cloud.pig.admin.api.feign.RemoteUserService;
import com.pig4cloud.pig.common.core.constant.SecurityConstants;
import com.pig4cloud.pig.common.core.util.R;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 移动授权类型处理
 *
 * @author lengleng
 * @date 2024/07/23
 */
@Service
public class MobileGrantTypeHandle implements SaOAuth2GrantTypeHandlerInterface {

	/**
	 * 获取所要处理的 GrantType mobile 类型
	 * @return /
	 */
	@Override
	public String getHandlerGrantType() {
		return SecurityConstants.SMS_PARAMETER_NAME;
	}

	/**
	 * 获取 AccessTokenModel 对象
	 * @param req /
	 * @param clientId
	 * @param scopes
	 * @return /
	 */
	@Override
	public AccessTokenModel getAccessToken(SaRequest req, String clientId, List<String> scopes) {
		String mobile = req.getParamNotNull(SecurityConstants.SMS_PARAMETER_NAME);

		UserDTO query = new UserDTO();
		query.setPhone(mobile);
		R<UserInfo> infoR = SpringUtil.getBean(RemoteUserService.class).info(query);

		if (Objects.isNull(infoR.getData())) {
			throw new SaOAuth2Exception("手机号不存在");
		}

		StpUtil.login(infoR.getData().getSysUser().getUsername());

		// 4、构建 ra对象
		RequestAuthModel ra = new RequestAuthModel();
		ra.clientId = clientId;
		ra.loginId = StpUtil.getLoginId();
		ra.scopes = scopes;

		return SaOAuth2Manager.getDataGenerate().generateAccessToken(ra, true);
	}

}

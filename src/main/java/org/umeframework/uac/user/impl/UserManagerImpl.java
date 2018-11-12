/*
 * Copyright 2014-2017 UME Framework Group, Apache License Version 2.0
 */
package org.umeframework.uac.user.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.umeframework.dora.exception.ApplicationException;
import org.umeframework.dora.service.BaseDBComponent;
import org.umeframework.dora.util.CodecUtil;
import org.umeframework.dora.util.StringUtil;
import org.umeframework.uac.entity.UmeUserDto;
import org.umeframework.uac.entity.crud.UmeUserCrudService;
import org.umeframework.uac.message.MessageConst;
import org.umeframework.uac.user.UserManager;

/**
 * 用户信息管理服务实现类。<br>
 * 
 * 
 * @author YUE MA
 *
 */
@Service
public class UserManagerImpl extends BaseDBComponent implements UserManager, MessageConst {
	/**
	 * userService
	 */
	@Resource
	UmeUserCrudService userService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.umeframework.uac.user.UserManager#changePassword(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	synchronized public void changePassword(String userId, String userPassword, String newPassword) {
		UmeUserDto user = new UmeUserDto();
		user.setUserId(userId);
		UmeUserDto exist = userService.find(user);
		if (exist == null) {
			throw new ApplicationException(UME_UAC_MSG_001);
		}
		if (!exist.getUserPassword().equals(CodecUtil.encodeMD5Hex(exist.getUserPassword()))) {
			throw new ApplicationException(UME_UAC_MSG_002);
		}
		user.setUserPassword(newPassword);
		userService.update(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.umeframework.uac.user.UserManager#createUser(org.umeframework.uac.entity.UmeUserDto)
	 */
	@Override
	synchronized public void createUser(UmeUserDto user) {
		String userPassword = user.getUserPassword();
		if (StringUtil.isEmpty(userPassword)) {
			throw new ApplicationException(UME_UAC_MSG_002);
		}
		user.setUserPassword(CodecUtil.encodeMD5Hex(userPassword));
		userService.create(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.umeframework.uac.user.UserManager#updateUser(org.umeframework.uac.entity.UmeUserDto)
	 */
	@Override
	public void updateUser(UmeUserDto user) {
		UmeUserDto param = new UmeUserDto();
		param.setUserId(user.getUserId());
		UmeUserDto exist = userService.find(param);
		if (exist == null) {
			throw new ApplicationException(UME_UAC_MSG_001);
		}
		if (!exist.getUserPassword().equals(CodecUtil.encodeMD5Hex(user.getUserPassword()))) {
			throw new ApplicationException(UME_UAC_MSG_002);
		}
		userService.update(user);
	}

}

/*
 * Copyright 2014-2017 UME Framework Group, Apache License Version 2.0
 */
package org.umeframework.uac.user.impl;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.umeframework.dora.bean.BeanUtil;
import org.umeframework.dora.service.BaseDBComponent;
import org.umeframework.dora.service.TableObject;
import org.umeframework.dora.web.user.UserAuthenticator;
import org.umeframework.uac.entity.UmeResourceDto;
import org.umeframework.uac.entity.UmeRoleAclDto;
import org.umeframework.uac.entity.UmeRoleDto;
import org.umeframework.uac.entity.UmeUserDto;
import org.umeframework.uac.message.MessageConst;
import org.umeframework.uac.user.dto.UserAclDto;

/**
 * 用户鉴权基础逻辑封装类。<br>
 * 
 * @author Yue Ma
 */
public abstract class BaseAuthenticator<USER_DTO> extends BaseDBComponent implements UserAuthenticator<UserAclDto>, MessageConst {
	/**
	 * UME_ROLE_ACL表中ACC_RES_ID属性使用的通配符。<br>
	 */
	private static final String WILDCARD_ACC_RES_ID = "*";
	/**
	 * 查询鉴权用户ACL的SQLID。<br>
	 */
	private String sqlFindUserACL = "org.umeframework.uac.entity.SEARCH_USER_ACL";
	/**
	 * 查询默认ACL的SQLID。<br>
	 */
	private String sqlFindUserDefaultACL = "org.umeframework.uac.entity.SEARCH_ALL_RESOURCE_AS_USER_ACL";
	/**
	 * 业务用户实体中可当作UID使用的属性ID。<br>
	 */
	private String keyUID = UmeUserDto.Property.userId;

	/**
	 * 根据loginId，loginPassword等获取业务用户对象实体并返回。<br>
	 * 
	 * @param loginId
	 * @param loginPassword
	 * @param options
	 * @return
	 */
	abstract public USER_DTO findActualUser(String loginId, String loginPassword, String... options);

	/**
	 * 根据loginId，loginPassword等进行用户鉴权，并对获取用户进行授权处理后，返回UserAuthDto实体。<br>
	 * 
	 * @see org.umeframework.dora.web.user.UserAuthenticator#doAuthentication(java.lang.String, java.lang.String, java.lang.String[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public UserAclDto doAuthentication(String loginId, String loginPassword, String... options) {
		USER_DTO bizUser = findActualUser(loginId, loginPassword, options);
		Map<String, Object> bizUserMap = null;

		if (bizUser instanceof Map) {
			bizUserMap = (Map<String, Object>) bizUser;
			if (bizUserMap.containsKey(UmeUserDto.Property.userPassword)) {
				bizUserMap.remove(UmeUserDto.Property.userPassword);
			}
			if (bizUserMap.containsKey(UmeUserDto.Property.userStatus)) {
				bizUserMap.remove(UmeUserDto.Property.userStatus);
			}
			if (bizUserMap.containsKey(UmeUserDto.Property.createAuthor)) {
				bizUserMap.remove(UmeUserDto.Property.createAuthor);
			}
			if (bizUserMap.containsKey(UmeUserDto.Property.createDatetime)) {
				bizUserMap.remove(UmeUserDto.Property.createDatetime);
			}
			if (bizUserMap.containsKey(UmeUserDto.Property.updateAuthor)) {
				bizUserMap.remove(UmeUserDto.Property.updateAuthor);
			}
			if (bizUserMap.containsKey(UmeUserDto.Property.updateDatetime)) {
				bizUserMap.remove(UmeUserDto.Property.updateDatetime);
			}
		} else if (bizUser instanceof TableObject) {
			((TableObject) bizUser).clearDefaultProperties();
			try {
				BeanUtil.setBeanProperty(bizUser, UmeUserDto.Property.userPassword, null);
			} catch (Exception e) {
				// Ignore set exception here
			}
		}
		// Prepare return instance
		UserAclDto userAcl = new UserAclDto();
		userAcl.setUser(bizUser);
		Object uid = null;
		try {
			uid = BeanUtil.getBeanProperty(bizUser, this.getKeyUID());
		} catch (Exception e) {
			// Ignore set exception here
		}
		if (uid != null) {
			userAcl.setUid(uid.toString());
		}

		return userAcl;
	}

	/**
	 * 用户授权处理。<br>
	 * 根据"用户角色关系表"，"角色表"，"资源表"，"角色资源关系表"，查询取得鉴权用户可访问的所有资源信息。<br>
	 * 
	 * @param userAclObj
	 * @param loginOptions
	 */
	@Override
	public void doAuthorization(UserAclDto userAclObj, String... loginOptions) {
		// prepare
		Set<String> roleList = new LinkedHashSet<String>();
		Set<Integer> accResTypeList = new LinkedHashSet<Integer>();
		Map<String, Map<String, Object>> accResMapA = new LinkedHashMap<String, Map<String, Object>>();
		Map<String, Map<String, Object>> accResMapB = new LinkedHashMap<String, Map<String, Object>>();

		// Query user accessible resource list by UID
		List<Map<String, Object>> accResList = getDao().queryForMapList(sqlFindUserACL, userAclObj.getUid());

		for (Map<String, Object> e : accResList) {
			String roleId = (String) e.get(UmeRoleDto.Property.roleId);
			Integer resType = (Integer) e.get(UmeResourceDto.Property.resType);
			String accResId = (String) e.get(UmeRoleAclDto.Property.accResId);

			if (accResId.contains("*")) {
				// mark and skip resource which contains '*'(such as 'image*') in 'UME_ROLE_ACL.ACC_RES_ID'
				this.saveAs(accResMapB, e);
				continue;
			}
			this.saveAs(accResMapA, e);
			roleList.add(roleId);
			accResTypeList.add(resType);
		}

		if (accResMapB.size() > 0) {
			for (Map.Entry<String, Map<String, Object>> entry : accResMapB.entrySet()) {
				String key = entry.getKey().replace(WILDCARD_ACC_RES_ID, "");
				Map<String, Object> value = entry.getValue();
				String roleId = (String) value.get(UmeRoleAclDto.Property.roleId);
				Integer accLevel = (Integer) value.get(UmeRoleAclDto.Property.accLevel);
				accResList = getDao().queryForMapList(sqlFindUserDefaultACL, key);
				for (Map<String, Object> e : accResList) {
					e.put(UmeRoleDto.Property.roleId, roleId);
					e.put(UmeRoleAclDto.Property.accLevel, accLevel);
					// e.put(UmeRoleAclDto.Property.accResId, e.get(UmeResourceDto.Property.resId));
					this.saveAs(accResMapA, e);
					Integer resType = (Integer) e.get(UmeResourceDto.Property.resType);
					accResTypeList.add(resType);
				}
			}
		}

		Set<Map<String, Object>> allUserAclList = new LinkedHashSet<Map<String, Object>>(accResMapA.values());
		userAclObj.setAccResList(allUserAclList);
		userAclObj.setRoleList(roleList);
		userAclObj.setAccResTypeList(accResTypeList);
		// CollectionUtil.sortAsc(user.getUserAclSet());
	}

	/**
	 * 合并资源访问表。<br>
	 * 
	 * @param target
	 * @param current
	 */
	protected void saveAs(Map<String, Map<String, Object>> target, Map<String, Object> current) {
		String accResId = (String) current.get(UmeRoleAclDto.Property.accResId);
		Integer accLevel = (Integer) current.get(UmeRoleAclDto.Property.accLevel);
		if (!target.containsKey(accResId)) {
			target.put(accResId, current);
		} else {
			Map<String, Object> exist = target.get(accResId);
			// compute new access level and rewrite
			Integer existAccLevel = (Integer) exist.get(UmeRoleAclDto.Property.accLevel);
			Integer newAccLevel = caculateAccLevel(existAccLevel, accLevel);
			exist.put(UmeRoleAclDto.Property.accLevel, newAccLevel);
		}
	}

	/**
	 * 重新计算Access Level。<br>
	 * 
	 * @param existLevel
	 * @param currentLevel
	 * @return
	 */
	protected Integer caculateAccLevel(Integer existLevel, int currentLevel) {
		existLevel = existLevel | currentLevel;
		return existLevel;
	}

	/**
	 * @return the sqlFindUserACL
	 */
	public String getSqlFindUserACL() {
		return sqlFindUserACL;
	}

	/**
	 * @param sqlFindUserACL
	 *            the sqlFindUserACL to set
	 */
	public void setSqlFindUserACL(String sqlFindUserACL) {
		this.sqlFindUserACL = sqlFindUserACL;
	}

	/**
	 * @return the sqlFindUserDefaultACL
	 */
	public String getSqlFindUserDefaultACL() {
		return sqlFindUserDefaultACL;
	}

	/**
	 * @param sqlFindUserDefaultACL
	 *            the sqlFindUserDefaultACL to set
	 */
	public void setSqlFindUserDefaultACL(String sqlFindUserDefaultACL) {
		this.sqlFindUserDefaultACL = sqlFindUserDefaultACL;
	}

	/**
	 * @return the keyUID
	 */
	public String getKeyUID() {
		return keyUID;
	}

	/**
	 * @param keyUID
	 *            the keyUID to set
	 */
	public void setKeyUID(String keyUID) {
		this.keyUID = keyUID;
	}

}

package org.umeframework.uac.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.umeframework.dora.bean.BeanConfigConst;
import org.umeframework.dora.cache.CacheManager;
import org.umeframework.dora.cache.impl.TempMemoryCachedImpl;
import org.umeframework.dora.message.MessageProperties;
import org.umeframework.dora.message.impl.MessagePropertiesImpl;
import org.umeframework.dora.service.user.UserAuthenticator;
import org.umeframework.uac.user.dto.UserAclDto;
import org.umeframework.uac.user.impl.DefaultAuthenticatorImpl;

/**
 * Spring Boot启动用的主类<br>
 * 只需在该类的头部用注解标注的形式，即可启动一个内嵌Web服务器的Java项目<br>
 * <br>
 * Dora框架提供基于注解自动配置和基于XML配置两种方案，分别采用如下说明的方法来选择配置方式：<br>
 * <li>基于注解自动配置 - 在主类的头部添加"@ImportAutoConfiguration"设置
 * <li>基于注解自动配置 - 在主类的头部添加"@ImportResource"设置
 * <li>对于要从自动配置中排除的模块，使用"@EnableAutoConfiguration(exclude=...)"设置 <br>
 * <li>基于XML配置的场合，使用：@ImportResource("classpath:applicationContext.xml")<br>
 * <li>基于DORA自动配置，但不配置数据源的场合，使用：@ImportAutoConfiguration({ DoraAutoConfigurationNoDataSource.class})<br>
 * <br>
 * <br>
 * 当默认的自动配置类org.umeframework.dora.appconfig.AutoConfiguration被导入·启动时，<br>
 * <br>
 * 
 * @author Yue MA
 */
// 排除SpringBoot自带的数据源初始化设置，采用application.properties中使用ume.jdbc设置的数据源连接。
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class })
// 导入项目的自动化配置。
@Import({
        // 启动UME框架的默认配置
        // 启用AutoConfiguration内建的默认配置项目
        org.umeframework.dora.appconfig.AutoConfiguration.class })
// 该设置创建并导入示例用的数据库及数据脚本；如果应用(通过application.properties的ume.jdbc设置)已变更为其他的数据库，请删除此设置。
@ImportResource("classpath:config/ume-uac/initDataConfiguration.xml")
@ComponentScan(basePackages = "org.umeframework.uac")
public class Application {

	/**
	 * Start application.<br>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SpringApplication.run(Application.class, args);
		} catch (Throwable e) {
			if (!e.getClass().getName().startsWith("org.springframework.boot.devtools")) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * User authentication implementation class.<br>
	 * 
	 * @return
	 */
	@Bean(name = BeanConfigConst.DEFAULT_USER_AUTHENTICATOR)
	public UserAuthenticator<?> userAuthenticator() {
		UserAuthenticator<UserAclDto> instance = new DefaultAuthenticatorImpl();
		return instance;
	}

	/**
	 * accessCodeCacheManager
	 *
	 * @return
	 */
	@Bean(name = "accessCodeCacheManager", initMethod = "init", destroyMethod = "shutdown")
	public CacheManager accessCodeCacheManager() {
		TempMemoryCachedImpl instance = new TempMemoryCachedImpl(60);
		return instance;
	}

	@Bean(name = "oauthClientConfig")
	public MessageProperties oauthClientConfig() throws Exception {
		MessageProperties instance = new MessagePropertiesImpl("config/ume-uac/oauthClient");
		return instance;
	}

	@Bean(name = "oauthServerConfig")
	public MessageProperties oauthServerConfig() throws Exception {
		MessageProperties instance = new MessagePropertiesImpl("config/ume-uac/oauthServer");
		return instance;
	}
}

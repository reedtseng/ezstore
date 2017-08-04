package io.ezstore.authserver.config;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Value("${ezstore.auth.jwt.key-store}")
    private String jwtKeyStore;

    @Value("${ezstore.auth.jwt.key-store-password}")
    private String jwtKeyStorePassword;

    @Value("${ezstore.auth.jwt.key-alias}")
    private String jwtKeyAlias;
    
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore())
//        		.tokenServices(tokenServices())
                .authenticationManager(authenticationManager)
//                .accessTokenConverter(accessTokenConverter())
//                .userDetailsService(new UserDetailsServiceImpl())                
                .accessTokenConverter(accessTokenConverter())
                ;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
                .allowFormAuthenticationForClients()
                .tokenKeyAccess("hasAuthority('ROLE_TRUSTED_CLIENT')")
                .checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    	
    	clients
        .inMemory()
        .withClient("ezstore_portal").secret("secret")
        .authorizedGrantTypes("implicit","refresh_token", "password", "authorization_code")
        .scopes("ezstore").autoApprove(true)
        .accessTokenValiditySeconds(36000)
        .and()
        .withClient("ezstore_service").secret("secret")
        .authorizedGrantTypes("client_credentials", "refresh_token")
        .scopes("ezstore").autoApprove(true);;
    }
    
    @Bean
    TokenStore tokenStore() {
    	
    	return new JwtTokenStore(accessTokenConverter());
    }        
        
    @Bean
    JwtAccessTokenConverter accessTokenConverter() {
    	
    	final JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
    	KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(jwtKeyStore), jwtKeyStorePassword.toCharArray());
    	accessTokenConverter.setKeyPair(keyStoreKeyFactory.getKeyPair(jwtKeyAlias));
    	return accessTokenConverter;
    }
    
    @Bean
	@Primary
	public DefaultTokenServices tokenServices() {
    	
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		return defaultTokenServices;
	} 
}



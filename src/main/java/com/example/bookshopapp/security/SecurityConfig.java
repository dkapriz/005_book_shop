package com.example.bookshopapp.security;

import com.example.bookshopapp.config.BookShopConfig;
import com.example.bookshopapp.security.code.BookShopUserDetailsService;
import com.example.bookshopapp.security.code.UserContactAuthenticationFilter;
import com.example.bookshopapp.security.code.UserContactAuthenticationProvider;
import com.example.bookshopapp.security.jwt.JWTLogoutHandler;
import com.example.bookshopapp.security.jwt.JWTRequestFilter;
import com.example.bookshopapp.security.oauth.OAuthAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final BookShopUserDetailsService bookShopUserDetailsService;
    private final JWTRequestFilter filter;
    private final JWTLogoutHandler logoutHandler;
    private final OidcUserService oidcUserService;

    @Autowired
    public SecurityConfig(BookShopUserDetailsService bookShopUserDetailsService, JWTRequestFilter filter,
                          JWTLogoutHandler logoutHandler, OidcUserService oidcUserService) {
        this.bookShopUserDetailsService = bookShopUserDetailsService;
        this.filter = filter;
        this.logoutHandler = logoutHandler;
        this.oidcUserService = oidcUserService;
    }

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(bookShopUserDetailsService)
                .passwordEncoder(getPasswordEncoder());
    }

    @Bean
    public OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler() {
        return new OAuthAuthenticationSuccessHandler();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        UserContactAuthenticationFilter userContactAuthenticationFilter = new UserContactAuthenticationFilter();
        userContactAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        UserContactAuthenticationProvider userContactAuthenticationProvider = new UserContactAuthenticationProvider();
        userContactAuthenticationProvider.setUserDetailsService(bookShopUserDetailsService);
        http
                .authenticationProvider(userContactAuthenticationProvider)
                .addFilterAfter(userContactAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/my/**", "/profile",
                        "/order", "/books/viewed", "/books/download/**").authenticated() //hasRole("USER")
                .antMatchers("/**").permitAll()
                .and()
                .formLogin()
                .loginPage(BookShopConfig.LOGIN_URI)
                .failureUrl(BookShopConfig.LOGIN_URI)
                .and()
                .logout()
                .addLogoutHandler(logoutHandler)
                .logoutUrl(BookShopConfig.LOGOUT_URI)
                .logoutSuccessUrl(BookShopConfig.LOGIN_URI)
                .deleteCookies("token")
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(oidcUserService)
                .and()
                .successHandler(oAuthAuthenticationSuccessHandler());

        http
                .addFilterBefore(filter, UserContactAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
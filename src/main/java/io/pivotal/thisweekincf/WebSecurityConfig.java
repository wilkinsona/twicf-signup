package io.pivotal.thisweekincf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import javax.sql.DataSource;


/**
 * Created by duncwinn on 24/11/14.
 * Spring Security Configuration
 * configure permits specific routes, I'm using a login page but this is not required.
 * configureGlobal provides the auth
 *
 */

@Configuration
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    //DataSource is required for jdbcAuthentication; this is picked up via application.yml
    @Autowired
    DataSource ds;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //JS resources require user to be authenticated in order to access them, so include webjars in permitAll
        //every other page will need to be authenticated
        http
            .authorizeRequests()
                .antMatchers("/", "/confirmation","/reconfirmation","/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        //In memory auth is for demo/test purposes.. it's a little dodge to expose creds in an app so use a DB
//        auth
//            .inMemoryAuthentication()
//                .withUser("duncwinn").password("letme1n").roles("USER");

        //first query is authentication,
        //second query is authorization
        //passwordEncoder tells auth to allow cleartext password vs the expected sha256.
        auth
                .jdbcAuthentication().dataSource(ds)
                .usersByUsernameQuery("SELECT email_address AS username, password, TRUE FROM subscription WHERE email_address=? ")
                .authoritiesByUsernameQuery("select email_address AS username, role from subscription WHERE email_address=?")
                .passwordEncoder(new PlaintextPasswordEncoder());
                //For ease, cheat and populate DB via V1_subscription DDL; should be populated independent to app via DBA.

        ///General Notes////////////////////////////////////////////////////////////////////////////////////////////////
        //For testing SQL use SQUirrle -
        //copy libs form .m2: cp postgresql-9.1-901.jdbc4.jar /Applications/SQuirreLSQL.app/Contents/Resources/Java/lib/
        //create user: create user postgres;
        //create db: create database twicfdb;
        //logon:  psql -U postgres twicfdb
        //then grant privileges for user postgres: grant all on subscription to postgres;
        //exit: ctrl d

    }
}
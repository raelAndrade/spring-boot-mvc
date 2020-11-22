package curso.springboot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import curso.springboot.service.impl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsServiceImpl userDetailsServiceImpl;

	@Override // Configura as solicitações de acesso por http
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf()
			.disable() // Desativa configurações padrão de memória
			.authorizeRequests() // Permitir restrigir acessos
			.antMatchers(HttpMethod.GET, "/").permitAll() // Qualquer usuário acessa a página inicial
			.antMatchers(HttpMethod.GET, "/cadastropessoa").hasAnyRole("ADMIN") // Só pode acessar essa url que tem o papel de ADMIN
			.anyRequest().authenticated()
			.and().formLogin().permitAll() // Permite qualquer usuário
			.loginPage("/login")
			.defaultSuccessUrl("/cadastropessoa")
			.failureUrl("/login?error=true")
			.and().logout().logoutSuccessUrl("/login") // Mapeia URL de Logout e invalida usuário autenticado
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
	}
	
//	@Override // Cria autenticação do usuário em memoria 
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().passwordEncoder(NoOpPasswordEncoder.getInstance())
//		.withUser("israel")
//		.password("123")
//		.roles("ADMIN");
//	}
	
	// Método usando codificador
//	@Override // Cria autenticação do usuário em memoria 
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
//		.withUser("israel")
//		.password("$2a$10$559.KcK58KvnUtm4AH.PJu8wxRnODcHrcfgLakP3DPcLMbQZzw67S")
//		.roles("ADMIN");
//	}
	
	// Método usando banco de dados para autenticar usuario
	@Override // Cria autenticação do usuário com banco de dados
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsServiceImpl)
		.passwordEncoder(new BCryptPasswordEncoder());
	}
	
	@Override // Ignora URL especificas
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/materialize/**", "/css/**", "/images/**", "/**.html");
	}
	
}

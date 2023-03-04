## 不清楚的问题 todo

1.鉴权spel是如何判断ture和false的

2.注解里的ture和false是什么时候判断的



## 1、APi详解

 ### 1.1 依赖

```xml
  <dependencies>
        <!-- 实现对 Spring MVC 的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 实现对 Spring Security 的自动化配置 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
    </dependencies>
```

### 1.2 配置文件

```xml
spring:
  # Spring Security 配置项，对应 SecurityProperties 配置类
  security:
    # 配置默认的 InMemoryUserDetailsManager 的用户账号与密码。
    user:
      name: user # 账号
      password: user # 密码
      roles: ADMIN # 拥有角色
```

### 1.3 SecurityConfig 配置

**老版本是继承`WebSecurityConfigurerAdapter ` **  

**新版是 加上`@AutoConfiguration`注解以及`@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)`**

```java
// SecurityConfig.java

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.
            // <X> 使用内存中的 InMemoryUserDetailsManager
            inMemoryAuthentication()
            // <Y> 不使用 PasswordEncoder 密码编码器
            .passwordEncoder(NoOpPasswordEncoder.getInstance())
            // <Z> 配置 admin 用户
            .withUser("admin").password("admin").roles("ADMIN")
            // <Z> 配置 normal 用户
            .and().withUser("normal").password("normal").roles("NORMAL");
}


// SecurityConfig.java

@Override
protected void configure(HttpSecurity http) throws Exception {
    http
            // <X> 配置请求地址的权限
            .authorizeRequests()
                .antMatchers("/test/echo").permitAll() // 所有用户可访问
                .antMatchers("/test/admin").hasRole("ADMIN") // 需要 ADMIN 角色
                .antMatchers("/test/normal").access("hasRole('ROLE_NORMAL')") // 需要 NORMAL 角色。
                // 任何请求，访问的用户都需要经过认证
                .anyRequest().authenticated()
            .and()
            // <Y> 设置 Form 表单登录
            .formLogin()
//                    .loginPage("/login") // 登录 URL 地址
                .permitAll() // 所有用户可访问
            .and()
            // 配置退出相关
            .logout()
//                    .logoutUrl("/logout") // 退出 URL 地址
                .permitAll(); // 所有用户可访问
}
```

- `<X>` 处，调用 `HttpSecurity#authorizeRequests()` 方法，开始配置 URL 的**权限控制**。注意看配置的**四个**权限控制的配置。下面，是配置权限控制会使用到的方法：
  - `#(String... antPatterns)` 方法，配置匹配的 URL 地址，基于 [Ant 风格路径表达式](https://blog.csdn.net/songdexv/article/details/7219686) ，可传入多个。
  - 【常用】`#permitAll()` 方法，所有用户可访问。
  - 【常用】`#denyAll()` 方法，所有用户不可访问。
  - 【常用】`#authenticated()` 方法，登录用户可访问。
  - `#anonymous()` 方法，无需登录，即匿名用户可访问。
  - `#rememberMe()` 方法，通过 [remember me](https://docs.spring.io/spring-security/site/docs/3.0.x/reference/remember-me.html) 登录的用户可访问。
  - `#fullyAuthenticated()` 方法，非 [remember me](https://docs.spring.io/spring-security/site/docs/3.0.x/reference/remember-me.html) 登录的用户可访问。
  - `#hasIpAddress(String ipaddressExpression)` 方法，来自指定 IP 表达式的用户可访问。
  - 【常用】`#hasRole(String role)` 方法， 拥有指定角色的用户可访问。
  - 【常用】`#hasAnyRole(String... roles)` 方法，拥有指定任一角色的用户可访问。
  - 【常用】`#hasAuthority(String authority)` 方法，拥有指定权限(`authority`)的用户可访问。
  - 【常用】`#hasAuthority(String... authorities)` 方法，拥有指定任一权限(`authority`)的用户可访问。
  - 【最牛】`#access(String attribute)` 方法，当 [Spring EL 表达式](https://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/html/expressions.html)的执行结果为 `true` 时，可以访问。
- `<Y>` 处，调用 `HttpSecurity#formLogin()` 方法，设置 Form 表单**登录**。
  - 如果胖友想要自定义登录页面，可以通过 `#loginPage(String loginPage)` 方法，来进行设置。不过这里我们希望像[「2. 快速入门」](https://www.iocoder.cn/Spring-Boot/Spring-Security/?self#)一样，使用默认的登录界面，所以不进行设置。
- `<Z>` 处，调用 `HttpSecurity#logout()` 方法，配置**退出**相关。
  - 如果胖友想要自定义退出页面，可以通过 `#logoutUrl(String logoutUrl)` 方法，来进行设置。不过这里我们希望像[「2. 快速入门」](https://www.iocoder.cn/Spring-Boot/Spring-Security/?self#)一样，使用默认的退出界面，所以不进行设置。

## 2 认证分析

![请添加图片描述](https://img-blog.csdnimg.cn/0bba28e346604d588c089ffae3d2119b.png)



![image-20230223101614278](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20230223101614278.png)

#### 2.1 Authentication

它用于存储登录信息

```java
public interface Authentication extends Principal, Serializable {
    //权限
    Collection<? extends GrantedAuthority> getAuthorities();
	//密码
    Object getCredentials();
	//细节信息 比如webAuthenticationDetails，它记录了访问者的ip地址和sessionid的值
    Object getDetails();
    //身份信息，比如放回UserDetails（自带实现类）接口的实现类
    Object getPrincipal();
	//是否以及认证
    boolean isAuthenticated();
	
    void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
```

#### 2.2 UsernamePasswordAuthenticationToken

继承于 AbstractAuthenticationToken 实现了 Authentication接口，封装用户信息

#### 2.3 SecurityContextHolder

安全上下文容器，将Authentication填充进去，填充也可以获取。

``securityContextHolder.getContext().setAuthentication(…)``

#### 2.4 **AuthenticationManager **

是接口，身份管理器。方法authenticate就是进行验证可以进行不同验证方式（比如密码、手势、人脸等）的实现。传入一个必要信息，返回一个完整的信息，包括权限，身份，细节等。密码是不能返回的。

```java
public interface AuthenticationManager {
    Authentication authenticate(Authentication authentication) throws AuthenticationException;
}
```

为了该段解释写一段代码

```java
public class AuthenticationExample {
private static AuthenticationManager am = new SampleAuthenticationManager();

	public static void main(String[] args) throws Exception {
        //创建实现了Authentication接口的UsernamePasswordAuthentication
		Authentication request = new UsernamePasswordAuthenticationToken(name, password);
        //将信息通过实现了AuthenticationManager接口的实现类进行验证并且返回具体的数据
		Authentication result = am.authenticate(request);
        //将返回的数据写入上下文便于后面调用
		SecurityContextHolder.getContext().setAuthentication(result);
	}
	class SampleAuthenticationManager implements AuthenticationManager {
        static final List<GrantedAuthority> AUTHORITIES = new ArrayList<GrantedAuthority>();

		static {
			AUTHORITIES.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		public Authentication authenticate(Authentication auth) throws AuthenticationException {
			if (auth.getName().equals(auth.getCredentials())) {
			return new UsernamePasswordAuthenticationToken(auth.getName(),
			auth.getCredentials(), AUTHORITIES);
		}
		
	}
}
```

#### 2.5   ProviderManager

ProviderManager是AuthenticationManager接口的常用实现类，内部会维护一个``List<AuthenticationProvider>``,存放多种认证方式

每一个AuthenticationProvier都包含一个Authentication，每个AuthenticationProvider的实现类都对应着一种认证方式

```
public interface AuthenticationProvider {
    Authentication authenticate(Authentication authentication) throws AuthenticationException;

    boolean supports(Class<?> authentication);
}
```

接下来为ProviderManager关键代码

```java
public class ProviderManager implements AuthenticationManager, MessageSourceAware,
		InitializingBean {

    // 维护一个AuthenticationProvider列表
    private List<AuthenticationProvider> providers = Collections.emptyList();

    public Authentication authenticate(Authentication authentication)
          throws AuthenticationException {
       Class<? extends Authentication> toTest = authentication.getClass();
       AuthenticationException lastException = null;
       Authentication result = null;

       // 依次认证
       for (AuthenticationProvider provider : getProviders()) {
          if (!provider.supports(toTest)) {
             continue;
          }
          try {
              //代码关键，由authentication传入进行验证并且返回authentication
             result = provider.authenticate(authentication);

             if (result != null) {
                copyDetails(authentication, result);
                break;
             }
          }
          ...
          catch (AuthenticationException e) {
             lastException = e;
          }
       }
       // 如果有Authentication信息，则直接返回
       if (result != null) {
			if (eraseCredentialsAfterAuthentication
					&& (result instanceof CredentialsContainer)) {
              	 //移除密码
				((CredentialsContainer) result).eraseCredentials();
			}
             //发布登录成功事件
			eventPublisher.publishAuthenticationSuccess(result);
			return result;
	   }
	   ...
       //执行到此，说明没有认证成功，包装异常信息
       if (lastException == null) {
          lastException = new ProviderNotFoundException(messages.getMessage(
                "ProviderManager.providerNotFound",
                new Object[] { toTest.getName() },
                "No AuthenticationProvider found for {0}"));
       }
       prepareException(lastException, authentication);
       throw lastException;
    }
}
```

#### 2.6  DaoAuthenticationProvider

AuthenticationProvider的实现类

DaoAuthenticationProvider 是数据访问层的实现类，即这个身份认证器是通过数据库的数据比对

![DaoAuthenticationProvider UML](http://kirito.iocoder.cn/QQ%E5%9B%BE%E7%89%8720170919204228.png)

这是UML图其中 ``userDetailsService``是接口，其中函数``loadUserByUsername``就是通过数据库查询来获取账户名和密码，后续这个密码会通过``DaoAuthenticationProvider``的``retrieveUser``进行获取并且通过``additionalAuthenticationChecks``进行校对

**UserDetailsService常见的实现类有JdbcDaoImpl，InMemoryUserDetailsManager，前者从数据库加载用户，后者从内存中加载用户，也可以自己实现UserDetailsService，通常这更加灵活。**



```java
public interface UserDetailsService { 
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
public interface UserDetails extends Serializable {

   Collection<? extends GrantedAuthority> getAuthorities();

   String getPassword();

   String getUsername();

   boolean isAccountNonExpired();

   boolean isAccountNonLocked();

   boolean isCredentialsNonExpired();

   boolean isEnabled();
}
```

获取和校对的代码如下

```java
//获取代码
protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        this.prepareTimingAttackProtection();

        try {
            //调用loadUserByUsername获取信息
            UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(username);
            if (loadedUser == null) {
                throw new InternalAuthenticationServiceException("UserDetailsService returned null, which is an interface contract violation");
            } else {
                return loadedUser;
            }
        } catch (UsernameNotFoundException var4) {
            this.mitigateAgainstTimingAttack(authentication);
            throw var4;
        } catch (InternalAuthenticationServiceException var5) {
            throw var5;
        } catch (Exception var6) {
            throw new InternalAuthenticationServiceException(var6.getMessage(), var6);
        }
    }
//校验代码
protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
    if (authentication.getCredentials() == null) {
        this.logger.debug("Failed to authenticate since no credentials provided");
        throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
    } else {
        //重点如下，通过用户传递过来的密码与数据库的密码进行对比
        String presentedPassword = authentication.getCredentials().toString();
        if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            this.logger.debug("Failed to authenticate since password does not match stored value");
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }
}
```

#### 2.7 整体架构图

![架构概览图](http://kirito.iocoder.cn/spring%20security%20architecture.png)

## 3、授权源码分析

授权过程执行``FilterSecurityInterceptor``的``doFilter``函数。接着是调用``invoke``函数

```java
public void invoke(FilterInvocation filterInvocation) throws IOException, ServletException {
    if (this.isApplied(filterInvocation) && this.observeOncePerRequest) {
        filterInvocation.getChain().doFilter(filterInvocation.getRequest(), filterInvocation.getResponse());
    } else {
        if (filterInvocation.getRequest() != null && this.observeOncePerRequest) {
            filterInvocation.getRequest().setAttribute("__spring_security_filterSecurityInterceptor_filterApplied", Boolean.TRUE);
        }
		//重点是这句在调用之前
        InterceptorStatusToken token = super.beforeInvocation(filterInvocation);

        try {
            filterInvocation.getChain().doFilter(filterInvocation.getRequest(), filterInvocation.getResponse());
        } finally {
            super.finallyInvocation(token);
        }

        super.afterInvocation(token, (Object)null);
    }
}
```

beforeInvocation方法

```java
protected InterceptorStatusToken beforeInvocation(Object object) {
    //找到配置文件中的需要的规则权限 如果配置了 .authorizeRequests().anyRequest().authenticated() 则会增加一栏 AnyRequestMatcher
	//匹配到的值是authenticated
     Collection<ConfigAttribute> attributes = this.obtainSecurityMetadataSource().getAttributes(object);
    //从SecurityContextHolder获取相应的Authentication
    Authentication authenticated = this.authenticateIfRequired();
    //尝试授权 Object：请求的所有信息  attributes：配置文件中的需要的规则权限   authenticated：用户的权限
    this.attemptAuthorization(object, attributes, authenticated);

}
```

``attemptAuthorization``

```
private void attemptAuthorization(Object object, Collection<ConfigAttribute> attributes, Authentication authenticated) {
    try {
    	//主要这句访问决策管理器，用来判断决定授权
        this.accessDecisionManager.decide(authenticated, object, attributes);
    } catch (AccessDeniedException var5) {
        if (this.logger.isTraceEnabled()) {
            this.logger.trace(LogMessage.format("Failed to authorize %s with attributes %s using %s", object, attributes, this.accessDecisionManager));
        } else if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Failed to authorize %s with attributes %s", object, attributes));
        }

        this.publishEvent(new AuthorizationFailureEvent(object, attributes, authenticated, var5));
        throw var5;
    }
}
```

``AccessDecisionManager`` 决策管理器接口

一共三个实现 AffirmativeBased、ConsensusBased、UnanimousBased

```java
public interface AccessDecisionManager {
    //实现授权
    void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException;

    boolean supports(ConfigAttribute attribute);

    boolean supports(Class<?> clazz);
}
```

``AffirmativeBased``

```
public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException {
    int deny = 0;
    Iterator var5 = this.getDecisionVoters().iterator();
	//其中1、2、3处表示只要有一个投票器为同意则直接return，如果都是拒绝（-1）则抛出异常
	
    while(var5.hasNext()) {
    	//获取投票器
        AccessDecisionVoter voter = (AccessDecisionVoter)var5.next();
        //根据投票器获取投票结果
        int result = voter.vote(authentication, object, configAttributes);
        switch(result) {
        case -1:
            ++deny;---------------------------2
            break;
        case 1: ------------------------------1
            return;
        }
    }

    if (deny > 0) {----------------------------3
        throw new AccessDeniedException(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
    } else {
        this.checkAllowIfAllAbstainDecisions();
    }
}
```

``ConsensusBased``

```java
public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException {
    int grant = 0;
    int deny = 0;
    Iterator var6 = this.getDecisionVoters().iterator();

    while(var6.hasNext()) {
        AccessDecisionVoter voter = (AccessDecisionVoter)var6.next();
        int result = voter.vote(authentication, object, configAttributes);
        switch(result) {
        case -1:
            //如果不同意则不同意票数+1
            ++deny;
            break;
        case 1:
            //如果同意则同意票数+1
            ++grant;
        }
    }
	//该实现类则是以多数赢少数的情况，
    //1----如果同意票数<否定票的话则直接抛异常
    //2----如果同意票数与否定票数相同的话且同意票数不为0的前提下，则根据allowIfEqualGrantedDeniedDecisions来判断（根据单词拆分就是是否同意当granted和deied相同时的决策，如果同意则代表两者相同时授予访问权限。不同意则抛出异常）
    if (grant <= deny) {
        if (deny > grant) { ------------1
            throw new AccessDeniedException(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
        } else if (grant == deny && grant != 0) {--------------2
            if (!this.allowIfEqualGrantedDeniedDecisions) {
                throw new AccessDeniedException(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
            }
        } else {
            this.checkAllowIfAllAbstainDecisions();
        }
    }
}
```

``UnanimousBased`` 一票否决权，如果有一个人不同意则直接否定

```java
public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) throws AccessDeniedException {
    int grant = 0;
    List<ConfigAttribute> singleAttributeList = new ArrayList(1);
    singleAttributeList.add((Object)null);
    Iterator var6 = attributes.iterator();

    while(var6.hasNext()) {
        ConfigAttribute attribute = (ConfigAttribute)var6.next();
        singleAttributeList.set(0, attribute);
        Iterator var8 = this.getDecisionVoters().iterator();

        while(var8.hasNext()) {
            AccessDecisionVoter voter = (AccessDecisionVoter)var8.next();
            int result = voter.vote(authentication, object, singleAttributeList);
            switch(result) {
            case -1: -----一票否决
                throw new AccessDeniedException(this.messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
            case 1:
                ++grant;
            }
        }
    }

    if (grant <= 0) { ---如果没有同意抛异常 
        this.checkAllowIfAllAbstainDecisions();
    }
}
```



``AccessDecisionVoter``投票器

实现该接口的有如下实现类 ``AbstractAclVoter``,``AuthenticatedVoter``,``Jsr250Voter``,``PreInvocationAuthorizationAdviceVoter``,``RoleVoter``,``RoleHierarchyVoter继承于RoloVoter``,``WebExpressionVoter``

```java
public interface AccessDecisionVoter<S> {
    int ACCESS_GRANTED = 1;//给与就是同意 	grantedg给与
    int ACCESS_ABSTAIN = 0;//弃权    		   abstain弃权
    int ACCESS_DENIED = -1;//否认				denied 拒绝、否认

    boolean supports(ConfigAttribute attribute);

    boolean supports(Class<?> clazz);
	投票方法
    int vote(Authentication authentication, S object, Collection<ConfigAttribute> attributes);
}
```

``WebExpressionVoter``

```java
    public int vote(Authentication authentication, FilterInvocation filterInvocation, Collection<ConfigAttribute> attributes) {
        Assert.notNull(authentication, "authentication must not be null");
        Assert.notNull(filterInvocation, "filterInvocation must not be null");
        Assert.notNull(attributes, "attributes must not be null");
        WebExpressionConfigAttribute webExpressionConfigAttribute = this.findConfigAttribute(attributes);
        if (webExpressionConfigAttribute == null) {
            this.logger.trace("Abstained since did not find a config attribute of instance WebExpressionConfigAttribute");
            return 0;
        } else 
            //重点看这边EvalutationContext 这边主要是设置上下文环境，它会引入相应的实体类
            EvaluationContext ctx = webExpressionConfigAttribute.postProcess(this.expressionHandler.createEvaluationContext(authentication, filterInvocation), filterInvocation);
            boolean granted = ExpressionUtils.evaluateAsBoolean(webExpressionConfigAttribute.getAuthorizeExpression(), ctx);
            if (granted) {
                return 1;
            } else {
                this.logger.trace("Voted to deny authorization");
                return -1;
            }
        }
    }
```

``AbstractSecurityExpressionHandler``

```
public final EvaluationContext createEvaluationContext(Authentication authentication, T invocation) {
	//讲操作类设置为上下文环境
    SecurityExpressionOperations root = this.createSecurityExpressionRoot(authentication, invocation);
    StandardEvaluationContext ctx = this.createEvaluationContextInternal(authentication, invocation);
    ctx.setBeanResolver(this.beanResolver);
    ctx.setRootObject(root);
    return ctx;
}
```

这为它的``SecurityExpressionOperations``操作类接口以及``SecurityExpressionRoot``实现类 

所以当你是没登陆 以``isAnymous``登录的时候，方法里会有``Authenticated``权限，他会匹配``isAuthenticated``方法进行校验是否有无权限

**这也是为啥当你在认证时拦截中不返回false 它后续也会报错的原因**

```java
//这为具体的操作类
public interface SecurityExpressionOperations {
    Authentication getAuthentication();

    boolean hasAuthority(String authority);

    boolean hasAnyAuthority(String... authorities);

    boolean hasRole(String role);

    boolean hasAnyRole(String... roles);

    boolean permitAll();

    boolean denyAll();

    boolean isAnonymous();

    boolean isAuthenticated();

    boolean isRememberMe();

    boolean isFullyAuthenticated();

    boolean hasPermission(Object target, Object permission);

    boolean hasPermission(Object targetId, String targetType, Object permission);
}

public abstract class SecurityExpressionRoot implements SecurityExpressionOperations {
	......
    public final boolean isAnonymous() {
        return this.trustResolver.isAnonymous(this.authentication);
    }
	//次方法主要是来判断是否是匿名访问的。
    public final boolean isAuthenticated() {
        return !this.isAnonymous();
    }

}
public class AuthenticationTrustResolverImpl implements AuthenticationTrustResolver {
    private Class<? extends Authentication> anonymousClass = AnonymousAuthenticationToken.class;
    private Class<? extends Authentication> rememberMeClass = RememberMeAuthenticationToken.class;

    public AuthenticationTrustResolverImpl() {
    }

	/// 这主要是判断this.anonymousClass.isAssignableFrom(authentication.getClass()) 判断这个类是否为AnonymousAuthenticationToken的继承类，如果不是则返回false 比如UsernamePasswordAuthenticationToken自定义则不是。
    public boolean isAnonymous(Authentication authentication) {
        return this.anonymousClass != null && authentication != null ? this.anonymousClass.isAssignableFrom(authentication.getClass()) : false;
    }

}
```

#### 授权的源码也是如此

在Controller的方法上会注释``@PreAuthorize("@ss.hasPermission('system:user:create')")``

前面的源码分析都是如此 都是在FilterSecurityInterception如此分析下来 最后在``AffirmativeBased``类下通过``PreInvocationAuthorizationAdviceVoter``下进行投票 具体分析此后面的流程

```
public int vote(Authentication authentication, MethodInvocation method, Collection<ConfigAttribute> attributes) {
    PreInvocationAttribute preAttr = this.findPreInvocationAttribute(attributes);
    if (preAttr == null) {
        return 0;
    } else {
    //重点关注这句
        return this.preAdvice.before(authentication, method, preAttr) ? 1 : -1;
    }
}

```

``ExpressionBasedPreInvocationAdvice``

```
public boolean before(Authentication authentication, MethodInvocation mi, PreInvocationAttribute attr) {
    PreInvocationExpressionAttribute preAttr = (PreInvocationExpressionAttribute)attr;
    EvaluationContext ctx = this.expressionHandler.createEvaluationContext(authentication, mi);
    Expression preFilter = preAttr.getFilterExpression();
    Expression preAuthorize = preAttr.getAuthorizeExpression();
    if (preFilter != null) {
        Object filterTarget = this.findFilterTarget(preAttr.getFilterTarget(), ctx, mi);
        this.expressionHandler.filter(filterTarget, preFilter, ctx);
    }

    return preAuthorize != null ? ExpressionUtils.evaluateAsBoolean(preAuthorize, ctx) : true;
}

public abstract class AbstractSecurityExpressionHandler<T> implements SecurityExpressionHandler<T>, ApplicationContextAware {
    public final EvaluationContext createEvaluationContext(Authentication authentication, T invocation) {
        SecurityExpressionOperations root = this.createSecurityExpressionRoot(authentication, invocation);
        StandardEvaluationContext ctx = this.createEvaluationContextInternal(authentication, invocation);
        //设置Bean初始化器，来执行ss.hasPermission方法。
        ctx.setBeanResolver(this.beanResolver);
        ctx.setRootObject(root);
        return ctx;
    }
}
```

至此权限就已经完成了。

![img](https://img-blog.csdnimg.cn/7113613716e04cf58cb5908a97f4a473.png)
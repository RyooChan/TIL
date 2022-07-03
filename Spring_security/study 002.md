# study 002
###### tags: `Tag(스프링 시큐리티)`

## 인증 API - 사용자 정의 보안 기능 구현

![](https://i.imgur.com/gfKhg6L.png)

이전 방식으로 만들어진 내용은 보안 시스템 자체는 작동 하는데, 유저 계정이 하나뿐이고 권한 또한 추가/변경이 되지 않는다.

또한 해커의 침입에 대비할 수 있는 보안 옵션 기능이 없다.

따라서, 이러한 기능을 추가해 주어야 할 것이다.

이런 보안 기능을 부여하기 위해 알아야 할 내용을 배워본다.

* WebSecurityConfigurerAdapter

해당 클래스는 핵심이 되는 클래스이다.

이는 스프링 시큐리티의 웹 보안 기능 초기화 및 설정을 위한 클래스이다.
즉 의존성 추가에서 시큐리티가 초기화되고, 우리의 스프링에 적용할 수 있게끔 도와주는 클래스이다.

또 해당 클래스는 HttpSecurity클래스를 생성한다.

* HttpSecurity

HttpSecurity클래스는 세부적 보안 기능을 설정할 수 있는 API를 제공한다.

---

## 실습

한번 기본적인 설정을 해보겠다.

* SecurityConfig 만들고 설정하기

```
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated();
        http
                .formLogin();
    }
}
```

처음에는 모든 리퀘스트에 대한 설정을 해준다.

* application.yml 수정

```
spring:
  security:
    user:
      name: user
      password: 1111
```

매번 만들어지는 비밀번호 사용은 귀찮으니까 기본 id와 password를 설정해준다.

이제 이 아이디와 비밀번호로 접속이 가능할 것이다.


# study 001
###### tags: `Tag(스프링 시큐리티)`

## 개발 환경

* JDK 
    * 1.8이상
* DB
    * PostgreSQL
* IDE
    * intelliJ

## 스프링 시큐리티 기본 API & Filter 이해

### 인증 API - 프로젝트 구성 및 의존성 추가

의존성 추가를 위해 Gradle쪽에 security 설정을 넣어준다.


```
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation 'org.springframework.security:spring-security-test'
```

다음으로 루트 경로를 생성해주고, 이를 보여줄 문자열을 적어준다.

* SecurityController 생성

```
package com.example.basicsecurity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {

    @GetMapping
    public String index(){
        return "home";
    }
}
```



그리고 앱을 실행하고 나서

`http://localhost:8080/login`

해당 링크로 들어가면

![](https://i.imgur.com/6ETdInV.png)

다음과 같은 화면이 기본으로 나온다.
여기에 security에 들어가기 위해서는 
`user`라는 아이디와
앱 실행시 만들어진 기본 password
![](https://i.imgur.com/GYzr5gP.png)

를 입력해주면 된다.

그렇게 되면 root 경로로 입장 가능하다!

![](https://i.imgur.com/1J4c3Lo.png)

---

### 스프링 시큐리티 의존성 추가 시

* 서버가 기동되면 스프링 시큐리티의 초기화 작업 및 보안 설정이 이루어진다.
    * 스프링 시큐리티가 기본으로 해 준다.
* 별도의 설정이나 구현 없이도 기본적인 웹 보안 기능이 현재 시스템에 연동되어 작동하게 된다.
    1. 모든 요청은 인증이 되어야 자원에 접근이 가능하다.
    2. 인증 방식은 폼 로그인 방식과 httpBasic 로그인 방식을 제공한다.
        * 위에서 진행한 것은 폼 로그인 방식이다.
    3. 기본 로그인 페이지를 제공한다.
    4. 기본 계정을 한 개 제공한다.
        * username
            * user
        * password
            * 랜덤 생성(콘솔에 출력됨)

#### 해당 내용의 문제점

* 계정 추가, 권한 추가, DB연동 등
    * 지금은 하나의 기본계정밖에 없기 때문. 나중에 추가해야 한다.
    * 관리자, 사용자, 등급 등등 권한을 추가해야 할것이다.
        * 해당 내용은 DB에 저장하는 등 관리를 해주는것이 좋을 것이다.
* 따라서 기본적인 보안 기능 외에 시스템에서 필요로 하는 더 세부적이고 추가적인 보안기능이 필요할 것이다.

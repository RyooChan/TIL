# 어댑터 패턴

> 기존 코드를 클라이언트가 사용하는 인터페이스의 구현체로 바꿔주는 패턴

이를 사용해서 클라이언트가 사용하는 인터페이스를 따르지 않는 기존 코드를 재사용할 수 있게 해준다.

![](https://i.imgur.com/QiTaWiC.png)

간단히 말하자면 나라마다 110v, 220v 등등 규격이 다른 것처럼 코드도 클라이언트 규격이 다를 수 있는데, 어댑터를 사용해서 이를 알맞게 변환해줄 수 있는 것이다.

그러니까 `내가 만들어 낸 코드`와 `클라이언트가 사용하는 코드` 간의 차이가 존재할 때에, 이를 맞추어 주기 위해 interface의 구현체로 만들어 주는 것이다.

## 코드로 확인

![](https://i.imgur.com/z4Qjdh1.png)

이런 식으로, security package에 3개의 java파일이 있다.

* LoginHandler

```
public class LoginHandler {

    UserDetailsService userDetailsService;

    public LoginHandler(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public String login(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUser(username);
        if (userDetails.getPassword().equals(password)) {
            return userDetails.getUsername();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
```

코드를 살펴보면
유저 아이디와 비밀번호를 입력받고
아이디로 유저를 가져온 후에, 입력한 비밀번호와 해당 유저의 비밀번호가 일치하면
username을 return하는 함수이다.

요게 Client쪽이다.

* UserDetails

```
public interface UserDetails {

    String getUsername();

    String getPassword();

}
```

* UserDetailsService

```
public interface UserDetailsService {

    UserDetails loadUser(String username);

}
```

그리고 해당 패키지 밖의 파일들을 보면

* Account

```
public class Account {

    private String name;

    private String password;

    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
```

* AccountService

```
public class AccountService {

    public Account findAccountByUsername(String username) {
        Account account = new Account();
        account.setName(username);
        account.setPassword(username);
        account.setEmail(username);
        return account;
    }

    public void createNewAccount(Account account) {

    }

    public void updateAccount(Account account) {

    }

}
```

요렇게 파일들이 존재한다.
이는 security 패키지 밖에 있는 것이다.

* security package의 경우는 여러 애플리케이션에서 사용할 수 있는 코드이다.
* Account, AccountService의 경우는 이 어플리케이션에서만 사용되는 종속적 코드이다.(다른데서는 알아서 바꿔서 쓸 수 있다는것..)

이제 이거를 한번 어댑터를 사용해서 전체적으로 적용할 수 있도록 해보겠다.

여기서 "AccountService - UserdetailsService", "AccountService - UserDetails"를 연결해 줄 것이다.

## 별도의 클래스를 통한 연결

별도 클래스를 하나 만들어서 연결해 본다.

* AccountUserDetailsService

```
public class AccountUserDetailsService implements UserDetailsService {

    private AccountService accountService;

    public AccountUserDetailsService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UserDetails loadUser(String username) {
        return new AccountUserDetails(accountService.findAccountByUsername(username));
    }
}
```

여기서 보면 `AccountService`에서는 return type으로 Account가 오는데, 이를 UserDetails로 바꾸어 주어야 할 것이다.
이를 위해 AccountUserDetails라는 클래스를 하나 구현해 준다.

* AccountUserDetails

```
public class AccountUserDetails implements UserDetails {

    private Account account;

    public AccountUserDetails(Account account) {
        this.account = account;
    }

    @Override
    public String getUsername() {
        return account.getName();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }
}
```

내용을 살펴보면, 본래 userDatails의 name과 password를 받아와서 어댑터를 통해 Account 형식으로 변경시켜 줄 수 있을 것이다.

## 확인

* App

```
public class App {

    public static void main(String[] args) {
        AccountService accountService = new AccountService();
        UserDetailsService userDetailsService = new AccountUserDetailsService(accountService);
        LoginHandler loginHandler = new LoginHandler(userDetailsService);
        String login = loginHandler.login("RyooChanHandsome", "RyooChanHandsome");
        System.out.println(login);
    }
}

```

여기서 한번 확인해 보면

![](https://i.imgur.com/orSlKNC.png)

이렇게 잘 나온다.

사실 기존 코드를 변경할 수 있으면 이걸 굳이 쓸 필요는 없다.

## 장단점 정리

* 장점
    * 기존 코드의 변경 없이 원하는 인터페이스 구현체를 통한 재사용이 가능하다.
        * 이 장점은 OCP(Open-Closed Principle)를 지키게 해 준다.
            * 기존 코드의 변경을 줄이고, 확장은 가능할 수 있기 때문
    * 기존 코드가 하던 일과 특정 인터페이스 구현체로 변환하는 작업을 각기 다른 클래스로 분리하여 관리 가능하다.
        * 이 장점은 SRP(Single Responsibility Principle)를 지키게 해 준다.
            * 각각의 역할에 맞추어 할 일이 정해져 있기 때문에 응집도가 증가한다.
* 단점
    * 새 클래스가 생기게 되어 복잡도가 증가한다.
        * 기존 코드가 해당 인터페이스를 구현하도록 수정하는 것이 더 좋을 수 있다.
            * 다만 이 경우 위의 객체지향 원칙을 위반하게 될 수 있다.

## 실무에서 어떻게 쓰이는가?

### java에서

* collections에서
    * 배열 -> List로 바꾸는 `Array.asList()`
    * List -> Enumeration로 바꾸는 `Collections.enumeration()`
    * Enumeration -> List로 바꾸는 `Collections.list()`
* io에서
    * `FileInputStream()`
    * `InputStreamReader()`
    * `BufferedReader()`
        * 대충 이런 칭구들이 변환에 쓰이는 adapter 패턴들이다.

### Spring에서

* Spring security에서 이런 패턴이 자주 사용되는데, 위의 코드가 사실 이 예제이다.
* Spring mvc에서 `HandlerAdapter`또한 이 패턴이 사용된다.
    * Handler란, mvc에서의 Controller의 역할을 하는 친구이다.
    * 얘는 여러 가지 타입을 Object형식으로 받아와서 mvc가 실행할 수 있도록 해준다.

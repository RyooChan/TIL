# spring boot의 self invocation, 이유와 해결법

회사에서 개발하는데 캐싱과 관련하여 self invocation 관련해서 이슈를 들었다.
그래서 한번 찾아보게 되었다.

## invocation

이거는 메서드 호출이라고 생각하면 된다.

## self-Injection??

일단 문제상황에 대해 알아본다

다음과 같은 코드를 작성한다. (그리고 캐시 설정은 되어있다고 가정한다.)

### Ryoochan.java

```
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Ryoochan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String look;

    public Ryoochan(String look) {
        this.look = look;
    }

    public Ryoochan() {

    }
}
```

간단한 class이다.

### TestService.java

```
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.blossom.healthyblossom.hello.domain.Ryoochan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    @Cacheable(cacheNames = "ryoochan", cacheManager = "testCacheManager")
    public Ryoochan ryoochanIsHandsome() {
        log.info("캐싱 없음!");
        return new Ryoochan("잘생김");
    }

    public void ryoochanIsGreat() {
        log.info("류찬은 최고다!");
        this.ryoochanIsHandsome();
    }

}
```

이렇게 있다.
여기서 `ryoochanIsGreat`가 **같은 서비스** 내에 있는 `ryoochanIsHandsome`을 사용하고 있다.

### TestServiceTest.java

```
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootTest
@EnableCaching
class TestServiceTest {
    @Autowired
    private TestService testService;

    @Test
    public void test() {
        this.testService.ryoochanIsGreat();
        this.testService.ryoochanIsGreat();
        this.testService.ryoochanIsGreat();
    }

}
```

테스트를 위한 클래스를 생성해서 테스트해준다.
본래 생각대로면 캐싱되어있는 데이터를 불러오기 때문에 
캐싱은 최초 수행 때에만 없고 그 뒤부터는 있다고 생각할 것이다.

### 테스트 결과

![image](https://github.com/RyooChan/TIL/assets/53744363/0e3b33d0-ef0c-4333-b393-1b1010a03925)


이렇게 나온다.
왜인지 모르겠는데, 캐싱이 계속 없다.

### 캐싱이 없는 이유

우리는 Spring에서 Cache를 사용할 때에 `@EnableCaching`을 사용한다.
저 어노테이션을 설정하는 것만으로 Cache를 사용할 수 있게 되는 것인데, 그 이유는 spring에서 이거를 proxy로 생성해서(알아서 동작할 때에 interface 기반의 인터페이스-구현체로 만듦) 동작시키기 떄문이다.

근데 문제는 Spring AOP에서 self-invocation(자신 내부의 메서드 호출)을 하는 경우는 이런 처리가 되지 않는다.

그래서 caching이 동작하지 않게 된 것이다!!

### 해결법

이제 이유를 알았으니 해결하면 되는데, 해결법은 간단하다.

저 Proxy가 만들어지게 하면 되는 것이다.

방법은 여러 가지가 있는데 내가 여기서 다루는 것은

* 외부 service를 통한 호출
* AopContext를 통한 호출
* Bean사용

이다.

#### 해결1) 외부 service를 통한 호출

걍 self invocation 하지 말고 외부에서 호출하는거다.
간단하니까 넘어감.

#### 해결2) AopContext를 통한 호출

그냥 지금 메서드에서 aop proxy를 만들도록 하는 것이다.

```
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.blossom.healthyblossom.hello.domain.Ryoochan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    @Cacheable(cacheNames = "ryoochan", cacheManager = "testCacheManager")
    public Ryoochan ryoochanIsHandsome() {
        log.info("캐싱 없음!");
        return new Ryoochan("잘생김");
    }

    public void ryoochanIsGreat() {
        log.info("류찬은 최고다!");
        ((TestService)AopContext.currentProxy()).ryoochanIsHandsome();
    }

}
```

이런 식으로 하는 것인데, 저기 `AopContext.currntProxy()` 메서드를 보면

![image](https://github.com/RyooChan/TIL/assets/53744363/158dd2c3-fa4b-45fa-a742-0bf0b779811f)




이렇게 나온다.
위에 보면 해당 메서드를 통해 Aop Proxy를 반환할 수 있다고 적혀 있다.
그리고 내부에서도 proxy라는 object를 만들어 반환한다.

근데 좀 살펴보면 저게 AOP를 통해 호출되고 AOP 프레임워크가 프록시를 노출하도록 설정된 경우에만 사용할 수 있다는 말이 있다. 그렇지 않으면 exception이 던져진다고 한다.

저 문제는 Spring Boot에서 매우 쉽게 해결될 수 있다.

`@EnableAspectJAutoProxy(exposeProxy = true)`

얘를 써주면 된다.
이것도 내부를 보면

![image](https://github.com/RyooChan/TIL/assets/53744363/aa6e53f3-e94d-4fed-8b8d-8643e3aef5fc)


요렇게 되어 있다.
AOP 프레임워크에 의해 스레드로컬로 노출되어야 함을 나타내는 것으로, 이걸 true로 해주면 노출되게 될 것이다.

그래서 이제 한번 시행해 보면

![image](https://github.com/RyooChan/TIL/assets/53744363/f235206c-1b6e-4a98-8100-106c44c009d7)

이렇게 캐싱이 잘 동작함을 확인 가능하다!!!

#### 해결3) Bean사용

위의 방식은 매번 저 Aop어쩌고를 써야하고, `@EnableAspectJAutoProxy(exposeProxy = true)`이것도 설정해줘야 했다.

알다시피 다양한 annotation들.. 예를 들어 `@Service` `@Controller` `@Component` 등을 쓰면 이는 bean으로 주입된다.

그러면 걍 해당 사용 위치에서 자기 자신을 미리 bean으로 등록시켜 둔다면??
간단하게 해결될 것이다.

##### ApplicationContextProvider.java

```
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
@Component
public class ApplicationContextProvider implements ApplicationContextAware{

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
```

ApplicationContext의 관리를 위한 클래스를 만들어 준다.
이렇게 하면 `getApplicationContext()`를 통해 applicationContext를 받아올 수 있게 된다.

##### Util.java

```
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component

public class Util {
    public static <T> T getBean(Class<?> classType) {
        ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
        return (T) applicationContext.getBean(classType);
    }
}

```

여기서는 위에서 만든 applicationContextProvider를 통해 getBean으로 applicationContext bean을 가져올 수 있게 된다.

generic method를 사용하여 어떤 곳에서든 바로바로 사용할 수 있도록 했다.

그럼 이제 service에서

```
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.blossom.healthyblossom.hello.domain.Ryoochan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.blossom.healthyblossom.config.Util.getBean;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {
    private TestService getTestService() {
        return getBean(TestService.class);
    }

    @Cacheable(cacheNames = "ryoochan", cacheManager = "testCacheManager")
    public Ryoochan ryoochanIsHandsome() {
        log.info("캐싱 없음!");
        return new Ryoochan("잘생김");
    }

    public void ryoochanIsGreat() {
        log.info("류찬은 최고다!");
        this.getTestService().ryoochanIsHandsome();
    }

}

```

이런 식으로, getBean을 써서 스스로 서비스의 클래스를 통해 빈을 등록시켜 주면 될것이다!!

테스트 해보면

![image](https://github.com/RyooChan/TIL/assets/53744363/25d6ea25-f485-47c4-b4bf-2ec88452ceea)

이렇게 잘 나온다.

## 결론

이상으로 self invocation의 원인과 문제점, 여러 방법을 통해 이를 해결하는 방안에 대해 알아보았다.
이게 참 문제가 되는데 모르면 당할수밖에 없는 것이라 미리미리 알아두어야 할 것 같다.

# 간단하게 메서드 수행시간 로깅을 해보자! (feat. template callback, annotation)

여기서는 그냥 메서드의 수행 시간을 파악하기 위한 예제이다.
실제로는 다양한 곳에 활용이 가능하겠지.

## 기본 세팅

- TestService.java

```
@Service
public class TestService {
    public void doSomething() {
        System.out.println("류찬은 최고에요!!");
    }
}

```

그냥 간단한 sout 하는 메서드를 만들고

- TestServiceTest.java

```
@SpringBootTest
class TestServiceTest {

    @Autowired
    private TestService testService;

    @Test
    public void testLogFirst() {
        testService.doSomething();
    }
}
```

그 메서드를 테스트하는걸 돌리면

![image](https://github.com/RyooChan/TIL/assets/53744363/aacc17e5-d82b-47d6-95af-9f13aafc5b87)

이렇게 내용이 출력된다.

## 수행시간을 로깅해보자.

그러면 이 메서드가 실제로 얼마나 소요되었는지 확인해보고 싶다.
(실제로 업무를 할 때에 트래픽을 고려하면 속도 모니터링을 위해 쓸 일이 많을것이다.)

```
@Service
@Slf4j
public class TestService {

    public void doSomething() {
        long startTime = System.currentTimeMillis();
        System.out.println("류찬은 최고에요!!");
        long endTime = System.currentTimeMillis();

        log.info("실행 시간 : " + (endTime - startTime) + "ms");
    }
}

```

일단 대충 이렇게 시간으로 감싸서 어쩌고저쩌고~ 하면 될것이다.

![image](https://github.com/RyooChan/TIL/assets/53744363/87e402f2-f586-4ac4-a770-0fadd1010e46)

0ms 소요됨. 굉장히 빠르다. 굳.

## 문제점

근데 딱 보면 알겠지만, 뭔가 되게 더럽다.
실제로 비즈니스 수행에 맞는건 그냥 sout 하나인데, 이를 위해 다른 쓸데없는게 너무 많이 들어간다.
그리고 보통 로깅을 여기저기서 많이 할텐데? 이 때마 이렇게 한다고 생각하니 어질어질하다.
그러면 이거를 어떻게 깔끔하게 할 수 있을까??


## Template callback

일단 템플릿 콜백이 뭔지는 대충 알 것이다.
간단히 설명하자면

- 객체지향의 상속을 통한 코드 재사용 및 확장 구조 활용
- 템플릿 메서드 + 콜백 메서드

이런 것이다.
말하자면 자주 쓰일것같은 애들을 미리 만들고 상속해서 쓰는거임.

미리 만들어두고 자주자주 쓰자.
이름은 걍 대충 지음

- TimeLogger.java 

```
@Slf4j
public abstract class TimeLogger {
    private static final int CALLER_INDEX = 2;
    public void executeTaskAndLog(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();

        long duration = startTime - endTime;

        String targetClassName = Thread.currentThread().getStackTrace()[CALLER_INDEX].getClassName();
        String targetMethodName = Thread.currentThread().getStackTrace()[CALLER_INDEX].getMethodName();

        log.info(targetClassName + "." + targetMethodName + " 의 실행 시간 : " + duration + "ms");
    }
}
```

근데 생각해보면, 여기저기서 자주 쓰인다면 어디서 쓰였는지를 알면 좋을 것 같다.
어떤 class의 어떤 method가 쓰였는가? 이를 알면 도움이 되겠다는 생각이 든다.

이렇게 하면 어디서 불린 누가 몇초 수행되었는지를 알 수 있을것이다.

- TestService.java

```
@Service
public class TestService extends TimeLogger{

    public void doSomething() {
        executeTaskAndLog(() -> System.out.println("류찬은 최고에요!!"));
    }
}

```

얘는 이제 상속만 하고 위의 메서드를 호출하면 간단해진다.

![image](https://github.com/RyooChan/TIL/assets/53744363/ecc1f8db-82c2-46dd-8c46-259be2268f0a)

이렇게, 어디의 누가 이리 걸리더라~ 하고 알 수 있게 되는것.

근데 과연 이렇게 void 형태만 나올까? 아니겠지.
모든 형태를 받도록 해주자.

- TimeLogger.java

```

@Slf4j
public abstract class TimeLogger {
    private static final int CALLER_INDEX = 2;

    public <T> T executeTaskAndLog(Supplier<T> task) {
        long startTime = System.currentTimeMillis();
        T result = task.get();
        long endTime = System.currentTimeMillis();

        long duration = startTime - endTime;

        String targetClassName = Thread.currentThread().getStackTrace()[CALLER_INDEX].getClassName();
        String targetMethodName = Thread.currentThread().getStackTrace()[CALLER_INDEX].getMethodName();

        log.info(targetClassName + "." + targetMethodName + " 의 실행 시간 : " + duration + "ms");
        return result;
    }

    public void executeTaskAndLog(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        long endTime = System.currentTimeMillis();

        long duration = startTime - endTime;

        String targetClassName = Thread.currentThread().getStackTrace()[CALLER_INDEX].getClassName();
        String targetMethodName = Thread.currentThread().getStackTrace()[CALLER_INDEX].getMethodName();

        log.info(targetClassName + "." + targetMethodName + " 의 실행 시간 : " + duration + "ms");
    }
    
}

```

이렇게 하면 모든 형태를 다 받을 수 있게 된다.

한번 확인해볼까??

- TestService.java

```
@Service
public class TestService extends TimeLogger{

    public void doSomething() {
        executeTaskAndLog(() -> System.out.println("류찬은 최고에요!!"));
    }

    public String doSting() {
        return executeTaskAndLog(() -> "류찬은 최고에요!!");
    }

    public int getRyoochanIQ() {
        return executeTaskAndLog(() -> 200);
    }
}
```

여러 형태가 있어도 다 받을 수 있다.

- TestServiceTest.java

```
@SpringBootTest
class TestServiceTest {

    @Autowired
    private TestService testService;

    @Test
    public void testLogFirst() {
        testService.doSomething();
    }

    @Test
    public void testLogSecond() {
        System.out.println(testService.doSting());
    }

    @Test
    public void testLogThird() {
        System.out.println(testService.getRyoochanIQ());
    }
}
```

그리고 테스트를 해보면

![image](https://github.com/RyooChan/TIL/assets/53744363/93d13344-11e8-4492-a2e6-0d4655a95d07)

Great!!

## annotation

근데 음.. 위의 방법도 좋기는 한데, 뭔가 매번 상속을 받아야하고 executeTaskAndLog이거를 굳이 매번 호출하는것도 좀 거슬린다는 생각이 든다.

그러면 그냥 annotation으로 응 이거 해줘~ 하면 해주는걸 만들면 되지 않나? 싶을수 있다.
그래서 annoataion으로 만들어서 활용하는 방안을 좀 고려해본다.

```
implementation 'org.springframework.boot:spring-boot-starter-aop'
```

이거 일단 Import 해주고

- ExecuteTaskAndLog.java

```
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecuteTaskAndLog {
}
```

어노테이션용으로 활용할 친구를 하나 만들어준다.
그리고 이게 불리면 어떻게 쓰일지를

```
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecuteTaskAndLogAspect {

    @Around("@annotation(com.example.logger.ExecuteTaskAndLog)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        String targetClassName = joinPoint.getSignature().getDeclaringTypeName();
        String targetMethodName = joinPoint.getSignature().getName();

        log.info(targetClassName + "." + targetMethodName + " 의 실행 시간 : " + duration + "ms");
        return proceed;
    }
}

```

여기서 부르면 된다.
아마 이거 보는사람들은 알테지만, `Around` 에서 불리는 쟤는 자기 프로젝트 내의 파일 위치에 따라 알아서 바꿔주자.

참고로 그리 중요한건 아니지만 여기서는 그냥 불린곳의 joinPoint 를 통해 위치를 알 수 있다.
어노테이션을 통해 부르는 곳을 알 수 있으니 좋다.

- TestService.java

```

@Service
public class TestService{

    @ExecuteTaskAndLog
    public void doSomething() {
        System.out.println("류찬은 최고에요!!");
    }

    @ExecuteTaskAndLog
    public String doSting() {
        return "류찬은 최고에요!!";
    }

    @ExecuteTaskAndLog
    public int getRyoochanIQ() {
        return 200;
    }
}
```

그러면 그냥 이렇게 annotation을 호출해주면 바로바로 쓸 수 있다.

![image](https://github.com/RyooChan/TIL/assets/53744363/98c83fc3-4712-4949-b7d5-2a30828b8420)

이렇게 잘 출력된다.

## 결론

- 매번 로깅을 하는건 좀 짜친다. 이를 바꾸는 방법으로 나는 template callback, annotation 두개를 제시했다.
- template callback
    - 장점
        - 사용할 위치를 알아서 선언 가능하다. 한 메서드 내에서도 다른 방식으로 적용이 된다는것.
    - 단점
        - 가독성이 annotation보다 떨어지는 느낌이 든다.
        - ExecuteTaskAndLog 코드가 중간에 들어가서 비즈니스 로직이 약간 애매해진다.
- annotation
    - 장점
        - 관심사의 분리가 가능하다. 시간 확인 로직을 아예 따로 가져가니까.
        - 아무튼 모듈화를 통해 관리가 쉽?다? (쉽긴 하지만 쉽지않은 그런느낌)
    - 단점
        - 메서드 위에 선언해야해서 중간에 확인 로직을 추가하고 처리하는 등의 확장성이 조금 떨어진다.
        - 디버깅할때 좀 곤란해질 수 있다.
        - ~~annotation 방식 자체의 뭔가뭔가 그런 느낌~~

아무튼 로깅은 다른 방식을 쓰는게 좋다.
근데 사실 걍 Util Class에다가 만들고 가져와서 쓰는것이 걍 제일 깔끔해보이긴 하지만 그래도 안씀ㅇㅇ

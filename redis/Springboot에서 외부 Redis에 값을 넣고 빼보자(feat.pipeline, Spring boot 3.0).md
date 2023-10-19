# Springboot에서 외부 Redis에 값을 넣고 빼보자(feat.pipeline, Spring boot 3.0)

해당 테스트를 하기 전에 Redis는 localhost가 아닌 [외부](https://hello-backend.tistory.com/270)에 존재한다고 가정한다. (외부 레디스를 사용하면 네트워크 지연 시간이 발생하게 될 것이다.)

알다시피 [레디스](https://hello-backend.tistory.com/190)는 굉장히 속도가 빠르다.
그렇지만 레디스와 서버는 [TCP 네트워크 모델](https://hello-backend.tistory.com/194)을 기반으로 통신한다.
이게 무슨 뜻일까... 하면, redis와 서버간의 통신 과정에서 TCP 3-way handshake를 따르고, 여러 번 통신을 하면 결국 속도에서 손해를 볼 수밖에 없다는 것이다.

한번 이를 테스트해 보자.

## 세팅

먼저 우리는 Redis를 써줄 것이다.
그리고 이를 위해 간단히 세팅해주자.

* build.gradle

```

	implementation 'org.springframework.boot:spring-boot-starter-data-redis:2.7.2'
	// https://mvnrepository.com/artifact/redis.clients/jedis
	implementation group: 'redis.clients', name: 'jedis', version: '4.4.0'
```

* application.yml

```
spring:
    data:
      redis:
        host: "redis 호스트명"
        port: "redis 포트번호"
        password: "redis 비밀번호"
```

* RedisConfig

```
@Configuration
@EnableTransactionManagement
public class RedisConfig {


    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisHost);
        redisStandaloneConfiguration.setPort(Integer.parseInt(redisPort));
        redisStandaloneConfiguration.setPassword(redisPassword);
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration);
        return lettuceConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

}
```

간단하게 설명하자면

- Redis와의 연결 메서드
- Redis와의 호환을 위한 메서드
    - key, value의 직렬화(key를 문자열, value를 object로 저장)

정도를 원함이다.
참고로 우리는 많은 데이터를 한꺼번에 넣는 테스트를 할거기 때문에 Spring의 RedisTemplate에서 제공하는 Transaction관리를 할 수 있도록 세팅해 두었다.


## 데이터 입력 테스트

이제 아무런 이름의 Service를 만들고, 여기서 테스트해본다.

* RedisService

```
@Service
@Transactional
public class RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
}
```

이런 식으로 RedisService를 일단 만들어 준다.

* 메서드 추가

```
    public void saveData() {
        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            String val = "value" + i;
            redisTemplate.opsForValue().set(key, val);
        }
    }
```

그리고 그 RedisService내부에 이런 이름의 메서드를 넣어준다.
대충 보면 500번 값을 넣는 것이다.

이제 테스트해보자

* RedisServiceTest

```
@SpringBootTest
public class RedisServiceTest {

    @Autowired
    private RedisService redisService;
    
}
```

* 테스트 작성

```
    @Test
    void 입력_테스트() {
        long start = System.currentTimeMillis();
        redisService.saveData();
        long end = System.currentTimeMillis();
        System.out.println("소요시간 = " + (end - start) + "ms");
    }
```

RedisServiceTest에 간단하게 이렇게 소요시간을 확인해보자

![image](https://github.com/RyooChan/TIL/assets/53744363/281b78e4-d67a-404a-b767-11cbe709e84e)

값을 입력하는데 2436ms의 시간이 소요됐다.
음.. 뭔가 오래 걸린 것 같은 느낌이 든다.

## 데이터 입력 Pipeline(set)

오래 걸린 이유는 상술했듯 500번의 저장을 위해 500번 redis에 통신했기 때문이다.

당연하지만 redis에서는 이런 경우를 해결하기 위해 `pipeline`을 지원한다.
간단히 말하자면 그냥 뭉탱이로 처리할 수 있게 해주는 것이다.

이거는 그냥 직접 사용해보자

* 메서드 추가

```

    public void saveDataByPipeline() {
        stringRedisTemplate.executePipelined(
            (RedisCallback<Object>) connection -> {
                connection.openPipeline();

                for (int i = 0; i < 500; i++) {
                    StringRedisConnection stringRedisConnection = (StringRedisConnection) connection;
                    String key = "key" + i;
                    String val = "value" + i;
                    stringRedisConnection.set(key, val);
                }

                connection.closePipeline();
                return null;
            }
        );
    }
```

RedisService에 이런 메서드를 추가한다.
대충 코드를 살펴보면 pipeline을 열어서 한꺼번에 처리하는 것이라고 느껴질 것이다.

* 테스트 작성

```
    @Test
    void 입력_파이프라인_테스트() {
        long start = System.currentTimeMillis();
        redisService.saveDataByPipeline();
        long end = System.currentTimeMillis();
        System.out.println("소요시간 = " + (end - start) + "ms");
    }
```

이제 RedisServiceTest에 이런 테스트 메서드를 만들어 테스트해보자

![image](https://github.com/RyooChan/TIL/assets/53744363/c9f4bdc3-9dbd-4f9f-bb66-a4366ef055cf)


음... 빠르다.
잠깐, 그러면 검색하는 경우는 어떨까??

## 데이터 검색

* 메서드 추가

```
    public List<String> findData() {
        List<String> results = new ArrayList<>();

        for (int i = 0; i < 500; i++) {
            String key = "key" + i;
            Object value = redisTemplate.opsForValue().get(key);
            results.add((String) value);
        }

        return results;
    }
```

RedisService에 다음과 같은 메서드를 추가한다.
딱 봐도 검색용이다.

* 테스트 작성

```
    @Test
    void 검색_테스트() {
        long start = System.currentTimeMillis();
        List<String> data = redisService.findData();
        long end = System.currentTimeMillis();
        System.out.println("소요시간 = " + (end - start) + "ms");
        System.out.println(data.toString());
    }
```

RedisServiceTest에서 이를 테스트해보면 (가져오는 자체의 시간 측정)

![image](https://github.com/RyooChan/TIL/assets/53744363/2c37da31-dcf2-4163-900c-e5662a0e3cf4)


이렇게 나온다...
검색에서 시간이 엄청나게 소요되는것이 확인된다.
이게 생각보다 중요한데, 서비스에서는 캐싱된 데이터를 읽는 경우가 많고 갱신을 따로 하기 위해 데이터를 나누어 둘 수도 있을 것이다.
그러면 그걸 한꺼번에 가져오는 경우 속도가 많이 느려질수도 있는 것이다.


## 데이터 검색 Pipeline(get)

* 메서드 추가

```
    public List<String> findDataByPipeline() {
        List<Object> values = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (int i = 0; i < 500; i++) {
                    StringRedisConnection stringRedisConnection = (StringRedisConnection) connection;
                    String key = "key" + i;
                    stringRedisConnection.get(key);
                }
                return null;
            }
        );

        return values.stream()
            .map(value -> (String) value)
            .toList();
    }
```

RedisService에 다음과 같이 pipeline을 통해 검색하는 메서드를 넣어준다.

* 테스트 작성

```
    @Test
    void 검색_파이프라인_테스트() {
        long start = System.currentTimeMillis();
        List<String> dataByPipeline = redisService.findDataByPipeline();
        long end = System.currentTimeMillis();
        System.out.println("소요시간 = " + (end - start) + "ms");
        System.out.println(dataByPipeline.toString());
    }
```

RedisServiceTest에서 이런 메서드를 테스트하면

![image](https://github.com/RyooChan/TIL/assets/53744363/281976dc-d09e-4d28-b095-e6ec4bb1afe3)


이런 식으로, 속도가 비교도 안되게 빨라진걸 알 수 있다.

## 결론

Redis는 보통 서버 외부에 존재하고, 이 처리에 있어 고려해야 할 사항이 많이 있다.
값의 갱신이나 검색에 있어 `pipeline`을 활용하면 좀 잘 처리할 수 있다.
다만 `pipeline`은 절대 만능키가 아니다.

* Atomic / 오류 처리 문제
    * 다수의 데이터 처리를 진행하기 때문에 이를 잘 처리해야 한다.
    * 나는 설정을 해주어서 문제가 없었지만, 만약 뭔가 빼먹은 상태에서 중간에 에러가 난다면? 그럼 망하는거임ㅇㅇ
* 안써도 되면 쓰지 말자
    * 예를 들어 하나의 명령만을 쓰는 경우는 이걸 쓰면 오히려 오버헤드때문에 느려지게 될 것이다.
* 코드가 복잡하다
    * 보면 대충 알겠지만... 그냥 쓰는게 훨씬 편하다

이거를 고려하고 쓰자.
그래도 이게 캐시를 써보면 진짜 신세계인것을 알 수 있다.

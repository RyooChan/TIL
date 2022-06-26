
서비스를 실제로 운영할 때에 [공격이 들어올 수 있고, 이를 막기 위해 노력](https://hello-backend.tistory.com/162)해야 한다.

디프만에서 우리는 모든 DB통신을 JPA와 querydsl을 통해 진행했기 때문에 SQL injection은 막을 수 있었다.

그러나 XSS의 경우는 추가적인 방어가 필요했다.

이를 위해 팀원분이 lucy filter를 도입하였고, 이 필터에 추가적인 기능을 부여하며 여러 테스트를 해 보았다.

## lucy필터의 장점

https://github.com/naver/lucy-xss-filter

네이버에서 만든 XSS 방어용 필터이다.
이를 사용하면

* XML설정만으로 XSS방어가 가능해진다.
* 비지니스 레이어의 코드 수정이 필요하지 않다.
* 직접 설정할 필요가 없으므로 코드를 잘못 입력하거나, 놓치고 적용하지 않는 경우가 없다.

## lucy필터 적용하기

* gradle에 lucy필터 관련 라이브러리 선언

![](https://i.imgur.com/Q8sFoGf.png)

* resources에 필터를 만들어준다.
    * lucy-xss-servlet-filter-rule.xml 생성 후 룰 기입

```
<?xml version="1.0" encoding="UTF-8"?>

<config xmlns="http://www.navercorp.com/lucy-xss-servlet">
  <defenders>
    <defender>
      <name>xssPreventerDefender</name>
      <class>com.navercorp.lucy.security.xss.servletfilter.defender.XssPreventerDefender</class>
    </defender>

    <defender>
      <name>xssSaxFilterDefender</name>
      <class>com.navercorp.lucy.security.xss.servletfilter.defender.XssSaxFilterDefender</class>
      <init-param>
        <param-value>lucy-xss-superset-sax.xml</param-value> 
        <param-value>false</param-value>        
      </init-param>
    </defender>

    <defender>
      <name>xssFilterDefender</name>
      <class>com.navercorp.lucy.security.xss.servletfilter.defender.XssFilterDefender</class>
      <init-param>
        <param-value>lucy-xss.xml</param-value>   
        <param-value>false</param-value>         
      </init-param>
    </defender>
  </defenders>

  <default>
    <defender>xssPreventerDefender</defender>
  </default>

  <global>
    <params>
      <param name="globalParameter" useDefender="false" />
      <param name="globalPrefixParameter1" usePrefix="true" useDefender="false" />
      <param name="globalPrefixParameter2" usePrefix="true" />
      <param name="globalPrefixParameter3" usePrefix="false" useDefender="false" />
    </params>
  </global>

  <url-rule-set>
    <!-- <url-rule>
        <url disable="true">/login/login/loginAjax</url>
    </url-rule> -->
  </url-rule-set>
</config> 
```


* resources에 superset을 만들어준다.
    * lucy-xss-superset-sax.xml 생성 후 룰 기입

```
<?xml version="1.0" encoding="UTF-8"?>

<config xmlns="http://www.nhncorp.com/lucy-xss"
  extends="lucy-xss-default-sax.xml">

  <elementRule>
    <element name="body" disable="true" /> <!-- <BODY ONLOAD=alert("XSS")>, <BODY BACKGROUND="javascript:alert('XSS')"> -->
    <element name="embed" disable="true" />
    <element name="iframe" disable="true" /> <!-- <IFRAME SRC=”http://hacker-site.com/xss.html”> -->
    <element name="meta" disable="true" />
    <element name="object" disable="true" />
    <element name="script" disable="true" /> <!-- <SCRIPT> alert(“XSS”); </SCRIPT> -->
    <element name="style" disable="true" />
    <element name="link" disable="true" />
    <element name="base" disable="true" />
  </elementRule>

  <attributeRule>
    <attribute name="data" base64Decoding="true">
      <notAllowedPattern><![CDATA[(?i:s\\*c\\*r\\*i\\*p\\*t\\*:)]]></notAllowedPattern>
      <notAllowedPattern><![CDATA[(?i:d\\*a\\*t\\*a\\*:)]]></notAllowedPattern>
      <notAllowedPattern><![CDATA[&[#\\%x]+[\da-fA-F][\da-fA-F]+]]></notAllowedPattern>
    </attribute>
    <attribute name="src" base64Decoding="true">
      <notAllowedPattern><![CDATA[(?i:s\\*c\\*r\\*i\\*p\\*t\\*:)]]></notAllowedPattern>
      <notAllowedPattern><![CDATA[(?i:d\\*a\\*t\\*a\\*:)]]></notAllowedPattern>
      <notAllowedPattern><![CDATA[&[#\\%x]+[\da-fA-F][\da-fA-F]+]]></notAllowedPattern>
    </attribute>
    <attribute name="style">
      <notAllowedPattern><![CDATA[(?i:j\\*a\\*v\\*a\\*s\\*c\\*r\\*i\\*p\\*t\\*:)]]></notAllowedPattern>
      <notAllowedPattern><![CDATA[(?i:e\\*x\\*p\\*r\\*e\\*s\\*s\\*i\\*o\\*n)]]></notAllowedPattern>
      <notAllowedPattern><![CDATA[&[#\\%x]+[\da-fA-F][\da-fA-F]+]]></notAllowedPattern>
    </attribute>
    <attribute name="href">
      <notAllowedPattern><![CDATA[(?i:j\\*a\\*v\\*a\\*s\\*c\\*r\\*i\\*p\\*t\\*:)]]></notAllowedPattern>
    </attribute>
  </attributeRule>

</config> 
```


* XssConfig.java 파일 생성 후 필터 적용 

```
import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class XssConfig implements WebMvcConfigurer {

  @Bean
  public FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
    final FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = new FilterRegistrationBean<>();
    filterRegistration.setFilter(new XssEscapeServletFilter());
    filterRegistration.setOrder(1);
    filterRegistration.addUrlPatterns("/*"); //filter를 거칠 url patterns
    return filterRegistration;
  }
}
```

이렇게 하면 일단 xss-filter가 만들어진다.
한번 이걸 사용하면 어떻게 동작하는지 확인해본다.

---

## String 단일 파라미터 테스트

* String input을 받는 controller 및 service 생성

```
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/v1/xss-test")
public class XssController {

  private final XssService xssService;

  @PostMapping("/parameter")
  public String strInput(@RequestParam String input){
    return xssService.stringTest(input);
  }
}
```

```
@RequiredArgsConstructor
@Service
public class XssService {
  public String stringTest(String input){
    return input;
  }
}
```

간단하게 xss필터를 적용할 친구들을 만들어준다.

---

* 잘 되는지 테스트해보기

![](https://i.imgur.com/zzuAwSZ.png)

이제 Swagger를 통해 테스트할 값을 넣어준다.

![](https://i.imgur.com/IMkNmrC.png)

그러면 이런 식으로 변경되어 값이 저장되게 된다.

---

## JSON파라미터 테스트

이제 String에 xss필터 적용은 완료되었다.
근데...만약에 JSON형태로 값을 받게되면 어떻게 될까???

* DTO 생성하기

```
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class XssRequestDto {
  private String input1;
  private String input2;
}
```

```
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class XssResponseDto {
  private String input1;
  private String input2;
}
```

String을 인자로 갖는 DTO를 만들어준다.
굳이 안나와도 되는데, 그래도 나누는게 깔끔하니 Request, Resonse로 나누어 주겠다.

* Controller에 DTO쪽 url 생성하기
    * dto라는 PostMapping이다.
        * 참고로 JSON형태의 입력은 GET에서 지양된다.

```
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/v1/xss-test")
public class XssController {

  private final XssService xssService;

  @PostMapping("/parameter")
  public String strInput(@RequestParam String input){
    return xssService.stringTest(input);
  }

  @PostMapping("/dto")
  public XssResponseDto dtoInput(@RequestBody XssRequestDto xssRequestDto){
    return xssService.dtoTest(xssRequestDto);
  }
}
```

* Service에 해당 내용 작성하기

```
@RequiredArgsConstructor
@Service
public class XssService {
  public String stringTest(String input){
    return input;
  }

  public XssResponseDto dtoTest(XssRequestDto xssRequestDto){
    return new XssResponseDto(
        xssRequestDto.getInput1(), xssRequestDto.getInput2()
    );
  }
}
```

---

* 테스트

![](https://i.imgur.com/H6doGyv.png)

![](https://i.imgur.com/psBhH93.png)

테스트해보면 필터링이 제대로 되지 않는다.

---

## JSON파라미터 XSS필터 적용하기

* HtmlCharacterEscapes 클래스 추가하기

```
public class HtmlCharacterEscapes extends CharacterEscapes {
  private final int[] asciiEscapes;

  public HtmlCharacterEscapes() {
    // 1. XSS 방지 처리할 특수 문자 지정
    asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
    asciiEscapes['<'] = CharacterEscapes.ESCAPE_CUSTOM;
    asciiEscapes['>'] = CharacterEscapes.ESCAPE_CUSTOM;
    asciiEscapes['\"'] = CharacterEscapes.ESCAPE_CUSTOM;
    asciiEscapes['('] = CharacterEscapes.ESCAPE_CUSTOM;
    asciiEscapes[')'] = CharacterEscapes.ESCAPE_CUSTOM;
    asciiEscapes['#'] = CharacterEscapes.ESCAPE_CUSTOM;
    asciiEscapes['\''] = CharacterEscapes.ESCAPE_CUSTOM;
  }

  @Override
  public int[] getEscapeCodesForAscii() {
    return asciiEscapes;
  }

  @Override
  public SerializableString getEscapeSequence(int ch) {
    return new SerializedString(StringEscapeUtils.escapeHtml4(Character.toString((char) ch)));
  }
}
```

* XssConfig 변경하기
    * ObjectMapper와 HtmlCharacterEscapes를 활용하여 필터 적용

```
@Configuration
@RequiredArgsConstructor
public class XssConfig implements WebMvcConfigurer {

  private final ObjectMapper objectMapper;

  @Bean
  public FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
    final FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = new FilterRegistrationBean<>();
    filterRegistration.setFilter(new XssEscapeServletFilter());
    filterRegistration.setOrder(1);
    filterRegistration.addUrlPatterns("/*"); //filter를 거칠 url patterns
    return filterRegistration;
  }

  @Bean
  public MappingJackson2HttpMessageConverter jsonEscapeConverter() {
    ObjectMapper copy = objectMapper.copy();
    copy.getFactory().setCharacterEscapes(new HtmlCharacterEscapes());
    return new MappingJackson2HttpMessageConverter(copy);
  }
}
```

---

## JSON파라미터 테스트(변경후)

이제 변경했으니 다시 해보자!

![](https://i.imgur.com/43OA62O.png)
![](https://i.imgur.com/3bcxsFy.png)

필터링이 잘 된다!!

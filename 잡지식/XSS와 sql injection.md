# XSS와 sql injection에 관하여

## SQL Injection이란??

먼저 SQL Injection이란, 이름 그대로 보안상의 취약점을 이용하여 임의의 SQL문을 주입하여 DB에서 이 SQL이 실행되도록 하는 공격이다.
이 SQL Injection은 OWASP Top10중 첫번째에 해당한다.
공격 방법이 간단하고, 이게 성공할 경우 큰 피해를 받게 되는 공격이다.

어떤 공격인지 예를 들어보자면

### 공격 방법

예를 들어 



| user_id | user_pass |
| -------- | -------- |
| RyooChan     | 1q2w3e!     |
| Michael Jordan      | bulls23     |
| Magic Johnson      | Lakers32     |

위와 같은 유저 정보를 가진 테이블이 있다고 할 때


`SELECT user FROM user_table where user_id = '[1]' AND user_pass = '[2]'`

이런 식으로 유저의 아이디와 비밀번호를 입력받아 체크하는 로직이 있다고 가정하자

내가 RyooChan이라는 유저라면
보통은 [1]에 아이디 (ex. RyooChan), [2]에 비밀번호 (ex.1q2w3e!) 를 입력할 것이다. 이게 맞으면 로그인되고 아니면 로그인되지 않을 것이다.

`SELECT user FROM user_table where user_id = 'RyooChan' AND user_pass = '1q2w3e!'`

그런데 이 때에 만약 RyooChan 유저가 Maginc Johnson이라는 아이디에 로그인하고 싶을 때에

[1]에 `Magic Johnson`, [2]에 `' OR '1' = '1`

이렇게 입력한다면?

`SELECT user FROM user_table where user_id = 'Magic Johnson' AND user_pass = ' ' OR '1' = '1'`

이렇게 될것이고, 로그인이 성공할 것이다!!

이런 식으로 sql에 입력할 때에 원하는 무언가를 입력하는 것으로 공격하는 방법이다.

만약 어딘가에 DROP TABLE을 넣는다거나 하면 진짜 대참사가 날 수 있다.

### 방어 방법

사실 나는 JPA에 관심을 갖게된 이후로, 거의 모든 경우 JPA나 querydsl을 통해 진행하고 있다...
이 경우 SQL Injection에 대해 크게 고민하지 않아도 된다.
그 이유는 JPA와 querydsl모두 Parameter Binding을 사용하기 때문이다.
다만 이 경우, SQL을 직접 사용하는 방식을 쓸 때에는 조심해야 한다.

---

## XSS란??

XSS란, Cross Site Scripting의 약자로(~~X는 없는데요? 이러지 말자~~) 공격자가 브라우저에서 스크립트가 실행되도록 하여 악의적 컨텐츠를 삽입하거나, 세션을 가로채거나, 웹사이트를 변조하는 등의 행위를 하는 보안 공격이다.


### 공격 방법

간단하게만 설명하자면, script언어를 중간에 삽입하여 공격하는 방법이 가장 보편적이다.

예를 들어
`<script>alert('XSS');</script>`

이런 내용으로 게시판에 글을 입력한다고 한다.
그러면 사용자가 이 글을 입력하면 저 XSS라는 글이 튀어나올 것이다.
이런 식으로 다양한 공격이 이루어질수 있다.

`<a href="javascript:alert('XSS')">XSS</a>`

단순한 alert로부터 시작하여 특정 사이트로 이동하거나 세션 탈취까지 너무 많은 공격방법과 결과들이 존재한다.

### 방어 방법

일단 결론부터 말하자면 XSS는 막기 굉장히 힘들다고 볼 수 있다...

방법을 몇 가지 서술하자면

1. <, >를 변환하기
이 <와 >는 script등에서 자주 사용되는 텍스트인데, 이를 &lt; 와 &gt; 같은 HTML문자로 변환하여 단순 문자로 인식하게 하는 방법이다.
이 경우는 글을 꾸미는것이 힘들것이다.

2. 상용 라이브러리 이용
가장 자주 사용되는 방법으로 Lucy필터나 OWASP antisamy등을 사용한다.
Lucy의 경우는 naver, antisamy는 OWASP에서 만드므로 공신력이 있다.

3. BBCode 사용
글에 스타일을 적용하는 경우, BBCode를 이용한다.
해당 방법은 기존의 < > 를 다른 문자로 대체하여 사용되는 것이다.
다만 이 경우 잘못하면 XSS를 제대로 막지 못하니 주의해야 한다.

추가로 OWASP에서 XSS를 주의하는 7계명을 발표했다.

```
0. 허용된 위치가 아닌 곳에 신뢰할 수 없는 데이터가 들어가는것을 허용하지 않는다.
1. 신뢰할 수 없는 데이터는 검증을 하여라.
2. HTML 속성에 신뢰할 수 없는 데이터가 들어갈 수 없도록 하여라.
3. 자바스크립트에 신뢰할 수 없는 값이 들어갈 수 없도록 하여라.
4. CSS의 모든 신뢰할 수 없는 값에 대해서 검증하여라.
5. URL 파라미터에 신뢰할 수 없는 값이 있는지 검증하여라.
6. HTML 코드를 전체적으로 한번 더 검증하여라.
```

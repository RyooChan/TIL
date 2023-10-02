보통 개발할 때에 반드시 공부하고 적용하는게 페이징일 것 같다.
그리고 아마... 처음에는 offset limit을 써서 적용을 할 것 같다.

이 offset limit에 대해, 그리고 offset limit의 문제점에 대해, 해결 방안에 대해 써보려고 한다.

일단은 간단한 테스트를 위해 

```
CREATE TABLE board (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    heart INT DEFAULT 0
);
```

이런 테이블을 하나 만들어 주자

```
-- 1, 11, 21, 31 .... 이런 식으로 뒷자리가 같으면 같은 좋아요 갯수를 갖도록 데이터 10000개를 만들어준다.
DELIMITER $$
CREATE PROCEDURE GenerateData()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE remainder INT; -- 변수 선언
    
    WHILE i <= 10000 DO
        SET remainder = i % 10; -- 변수에 값을 할당
        
        INSERT INTO board (title, created_at, updated_at, heart)
        VALUES
            (CONCAT('제목', i), NOW() - INTERVAL i DAY, NOW(), 10 + remainder);
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 저장 프로시저 실행
CALL GenerateData();

```

이렇게 데이터를 넣어준다.
참고로 시간은 오늘로부터 하루씩 빼면서 진행했고, 보면 heart(좋아요) 개수는 같은것도 몇개 있을 것이다.

## 요구조건

* 게시글(board)는 두 개의 조건으로 오더링된다.
    * 최신순 정렬
    * 좋아요 순 정렬
        * 좋아요가 같은 경우 추가로 최신순을 통해 정렬한다.

참고로 이거 실제로 개발을 해보면 알겠지만, 막 댓글 많은 순 이런거는 join해서 값을 가져오게 되는 골때리는 경우도 생길 수 있다.
일단 여기서는 그런경우를 제외하고 해보자

## offset limit

ORM을 쓴다면 마이바티스나 JPA querydsl 등등 많은걸 쓰려고 하겠지만, 사실 개인적으로 어차피 어느 수준까지는 결국 얘들은 쿼리를 만드는거라 그게 그거라고 느껴진다.
그래서 걍 MySQL 쿼리로 진행하겠다.

### 최신순 정렬

```
SELECT * FROM board
ORDER BY created_at DESC
LIMIT 10 OFFSET 0;
```

![](https://hackmd.io/_uploads/HJCveIdg6.png)

대충 이런 느낌일 것이다.
다른 페이지를 보려고 한다면

```
SELECT * FROM board
ORDER BY created_at DESC
LIMIT 10 OFFSET 10;
```

![](https://hackmd.io/_uploads/ryiugI_eT.png)

이렇게 가져올 수 있겠지

### 좋아요 순 정렬

다음은 좋아요 순서이다.
사실 데이터가 정렬되어 있으면 offset limit이 알아서 해주므로

```
SELECT * FROM board
ORDER BY heart DESC, created_at DESC
LIMIT 10 OFFSET 0;
```

![](https://hackmd.io/_uploads/r1xixL_g6.png)

이것도 쉽다.
보면 좋아요 순서대로 나오고, 같은 경우 다른걸 찾아올 수 있는게 보인다.

### offset의 장점

보면 알겠지만, offset을 쓰면 코드가 굉장히 간단하다.
그리고 사실상 여러 조건이 추가되는것은 그냥 order by의 조건으로 분기한다고 생각하면 된다.
그렇기 때문에 지금은 좋아요 순서지만, 나중에 뭔가 요상한 내용이 들어간다고 해도 그걸 그냥 가져와서 order by 에 넣어주면 알아서 잘 해줄 것이다.

### offset의 단점

당연한 얘기겠지만(이게 안당연했으면 좋겠지만 슬프게도...) 간단한 코드는 보통 성능이 좋지 않다.

- 성능
    - offset이 작을 때에는 별 문제가 없다.
    - 근데 이게 커지면, 데이터베이스가 offset만큼의 값을 스캔하고 이거를 뛰어넘은 다음에 원하는 값을 가져온다.
        - 말하자면 offset 100이라면 100 이후부터를 가져오는게 아니라 100개를 확인한 후 그 다음으로 간다는 것이다.

이거는 MySQL의 자료구조와 관련이 있는데, MySQL은 트리형 자료구조(B+ Tree)를 갖고 있다.
이렇게 되면 offset을 썼을 때에 그냥 바로 그 위치로 가는게 아니라 리프노드를 통해 확인하면서 가야한다는 것이다.

### 서비스 기업에서의 해결법

사실 서비스 기업에서는 보통 이 경우

- offset limit을 쓰는 경우 몇 건 이상의 경우는 그냥 검색을 안하도록 하는 방법
    - 구글이 이런식으로 하는 것 같다. 1000건 이상은 그냥 못찾더라..
- 인피니티 스크롤 방식
    - 브런치가 이런식으로 진행한다.

### Offset - limit의 단점 확인 및 해결방법

이거를 쓰기 위해 해당 글을 작성했다.
인피니티 스크롤이랑 번호식이랑 뭐 많이 다르냐? 할 수 있는데 사실 많이 다르다.
직접 보면서 말하겠다.

> 여기서는 좋아요, 최신순 오더링을 기준으로 작성하겠다.

### 기존 Offset limit의 방법에서 11 ~ 20 데이터를 가져오는 방식

먼저 처음부터 10개의 데이터를 가져와보자(1 ~ 10)

```
SELECT * FROM board
ORDER BY heart DESC, created_at DESC
LIMIT 10 OFFSET 0;
```

![](https://hackmd.io/_uploads/Skm3lL_x6.png)

이제 다음으로 11 ~ 20 데이터를 가져오겠다.

```
SELECT * FROM board
ORDER BY heart DESC, created_at DESC
LIMIT 10 OFFSET 10;
```

![](https://hackmd.io/_uploads/r11pe8OeT.png)

가져와 졌다.

그럼 이번에는 한번 8001 ~ 8010의 데이터를 가져와 보자

![](https://hackmd.io/_uploads/B1NkbIdx6.png)

잘 가져와 지는 것 같다.

그런데 한번 가져오는 속도를 확인해 보자

![](https://hackmd.io/_uploads/H1TLW8dea.png)

* 0개부터 
    * 0.011초
* 10개부터
    * 0.013초
* 8000개부터
    * 0.024초

이렇게 걸린다.
아까 설명했던 것 처럼, 뒤의 데이터를 가져오게 된다면 속도가 느려지는 문제점이 생길 것이다.

### where 조건을 활용한 페이징 방식

> 이 부분은, 원하는 페이지로의 바로 이동이 불가능하고 이전 페이지의 정보를 활용하여 다음 페이지를 불러올 수 있다.

```
SELECT * FROM board
ORDER BY heart DESC, created_at DESC
LIMIT 10 OFFSET 7999;
```

먼저 이렇게, 8000번째 데이터 이전의 값을 가져와보자.
여기서 id `9992`가 8000번째 데이터가 될 것이다.
이를 활용해서

```
SELECT * FROM board
WHERE 1=1
AND 
(
    (heart = 12 and created_at < '1996-05-24 23:13:40')
    or heart < 12
)
ORDER BY heart DESC, created_at DESC
LIMIT 10;
```

이렇게 해 보자

해설하자면

1. 좋아요 개수가 동일한 경우, 8000번째 데이터보다 늦게 만들어진 게시물을 가져온다.
2. 이후로는 좋아요 개수가 8000번째 데이터보다 적은걸 찾아온다.

이렇게 하는 것이다.
이러면 좋은점은

![](https://hackmd.io/_uploads/HyHqz8Og6.png)

잘 가져와지고

![](https://hackmd.io/_uploads/r1IozU_la.png)

이전 데이터 말고 원하는 값을 가져오기 때문에 offset limit보다 훨씬 속도가 빠르게 나온다.
그리고 이거의 또 하나 좋은점은

현재 방식의 실행계획을 확인해 보면

![](https://hackmd.io/_uploads/rJkkXUOea.png)

일단 아무런 인덱스도 없고, 원하는 값을 가져오지 않는 것을 볼 수 있는데,

```
CREATE INDEX idx_heart_created_at ON board (heart DESC, created_at DESC);
```

다음과 같이 오더링에 쓰이는 `heart`, `created_at`의 복합 인덱스를 구성하면

![](https://hackmd.io/_uploads/rySN7LdlT.png)

쓰이지 않는 데이터를 완벽히 필더링하고 인덱스를 제대로 탈 수 있도록 만들 수 있다.
그러면 이전보다 훨씬 빠르게 확인할 수도 있다.

![](https://hackmd.io/_uploads/rywvE8Oxa.png)

이렇게 말이다.

인덱스를 만들기 간단한것은 덤이다(그냥 오더링 기준으로 하면 되기 때문)
잘 되는지 보기 위해 한번 동일한 좋아요 기준으로 봐보자 (10개씩 진행했기 때문에 가운데 기준으로 정렬해 보자)

```
SELECT * FROM board
ORDER BY heart DESC, created_at DESC
LIMIT 10 OFFSET 8005;
```

![](https://hackmd.io/_uploads/H1TWV8dgp.png)


```
SELECT * FROM board
where 1=1
and 
(
(heart = 11 and created_at < '2023-08-22 23:13:39')
or heart < 11
)
ORDER BY heart DESC, created_at DESC
limit 10;
```

![](https://hackmd.io/_uploads/HJDGVUul6.png)

![](https://hackmd.io/_uploads/BJjd4I_lT.png)

같은 데이터를 보는데, 정말 비교도 안되게 빠른 속도로 데이터를 찾아올 수 있게 된다.

## 정리

* offset limit
    * 장점
        * 구현이 간단하다.
        * 여러 오더링 조건에 대한 적용이 수월하다.
        * 현재 테이블에 많은 정보가 없어도, join 등을 통해 유연하게 조건 추가가 가능하다.
    * 단점
        * 속도가 느리다(뒤로 갈수록 느려진다.)
            * 앞의 데이터를 넘어가야 하므로
        * 인덱스를 태우기 쉽지 않다.
* where조건 페이징
    * 장점
        * 속도가 정말 빠르다.
        * 오더링 관련 인덱스를 구현하면, 추가적인 성능향상도 가능하다.
    * 단점
        * 앞의 데이터의 정보를 가지고 다음 페이지를 가져와야한다.(번호를 통한 이동의 구현이 불가)
            * 인피니티 스크롤을 통한 방식으로 구현할 수밖에 없다.
        * 오더링이 많아질수록 쿼리가 점점 복잡해진다.
        * 상황에 따라 기존의 테이블에 오더링용 데이터를 보관해야 할 수 있다.
            * 여기서 count나 상태 관련 내용이 있을 경우, 그 상태가 변화될 때 마다 데이터가 바뀌어야 할 수 있는데, 여기서의 trade-off가 발생하게 된다.

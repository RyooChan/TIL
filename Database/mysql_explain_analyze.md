## MySQL 8.0부터 추가된 explain analyze 에 대해 ARABOZA!

MySQL에서 쿼리 인덱스랑 실제 동작 관련 테스트를 하는데... 엄청 많은 조건이 있어서 대체 어디서 시간이 오래걸리는지 이런거가 매우 헷갈렸다.
이 때에 아주 유용한게 `explain analyze` 이다.

사실 [이거](https://dev.mysql.com/blog-archive/mysql-explain-analyze/) 보면 매우매우 잘 설명되어있기는 한데 걍 정리해본다.

실제로 경험한 일인데, 상세한 내용을 말하기는 어렵고 통해 그냥 이거의 장점만 훑으려 한다.
일단 기존의 explain 과 다른 점은
- explain은 실제로 SQL 쿼리를 실행하지 않은 상태에서 옵티마이저가 계획한 쿼리 실행 계획을 보여준다.
- explain analyze 는 쿼리를 실제로 실행한 뒤에 그 결과를 통해 실행 계획을 보여준다. 또, 실제로 실행하면서 각 단계에서 처리된 데이터 양과 실행 시간을 보여준다.

정도이다.
즉

- 그냥 계획이 이렇구나가 아니라 실제로 동작하면 어떻게 될지
- 그 동작들의 소요 시간과 데이터 갯수, 그리고 반복 횟수 와 같은 상세한+실질적 정보
- 를 얻을 수 있게 되는 것이다.


솔직히 이게 굳이 필요한가 싶은데 암튼 테스트 데이터를 넣어서 보겠다.

### 대충 데이터 입력

```
CREATE TABLE Authors (
    AuthorID INT AUTO_INCREMENT PRIMARY KEY,
    FullName VARCHAR(255) NOT NULL,
    Email VARCHAR(100),
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

```
CREATE TABLE Articles (
    ArticleID INT AUTO_INCREMENT PRIMARY KEY,
    AuthorID INT,
    Title VARCHAR(255) NOT NULL,
    Content TEXT,
    PublishedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (AuthorID) REFERENCES Authors(AuthorID)
);
```

```
CREATE TABLE Donations (
    DonationID INT AUTO_INCREMENT PRIMARY KEY,
    ArticleID INT,
    DonorName VARCHAR(255),
    Amount DECIMAL(10, 2),  -- 금액은 소수점 두 자리까지 표현
    DonatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ArticleID) REFERENCES Articles(ArticleID)
);
```

```
CREATE INDEX idx_author_id ON Articles(AuthorID);
```

```
CREATE INDEX idx_published_at ON Articles(PublishedAt);
```

```
CREATE INDEX idx_article_id ON Donations(ArticleID);
```

```
CREATE INDEX idx_donated_at ON Donations(DonatedAt);
```

```
CREATE INDEX idx_author_date ON Articles(AuthorID, PublishedAt);
```

뭐 대충 이런 식으로 테이블이랑 인덱스를 설정한다.
그리고 샘플 데이터 입력을 위해


```
DELIMITER //

CREATE PROCEDURE GenerateSampleData()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE j INT;
    DECLARE k INT;
    DECLARE authorID INT;
    DECLARE articleID INT; -- Article ID를 저장할 변수 추가

    -- Authors 데이터 생성
    WHILE i < 50 DO  -- Author 50명 생성
        INSERT INTO Authors (FullName, Email)
        VALUES (CONCAT('Author', i, ' Name'), CONCAT('author', i, '@example.com'));
        SET authorID = LAST_INSERT_ID();

        -- 각 Author에 대해 5개의 Articles 생성
        SET j = 1;
        WHILE j <= 5 DO
            INSERT INTO Articles (AuthorID, Title, Content)
            VALUES (authorID, CONCAT('Title ', j), CONCAT('Content ', j));
            SET articleID = LAST_INSERT_ID(); -- ArticleID를 새로 삽입된 글의 ID로 설정
            
            -- 각 Article에 대해 3개의 Donations 생성
            SET k = 1;
            WHILE k <= 3 DO
                INSERT INTO Donations (ArticleID, DonorName, Amount)
                VALUES (articleID, CONCAT('Donor', k), ROUND(RAND() * 100, 2)); -- 저장된 ArticleID 사용
                SET k = k + 1;
            END WHILE;

            SET j = j + 1;
        END WHILE;

        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;
```

요렇게 프로시저를 만들어서

```
CALL GenerateSampleData();

```

한번 수행해준다.

```
SELECT
    Authors.FullName AS AuthorName,
    Articles.Title AS ArticleTitle,
    Articles.Content AS ArticleContent,
    Donations.DonorName AS DonorName,
    Donations.Amount AS DonationAmount
FROM
    Authors
JOIN Articles ON Authors.AuthorID = Articles.AuthorID
JOIN Donations ON Articles.ArticleID = Donations.ArticleID
ORDER BY
    Authors.FullName, Articles.ArticleID, Donations.DonorName;
```

이렇게 쿼리를 수행하면
![image](https://github.com/RyooChan/TIL/assets/53744363/fbb64ed2-e564-45d2-b983-9d4855222cfb)

뭐가 많이 나올거다.

근데 우리가 필요한건 이게 아니지

### explain을 통한 확인

```
EXPLAIN FORMAT = TREE SELECT
    Authors.FullName AS AuthorName,
    Articles.Title AS ArticleTitle,
    Articles.Content AS ArticleContent,
    Donations.DonorName AS DonorName,
    Donations.Amount AS DonationAmount
FROM
    Authors
JOIN Articles ON Authors.AuthorID = Articles.AuthorID
JOIN Donations ON Articles.ArticleID = Donations.ArticleID
ORDER BY
    Authors.FullName, Articles.ArticleID, Donations.DonorName;
```

explain을 통해 확인해보자.
알아보기 쉽게 하기 위해(어떤 순서로 걸리는지, 그리고 후술할 `explain analyze` 와의 비교를 위해) format은 tree 형태로 했다.

```
| -> Sort: authors.FullName, articles.ArticleID, donations.DonorName
    -> Stream results  (cost=332 rows=752)
        -> Nested loop inner join  (cost=332 rows=752)
            -> Nested loop inner join  (cost=68.7 rows=251)
                -> Table scan on Authors  (cost=5.35 rows=51)
                -> Index lookup on Articles using idx_author_id (AuthorID=authors.AuthorID)  (cost=0.76 rows=4.92)
            -> Index lookup on Donations using idx_article_id (ArticleID=articles.ArticleID)  (cost=0.75 rows=3)
 |
 ```
 
요런 식으로 나올것이다.
사실 근데 음 뭐 알겠어 이런 순서로 동작하고 이런 인덱스가 걸리겠지... 근데 아마 나중에 쿼리가 복잡해지고 데이터가 많다면 어디서 실질적인 시간이 걸리고 얼마나 데이터가 나올까? 에 대해 궁금할것이다.~~(안궁금할수도 있는데 실무에서 쿼리가 복잡해지면 아마 궁금해질것이다. 어떻게 아냐고? 나도 알고싶지 않았어...)~~ 

암튼 

### explain analyze 활용해서 확인해보기

```
EXPLAIN ANALYZE SELECT
    Authors.FullName AS AuthorName,
    Articles.Title AS ArticleTitle,
    Articles.Content AS ArticleContent,
    Donations.DonorName AS DonorName,
    Donations.Amount AS DonationAmount
FROM
    Authors
JOIN Articles ON Authors.AuthorID = Articles.AuthorID
JOIN Donations ON Articles.ArticleID = Donations.ArticleID
ORDER BY
    Authors.FullName, Articles.ArticleID, Donations.DonorName;
```

이렇게 하면 확인이 가능하다.
참고로 저거 썼을때 `select` 쪽에 빨간줄 그어지면 아마 MySQL 버전이 낮은걸거다.

`select version()`

으로 MySQL 8.0 이상인지 확인해보자.

돌려보면

```
| -> Sort: authors.FullName, articles.ArticleID, donations.DonorName  (actual time=4.36..4.53 rows=752 loops=1)
    -> Stream results  (cost=332 rows=752) (actual time=0.283..3.67 rows=752 loops=1)
        -> Nested loop inner join  (cost=332 rows=752) (actual time=0.263..3.06 rows=752 loops=1)
            -> Nested loop inner join  (cost=68.7 rows=251) (actual time=0.229..0.878 rows=251 loops=1)
                -> Table scan on Authors  (cost=5.35 rows=51) (actual time=0.158..0.173 rows=51 loops=1)
                -> Index lookup on Articles using idx_author_id (AuthorID=authors.AuthorID)  (cost=0.76 rows=4.92) (actual time=0.00453..0.0131 rows=4.92 loops=51)
            -> Index lookup on Donations using idx_article_id (ArticleID=articles.ArticleID)  (cost=0.75 rows=3) (actual time=0.00677..0.0079 rows=3 loops=251)
 |
 ```
 

요렇게 나온다.

위의 `explain` 쿼리와 동작 자체는 사실 거의 똑같다. (뭐 다른 경우가 있을지도 모른다는데 나는 아무리 테스트해도 못찾았다. 예상 데이터와 실제 데이터가 다른 경우만 찾아볼 수 있었다. 옵티마이저의 실행계획이 아주 좋다는 증거일듯.)
다만 보여지는 정보가 다르다. 저 정보들은

- actual time : 각 연산 단계에서 실제 소요된 시간(앞은 연산 시작후 첫번째 행 실행까지 걸린 시간 / 뒤는 전체 연산 완료까지 걸린 시간)
- rows : 실제로 처리된 데이터 행 수
- loops : 해당 연산의 반복 횟수

이다.
이를 통해 조금 더 상세한 정보를 알 수 있다.

### 정리

- `explain`을 썼을 때 인덱스도 잘 걸린것 같고 별 문제가 없어보이는데...? 싶으면 `explain analyze`를 활용해보자. 실제로 시간이 오래 걸리는곳 / 데이터가 많이 나타나는곳 의 확인이 가능하다.
- 어디서 얼마나 시간이 소요되는지, 데이터가 몇개 나타나는지, 몇번 쿼리를 도는지 등의 정보도 이를 통해 알 수 있다.
- 근데 이거 실제로 한번 쿼리를 돌려서 확인하는거다. 실행 비용을 확인하고 해보자.
- 실제로 쿼리를 돌릴 때에 DB 상황에 따라 다른 결과가 수행될 수 있다. 여러번 체크해보자.
- 참고로 `insert`, `update`, `delete`, `table` 과 같은 쿼리에도 수행 가능하다.
    - 근데 데이터가 바뀌지는 않더라. 아무리 공식 문서를 찾아 해메어도 못찾아서.. 걍 [직접 질문](https://forums.mysql.com/read.php?20,724407) 남겼다. 언젠가 답변이 달리겠지...

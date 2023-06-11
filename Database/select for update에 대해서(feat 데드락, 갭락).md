# select for update가 뭘까??

이름에서 알 수 있듯, 업데이트를 하기 위해 검색을 하는 것이다.
즉, 이후의 Update를 위해 select를 진행하는 것이라 할 수 있다.

## 그래서 뭐요?

사실 그냥 보면 뭐 그런갑다.. 할 수 있다.
근데 사실 저 `select for update`에서 중요한 것은, **Select된 행의 UPDATE가 실제로 커밋되기 전까지 다른 트랜잭션이 이 행을 수정할 수 없다**는 것이다.

## 뭔소리에요??

그러니까 여러 트랜잭션이 하나의 레코드에 접근한다고 가정할 때, 현재 `select for update` 수행중인 내용은 변경 불가하다는 것이다!!

아래에서 실제로 보여주겠다.

## 실습

### Table

![image](https://github.com/RyooChan/TIL/assets/53744363/fcad0ff0-05d6-4385-ac96-dd207aa76ef6)

먼저 `human` 테이블에 다음과 같은 데이터가 있다.

3개의 트랜잭션을 수행해 볼 것이다.
참고로 각각의 트랜잭션은 **꼭!!** 다른 세션에서 수행시키도록 하자.

### Transaction1

1번 트랜잭션을 만들고, 여기서 select for update를 **1번 데이터** 에 시도해 보겠다.

```
START TRANSACTION;

SELECT * FROM human WHERE no=1 FOR UPDATE;
```

![image](https://github.com/RyooChan/TIL/assets/53744363/6e5e8129-9bf1-4d4c-9009-28fcf9e971ab)

수행하면 일단 SELECT의 결과로 다음의 데이터가 출력되고, 현재 `commit`되지는 않았으므로 트랜잭션은 계속 돌고 있을 것이다.

### Transaction2

2번 트랜잭션도 동일하게 시도해 보겠다.

```
START TRANSACTION;

SELECT * FROM human WHERE no=1 FOR UPDATE;
```

![image](https://github.com/RyooChan/TIL/assets/53744363/35764063-49fc-4f25-b836-f8e4ab602d58)

엥?? 왠지 수행이 안된다!!

### Transaction3

3번 트랜잭션에서는 왠지 모르겠지만 그냥 SELECT를 해본다.

```
SELECT * FROM human WHERE no=1;
```

![image](https://github.com/RyooChan/TIL/assets/53744363/9cbf0af3-3fab-4c26-b96c-c32d4c5e0b1a)

여기서는 잘 나온다...

### Transaction2 update

눈치가 빠른 사람은 이미 이게 왜 이런지 알게 되었을 것이다.
2번 트랜잭션의 이전 쿼리를 멈춘 후에 이번에는 update를 해보자!

```
START TRANSACTION;

UPDATE human SET look = 'so handsome' WHERE no=1;
```

![image](https://github.com/RyooChan/TIL/assets/53744363/1bf1cfa5-00e1-4e98-8a4c-53b51d02f6fd)

이번에도 빙글빙글 돌아가고 안되고 있을 것이다.

### Transaction1 update

이제는 1번 트랜잭션에서 업데이트를 해보자!

```
UPDATE human SET look = '개 존 잘' WHERE no=1;
```

### Transaction 3 select

한번 Transaction3에서 이거를 확인해 보자

```
SELECT * FROM human WHERE no=1;
```

![image](https://github.com/RyooChan/TIL/assets/53744363/8418435f-4269-4b1b-ac66-405980e3d26e)

요렇게 나왔을 것이다.

### Transaction 1 COMMIT

이제 Transaction1 에서 커밋해보자

```
COMMIT;
```

### Transaction 2 확인

![image](https://github.com/RyooChan/TIL/assets/53744363/4a19eb45-e9ab-401a-b67d-74f87a59c7ce)

완료됐다.

### Transaction 3 select

```
SELECT * FROM human WHERE no=1;
```

![image](https://github.com/RyooChan/TIL/assets/53744363/de932a87-4f81-4698-bc03-8a397d42b41c)

바꼈다!!

방금 전까지 1번 트랜잭션의 값이었다가 commit하고 나니까 2번 트랜잭션의 값이 되었다.

## 요약하자면

이게 이렇게 되는 이유는 다음과 같다.

1. 1트랜잭션이 `select for update`를 통해 해당 레코드(no 1) 선점
2. 2트랜잭션의 `select for update`, `update`와 같은 no 1 레코드에의 데이터 변화를 수반하는 접근이 막혀있음
3. 단순히 검색만을 하는 3트랜잭션의 `select`는 수행 가능함
4. 그럼에도 불구하고 2트랜잭션에서 `update`를 시도
5. 막혀있다 == lock 즉, 2트랜잭션에서 수행한 `update`는 수행되지 않았다는게 아니라 대기하고 있는 중이다.
6. 1트랜잭션에서 `update` 진행
7. 해당 `update`내용은 레코드에 적용되었지만, 아직 commit되지는 않음 (Tran 시작 후 COMMIT하지 않았기 때문)
8. 따라서 2트랜잭션 `update`는 아직 대기중이다.
9. 그래서 이 때에 3트랜잭션에서 `select`하면 1트랜잭션의 `update`값이 보인다. (참고로 이거는 `UNDO`데이터를 보여주는 것이다. 이후 설명함)
10. 1트랜잭션에서 `commit`수행
11. `select for update`가 완료되어 선점 락 해제
12. 2트랜잭션의 `update` 수행됨
13. 다시 3트랜잭션에서 `select`해보면 마지막으로 `update`한 2트랜잭션의 데이터로 변경되어 있다.

참고로 여기서 `select`는 가능하고 `update`와 같은 쿼리가 불가능한 이유는 결국 `select for update`가 배타적 잠금을 하기 때문인데, 그게 뭔지는 [여기](https://hello-backend.tistory.com/219)서 확인할 수 있다.

## 그래서 이걸 왜 쓰나요??

다양한 용도가 있을 것이다.

예를 들어 은행 이자를 생각해 보자.

1. 현재 잔고에 100원이 있음
2. 매월 1일에 이자 10% 붙음
3. 내가 12월 31일 11시 59분 59.9999999초에 10000원을 저금함

이 흐름대로면 12월 31일의 총 금액 10100원에 이자 101원이 붙어야 할 것이다.
근데 만약에 DB에서 10000원이 입금되기 전 -> 아직 100원인 상태에서 1일이 되어 이자가 계산된다면??
분명 아직 1일이 되기 전에 수행되었는데도 불구하고 100원에 대해서만 이자가 붙어 1원이 붙을 것이다.
그래서 이 전에 배타적 잠금을 걸어 이런 불상사를 예방하는 것이다.

이런 식으로, 이는 

* 데이터 변경의 일관성 유지
* 순서가 중요한 update의 수행

등에 매우 요긴하게 사용될 수 있을 것이다.

## 단점

근데 뭔가 계속 마음에 걸리는게 있다.
1번 트랜잭션이 `select for update`중인 레코드에 2번 트랜잭션이 `update`를 실시하면 걍 빙글빙글 돌고 있다.
이는, 계속해서 2번 트랜잭션은 lock에 걸려있다는 것이다...

그러면 1번 트랜잭션이 수행되지 않으면 **그 레코드에 대해** 2번 트랜잭션은 계속 lock이 걸려 있겠네??

## 단점!!!!!!

위에 단점에서 "그 레코드에 대해" 라고 적혀있었다.
근데 이거 아주 골때리게 락 에스컬레이션이 발생할 수 있다.

먼저 DB는 [인덱스](https://hello-backend.tistory.com/257)를 사용하는데, 인덱스와 락의 콜라보로 데드락이 걸리게 될 수 있다.

### 실습

일단 이번 실습에서는 간단하게 클러스터 인덱스를 통해 진행해 보겠다.
클러스터 인덱스는 자동으로 PK에 대해 설정된다.

### 1. 데이터 초기화

```
TRUNCATE TABLE human;
```

human의 데이터를 다 날려버린다.

### 2. 트랜잭션1에서의 `select from update`

```
TRANSACTION;

SELECT * FROM human WHERE no=1 FOR UPDATE;
```

**없는 데이터 1**에 대해 SELECT FOR UPDATE를 걸어줘보자

### 3. 트랜잭션2에서의 `select from update`

```
TRANSACTION;

SELECT * FROM human WHERE no=2 FOR UPDATE;
```

**없는 데이터 2**에 대해 SELECT FOR UPDATE를 걸어줘보자

여기서 trasaction1과 transaction2는 각각 다른 데이터에 분명히 트랜잭션을 걸어줬다.

### 4. 트랜잭션 3에서 `insert`

```
TRANSACTOIN;

INSERT INTO human (no, name, look) values (18, 'ryoochan', '존잘그잡채');
```

트랜잭션 3에서 insert를 진행하면 안된다 이거...
분명 락이 걸렸는데, 이게 트랜잭션1,2에서 설정해준 레코드가 아니다ㅋㅋ

### 5. 트랜잭션 1에서 'insert'

```
TRANSACTOIN;

INSERT INTO human (no, name, look) values (11, 'ryoochan', '존잘그잡채');
```

이것도 안될것이다.

### 6. 트랜잭션 2에서 'insert'

```
TRANSACTOIN;

INSERT INTO human (no, name, look) values (24, 'ryoochan', '존잘그잡채');
```

![image](https://github.com/RyooChan/TIL/assets/53744363/fa5a8a14-1728-4e8a-b4f8-9b02bdbed7eb)

```
INSERT INTO human (no, name, look) values (11, 'ryoochan', '존잘그잡채')	Error Code: 1213. Deadlock found when trying to get lock; try restarting transaction	0.154 sec

```

데드락 발생을 확인할 수 있다.

## 단점!!!!

여기서 보이듯, 만약 없는 레코드에 락을 걸고 나면 이게 생각한 것과 다르게 동작할 수 있다.
그리고 만약에 이 상태에서 여기저기서 insert를 한다? 싹다 데드락이 된다.

이게 갭 락은 조회한 데이터의 바로 인접한 다음 레코드까지의 데이터를 잠궈서 그 안에 다른 데이터가 들어올 수 없게 하는 역할을 하는데, 지금 저 경우 no=1 no=2에 락을 걸면 얘들이 바로 다음 인접한 값을 찾아가기 때문이다.

근데 문제는 이게 실제로는 아무런 데이터가 없기 때문에, 이것들은 실제로 자기 레코드도, 다음 레코드도 알지 못한다.

실제로 한번 락을 확인해 보면

![image](https://github.com/RyooChan/TIL/assets/53744363/36544a65-6580-4f7e-a6b2-abb3083f208f)

요렇게 나온다.

참고로 X락은 exclusive lock, IX락은 intention exclusive lock이다.
즉 Table 단위로는 여러 트랜잭션에서 접근이 가능한 IX락이, 레코드 단위로는 이게 불가능한 X락이 걸리는 것이다.

여기서 중요한 것은 record lock이 걸린 값인데, LOCK_DATA를 보면 `supremum pseudo-record`라고 적혀 있다.

이녀석이 뭐냐면 

![image](https://github.com/RyooChan/TIL/assets/53744363/ec3f4807-f630-452d-8dfd-8654165230e5)

~~Thank you chatGPT!!~~

간단하게 말해서 최대값이다.

즉 갭락이 레코드를 건 값 ~ 끝까지 다 걸린것이다!!!!

이게 이렇게 데이터가 없는 경우는 끝까지 싹다 걸려버리고, 중간에 있는 경우라고 해도 어차피 다음 레코드 전까지 갭 락이 걸려서 그 안의 값에 대해서는 insert가 불가능해 질 것이다.

예를들어 
2, 5, 14, 20, 55 의 값이 있을 때에 21에 대해 `select for update`를 걸면
21 ~ 55 까지는 gap lock이 걸리는 식이다.

따라서 이 경우에도 해당 insert진행시 deadlock의 원흉이 될 수 있다.

## 결론

* 장점
    * 동시성 제어에 잘 사용된다. -> 일관성 및 무결성 유지
        * 순서를 중요하게 보기 때문이다.
    * 데이터 일관성을 유지시켜준다.
        * 행을 잠근 상태에서는 다른 트랜잭션의 접근이 안되기 때문이다.
* 단점
    * 데드락
        * 상술
    * 성능 저하
        * 락을 걸고 그동안 다른 트랜잭션의 접근이 힘들기 때문이다.

개인적인 결론은 이거는 진짜 중요한 데이터에만 걸어주고, 자주 쓰지 않는게 좋을 것 같다.
특히 대규모 쿼리가 돌아가는 배치 등에서 쓰면 진짜 큰일날듯!

# save랑 saveAll(feat. Transactional)

[Spring Data JPA](https://github.com/RyooChan/data-jpa)를 통해 다양한 쿼링을 쉽게 해줄 수 있다.
근데 만약에 여러 insert를 해줘야 하는 경우가 있다면 어쩔까??

## 상황

일단 Spring Data JPA에 대해 안다고 가정한다.

여러 값을 저장할 때에

> save방식

```
넣을값들.forEach(
    index -> 저장레포지터리.save(index)
)
```

이거랑

> saveAll방식

```
저장레포지터리.saveAll(넣을값들);
```

의 차이가 뭘까??

## save

일단 save를 한번 본다.

![](https://i.imgur.com/z8DXxgc.png)

## saveAll

이제 saveAll을 본다

![](https://i.imgur.com/rt75LiB.png)

## 뭐가 다른겨?

사실 saveAll을 보면

1. 최초에 Transactional 어노테이션 실행
2. 해당 함수 내에서 save함수 실행
3. 저 save는 save함수임

이렇게 간다.
얼핏 보기에는 어차피 저기서 불러주는 거니까 똑같고, 그냥 save들을 편하게 하려고 saveAll을 쓰는 것처럼 보인다.

한번 [여기](https://hello-backend.tistory.com/212)에서 기본 설명이랑 propagation을 보고 오자.
propagation에서 REQUIRED가 기본 설정으로 되어 있다.

여기서 중요한 점은

* 현재 활성화된 트랜잭션이 있으면 그 안에서 실행

이라는 설명이다.
이를 보고 다시 한번 살펴보자.

## saveAll은 그니까

* 최초에 Transactional 어노테이션 실행
    * AOP를 통해 전체 값들의 ACID를 보장하는 proxy 객체 생성!
* 해당 함수 내에서 save함수 실행
    * save함수의 Transactional 어노테이션 실행
        * 어? 근데 부모 Transactional이 있네?
            * 그럼 그냥 여기서 따로 안만들고 함수를 실행하면 되겠다.
* save함수 로직만 실행

이렇게 된다.

## 그럼 save함수 여러개는??

하나하나 실행한다고 하면

* save실행
    * 여기서 Transactional 어노테이션 실행
        * 부모 Transactional이 없네?
            * 하나 만들자
* 다음 for문에서 save 실행
    * 얘는 다른곳에서 돌아감
        * 부모 Transactional이 없네?
            * 하나 만들자
* 계속 이렇게 간다.

이런식으로 진행된다.

## 그럼 비교하자면

* saveAll은 부모 트랜잭션 내에서 save함수의 로직만 실행
* save의 경우는 매번 트랜잭션까지 AOP해서 proxy계속 만듬

이렇데 된다.

## 따라서

save를 계속 해주는 경우는, 속도도 느려지고 리소스도 낭비된다.
그니까 여러 값을 해줄때에는 saveAll을 쓰자!

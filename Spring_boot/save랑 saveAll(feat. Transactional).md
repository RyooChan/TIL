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

이 REQUIRED는 상위 트랜잭션을 살펴보고, 부모 트랜잭션이 있으면 거기 참여하고 없으면 새로 생성하는 로직이다.

근데 한가지 추가사항은 바로 **Bean객체 내에서 내부 함수를 호출하면 Transaction 어노테이션을 실행하지 않는다**는 점이다.

## saveAll은 그니까

* 최초에 Transactional 어노테이션 실행
    * AOP를 통해 전체 값들의 ACID를 보장하는 proxy 객체 생성!
* 해당 함수 내에서 save함수 실행
    * save함수의 Transactional 어노테이션은 아예 타지 않는다
* save함수 로직만 실행

이렇게 된다.

## 그럼 save함수 여러개는??

하나하나 실행한다고 하면

* save실행
    * 여기서 Transactional 어노테이션 실행
        * propagation 옵션을 통한 추가옵션 수행
            * 이후 save로직 수행
* 다음 for문에서 save 실행
    * 또 servive에서 bean객체내의 save메서드 호출
        * Transactional 어노테이션 실행
            * 끼요오옷
* 계속 이렇게 간다.

이런식으로 진행된다.

## 그럼 비교하자면

* saveAll은 동일 bean객체(둘다 repository내에 구현되어싰음)내에서 save함수의 로직만 실행
* save의 경우는 외부 bean이 AOP로 돌아가서 매번 transactional 어노테이션의 기능 확인

이렇데 된다.

## 따라서

saveAll()을 사용하면 여러번 트랜잭셔널 확인 없이 동작해서 시간낭비를 줄일수 있다!!
라는 것.

그니까 다수의 insert진행시 saveAll()을 쓰자구요

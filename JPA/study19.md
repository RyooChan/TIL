## 한계 돌파

* study16에서 이어지는 내용이다.

이전에 컬렉션을 페치조인하면 일대다 조인이 발생하여 페이징을 할 수 없다고 했다.
일을 기준으로 페이징 하고싶은데, DB에서 데이터는 다를 기준으로 row가 생성되기 때문이다.

그러면 페이징 + 컬렉션 엔티티를 함께 조회하려면 어떻게 해야할까??

페이징+컬렉션 엔티티 조회를 코드도 단순하고 성능 최적화도 보장하는 방법이 있다.

1. xToOne관계를 모두 페치조인한다. 이 xToOne관계들은 딱히 데이터 row수 증가에 영향을 끼치지 않기 때문이다.
2. 컬렉션은 지연 로딩으로 조회한다.
3. 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size, @BatchSize를 적용한다.

![](https://i.imgur.com/4Pr3egg.png)
![](https://i.imgur.com/hRiLGBT.png)


```
select orderitems0_.order_id as order_id5_5_1_, orderitems0_.order_item_id as order_it1_5_1_, orderitems0_.order_item_id as order_it1_5_0_, orderitems0_.count as count2_5_0_, orderitems0_.item_id as item_id4_5_0_, orderitems0_.order_id as order_id5_5_0_, orderitems0_.order_price as order_pr3_5_0_ from order_item orderitems0_ where orderitems0_.order_id in (?, ?)
select orderitems0_.order_id as order_id5_5_1_, orderitems0_.order_item_id as order_it1_5_1_, orderitems0_.order_item_id as order_it1_5_0_, orderitems0_.count as count2_5_0_, orderitems0_.item_id as item_id4_5_0_, orderitems0_.order_id as order_id5_5_0_, orderitems0_.order_price as order_pr3_5_0_ from order_item orderitems0_ where orderitems0_.order_id in (4, 11);
```


```
select item0_.item_id as item_id2_3_0_, item0_.name as name3_3_0_, item0_.price as price4_3_0_, item0_.stock_quantity as stock_qu5_3_0_, item0_.artist as artist6_3_0_, item0_.etc as etc7_3_0_, item0_.author as author8_3_0_, item0_.isbn as isbn9_3_0_, item0_.actor as actor10_3_0_, item0_.director as directo11_3_0_, item0_.dtype as dtype1_3_0_ from item item0_ where item0_.item_id in (?, ?, ?, ?)
select item0_.item_id as item_id2_3_0_, item0_.name as name3_3_0_, item0_.price as price4_3_0_, item0_.stock_quantity as stock_qu5_3_0_, item0_.artist as artist6_3_0_, item0_.etc as etc7_3_0_, item0_.author as author8_3_0_, item0_.isbn as isbn9_3_0_, item0_.actor as actor10_3_0_, item0_.director as directo11_3_0_, item0_.dtype as dtype1_3_0_ from item item0_ where item0_.item_id in (2, 3, 9, 10);
```



---

이렇게 하면

1. 처음에 xToOne으로 fetch join으로 만들어진 데이터들을 가져온다.
2. hibernate.default_batch_fetch_size 에 설정한 데이터만큼 추가로 in 쿼리를 사용하여 데이터를 더 뽑아온다.
3. 그리고 그 Many쪽과 join되어 있는 데이터를 또 in을 사용해서 가져와준다..

이를 사용하면

**최적화도 엄청 잘되고(size를 100으로 설정하면 1000개를 가져온다 해도 10번의 루프밖에 돌지 않는다.), 페이징도 처음에 fetch join으로 가져온 것을 기반으로 진행하여서 가능하다!!!**

---

추가로 해당 방식은 기존 방식에 비해 정규화된 데이터를 제공한다.

예를 들어 기존처럼 모든 데이터를 싹다 fetch하는 경우는 쿼리 자체는 한번에 끝나지만, 데이터 자체는 많은 중복이 발생하게 된다.
이는 DB -> 어플리케이션으로의 통신이 진행될 때에 전송되는 데이터가 많다는 것을 뜻하며 용량 이슈로 이어지게 된다.

반대로 만약 처음에 xToOne만을 fetch하고, 이후 해당 방식으로 진행하게 된다면
1. 처음에 fetch한 데이터는 정확하게 원하는 데이터만을 중복 없이 가진다.
2. in으로 추가로 가져온 데이터는 중복이 없다.
3. 만약 in으로 가져온 데이터와 또 연결되어 있는 데이터가 존재하는 경우, 이곳에서도 중복 없이 데이터를 가져올 수 있다.

즉 해당 방식을 사용하는 경우는 정규화된 상태로 DB조회를 진행할 수 있다는 장점 또한 가진다.

---

추가로 하나씩 적용하고 싶으면 엔티티에 @BatchSize 를 쓰면 된다.

### 장점
* 쿼리 호출 수가 1+N에서 1+1로 최적화된다!!
* 조인보다 DB데이터 전송량이 최적화 된다.(하나씩 싹다 조회하기 때문에 중복이 없다)
    * 기존 페치 조인 방식과 비교하면 쿼리 호출 수는 증가하지만 DB데이터 전송량은 감소한다.
* 페이징이 가능해진다!!

### 결론
xToOne관계는 페치조인하고, LAZY로 선언해서 바로 가져오지 않도록 하고 hibernate.default_batch_fetch_size로 최적화하자.

참고로 default_batch_fetch_size는 100 ~ 1000사이를 선택한다.
1000넘어가면 오류를 일으키는 DB가 가끔 있기도 하고 DB에 부하가 걸릴수도 있다.

부하가 없는 선에서 늘려가면서 하면 될듯싶다.

---


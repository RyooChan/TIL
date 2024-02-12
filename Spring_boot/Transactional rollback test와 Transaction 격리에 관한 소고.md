## Transactional rollback test와 Transaction 격리에 관한 소고

다들 알다시피, @SpringBootTest 에서 @Transactional 을 설정하면, 테스트 환경에서 진행한 내용이 실제로 DB와 격리된다.  
즉, 테스트에서 뭘 하든 간에 실제 DB에 적용되지 않는다는 것이다.

나는 이거를 통해 여러 테스트를 해 보았는데(사실 근데 혼자 테스트할때는 거의 맨날 넣었다) 이번에 좀 생각치도 못한 변수를 마주해서 정리한다.

### Transactional의 기본 propagation은 `REQUIRED`이다.

기본적으로 Spring Boot의 [propagation](https://hello-backend.tistory.com/212)은 `REQUIRED`로 되어있고, 이건 이전에 활성화된 트랜잭션이 있다면 그 안에서 따로 새로운 트랜잭션의 호출 없이 거기 참가한다는 것이다.

### Transactional은 기본적으로 원자성을 보장한다.

당연한 얘기지만, 기본적으로 트랜잭션은 원자성을 보장하여 하나의 트랜잭션 내에서 실패하면 그 내용이 적용되지 않는다.

### 상위 Transactional 안에서 호출되는 경우

```
    @Test
    @Transactional
    public void 물건_구매_실패_테스트() {
        Store store = this.storeService.saveStore("store", StoreType.KOREAN);
        Item item = this.itemService.itemSave(store.getStoreId(), "비빔밥", 8000, 1);
        Customer c1 = this.customerService.customerSave("c1", 5000);
        Customer c2 = this.customerService.customerSave("c2", 10000);
        Customer c3 = this.customerService.customerSave("c3", 10000);

        {
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> this.customerService.buyItem(c1.getCustomerId(), item.getItemId())
            );
            assertThat(illegalStateException.getMessage()).isEqualTo("소지금보다 비싼 물건을 구매할 수 없습니다.");
        }

        {
            CustomerItem customerItem = this.customerService.buyItem(c2.getCustomerId(), item.getItemId());
            assertThat(customerItem.getCustomerId()).isEqualTo(c2.getCustomerId());
            assertThat(customerItem.getItemId()).isEqualTo(item.getItemId());
        }

        {
            IllegalStateException illegalStateException2 = assertThrows(IllegalStateException.class,
                () -> this.customerService.buyItem(c3.getCustomerId(), item.getItemId())
            );
            assertThat(illegalStateException2.getMessage()).isEqualTo("남은 재고가 없습니다.");
        }

    }
```

```
    @Transactional
    public CustomerItem buyItem(long customerId, long itemId) {
        Item item = this.itemService.minusRemained(itemId);
        Customer customer = this.minusPoint(customerId, item);
        CustomerItem customerItem = this.customerItemService.customerItemSave(customer.getCustomerId(), item.getItemId());
        return customerItem;
    }
```

위에서 말한대로  
SpringBootTest에서 Transactional을 걸고, 거기서 여러개의 테스트를 호출하는 경우 모든 테스트가 상위 Test의 Transactional에 참가한다.

내가 겪은 상황은

-   `물건 구매` 메서드를 호출하면 내부적으로 3개의 비즈니스 로직을 호출한다.
    -   물건 갯수 빼기
    -   유저 포인트 빼기
    -   유저가 구매한 물건 데이터 입력

그리고 하나의 테스트 메서드에서는

1.  유저가 물건을 살 때 가지고있는 포인트보다 비싼 물건을 구매한다.
2.  2번째의 유저 포인트 빼기 로직에서 Exception 발생

이렇게 되고, 바로 동일한 테스트 메서드에서 정상 구매 케이스를 입력했다.

그런데 왠지 모르겠는데 물건의 갯수가 이미 빠져있는 것으로 보였다.  
그래서 이거 왜지..? 했는데 생각해보니까 그럴만한게 호출된 `물건 구매` 메서드에서는 Transactional이 없으니까 안될만 한것 같았다.

### 그래서 호출되는 Transactional의 Propagation을 REQUIRES\_NEW 로 설정 - 실패

```
    @Test
    @Transactional
    public void 물건_구매_실패_테스트() {
        Store store = this.storeService.saveStore("store", StoreType.KOREAN);
        Item item = this.itemService.itemSave(store.getStoreId(), "비빔밥", 8000, 1);
        Customer c1 = this.customerService.customerSave("c1", 5000);
        Customer c2 = this.customerService.customerSave("c2", 10000);
        Customer c3 = this.customerService.customerSave("c3", 10000);
        em.flush();
        em.clear();

        {
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> this.customerService.buyItem(c1.getCustomerId(), item.getItemId())
            );
            assertThat(illegalStateException.getMessage()).isEqualTo("소지금보다 비싼 물건을 구매할 수 없습니다.");
        }
        em.flush();
        em.clear();

        {
            CustomerItem customerItem = this.customerService.buyItem(c2.getCustomerId(), item.getItemId());
            assertThat(customerItem.getCustomerId()).isEqualTo(c2.getCustomerId());
            assertThat(customerItem.getItemId()).isEqualTo(item.getItemId());
        }
        em.flush();
        em.clear();

        {
            IllegalStateException illegalStateException2 = assertThrows(IllegalStateException.class,
                () -> this.customerService.buyItem(c3.getCustomerId(), item.getItemId())
            );
            assertThat(illegalStateException2.getMessage()).isEqualTo("남은 재고가 없습니다.");
        }
        em.flush();
        em.clear();

    }
```

```
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CustomerItem buyItem(long customerId, long itemId) {
        Item item = this.itemService.minusRemained(itemId);
        Customer customer = this.minusPoint(customerId, item);
        CustomerItem customerItem = this.customerItemService.customerItemSave(customer.getCustomerId(), item.getItemId());
        return customerItem;
    }
```

하위에서 불려온다면 아예 `상위 트랜잭션 내에서 새로운 트랜잭션을 만들기` 로 한다면 문제가 해결되지 않을까? 싶었다.  
그래서 REQUIRES\_NEW 를 통해 호출되는 트랜잭션도 새로운 트랜잭션을 만들려 했다.

근데 그랬더니 아예 처음에 불러오는 테스트에서 아예 물건의 data가 없다고 떴다!!!  
뭐야 이건 왜 또 했는데, 생각해보니 상위 Transaction에서 아직 값이 입력되지 않고(스프링에서 프록시로 처리되는거라서 아예 DB에 없고 아직은 영속성 컨텍스트 내에 있는 상황) 하위에서는 없는 데이터를 불러오는거니까 그럴만 한것 같았다.

그래서 아예 given / when / then 과 각각의 로직 사이에 `em.flush()` `em.clear()` 를 해 주었다.

그런데도 이게 제대로 작동하지를 않았다.  
이유를 생각해 보니, Spring boot의 `REQUIRES_NEW`의 경우 serializable 즉 완전한 격리 수준을 갖기 때문에 이전에 호출한 데이터의 값이 commit되지 않으면 이를 확인하기가 불가능하겠다는 생각이 들었다.

+) 그리고 추가로, 해당 메서드의 경우 실제로 활용할 때에는 부모 트랜잭션에 합류하는게 일반적일텐데(구매를 진행하는 역할을 하기 때문에 여기서 실패하면 모든 로직 또한 실패하는게 이치에 맞을것이다.) 테스트 케이스를 위해 트랜잭션을 하나 더 만드는게 과연 맞을까? 하는 생각도 들었다.

### 아예 Test에서 Transactional을 빼고 사용

```

    @Test
    public void 물건_구매_실패_테스트() {
        Store store = this.storeService.saveStore("store", StoreType.KOREAN);
        Item item = this.itemService.itemSave(store.getStoreId(), "비빔밥", 8000, 1);
        Customer c1 = this.customerService.customerSave("c1", 5000);
        Customer c2 = this.customerService.customerSave("c2", 10000);
        Customer c3 = this.customerService.customerSave("c3", 10000);

        {
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> this.customerService.buyItem(c1.getCustomerId(), item.getItemId())
            );
            assertThat(illegalStateException.getMessage()).isEqualTo("소지금보다 비싼 물건을 구매할 수 없습니다.");
        }

        {
            CustomerItem customerItem = this.customerService.buyItem(c2.getCustomerId(), item.getItemId());
            assertThat(customerItem.getCustomerId()).isEqualTo(c2.getCustomerId());
            assertThat(customerItem.getItemId()).isEqualTo(item.getItemId());
        }

        {
            IllegalStateException illegalStateException2 = assertThrows(IllegalStateException.class,
                () -> this.customerService.buyItem(c3.getCustomerId(), item.getItemId())
            );
            assertThat(illegalStateException2.getMessage()).isEqualTo("남은 재고가 없습니다.");
        }

    }
```

```
    @Transactional
    public CustomerItem buyItem(long customerId, long itemId) {
        Item item = this.itemService.minusRemained(itemId);
        Customer customer = this.minusPoint(customerId, item);
        CustomerItem customerItem = this.customerItemService.customerItemSave(customer.getCustomerId(), item.getItemId());
        return customerItem;
    }
```

가장 간단하고 로직적으로 확인하기도 편한 방법이다.  
상위에 트랜잭션이 없으므로 각각의 로직이 문제없이 돌아갈 수 있고 논리적으로 볼 때에도 3명의 사람이 구매할 때에는 각각이 한 트랜잭션 내에서 수행될일도 없으므로 `buyItem`의 로직이 문제없이 돌아가는지만 보면 되기 때문이다.  
다만 이게 테스트 완료 후에는 더미 데이터라고 해야할까? 테스트를 위해 만든 데이터들이 남는 문제가 생겼다.  
나는 아예 테스트를 위한 프로젝트를 진행하기도 했고, 보통 사람들을 보면 실서버랑 테스트용 환경이 분리되어 있기 때문에 일단은 테스트 데이터를 남기거나 걍 테스트 후에 다 지우거나 하자! 라는 결론에 도달했다.  
근데 나는 테스트 데이터가 그냥 남으면 좀 보기 싫어서 지우기로 했다.

```
    @AfterEach
    void afterTest() {
        customerRepository.deleteAll();
        itemRepository.deleteAll();
        storeRepository.deleteAll();
    }
```

## 결론

-   Spring Boot Test에서의 Transactional은 확실히 매력적인 기능이다. 따로 데이터를 관리할 필요 없이 처리해주니까.
    -   그런데 이거를 잘 알고쓰지 않으면 테스트 과정에서 분명 문제에 부딪힐 것이다.
-   Transactional의 propagation은 장단점과 용도, 그리고 내부 로직이 모두 다르므로 이를 잘 확인하자.
    -   내가 공부하면서 느낀점은 결국 이것도 Spring의 annotation기반이므로 AOP와 밀접한 연관이 있다. -> 스프링의 철학에 대해 더 잘 알았으면 이런 문제를 겪을 일도 없었을 것이라는(근데 솔직히 그래도 겪었을테지만 더 빠르게 해결했겠지) 거다.
-   테스트 로직에서 내가 무엇을 검증하고 싶은지, 그리고 그것을 위해 어떤 환경과 트랜잭션 경계, 격리 수준을 만드는것이 좋을지를 배우는 좋은 경험이 된 것 같다.

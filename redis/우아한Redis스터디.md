
이 내용은 [우아한 Redis](https://www.youtube.com/watch?v=Gimv7hroM8A)를 보고 정리한 내용이기 때문에 아직 이해가 부족하다...

## Redis란?

* In-memory Data structure Store
* BSD 3 License Open Source
    * 레디스를 쓸 때에 마음대로 코드를 수정하거나, 숨기거나 할 수 있다.
    * 추가로 Redis module은 느낌이 다른데, 이건 Redis Enterprise를 제공해주는걸로 얘는 변경시 코드를 공개해야한다.
* Support Data Structures
    * String, set, sorted-set, hashed, list
    * Hyperloglog
        * 유일한 원소 개수 추정에 사용되는 알고리즘. 속도가 매우 빠르고 오차가 적다고 한다.
    * bitmap
    * geospatial index
        * 나에게서 얼마만큼의 거리에 있는 데이터들을 가져온다.
    * stream
* Only 1 Committer
    * 레디스 소스를 고칠 수 있는 사람은 딱 한명밖에 없다.
    * ![](https://i.imgur.com/ZOmWHh7.png)
    * 이 아저씨임

## Cache가 뭘까?

> 나중에 요청이 들어올 수 있는 결과값을 미리 저장해 두었다가 보여주어, 빠르게 서비스를 제공할 수 있는 것

예를 들어 DP에서 앞의 연산을 통해 뒤의 연산을 가져오는 식으로 생각하면 된다.

### Cache의 구조 #1 Look aside Cache

![](https://i.imgur.com/J1yV0ce.png)

일반적으로 제일 많이 사용되는 패턴이다.

1. 먼저 Cache에 데이터 있는지 찾아봄
2. 없으면 다음으로 DB에 데이터를 찾아봄
3. DB에 데이터가 있는 경우 DB에서 가져온 데이터를 캐시에 저장
4. 이후 데이터를 사용자에게 전달

### Cache의 구조 #2 Write Back

![](https://i.imgur.com/evyq1X2.png)

쓰기가 굉장히 빈번하게 일어나는 경우 사용하는 방식이다.
-> 로그 데이터의 경우 굉장히 빈번하게 발생되고, 이를 DB에 매번 저장하기 그러니까 이 방식이 요긴하게 쓰인다고 한다.

1. Cache에 데이터를 저장시켜 준다.
2. 특정 시간마다 DB에 해당 데이터를 저장시킨다.
3. DB에 데이터가 저장되면 Cache에서는 이걸 지운다.

이걸 쓰는 이유는 write가 자주 일어나는데, 그런 자주 변경되거나 추가되는 데이터를 캐싱해 두면 사용자가 저장된 데이터를 불러올 때에 문제가 없기 때문이다.

`batch`라고 생각하면 편하다.
500개의 데이터를 저장해야 하는 경우, insert를 500번 하는것보다 한번의 뭉탱이 insert로 저장하는 느낌이다.

* 다만 단점으로는 처음에 Cache에 데이터가 저장되는데, 이게 메모리이기 때문에 장애가 생기면 데이터가 없어져버릴 수 있다.

## Collection의 중요함

* 개발의 편의성
* 개발의 난이도

Redis외에 memcache라는 친구가 있는데, 얘가 Redis에 밀리는 큰 이유중에 하나가 Collection이라고 한다.

### 개발의 편의성

> 랭킹 서버를 직접 구현한다면?

* 가장 간단한 방법
    * DB에 유저의 Score을 저장하고 Score로 order by로 정렬 후 읽어오기
    * 개수가 많아지면 속도에 문제가 발생할 수 있다.
        * 해당 방식은 디스크를 사용하기 때문이다.

그렇기 때문에 In-Memory로 이걸 만들어야 할 것이다.

* Redis의 Sorted Set(Collections의 종류 중 하나)을 이용하면 랭킹을 구현할 수 있다.
    * 추가로 Replication도 가능
        * 두 개의 이상의 DBMS 시스템을 Mater / Slave로 나눠서 동일한 데이터를 저장하는 방식
    * 다만 가져다 쓰면 거기의 한계에 종속적이게 된다.

### 개발의 난이도

> 친구 리스트를 관리한다면?

* 친구 리스트를 Key-Value 형태로 저장해야 한다고 하면
    * 현재 유저 `123`의 친구 Key는 `friends:123`

이 상태에서 현재 친구 A가 유저 123의 가장 최근 친구라고 생각해 보자
아래 두개의 Transaction이 동시에 일어난다고 한다면
> Race Condition

* Transaction1 (친구 B추가)
    * 친구 리스트 `friends:123`을 가져와서 그 끝에 친구 B 추가
    * 여기에서 맨 마지막 친구는 A일 것이다.
    * 그럼 A다음에 B가 위치할것이다.
* Transaction2 (친구 C추가)
    * 친구 리스트 `friends:123`을 가져와서 그 끝에 친구 B 추가
    * 여기에서 맨 마지막 친구는 A일 것이다.
    * 그럼 A다음에 C가 위치할것이다.

이런 식으로 뭔가 들어가야 하는 데이터가 안들어가게 될 수 있다.

* 여기서 Redis의 장점이 도움이 되는데, Redis는 자료구조가 Atomic하기 때문에 위의 `Race Condition`을 최소화 할 수 있다고 한다.
    * 이걸 Collectino이 제공해준다.
        * 물론 그래도 잘못짜면 발행함

## 그래서 Redis를 어따 쓰면 되는가?

* Remote Data Store
    * A, B, C서버에서 데이터를 공유하고 싶을때
* Redis자체가 싱글 스레드이기 때문에 Atomic 보장
* 주로 쓰이는 경우
    * 인증 토큰 저장(Strings 또는 hash)
    * Ranking보드 사용(Sorted Set)
    * 유저 API Limit
    * 잡 큐(list)

## Redis Collections

* Strings
    * Key-Value 형태로 저장하는 방식
* List
    * 보통 자료구조의 List와 같다.
    * 이거는 자료의 맨 앞/뒤에 값을 넣는것은 빠르지만, 중간에 넣을 때에는 오래 걸릴 것이다.
* Set
    * 중복 데이터를 다루지 않을 때 사용할 것이다.
* Sorted Set
    * Score을 통해 순서를 보장할 수 있다.
    * 위의 Ranking을 할 때에도 이름 외에 Score을 주기 때문에 간편하게 구현할 수 있는 것이다.
* Hash

### Strings - 단일 key

* 기본 사용법
    * Set \<key> \<value>
    * Get \<key>

Set Token : 류찬 천재
Get Token : 류찬
-> 천재

### Strings - 멀티 Key

* 기본 사용법
    * mset \<key1> \<value1> \<key2> \<value2> \<key3> \<value3>...

### Strings - 간단한 SQL을 대체한다면?

```
Insert into users(name, email) values('ryoochan', 'fbcks97@naver.com');
```

이라는 코드를 이걸로 대체하려면

> Set을 쓰는 경우

```
Set name:ryoochan ryoochan
Set email:ryoochan fbcks97@naver.com
```

> mset을 쓰는 경우

```
Mset name:ryoochan ryoochan email:ryoochan fbcks97@naver.com
```

요런 식으로 써줄 수 있다.

### List : insert

* 기본 사용법
    * Lpush \<key> \<A>
        * Key: (A)
    * Rpush \<key> \<B>
        * Key: (A, B)
    * Lpush \<key> \<C>
        * Key: (C, A, B)
    * Rpush \<key> \<D, A>
        * Key: (C, A, B, D, A)
        
앞/뒤로 값 추가하는거
Lpush->앞 / Rpush->뒤

### List : pop

* 기본 사용법
    * Key: (C, A, B, D, A)
    * LPOP \<key>
        * Pop C, Key: (A, B, D, A)
    * RPOP \<key>
        * Pop A, Key: (A, B, D)
    * RPOP \<key>
        * Pop D, Key: (A, B)

있던 데이터를 지우는 것이다.

### List : lpop, blpop, rpop, brpop

* 기본 사용법
    * Key: ()
    * LPOP \<key>
        * No Data
    * BLPOP \<key>
        * 누가 데이터를 Push하기 전까지 대기한다.


### Set : 데이터가 있는지 없는지만 체크하는 용도

* 기본 사용법
    * SADD \<key> \<value>
        * Value가 이미 key 에 있으면 추가되지 않는다.
    * SMEMBERS \<key>
        * 모든 Value를 돌려줌
    * SISMEMBER \<key> \<value>
        * Value가 존재하면 1, 없으면 0

보통 특정 유저의 `친구리스트` / `팔로워리스트` 등을 찾을 때에 많이 사용된다.

### Sorted Sets : 랭킹에 따라서 순서가 바뀌길 원할 때에

* 기본 사용법
    * ZADD \<key> \<score> \<value>
        * Value가 이미 key에 있으면 해당 Score로 변경됨
    * ZRANGE \<key> \<StartIndex> \<EndIndex>
        * 해당 Index범위 값을 모두 return한다.
        * Zrange testkey 0 -1
            * 모든 범위를 가져옴 (마이너스 1을 넣으면 열려있어서)
* 유저 랭킁 보드로 사용 가능
* Sorted sets의 score은 double 타입이기 때문에 값이 정확하지 않을 수 있다.
    * 컴퓨터에서는 실수가 표현할 수 없는 정수값들이 존재
        * JS에서도 뭔가 Long으로 표현하지 못하는 것들이 있어서 이를 String으로 보내는 경우가 있는데, 이도 마찬가지이다. 실수형이 표현하지 못하는 정수값은 다른 값으로 대체되고, 이 때문에 score이 틀어질 수 있는 것

> 만약 Sorted Sets에서 정렬이 필요한 값이 필요하다면?

`select * from rank order by score limit 50, 20;`
-> `zrange rank 50 70` 

(desc정렬은?)

`select * from rank order by score desc limit 50, 20;`
-> `zrevrange rank 50 70` 

> 만약 Score 기준으로 뽑고 싶다면?

`select * from rank where score >= 70 and score < 100;`
-> `zrangebyscore rank 70 100`

`select * from rank where score > 70;`
-> `Zrangebyscore rank (70 +inf`

### Hash : Key 밑에 sub key가 존재

* 기본 사용법
    * Hmset \<key> \<subkey1> \<value1> \<subkey2> \<value2>
        * key-value set 내에 다시 key-value set을 갖는 것.
    * Hgetall \<key>
        * 해당 key의 모든 subkey와 value를 가져옴
    * Hget \<key> \<subkey>
    * Hmget \<key> \<subkey1> \<subkey2> .... \<subkeyN>

### Hash - 간단한 SQL 대체한다면?

`insert into users(name, email) values('ryoochan', 'fbcks97@naver.com')`
-> `hmset ryoochan name ryoochan email fbcks97@naver.com`

### Collection의 주의사항

* 하나의 컬렉션에 너무 많은 아이템을 담으면 좋지 않다.
    * 만개 이하로 가자
* Expire은 Collection의 item 개별로 걸리지 않고 전체 Collection에 대해서만 걸린다.
    * 1000개를 하나하나 expire하는게 아니라 1000개가 한꺼번에 사라짐.
    
## Redis의 운영

중요한게 4가지임

* 메모리 관리를 잘하자
* O(N)관련 명령어는 주의하자
* Replication
* 권장 설정 Top

### 메모리 관리를 잘하자

* 메모리는 In-memory기반이기 때문에 특히나 메모리 관리를 잘 해야 한다.
    * 그래서 Physical Memory이상을 사용하면 문제가 발생한다.
        * Swap이 있다면 Swap사용으로 해당 메모리 Page접근 시마다 늦어진다.
            * Swap은 메모리 페이지를 디스크에 저장해 두고 필요할 때마다 이거를 사용하는 것이다.
                * 한번이라도 Swap이 일어나서 데이터를 저장하게 되면, 여기 접근할 때마다 Swap이 발생하므로 성능이 엄청 떨어지게 된다.
        * Swap이 없다면 죽음ㄹㅇㅋㅋ
* Maxmemory를 설정하더라도 이보다 더 사용할 가능성이 크다.
    * Redis는 `jemalloc`을 메모리 관리자로 사용하는데, 이친구가 삭제했다고 하는 데이터가 실제로는 삭제되지 않았거나 혹은 메모리 관리가 조금 틀어질 수 있어서 그렇다.
* RSS(운영체제에서 Redis에 할당한 메모리 값)값을 모니터링 해야한다.

### 메모리 관리

큰 메모리를 사용하는 instance 하나보다는 적은 메모리를 사용하는 instance 여러개가 안전하다.

레디스는 fork를 하게 되는데(Master - Slave를 쓰는 경우 fork를 사용한다), 만약 write를 하게 되면 최대 메모리의 두배를 사용하게 될 수 있다. -> copy-on-write(COW)

---

간단히 COW에 대해 설명하자면, 자식 프로세스를 처음 생성(fork)하게 되는 경우 부모와 같은 메모리 공간을 사용하는데, 이 때 부모 프로세스가 데이터를 쓰거나/수정하거나/지우게 되면 동일한 메모리 공간을 공유할 수 없게 된다.
이 때 부모 프로세스는 해당 페이지를 복사한 다음 수정하고 이를 COW라고 한다.
그러니까 이게 
1. 자식 프로세스가 생성되어 작업한다.
2. 데이터의 변경이 일어날 때에 메모리 페이지의 복사가 일어남

때문에 최대 2배까지의 데이터 사용이 일어날 수 있다고 한다.

---

그러니까 `24GB InstanceX1`보다 `8GB InstanceX3`이 운영의 안정성 측면에서 이득이라는 것이다.
 
다시 메모리 관리로 돌아와서

* Redis는 메모리 파편화가 발생할 수 있다. -> 사용하려는 것보다 많은 데이터를 쓰게 된다.
    * 4.x대부터 이를 줄이도록 jemalloc에 히트를 주는 기능이 들어감
        * 다만 이도 jemalloc 버전에 따라 다르게 동작할 수 있다.
    * 3.x대 버전의 경우
        * 실제 used memory는 2GB로 보고되는데, 실제로는 11GB의 RSS를 사용하는 경우가 자주 발생한다.
* 다양한 사이즈를 가지는 데이터보다는 유사한 크기의 데이터를 가지는 경우가 유리하다.
    * 이렇게 하면 메모리 파편화를 줄일 수 있다.

### 메모리가 부족할 때에는?

* Cache is Cash!
    * 캐시는 생각보다 비싸다.
    * 좀 더 메모리 많은 장비로 Migration
    * 메모리가 빡빡하면 Migration 중에 문제가 발생할 수도 있다.
        * 한 60~70프로정도 메모리를 쓰고있으면 이전하는게 좋다고 한다.
* 있는 데이터 줄이기
    * 데이터를 일정 수준에서만 사용하도록 특정 데이터를 줄임.
    * 다만 이미 Swap을 사용중이라면, 프로세스를 재시작해야한다.

### 메모리를 줄이기 위한 설정

* 기본적으로 Collection들은 다음과 같은 자료구조를 사용한다.
    * Hash -> HashTable을 하나 더 사용
    * Sorted Set -> Skiplist와 HashTable을 이용
        * 값/인덱스 모두를 사용해서 찾아야하기 때문이다.
    * Set -> HashTable 사용
    * 해당 자료구조들은 메모리를 많이 사용한다.
* Ziplist를 이용하기
    * 이거는 참고로 속도는 느려지는데, 메모리는 적게 쓴다.
    * 따로 뭐 해준다기 보다는, 위의 녀석들을 Redis상에서 내부적으로는 ziplist를 이용해주는 식으로 쓰는 것이다.

#### 그래서 ziplist가 뭔데?

* In-memory의 특성 상 적은 개수라면 선형 탐색을 하더라도 속도가 빠르다.
    * 어차피 in-memory 자체가 빠르거든

여기서 나타나는 친구인데, ziplist는 그냥 데이터를 선형으로 쭉 저장시킨다.

![](https://i.imgur.com/dd3aLJt.png)

요런 식으로 저장시켜서 본다.

* List, hash, sorted set등을 ziplist로 대체해서 처리하는 설정
    * hash-max-ziplist-entries, hash-max-ziplist-value
        * hash 최대 몇개까지는 ziplist를 쓰겠다, value얼마까지는 쓰겠다.
    * list-max-ziplist-size, list-max-ziplist-value
    * zset-max-ziplist-entries, zset-max-ziplist-value

### O(N)관련 명령어는 주의하자.

> Redis는 Single Threaded이다.

그렇기 때문에 Redis는 동시에 하나의 명령밖에는 처리할 수 없다.
참고로 단순한 get/set의 경우, 초당 10만 TPS이상 가능하다.(CPU속도에 영향을 받는다.)

![](https://i.imgur.com/wIgZH7y.png)

하나의 packet이 처리되어야만 뒤에 있는 packet을 처리할 수 있다는 것이다.

그래서 get/set은 뭐 금방금방 처리되니까 괜찮은데, 오래 걸린다고 한다면?

#### 대표적인 O(N)명령들

* KEYS
    * 모든 키를 순회하는 명령이다.
* FLUSHALL, FLUSHDB
    * 모든 데이터를 지우는것인데, 이건 필요하면 써야지 뭐..
* Delete Collections
    * 자료구조 애들을 지우는것이다.
* Get All Collections
    * 모든 애들을 가져오는것

참고로 예전의 Spring security oauth RedisTokenStore이 딱 이 문제를 가지는 것이라고 한다.
지금은 괜찮다고 한다.

#### 그럼 어떻게 위의 애들을 대체하는지?

> KEYS의 경우

scan명령을 사용하는 것으로 하나의 긴 명령을 짧은 여러번의 명령으로 바꿀 수 있다.
명령 사이사이에 다른 명령들을 끼워넣어서 처리하는 것이다.

> Collection의 모든 item을 가져와야 할 경우

* Collection의 일부만 가져오거나
    * Sorted Set
* 큰 Collection을 여러 개의 작은 Collection으로 나누어서 저장하기
    * Userranks -> Userrank1, Userrank2, Userrank3 ..
    * 하나당 몇천개 안쪽으로 저장하는게 좋다.

> Spring security oauth RedisTokenStore 이슈

* Access Token의 저장을 List(O(N)) 자료구조를 통해 이루어진다.
    * 검색/삭제시에 모든 item을 매번 찾아봐야 한다.
        * 100만개쯤 되면 전체 성능에 영향을 준다.
    * 현재는 Set(O(1))을 이용해서 검색, 삭제를 하도록 수정됨.

## Redis Replication

Replication이란 A서버의 데이터를 B서버에서 동일하게 가지고 있는 것이다.
Master-Slave같은 느낌

### Redis Replication 특징

* Async Replication
    * Replication Lag이 발생할 수 있다.
        * A에 있는 데이터가 변경된 후 B에 해당 데이터를 변경하라고 명령을 하는데, 그 틈 사이에는 A와 B가 데이터가 다르다.
            * 추가로 이렇게 Replication 관련 에러가 커지면 slave측에서 그냥 master와의 연결을 끊어버린 후에 다시 연결해서 Replication을 진행한다. 이 때에 부하가 커질 수 있음.
* 'Replicaof'(>=5.0.0) or 'slaveof' 명령으로 설정 가능
    * Replicaof hostname port
        * 이런 식으로(본래 slaveof였던걸 replicaof로 바꿈.. 이유는 slave안쓰려고 하는듯?) 기존 데이터를 다른 쪽으로 보내줄 수 있다.
* DBMS로 보면 statement replication가 유사하다.
    * 즉 이렇게 쿼리로 보내는 것인데, 이 경우 `now` 라는 명령어를 보낸다고 가정하면 A와 B의 데이터가 다를 수 있다(시간을 각자 계산하므로)
        * 그래서 경우에 따라 다른 값이 나올수도 있다.

### Redis Replication 설정 과정

1. Secondary(slave)에 `replicaof` or `slaveof` 명령을 전달한다.
2. Secondary는 Primary(master)에 `sync`명령 전달
3. Primary는 현재 메모리 상태를 저장하기 위해 Fork한다.
4. Fork한 프로세서는 현재 메모리 정보를 disk에 dump
5. 해당 정보를 Secondary에 전달한다.
6. Fork이후의 데이터를 secondary에 계속 전달한다.

### Redis Replication 주의점

* Replication 과정에서 fork가 발생하므로 메모리 부족이 발생할 수 있다.
* `Redis-cli --rdb` 명령은 현재 메모리 스냅샷을 가져오므로 같은 문제를 발생시킨다.
* AWS나 클라우드의 Redis는 좀 다르게 구현되어서 좀 더 해당 부분이 안정적이다.
    * 여기서는 fork없이 전달하는 기능이 있는 것 같다.
    * 그대신 좀 느리다.
* 많은 대수의 Redis서버가 많은 Replica를 두고 있다면
    * 네트웍 이슈나, 사람의 작업으로 동시에 replication이 재시도 되도록 하면 문제가 발생할 수 있다.

### Redis 권장 설정

> `redis.conf` 권장 설정 Tip

* Maxclient 설정 50000
    * 이만큼만 네트워크로 접속 가능
* RDB/AOF 설정 off
    * 성능상/안정성에 유리하다.
* 특정 commands disable
    * Keys
        * AWS의 ElasticCache는 이미 하고 있다.
* 전체 장애의 90% 이상이 KEYS와 SAVE설정(RDB Default)을 사용해서 발생한다.
    * SAVE가 뭐냐면 예를 들어 1분안에 데이터 10000개가 바뀐 경우 지금 memory를 dump시키는 것으로, 실제로 해당 설정에 있는 dump가 일어나는 경우가 많다. 따라서 이렇게 설정하면 엄청 자주 fork해서 망함..
* 적절한 ziplist설정

## Redis 데이터 분산

* 데이터의 특성에 따라 선택할 수 있는 방법이 달라진다.
    * Redis는 Cache로 쓰자
    * Redis를 Persistent하게 쓰면 안된다!!

### 데이터 분산 방법

* Application
    * Consistent Hashing
        * Key를 Hashing해서, 10000/20000/30000 ... 이렇게 서버를 두고 데이터도 해싱해서 1500, 14300, ... 이렇게 나오면 `자신보다 크고, 가장 가까운 곳에 저장시키는 방식` 으로 데이터를 두는 방식이다.
            * 서버가 죽거나, 추가되거나, 복구되거나 하는 경우 1/N의 데이터만 이동하게 된다.
        * twemproxy를 사용하는 방법으로 쉽게 사용 가능
    * Sharding
        * 상황마다 샤딩 방법이 달라진다.
            * 가장 쉬운 방법은 Range이며, 특정 Range를 정의하고 해당 Range에 속하면 거기에 저장시키는 방법이다.
                * 이 경우는 서버의 상황에 따라 놀고있는서버와 안놀고있는 서버가 나뉘고, 이걸 마음대로 분배해주기 힘들다.
            * modular를 통해서 진행하면 `0, 1`을 했다가 `0, 1, 2, 3` 으로 두배씩 늘려주면 기존 애들에서 절반이 자기의 2배를 해서 그쪽으로 간다. 즉 정해진 값이 이동하게 되는 것이다.
                * 이 경우는 균등하게 값들이 이동하게 되는 것이다. 다만 이거는 늘리려면 2배씩 퍽퍽 늘어난다.
            * indexed방식도 있는데, 이거는 해당 key가 어디 저장되어야 할지 관리하는 서버를 따로 두고, 여기서 갈곳을 명령해준다.
                * 단점은 모든 정보를 index서버가 관리해서 이게 죽으면 서비스가 안된다.
* Redis Cluster
    * Hash기반으로 Slot 16384로 구분한다.
        * Hash알고리즘은 CRC16사용
        * Slot = crc16(key) % 16384
            * 아무리 많이 만들어도 클러스터의 수는 16384개를 넘지 못할것.
        * Key가 Key{hashkey} 패턴이며면 실제 crc16에 hashkey가 사용된다.
        * 특정 Redis서버는 이 slot range를 가지고 있고, 데이터 migration은 이 slot단위의 데이터를 다른 서버로 전달하게 된다.(migrateCommand 이용)
            * slot전체를 옮겨버리는 느낌이다.
            * 그대신 이거는 자동으로 되는게 아니고 관리자가 직접 해줘야한다.

### Redis Cluster 동작

![](https://i.imgur.com/KbZReIV.png)

Primary가 죽으면 Secondary가 이를 대신해준다.

![](https://i.imgur.com/k2iazU0.png)

근데 이곳에서

![](https://i.imgur.com/rYyCODm.png)

이런 식으로 Slot Range가 0~5555 인 슬롯이 있는데, 여기에 만약 `Slot0_key abc`를 보내면 자신에 해당되는 친구라서 저장시켜 준다.

근데 만약에 `Slot5506_key abc`가 오면 얘는 여기 해당되는애가 아니다.
이는 2번 슬롯에 해당되는 애다.

![](https://i.imgur.com/0t3jKZd.png)

이렇게 잘못된 곳에 값을 보내주게 되면

![](https://i.imgur.com/KapMMIR.png)

이런 식으로, `-MOVED` 에러를 보여주게 되고, 2번 Slot이 정확한 곳이라고 알려주는 메세지를 return한다.
그러면 Client측에서 다시 2번 슬롯에 보내줘야 한다는 것이다.

### Redis Cluster 장/단점

* 장점
    * 자체적인 Primary, Secondary Failover
    * Slot단위의 데이터 관리
* 단점
    * 메모리 사용량이 많음
    * Migration자체는 관리자가 시점을 결정해야 한다.
    * Library구현이 필요함.
        * 위의 경우처럼 에러가 발생하면 해당 위치에 다시 보내주는 걸 구현해야 하는듯

## Redis Failover

* Coordinator 기반 Failover
* VIP/DNS 기반 Failover
* Redis Cluster의 사용

### Coordinator 기반 Failover

> Zookeeper, etcd, consul 등의 Coordinator를 사용한다고 한다.

![](https://i.imgur.com/APWoHld.png)

위의 Coordinator에 어떤 서버를 쓸지 미리 저장시켜 둔다(예를 들어 Redis #1만을 쓰도록 하는 느낌으로)
그러다가 Redis #1이 죽으면 Health Checker가 Redis #2를 Primary로 승격시키는 것이다.
그리고 다시 Health Checker가 current Redis가 Redis #2라고 업데이트해준다.

![](https://i.imgur.com/ezEAdaF.png)

요 경우는 근데 Coordinator에도, API Server에도 이 정보를 알려주어야 할 것이다.

* 장점
    * Coordinator 기반으로 설정을 관리한다면 동일한 방식으로 관리가 가능하다.
* 단점
    * 해당 기능을 이용하도록 개발이 필요하다.
    * 즉 다른 곳에서 이 기능을 쓰려면 또 개발해줘야 한다.

## VIP 기반 Failover

![](https://i.imgur.com/sQO0ETC.png)

Virtual IP기반으로 가상 아이피를 하나 할당해 주고, 실제 IP는 다른 것인데 API서버는 `10.0.1.1`로만 접속하는 것이다.
이렇게 하면 일단 당연히 Redis #1로만 접속할 것이다. -> Redis #1이 해당 VIP를 가지고 있다.
그러던 중 Redis #1가 죽으면 Redis #2를 Primary로 승격시킨다.
그리고 이제 Redis의 VIP를 `10.0.0.1`로 할당시킨다. -> VIP는 마음대로 할당 가능
이렇게 되면 이게 Primary가 될 수 있다.

참고로 이거는 굳이 Failover아니어도 그냥 Health Checker가 Redis #1의 연결을 끊어만 주어도 알아서 저리 된다.

## DNS 기반 Failover

![](https://i.imgur.com/GanF7CJ.png)

사실 위와 거의 똑같은 로직이다.
그냥 도메인을 옮겨가면서 하는거고 동작 방식은 위랑 같음.

이 두 방식은 Code를 바꾸는 것이 아니기 때문에 어떤 곳에서도 다 사용 가능하다.

* VIP/DNS기반 장점
    * 장점
        * 클라이언트에 추가적인 구현이 필요없다.
        * VIP기반의 장점
            * 외부로 서비스를 제공해야 하는 서비스 업자에 유리하다.
            * Java의 경우는 DNS를 Cache하는 경우가 있는데, 이거 잘못하면 DNS를 바꾸어도 제대로 접속되지 않을 것이다.
        * DNS기반의 장/단점
            * DNS Cache TTL을 관리해야 한다.
                * 위의 VIP기반의 장점과 상응되는 것으로, 잘못하면 DNS가 바뀌어도 Failover
            * 하지만 가격이 싸다.

## Monitoring

* Redis Info를 통한 정보
    * RSS
        * 가장 처음 모니터링해야하는 것이다.
        * Physical memory를 얼마나 쓰고 있느냐?
    * Used Memory
        * OS는 이걸로 사용하고 있는 메모리를 보고 있다.
        * 위에서 설명했던 내용인데, 이 Used memory보다 실제 데이터는 더 많이 먹고있는 경우가 있다.
    * Connection 수
        * 클라이언트가 연결이 생기고/끊기고가 얼마나 자주 일어나는지
        * Redis는 이 Connection이 많으면 안된다.
    * 초당 처리 요청 수
        * 보통 CPU에 의해 영향받는다.
* System
    * CPU
    * Disk
        * fork하거나 하는 경우 쓰일수 있다.
    * Network rx/tx

### CPU가 100%를 치는경우?

* 처리량이 매우 많다면?
    * CPU성능이 좋은 서버로 이전하기
    * 실제 CPU성능에 영향을 받는다.
        * 그러나 단순 `get/set`은 초당 10만 이상 처리 가능
* O(N)계열 특정 명령이 많은 경우
    * Monitor 명령을 통해 특정 패턴을 파악하는 것이 필요하다.
        * 현재 쓰고있는 명령이 쭉 나온다.
    * Monitor 잘못쓰면 부하로 해당 서버에 더 큰 문제를 일으킬 수도 있다.
        * 그니까 잠시만 써서 스크립트를 파악하고 어디에 문제가 있는지 분석해보면 된다!

## 결론

* 기본적으로 Redis는 매우 좋은 툴이다.
* 그러나 메모리를 빡빡하게 쓰면 관리가 어렵다.
    * 32기가 장비라면 24기가 이상 사용시 장비 증설 고려하기
    * Write가 Heavy하면 migration도 주의하기
* Client-output-buffer-limit 설정이 필요하다.
    * 이걸 넘어가면 그냥 네트워크가 끊어져버린다고 한다...

## Redis as Cache

* Cache일 경우는 문제가 적게 발생한다.
    * 사실 이경우는 그냥 속도가 느려지고 DB나 서버에 부하가 생기는 정도의 문제일 것이니까.
    * Redis가 문제가 있을 때 DB등의 부하가 어느정도 증가하는지 확인하기
    * Consistent Hashing도 실제 부하를 아주 균등하게 나누지는 않는다.
        * Adaptive Consistent Hashing을 이용해 볼 수도 있다고 한다.

## Redis as Persistent Store

절대로 지워져서는 안되는 데이터들을 관리하는 경우..

* 좀 문제가 있지..
    * 무조건 Primary/Secondary구조로 구성이 필요하다.
    * 메모리를 절대 빡빡하게 사용하면 안된다.
        * 정기적인 migration이 필요하다.
        * 가능하면 자동화 툴을 만들어서 이용하기
    * RDB/AOF가 필요하다면 Secondary에서만 구동한다.
        * 그리고 RDB보다는 AOF가 IO가 여러번 발생해서(오래 걸리는일이 적어서) 그나마 안정적...?이라고 한다.

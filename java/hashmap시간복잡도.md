# java의 HashMap

HashMap은, Map인터페이스의 컬렉션 중 하나이다.
이 HashMap은 key-value로 하나의 key가 하나의 value를 갖도록 한다.

이전에 ArrayList와 LinkedList의 차이점에 대한 글을 작성한 적이 있다.
각각의 내용들에 대해서 생각해 보면, 두 자료구조 모두 값을 읽어오거나 변경할 때에 시간복잡도를 고려해줄 필요성이 있었다.

근데 이 HashMap은 값을 넣거나 변경할 때 모두 시간복잡도가 O(1)이다!!
이게 어떻게 가능할까?

## Put의 시간복잡도

### HashMap의 동작 방식

위에서 설명하였듯 HashMap은 하나의 key가 하나의 value를 갖는다.

이 key를 찾아가기 위해서 어떠한 key가 입력되면 java에서는 이곳에 해시함수를 적용시켜 고유 index를 만든다.

예를 들어 `RyooChan`이라는 사람에 대한 특징을 저장하는 HashMap이 있다고 가정해 보자.
다음과 같이 그의 특징들을 저장할 때에

![](https://i.imgur.com/bsaugEB.png)

이렇게 hash함수를 적용시켜 변경된 index를 통해 key를 구성한다.
참고로 실제 값이 저장되는 곳을 bucket이라고 부른다고 한다.

### HashMap코드 확인

저 코드를 한번 실제로 보면서 확인하면

```
Map<String, String> ryooChan = new HashMap<>();
```

이렇게 ryooChan이라는 HashMap을 만들어 보자.
여기서 HashMap을 만드는 내용을 확인하기 위해 new HashMap의 내부를 보면

![](https://i.imgur.com/G4yazua.png)

요렇게 DEFAULT_LOAD_FACTOR를 loadFactor로 가지게 된다.

그럼 저 DEFAULT_LOAD_FACTOR은 또 뭐지?

![](https://i.imgur.com/PwM2iEP.png)

~~음...이게 뭘까?~~

저 loadFactor이라는걸 말하자면 위에서 말한 bucket의 크기가 얼마나 차 있는지를 보여주는 수치이다.
그래서 만약에 hashmap에 값이 늘어난다면(bucket에 값이 들어가면) 저게 변경된다 한다.

```
        Map<String, String> ryooChan = new HashMap<>();
        ryooChan.put("Intelligence", "Smart");
        ryooChan.put("Look", "Handsome");
        ryooChan.put("Character", "Kind");
        ryooChan.put("Fault", "None");
```

이렇게 ryooChan이 가진 특징들을 put을 통해 넣는다고 하면

![](https://i.imgur.com/CAwB8k3.png)

HashMap의 put은 이렇게 key를 hash시켜 값을 저장하게 된다.

(다음이 hash)
![](https://i.imgur.com/KmNLQdf.png)

그리고 보면 알겠지만 hash는 final int형식이다.
**즉 무엇이 key로 사용되어도 결국 그것은 int형식으로 사용된다는 것이다!!**

## Get의 시간복잡도

### HashMap의 동작 방식

Get을 할 때에도 마찬가지이다.
검색에 필요한 key를 hash시켜 값을 가져오게 된다.

![](https://i.imgur.com/5Qu2BWe.png)

### HashMap코드 확인

이렇게 RyooChan의 inTelligence를 확인하는 코드를 작성해 보자면

![](https://i.imgur.com/JfnD0qs.png)

get을 통해 가져온다. 이 get코드는

![](https://i.imgur.com/6pcuCwx.png)

다음과 같이, Node에서 key를 통해 getNode로 값을 가져온다.
내부 코드를 보면 key를 hash하여 이를 통해 검색한다.

## Remove의 시간복잡도

이제부터는 뭐 따로 설명도 필요 없을 듯 하다.

![](https://i.imgur.com/TqzEcKy.png)

이런 식으로 remove를 할 때에도 hash된 key를 통해 검색하게 된다.

## 결론

HashMap은 key를 integer형식의 index로 변환하고, 이를 통해 CRUD를 진행한다.

이를 통해

> 값을 저장할 때에는 index로 int형식을 사용해서 찾아가기 때문에 O(1)의 시간복잡도가 발생한다

는 것을 알 수 있다.

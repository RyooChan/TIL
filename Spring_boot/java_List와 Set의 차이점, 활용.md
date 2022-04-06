# List와 Set의 차이점, 활용


List랑 Set의 차이가 뭘까??
그리고 둘의 차이점에서 어떤 활용이 가능할까?? 이에 대해 알아보려고 한다.

## 1. List는 순서가 존재하고 Set은 순서가 없다.
[예전](https://hello-backend.tistory.com/112)글에서 LinkedList와 ArrayList의 차이를 알아보았다.
여기서 중요한 내용중 하나로 나온것이 바로 '순서'이다.

> List는 순서를 가지며, 데이터가 이 순서에 맞추어 쌓아진다.

코드를 통해서 확인해보자

```
public class Main {
    public static void main(String args[]) {
        ArrayList<String> fact = new ArrayList<>();
        
        fact.add("ryoo");
        fact.add("chan");
        fact.add("is");
        fact.add("genius!");
        fact.add("He");
        fact.add("is");
        fact.add("so");
        fact.add("handsome!!!");
        
        System.out.println(fact);
        System.out.println(fact.get(1));
    }
}
```
다음과 같은 코드가 있다고 하자. 그러면 List는 이 값들을 인덱스에 맞추어 하나씩 저장한다.
값을 출력하면 저장한 순서대로 나오고, get메소드를 통해 특정 위치의 값을 꺼내올 수도 있다.

![](https://i.imgur.com/uqFe3y6.png)
이런 식으로 잘 출력된다.

---

> Set은 순서를 가지지 않는다. 

* 즉 데이터를 인덱스로 관리하지 않는다는 것이다.
* 데이터를 검색하기 위해서 귀찮게 iterator등을 사용하는 이유도 여기서 기인한다.

그렇기 때문에 값이 순서대로 나오지도 않고, get으로 떠내올수도 없을 것이다.(get사용시 에러남)

코드를 통해 확인해보겠다.

```
public class Main {
    public static void main(String args[]) {
        HashSet<String> fact = new HashSet<>();
        
        fact.add("ryoo");
        fact.add("chan");
        fact.add(" is ");
        fact.add("genius!");
        fact.add("He");
        fact.add("is");
        fact.add("so");
        fact.add("handsome!!!");
        
        System.out.println(fact);
    }
}
```

get은 아예 사용할수도 없어서 제외했다.
값을 순서대로 넣어서 출력하면 다음과 같이 나온다.

![](https://i.imgur.com/nTRDBv1.png)

넣은 순서가 나올때 보장되지 않는다!

## 2. List는 중복 허용, Set은 중복 불허

List는 같은 값을 넣어도 허용된다.
그리고 Set은 허용되지 않는다. 이건 그냥 바로 코드로 알아보겠다.

```
public class Main {
    public static void main(String args[]) {
        HashSet<String> hs = new HashSet<>();
        ArrayList<String> arr = new ArrayList<>();
        
        hs.add("ryoo");
        arr.add("ryoo");
        hs.add("ryoo");
        arr.add("ryoo");
        hs.add("ryoo");
        arr.add("ryoo");
        
        System.out.println("set의 크기 : " + hs.size());
        System.out.println("list의 크기 : " + arr.size());
        
        System.out.println("---------------------");
        
        System.out.println("set의 구성 -> " + hs);
        System.out.println("list의 구성 -> " + arr);
        
    }
}
```

동일한 데이터를 3번에 걸쳐 넣고 각각 확인해 보았다.
![](https://i.imgur.com/MnTewlP.png)

이렇게 list는 중복된 값이 들어가도 받아주고, set은 허용하지 않음을 보여준다.

## 결론


> Q. 그러면 중복이 필요없으면 걍 List쓰는게 더 편하네요? 순서도 알아서 정렬해주고~
> 
> A. List가 더 편하다는 것에서 짐작했겠지만... 중복이 필요할 때 제외하고는 Set을 쓰는게 성능면에서 월등하다.


만약 contains를 사용하여 특정 값이 존재하는지 확인해본다고 하면
* List는 O(n)의 시간복잡도를 가짐
* Set은 O(1)의 시간복잡도를 가짐

이유를 생각해보면, List는 순서를 관리하므로 내부적으로 인덱스가 구현되어 있다.
따라서 내부적으로 배열이 구현되어 있는데 이 배열 순서대로 값을 찾아가기 때문에 O(n)의 시간 복잡도가 걸리게 되는 것이다.

Set은 순서랑 상관없이 그냥 어떻게든 찾아오면 되기 때문에 당연히 전체 값에 대해 찾아올 필요가 없으므로 훨씬 빠르다.

그러므로 **둘간의 차이를 잘 알고 사용하는 것이 중요하며, 만약 두 방법 모두를 통해 구현 가능한 경우 set을 쓰는것이 시간복잡도면에서 더 이익을 얻을 수 있다.** 라고 정리할 수 있을 것 같다.

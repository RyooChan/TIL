### Traversing
그래프에서 매우 중요한 개념 중 하나는 "Traversal" 즉 순회이다.
node와 relationship사이의 chain이다.

* Traversal가 이루어지는 3가지 다른 방법은 다음과 같다.
    * Walk
        * Nodes와 Relationships의 정돈되고 교대인 sequence이다.
            * 모든 Node와 Relation을 여러 번 방문할 수 있다.
        * walk는 traversal의 가장 기본적인 타입이다.
        * ![](https://i.imgur.com/V3t36tC.png)
        * 1 - 2 - 3 - 2 - 6 - 2 ...
            * 2 - 3사이에는 1개의 관계밖에 없지만, walk에서는 몇 번이든 상관없이 사용 가능하다.
    * Trail
        * Trail은 Node는 여러 번 방문 가능하나, Relationship은 한 번 밖에 방문할 수 없다.
        * ![](https://i.imgur.com/G9Svp6b.png)
        * 2 - 6사이에는 두개의 relationship이 있기 때문에 왕복 가능하나, 2 - 3은 관계가 하나뿐이라 반복 방문이 불가하다.
    * Path  
        * 모든 항목이 고유하다.
            * Node, Relationship 모두 반복되지 않는다.
        * ![](https://i.imgur.com/D7rB7QE.png)

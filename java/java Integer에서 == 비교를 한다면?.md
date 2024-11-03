Java 에서 Integer 타입을 쓰는 경우가 은근 많다.

사실 실제로 업무를 할 때에는 nullable 하지 않은 경우는 int를 사용하라고 하지만(Primitive type) 아마 null safe를 위해 이걸 쓰는 경우도 있을 것이다.

근데 요거 == 비교를 하면 골때릴때가 있다.

```java
public class Main {
	public static void main(String[] args) {
	  Integer a = 1;
	  Integer b = 1;
	  
	  System.out.println("compare 1 : " + (a == b));
	  
	  a = 127;
	  b = 127;
	  
	  System.out.println("compare 127 : " + (a == b));
	  
	  a = -128;
	  b = -128;
	  
	  System.out.println("compare -128 : " + (a == b));
	  
	  a = -129;
	  b = -129;
	  
	  System.out.println("compare -129 : " + (a == b));
	  
	  a = 128;
	  b = 128;
	  
	  System.out.println("compare 128 : " + (a == b));
	  
	  a = 2;
	  b = 2;
	  
	  System.out.println("compare  : " + (a == b));
  }
}
```

요런 식으로 돌려보면 어떻게 나올까?

![image](https://github.com/user-attachments/assets/fa5e8113-8f94-4484-b1ef-62a873be92c5)

이와 같이 나온다.

이를 한번 java 코드를 살펴보고 알아보자.

- Integer.java

```java
    @IntrinsicCandidate
    public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }
```

코드를 한번 들여다보면, valueOf 라는 메서드가 하나 있다.

근데 여기서 보면 어째 IntegerCache.low ~ IntegerCache.high 까지는 아예 IntegerCache 라는 배열에서 값을 꺼내서 쓰고, 그 범위가 아닌 애들은 Integer 값을 가져온다.

이유는 사실 바로 위에 적혀있는데

```
Returns an Integer instance representing the specified int value. If a new Integer instance is not required, this method should generally be used in preference to the constructor Integer(int), as this method is likely to yield significantly better space and time performance by caching frequently requested values. This method will always cache values in the range -128 to 127, inclusive, and may cache other values outside of this range.
Params:
i – an int value.
Returns:
an Integer instance representing i.
Since:
1.5
```

요렇게 되어있다. 번역해보면 결국 그냥

```
자주 요청되는 값을 캐시하여 더 나은 공간 및 시간 성능을 제공한다.

-128 ~ 127까지의 범위를 캐시하고 범위 밖의 다른 값도 캐시할 수 있다.
```

라는 것이다.

그래서 저 범위는 어떻게 되어 있을까? 하면

```java
    private static final class IntegerCache {
        static final int low = -128;
        static final int high;

        @Stable
        static final Integer[] cache;
        static Integer[] archivedCache;

        static {
            // high value may be configured by property
            int h = 127;
            String integerCacheHighPropValue =
                VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    h = Math.max(parseInt(integerCacheHighPropValue), 127);
                    // Maximum array size is Integer.MAX_VALUE
                    h = Math.min(h, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;

            // Load IntegerCache.archivedCache from archive, if possible
            CDS.initializeFromArchive(IntegerCache.class);
            int size = (high - low) + 1;

            // Use the archived cache if it exists and is large enough
            if (archivedCache == null || size > archivedCache.length) {
                Integer[] c = new Integer[size];
                int j = low;
                for(int i = 0; i < c.length; i++) {
                    c[i] = new Integer(j++);
                }
                archivedCache = c;
            }
            cache = archivedCache;
            // range [-128, 127] must be interned (JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }

        private IntegerCache() {}
    }
```

이렇게 나타난다.

한번 살펴보면 결국

- low : -128이고 고정되어 있다.
- high : 이것도 상수이기는 한데, 초기화가 되어있지 않고 밑의 static 부분에서 설정된다. 기본적으로는 127로 설정되지만, 설정에 따라 다른 값으로 세팅이 가능하다.
- @Stable
    - 이거는 뭔가 했는데 결국 이게 선언되면 초기 단계에서 값을 설정한 이후로는 변경되지 않는다는것.
    - 즉 처음 설정된 null 이 아닌 값을 안정된 값으로 간주하여 VM이 해당 값을 최적화하여 계속 사용할 수 있다.
    - HotSpot VM에서 요게 null이 아니라 값이 세팅된 경우 상수로 인식한 최적화가 가능하다.
    - 참고로… final 이랑 비슷하고 걔는 아예 안바뀌는데 이거는 변화가 없다고 말해주는것. (참고로 이거 설정하면 한번 값을 세팅하면 안바꾸는게 좋다.)
- cache : 캐싱된 객체
- archivedCache : 임시로 저장하는 캐시 배열
- h 를 통해 high 를 세팅하는데, 이거 살펴보면 최소 127보다는 커야하고, 배열의 최대 크기는 넘어갈 수 없게끔 해준다.
- 그 후에는 이 캐시를 세팅해주는것

여기서 알 수 있는것은 결국

- Integer 은 자주 쓰는 값을 캐싱해두고 불러온다.
    - -128 부터 default 127 까지를 캐싱함
    - 캐싱되는 최대 크기는 127보다 크거나 배열의 최대 크기까지 가능하다.
- 근데 이 캐싱된 배열의 값이 아니라면 Integer 객체로 return시키기 때문에 == 으로 비교하면 당연히 다르게 나올 수밖에 없다.

### 그러면 Equals 는?

```java
	  a = 128;
	  b = 128;
	  
	  System.out.println("compare by equals : " + (a.equals(b)));
```

요렇게 하면 어떻게 될지?

아마 다들 뻔하다고 생각하겠지만

![image](https://github.com/user-attachments/assets/22c5c406-b803-42d4-86db-088081eefa5d)

ㅇㅇ true 가 나온다.

### 결론

- Integer는 시간/공간 최적화를 위해 미리 일정 범위 (default -128 ~ 127)의 값을 캐싱해서 돌려준다.
- 그렇기 때문에 그 범위 내에서는 같은 값이 나온다.
- 이 범위를 벗어나면 객체가 return 되고 당연히 `==` 비교에서는 다르다고 나온다.
- `Equals` 로 비교하자.
- 참고로 Long도 마찬가지임

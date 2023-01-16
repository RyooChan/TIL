# No serializer found for class 오류

프로젝트를 하던 중에 만난 문제인데, 쓸데없이 시간을 자꾸 낭비하게 되어 기록하게 되었다.

A의 값을 가져올 때에 
A_DTO, B_DTO 이런 식으로 여러 DTO들을 통해 값을 가져오는데 자꾸 `No serializer found` 에러가 났다.

아니 근데 DTO를 써주고 있는데 직렬화 문제가 왜 나는거지...? 싶었는데 1시간동안 삽질하다가 간단한 문제를 발견했다.

![](https://i.imgur.com/b4vlwFa.png)

이런 느낌으로 에러가 있을 때에 결국 실제 문제는 `tagMemeDetailResponses`쪽에서 난건데...? 하고 보았는데 @Getter가 설정이 안돼있었다

그러니까 DTO를 쓸때에 혹시라고 seriablizer 에러가 나면

> 사용하는 모든 DTO를 확인하면서 `@Getter` 어노테이션이 있는지부터 확인해보자.

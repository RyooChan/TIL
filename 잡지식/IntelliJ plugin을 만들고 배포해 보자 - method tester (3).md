# IntelliJ plugin을 만들고 배포해 보자 - method tester (3)

**(이 내용은 플러그인의 테스트와 배포에 관한 내용이다. 구현 부분은 [이전 글](https://hello-backend.tistory.com/334)을 참고하면 좋다.)**

만들었으니 이거를 테스트하고 배포해 보면 된다.(사실 근데 테스트는 이미 많이 해야한다 나는 처음부터 된 것처럼 썼지만 엄청 테스트 많이함)

## Test 방법

인텔리제이 오른쪽에 build.gradle을 보면

![image](https://github.com/user-attachments/assets/26c975a7-a66c-4ada-b1f2-221224dfee84)

요렇게 선택된 두개가 있다.

먼저 `runIde`에 대해 설명하면

저거 한번 수행해보면 intelliJ community 였나? 아무튼 인텔리제이 프로젝트 하나가 나오게 될 것이다.
이게 테스트 환경이라고 생각하면 된다.
저기서 원하는 프로젝트를 만들거나 열고, 이후에 테스트를 진행해 본다.

좋은 점은 문제가 있거나 확인할 부분들이 플러그인 콘솔에 뜬다는거!! 엄청난 장점이라고 할 수 있다.
이걸로 하나씩 기능을 테스트해 본 후에는 이제

## Plugin 생성 및 프로젝트 적용

`buildPlugin` 을 사용해서 실제로 플러그인을 만들면 된다.
그렇게 되면

![image](https://hackmd.io/_uploads/B1KDu8Wzke.png)

요런 식으로 프로젝트에 build-distributions-파일명

으로 플러그인이 생성된다.
이를 쓰기 위해서는

1. 플러그인을 사용하고 싶은 프로젝트 들어감
2. preference 들어감(mac 기준 command + ,)
3. plugins 검색
4. 톱니바퀴 클릭
5. Install Plugin from Disk 클릭
![image](https://github.com/user-attachments/assets/224ddc69-9363-42e4-aec8-c0c8ad3583e9)
6. 플러그인 프로젝트에서 만들어진 zip파일 선택
![image](https://github.com/user-attachments/assets/c5a11c59-2452-4843-83a1-fba41b335581)
7. 끝!!!!

이러면 만든 플러그인의 활용이 가능하다.

## 잘 되었는지 보자

[여기서](https://hello-backend.tistory.com/333) 나는 methodForTest 라는 친구를 테스트하기로 결정했다.

![image](https://github.com/user-attachments/assets/1a53587c-a05b-40f3-a36c-dd6f204c8572)

마우스 오른쪽 키를 누르면 요렇게 나온다.

그리고 `Run All Related Tests` 버튼을 누르면

![image](https://github.com/user-attachments/assets/9b0d6021-30aa-4a3e-a735-26f558c07ba7)

이런 식으로 모든 관련 테스트가 한번에 수행된다.

![image](https://github.com/user-attachments/assets/4e0ef97c-c378-45c8-ab89-dac1eaab9443)

참고로 methodForTest 에 대해

- methodForTest 를 직/간접적으로 호출하는 모든 테스트
    - 수행 대상 O
- methodForTest 가 직/간접적으로 호출하는 모든 테스트
    - 수행 대상 X

이다.
왜냐면 영향도는 이를 사용하는 곳에 체크하면 되고, 얘가 쓰는 것들은 반대로 그 영향에 들어가지 않기 때문이다.

잘 되었는지 확인해 보니, 문제가 없다.
이제 이를 한번 마켓에 올려 보려고 한다.

## 만든 플러그인 등록(배포) 하기

https://plugins.jetbrains.com/
젯브레인 플러그인에 들어간다.

![image](https://github.com/user-attachments/assets/6c9c65e7-4162-4138-8f6b-500639b0b902)

로그인해서 자기 이름 누르면 저렇게 Upload plugin 이라는 버튼이 나온다.

들어가서 이제 필요한 것들을 하나씩 적어주면 되는데

- vendor
    - Individual (나는 개인배포니까)
- Plugin file
    - 아까 만들어진 플러그인 파일
- License
    - Apache license 2.0
        - 나는 이걸로 했는데, 아파치는 개인배포에 자유롭고 사용할 수 있기 때문이다(상업적 이용도 되기는 하는데 무료배포니까 상관없긴함)
        - 보통 이걸로 하면 될것 같기는 하다.
- 나머지
    - 알아서 채우기ㅇㅅㅇ

이러면 된다.
이제 등록이 된거다. 되게 간단하다.
참고로 저거 만들어지면 Media 라고 있는데, 간략한 설명과 영상을 올려주면 도움이 된다.

저렇게 업로드가 되면 개인 메일로

![image](https://github.com/user-attachments/assets/d9904eda-999f-4105-b6bb-caa283020ed4)

이렇게 연락이 온다.
working day 기준 한 2~3일 정도면 된다고 한다.

별 문제가 없으면

![image](https://github.com/user-attachments/assets/6f546e19-e914-426d-9b33-1d67293e454a)

이렇게 등록이 된다!!!!

다음과 같은 과정으로 등록에 대해 살펴 보았는데, 생각보다 난이도가 높지도 않고 재미있다.

이거 있으면 좋겠는데... 싶은 기능이 있다면 슬쩍 만들어 보는걸 추천한다.(근데 왠만한 기능은 다 이미 있다. 자신이 생각하는 그대로의 기능을 원할 때에 만들고 나머지는 걍 검색해 쓰십쇼.)

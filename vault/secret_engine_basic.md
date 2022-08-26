# Secret Engine

데이터를 암호화하고, 만들어내고, 저장하는 컴포넌트를 Secret Engine이라 한다.
그들의 기능 측면으로 생각하는 것이 가장 이해하기 쉬운 방법이다.

Secret Engine에는 일부 데이터의 집합이 제공되며, 그 데이터에 대한 작업을 수행하고, 결과를 반환한다.

몇몇 Secret Engine들은 암호화된 Redis / Memcached처럼 데이터를 저장하고 읽어온다.
다른 Secret Engine들은 다른 서비스에 연결하고 dynamic credentials를 요청할 때 마다 생성한다.
또  다른 Secret Engin들은 암호화를 서비스, totp생성, 인증 등등을 제공한다.

Secret Engine은 Vault의 `path`에서 사용 가능하다.
Vault로 request가 오면 라우터는 자동으로 route prefix가 있는 모든 친구들을 Secret Engine으로 전송한다.
이런 방식으로, 각각의 secret engine은 자체적으로 `paths`와 `properties`를 정의한다.
Secret Engine은 사용자에게 가상 파일시스템과 비슷하게 등작하여 읽기 쓰기 삭제 작업을 지원한다.

## Secret Engine의 라이프사이클

대부분의 Secret Engine은 API / CLI를 통하여 활성화, 비활성화, 튜닝, 이동이 가능하다.

### 활성화(Enable)

주어진 경로에서 Secret Engine의 사용을 가능하게 한다.
몇몇 예외를 제외하고 Secret Engine은 여러 경로에서 활성화될 수 있다.
각 Secret Engine은 그 자신의 경로에 격리되어 있다.
기본적으로 Secret Engine은 `type`에 의하여 활성화된다. (ex : "aws"는 `aws/` 라는 경로에서 활성화된다.)

> Secret Engine의 경로에서 path는 대소문자를 비교한다!!


### 비활성화(Disable)

기존 secret engine을 비활성화한다.
이 경우 모든 secret이 취소되고 물리 저장소 layer의 해당 engine에 저장된 전체 데이터가 삭제된다.

### Move

기존 secret engine의 `path`를 변경한다.
이 프로세스는 모든 secrets를 취소시키는데, 이는 secret lease들이 그들이 생성된 path에 묶여 있기 때문이다.
engine에 저장된 구성 데이터는 `move`를 통해 유지된다.

### Tune

TTL과 같은 global configuration을 조정한다.

---

Secret engine이 활성화되면, 자체 API에 따라 직접 그 경로에서 상호작용 가능하다.
`vault path-help`를 사용하여 응답 경로를 결정할 수 있다.

Mount point는 Vault내에서 conflict될 수 없다는 것을 기억해야 한다.
위의 내용에는 2가지 내용이 있다.

1. 기존 존재하는 mount로 prefixed된 mount를 가질 수 없다.
2. 존재하는 mount로 prefixed된 mount point를 만들 수 없다.

예를 들어 `foo/bar` mount와 `foo/baz` mount는 평화롭게 서로 공존할 수 있지만,  `foo` mount와 `foo/baz` mount는 그럴 수 없다.

## Barrier View

Secret Engine은 Vault의 물리 storage에 대한 `barrier view`를 제공한다.
Secret Engine이 활성화되면 랜덤한 UUID가 생성된다.
이것이 해당 엔진의 data root가 된다.
Engine이 물리 저장 층에 무언가를 적을 때 마다 해당 UUID폴터 접두사가 붙게 된다.
Vault의 Storage 계층은 상대경로를 지원하지 않으므로(`../` 이런거) 사용 가능한 Secret Engine이 다른 데이터에 접근하는 것을 이 Barrier View가 허용해 준다.

이것은 Vault에서 중요한 보안 기능이며, 악성 엔진도 다른 엔진의 데이터에 액세스 할 수 없다.

# OIDC와 OAuth

OIDC와 OAuth의 차이점은 OIDC는 Authentication에 집중하고, OAuth의 경우는 Authorization을 포함한다는 것이다.

## Authentication과  Authorization

* Authentication(인증)
    * 사용자가 누구인지 확인하는 절차
    * 해당 어플리케이션에의 접근 가능 여부를 확인한다.
* Authorization(인가)
    * 사용자의 권한을 확인하는 절차
    * 어플리케이션에 접근 후, 특정 서비스에의 사용 가능 여부를 확인한다.

## OIDC / OAuth 차이

OAuth는 클라이언트가 리소스 소유자로부터 서버의 자원에 대해 인가받고 접근하기 위해 사용된다.
즉 인가를 주요 목적으로 하고 있고, 인증은 이의 주요 목적이 아니다.
OAuth에서는 사용자에 대한 정보를 명시적으로 제공하지 않고 Access Token형태로 권한을 제공한다.
이 Access Token을 통해 어떤 권한이 있는지 알 수 있지만, 사용자에 대한 정보를 알 방법은 없다.

OIDC는 인증을 목표로 한다.
이를 사용하면 클라이언트가 ID Token을 얻을 수 있고, 이 Token에는 사용자의 신원 정보가 포함된다.
해당 ID Token에는 사용자의 정보(이름, Email 등..)이 있기 때문에 사용자의 인증이 가능하지만, 권한을 알 방법은 없을 것이다.

정리하자면 OAuth의 경우 access token, OIDC의 경우 사용자 ID Token 확보에 목적이 있다.

## OIDC의 사용 이유

그런데 위에서 확인했을 때에 OAuth에서 사용자의 권한을 가져오려 한다면, 그 전에 사용자 정보를 확인해야 할 것 같다.
그러면 OAuth를 할 때에, 그리고 Access Token을 활용해서 해당 사용자의 정보를 가져오는것도 가능 할 것이다.

그런데 굳이 OIDC를 사용하는 이유가 무엇일까??

OIDC는 사실 OAuth 프로세스를 확장하여 구현한다.
OAuth는 인가를 목적으로 하는데, 이 인가의 과정에서 인증을 진행하게 된다.
여기서 OAuth의 스코프(클라이언트가 리소스 서버에 접근할 수 있는 제한 범위)로 openid값이 포함되어 들어오면 OIDC가 Access Token과 함께 사용자 인증에 대한 정보를 ID Token으로 불리는 JWT로 반환한다.

이런 식으로 OIDC를 따로 진행하게 된다면, OAuth만으로 통신하는 경우

1. Access Token 발급
2. 해당 Access Token을 통하여 사용자 리소스 발급

에서

1. OIDC를 사용하여 바로 Access Token과 함께 ID Token 발급

으로 사용자 정보 조회 통신을 절반으로 줄일 수 있게 된다.

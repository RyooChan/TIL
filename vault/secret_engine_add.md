# Identity Secrets Engine

Identity Secrets Engine은 Vault의 Identity 관리 솔루션이다.
Secret Engine은 Vault에 의해 인식된 clients들을 내부적으로 관리한다.
각 client는 내부적으로 `Entity`로 간주된다.
하나의 Entity는 여러개의 `Aliases`를 가질 수 있다.
예를 들어, GitHub와 LDAP 모두의 계정을 가지고 있는 하나의 유저는 두 개의 aliases(GitHub type과 LDAP type)를 가진 한 개의 entity에 매핑된다.
client가 어떤 backend(Token방식은 제외)를 써서 authenticate하면 기존 entity가 없는 경우 Vault는 새로운 entity를 만들고 거기에 새로운 alias를 붙인다.
entity 식별자는 authenticated된 token에 연결된다.
이러한 토큰을 사용하면 entity 식별자가 특정 유저에 의해 수행된 actions들을 log하여 표시된다.

Identity 저장소는 운영자가 Vault에서 entity들을 관리할 수 있도록 허가해준다.
ACL의 API를 통해 entity가 만들어질 수 있고, alias들이 붙여질 수 있다.
entity 식별자에 연결된 token에 기능을 추가하는 entity들에는 policy(정책)들이 설정될 수 있다.
entity에서 상속되는 Token의 기능들은 기존 기능을 대체하지 않고 추가되게 된다.
이미 언급한 것처럼 이는 token의 aceess를 유연하게 제어할 수 있다.

> 이러한 secret engine은 기본적으로 mount된다.
> secret engine은 비활성화되거나 옮길 수 없다.

Vault identity secret engine은 여러 가지 기능을 제공한다.
각각의 것들은 따로따로 자체 페이지에 저장된다.


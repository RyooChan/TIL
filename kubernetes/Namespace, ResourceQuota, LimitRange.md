## Namespace, ResourceQuota, LimitRange

쿠버네티스 클러스터에는 전체 사용할 수 있는 자원이 있다.
메모리, CPU 등의 자원
그래서 각각의 namespace 에는 여러 파드를 만들 수 있다.
각 파드는 필요한 자원을 클러스터 내의 자원을 공유해서 쓰는데, 만약에 한 파드에서 전부 써버리면 다른 파드에서는 사용할 수가 없다.
이런 문제 해결을 위해 Resource Quota 가 존재해서 각 namespace 마다 최대 한계를 정할 수 있다.
하나의 파드에서 문제가 있기는 해도 다른 namespace 에서의 문제는 없다.
근데 한 파드에서 너무 사용량을 크게 하면 그 namepspace 에 들어갈 수 없는데, limitRange 를 통해 한 pod 가 namespace 에 들어가도록 제한 가능
파드 뿐 아니라 클러스터에도 가능하다.

- namespace
    - 한 namespace 내에서 pod 의 이름은 중복이 불가능하다.
    - 타 네임스페이스의 자원과는 분리되어 관리된다.
        - 즉, 어떤 namespace 의 service 가 다른 namespace 의 pod에 접근이 안되는거
    - namespace 를 지우면 그 내부의 자원들이 다 지워진다.
- ResourceQuota
    - 네임스페이스의 자원 한계를 설정
    - 제한하고 싶은 자원을 적어서 설정 가능
    - ResourceQuota 에 제한된 것을 만들어 줬다면 내부의 pod 들에도 그 내용을 작성해 줘야 한다.
- LimitRange
    - 각 파드마다 네임스페이스에 들어올 수 있는지 자원을 체크해 준다.

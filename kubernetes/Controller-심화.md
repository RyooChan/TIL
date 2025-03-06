## StatefulSet

- Stateless Application
    - Web Server(아파치나 ngingx, iis 등)
    - 앱이 여러개 배포되어도 서비스의 역할을 한다.
        - 즉 얘는 앱이 하나 죽어도 걍 복제해주면 된다.
    - 참고로 얘는 따로 volume이 필요하지 않다.
    - 대체로 사용자가 접속하고 트래픽을 분산시켜야 한다.
    - 이 역할에 맞춰서 사용되는게 ReplicaSet Controller
- Stateful Application
    - Batabase(mongo, mariaDB, redis 등)
    - 각각의 앱마다 자신의 역할이 있다.
        - main - arbiter(감지해서 secondary가 main되도록 해둠) - secondary 이런식
            - 그래서 만약에 앱이 하나가 죽어버리면 그 역할을 하는 애가 반드시 만들어야 하고 이름도 변경되어서는 안된다.
    - 이거는 각각의 역할이 달라서 volume를 각각 써야 한다.
        - 그래야 하나의 앱이 죽어도 다른 것이 역할을 이어가야 한다.
    - 트래픽은 그냥 분산이 아니라 각 역할에 맞춰서 들어와야 한다.
    - 이 역할에 맞춰서 사용되는게 StatefulSet Controller

그래서 Stateful Application 이 될 수 있도록 해주는게 StatefulSet Controller.
해당 파드에 접근하기 위해서 목적에 따라 연결하기 위해 Headless Service를 달아주면 된다.

- StatefulSet Controller
    - replicas와 비교해 보자면
    - replicas를 정하면
        - statefulSet은 랜덤 이름이 아니라 0부터 숫자가 생겨서 순차적으로 생성된다.
            - 추가로 하나의 파드 삭제 후 생성하면 삭제된 기존 이름과 동일한 이름으로 생성된다.
        - 숫자를 줄이면 index가 높은 것부터 순차적으로 삭제된다.
    - 볼륨의 경우
        - ReplicaSet은 PV과 연결을 위해서는 PVC를 별도로 생성해야 한다.
            - StatefulSet은 template를 통해 파드가 만들어지고 추가적으로 volumeClaimTemplates 를 통해 PVC가 동적으로 생성되고 바로 연결된다.
        - replicas가 여러개라면 ReplicaSet은 하나의 PVC와 연결이 된다.
            - StatefulSet 은 volumeClaimTemplates 을 통해 각각 PVC PV가 따로 연결된다.
                - 그리고 기존 Pod가 삭제되면 기존의 PVC에 신규 pod가 붙게 된다.
        - replicas를 0으로 두어도 Stateless 는 기존의 PVC, PV는 삭제되지 않는다.
            - ServiceName을 Headless로 두면 이름을 알기가 쉬워서 외부에서 원하는 pod 로 접근이 가능하다.

## ingress - Nginx

- 용례
    - Service LoadBalancing
        - 여러 service를 pod 별로 나눠서 관리하면 하나에 문제가 생겨도 다른 것에는 문제가 없다.
        - 그리고 각각에 서비스를 달아주고 Ingress를 사용해서 L4, L7 스위치를 대체할 수 있다.
            - path 를 통해서 각각 어디로 연결될지를 정해준다.
                - 이걸로 IP loadBalancing 이 따로 필요 없다.
    - Canary Upgrade
        - v2가 있다고 할 때 사용자가 Ingress로 접근했을 때에 몇 %만 이동하거나 header를 통해서 가는 애들을 조정할 수 있다.
            - weight 같은거 쓰면 10%만 원하는 위치로 보내던가 그럼
    - Https
        - 참고로 이거는 443포트로 연결해야 한다.
        - TLS에 secret 오브젝트로 연결하여, 데이터로 담은 인증서 값을 쓰면 https 로만 접근할 수 있도록 한다.
- ingress는 kubernetes 가 있으면 바로 만들 수 있다.
    - Host(Domain name), Path(어떤 path는 어디로 갈지)
    - 근데 위의 내용만 있으면 별 의미가 없고, 이를 실제로 동작하게 하기 위해서는 plugin이 필요하다. (NginX, Kong 등)
        - 그래서 이를 통하면
            - namespace가 생기고
            - 그 위에 Deployment, ReplicaSet이 만들어지면서 NginX 구현체인 Pod가 생겨서 얘가 이제 각각 Service에 접근을 한다.
            - 그리고 그 Pod에 연결하기 위해서 Service 가 필요하다.(여기서는 NodePort, LoadBalancer 등으로 접근할 것)
            - 그래서 요 NginX로 외부에서 접근하게 되면 이를 통해 연결이 되는것
            - Ingress는 여러개 사용할 수도 있다.
                - Host 여러개 가능

## AutoScaler - HPA

- HPA
    - 파드의 갯수를 늘림 (scale out)
    - replicas가 정해져 있는데, pod의 리소스를 모두 사용하게 된다면 pod가 죽을 수 있다.
        - 그런데 미리 HPA를 만들고 Controller에 만들어 둔다면 HPA가 pod 상태를 보고 있다가 필요할 때에 replicas를 높여 준다.
            - 반대로 트래픽이 다시 감소해서 리소스 사용량이 줄어들면 파드를 삭제한다.
                - 기동이 빠른 **Stateless APP** 에 사용하기.
- VPA
    - 파드의 리소스를 증가시킴 (scale up)
    - Stateful App 에 사용하면 된다.
    - 그리고 한 Controller 에 HPA와 함꼐 사용이 안된다.
- CA
    - 클러스터 노드를 추가
    - 노드 2개가 있고 파드들이 운영되고 있다고 할 때에 각 파드들이 node의 모든 자원을 소모하고 있으면 더이상 노드에 pod 추가가 안된다.
    - 그럴 때 Scheduler 는 CA 한테 node를 만들어달라 하고 얘가 외부 Cloud Provider에 node를 만든다.
        - 그러면 Scheduler가 해당 node에 pod 생성 후 사용
    - 이런 후 기존 노드에 자원이 남으면 스케쥴러가 이를 감지하고 CA에게 기존 노드 삭제하도록 요청한다.

## HPA Architecture

- Master node와 worker node가 있다고 가정하면
- Master node에 Control Plane Component가 있다면
    - 쿠버네티스 주 기능을 하는 컴포넌트가 Pod 형태로 돌아가고 있다.
        - Controller Manager에는 자주 사용하는
            - Deployment, ReplicaSet, DaemonSet, HPA, VPA, CA ... 기능이 스레드 형태로 돌아감
            - apiserver는 컴포넌트가 서로 통신하거나 모든 과정에서 사용
- Worker Node Component
    - kubelet
        - 실제 컨테이너를 생성하고 삭제하는 구현체

그래서 이제 사용자의 관점에서 볼 때 Pod를 하나 만든다고 한다면

1. 사용자가 ReplicaSet에 Pod 생성 요청
2. ReplicaSet은 kube-apiserver 를 통해 워커노드에 요청
3. kubelet에서 Controller Runtime으로 실제 컨테이너 생성
4. 도커가 노드 위에 컨테이너를 만들어 준다.

이제 여기서 HPA가 어떻게 파드 생성을 알게 되는지를 보면

1. Controller Runtime로부터 ResourceEstimator가 도커로부터 memory, CPU 정보를 가져갈 수 있도록 kubelet이 해놨다.
2. matrics-server이 각각 worker node의 kubelet한테 메모리와 CPU 정보를 가져와서 저장한다.
3. 그리고 이 데이터를 다른 컴포넌트들이 사용할 수 있도록 master node의 kube-apiserver에 저장해놓는다.
4. 그러면 이제 HPA가 이를 체크하고 있다가 파드의 리소스 사용량이 증가한다고 하면
5. 얘가 ReplicaSet의 replicas를 증가시킨다.
6. 참고로 prometheus를 쓰면 이것저것 다 수집 가능하다.

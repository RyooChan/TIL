## Service - Headless, Endpoint, ExternalName

Pod 의 입장에서 pod 나 혹은 service, 아니면 외부 서비스에 연결하는 방법에 관함
사용자의 접근은 서비스가 만들어진 후에 IP 를 통해 접근하면 되는데, pod의 경우는 자원이 동시에 배포될 수 있다.
이렇게 되면 생성 시점에 동적으로 IP가 할당되기 때문에 이쪽의 pod의 IP 를 알기 어렵기도 하고... 재생성되는 경우 이전 IP 에서 변경이 된다.
이를 해결하기 위해서는 DNS, Headless 가 필요하다.
그리고 외부 서비스를 사용할 때에 A 서비스에 연결하다가 B 서비스로 변경하려고 한다면.. 재배포가 아니라 ExternalName 을 이용한다면 따로 파드 수정 없이 변경이 가능하다.

Kubernetes Cluster 에는 DNS Server가 별도로 존재하고, 얘는 service 의 도메인 이름과 IP number 가 있다.
내부망에서도 DNS Server가 있다면 내부 서버들이 생겼을 때 이 이름들이 DNS에 등록되어 있을 거고, 파드가 유저 1을 찾았을 때 쿠버네티스 DNS가 없다면 상위 DNS를 찾게 되고 해당 이름의 IP를 알려준다.
외부 External Network도 마찬가지이다.

Headless에 service-pod 를 등록하면 그냥 그대로 DNS Server에 등록되기 때문에 이를 통해 바로 해당 pod 를 찾을 수 있다.
ExternalName은 특정 외부 도메인 주소를 넣을 수 있는데 잘 넣어 주면 DNS 타고 가서 해당IP를 받아올 수 있다.

- Headless
    - Service 를 만들면 설정한 이름을 바탕으로 DNS의 값이 만들어진다(예를 들어 FQDN방식으로는 service이름.namespace설정값.svc(서비스라면).cluster.local(DNS이름) 요런 식으로) <- 최초에 만들 때에 기입한 값을 토대로 만들어지기 때문에 예측할 수 있는 값이 나온다.
        - 참고로 pod 같은 애들은 아이피가 박혀있어서 못쓴다고 봐도 됨...
            - 그래서 clusterIP를 통해서 service 로 접근하기 위해서는 이걸 써도 좋음
    - 위의 방식으로는 DNS 를 써도 pod -> pod 는 불가능할 것 같은데, 이를 연결하기 위해서 Headless 가 사용된다.
        - service 의 clusterIP를 none으로 하고 pod에 hostname과 subdomain을 두면 DNS에서 pod 도 ip가 아니라 이렇게 설정한 값들을 기반으로 만들어지게 된다.
- Endpoint
    - Service - pod 연결이 실제로는 Endpoint로 되어 있다.
        - Endpoint가 service 이름과 pod IP 주소, 포트를 가지고 연결해주는것
            - 그래서 이를 활용해서 따로 selector 없이도 Endpoint를 활용해서 service - pod 간 연결이 가능하다.
                - 그런데 만약 Endpoint를 활용해 외부 도메인과 연결을 하려면 어떻게 할까?
                    - 이를 위한게 ExternalName
- ExternalName
    - DNS cache가 내부와 외부 DNS를 찾아 IP를 알아낸다.
        - 그래서 pod가 이를 활용해 외부 서비스를 잘 찾아낸다.

## Volume - Dynamic Provisioning, StorageClass, Status, ReclaimPolicy

먼저 볼륨은, 데이터를 안정적으로 유지하기 위해서 사용하는 것이다.
그래서 실제 데이터는 쿠버네티스 클러스터와 분리되어 관리된다.
그 볼륨은 크게 두개 종류가 있는데, 내부망에서 관리하는 종류와 외부망에서 관리하는 종류가 있다.
외부망은 AWS, GCP, Azure같은 클라우드 스토리지에서 내 쿠버네티스 클러스터와 연결해서 사용 가능
내부망은 물리적인 공간에 데이터를 넣는 hostPath, local을 사용하거나 GlusterFS 같은 솔루션을 쓸 수도 있다. 아니면 NFS를 통해 다른 서버를 쓸 수도 있고.

관리자는 PV를 통해 Storage(몇기가인지?)와 AccessMode(ReadWriteOnce, ReadWriteMany 등) 을 선택한다.
그리고 사용자는 PVC를 통해 적절한 PV와 연결하고, 이 PVC를 pod에서 사용한다.

근데 위의 방식으로 쓰면 볼륨이 필요할 때마다 PV를 만들어야 하고 그에 맞춰서 Storage, AccessMode를 맞춰야 하는데 굉장히 불편하다.

그래서 kubernetes 에서는 Dynamic Provisioning이라고 PVC를 만들면 알아서 PV를 만들어 볼륨과 연결해주는 기능이 있다.
그리고 상태를 통해 각 Status 확인 가능하고 PV삭제에도 정책적인 내용이 있다.

- Dynamic Provisioning
    - StorageOS같은 솔루션의 설치가 필요하다.
    - StorageClass
        - PVC를 통해 PV를 바로 만들고 연결까지 할 수 있게 해준다.
- Status, ReclaimPolicy
    - PV를 직접 만드는 경우에는 만들어졌다고 바로 볼륨이 만들어지는 것은 아니고 pod가 연결되어야 실제로 볼륨이 만들어진다.
        - 그런데 중간에 그 pod가 삭제되어도 데이터에는 문제가 없다.
            - PVC가 삭제되어야 PV가 Released가 된다.
                - 그리고 PV와 실제 데이터가 연결이 끊기면 Failed가 된다.
    - 여기서 PVC와 PV 연결이 끊기는 Released 상태에 관하여
        - ReclaimPolicy에 따라 실제 동작이 달라지는데
            - Retain
                - PVC 삭제되면 PV Released -> 데이터는 보존되지만 다른 PVC에 연결할 수는 없다.
                    - 즉 재사용 불가
                - 그냥 PV 만들면 기본 정책이다.
            - Delete
                - PVC 삭제되면 Volume에 따라 데이터가 삭제가 되기도 하고 안되기도 함??
                - StorageClass 를 통해 만들어지는 경우 기본 정책이다.
            - Recycle
                - 데이터는 삭제되는데 재사용은 가능함
                - 근데 이거 Deprecated됨

## Accessing API

마스터 노드에 kubernetes API 서버가 있는데, 이 API server를 통해서만 kubernetes 에 자원을 만들거나 조회할 수 있다.
외부에서 API로 접근할 수 있다면 인증서를 가지고 있는 사람만 https를 통해 접근 가능
만약 운영자가 proxy로 열어주는 경우는 http로 가능하기는 함.
그리고 kubectl은 마스터에만 쓸 수 있는게 아니고 외부에서도 가능
config를 사용하면 kubernetes cluster이 여러개 있을 때 어디로 연결할지 확인할 수 있고 연결이 된 상태에서는 파드 정보를 가져올 수 있다.
유저 입장에서 API 서버에 접근하는 방법이고 이거를 User Account 라고 한다.

그런데 pod에서 kubernetes API server에 접근한다면?
Service Account를 사용해서 접근한다.

즉 유저들이 Kubernetes API에 접근하는 User Account, Pod 에서 Kubernetes API에 접근하는 Service Account 가 있다.

- Authentication
    - 접근이 가능한지 인증의 경우
- Authorization
    - 접근해서 원하는 기능을 사용할수 있는지에 대한 인가의 경우
- Admission Control
    - PV 만들 때 사용자가 용량을 1기가 이상으로 못만들게 해놨다면 막는 등등의 방법

## Authentication

- X509 Client Certs
    - kubernetes API Server 6443
        - kubeconfig 에 CA crt, Client crt, Client Key 가 있어서 이를 통해 진행
- kubectl
    - 외부 서버에 이를 설치해서 멀티클러스터에 접근
    - 사전에 각 클러스터에 있는 kubeconfig 이 내 kubectl에 있으면 가능
        - clusters 라는 곳에 이름과 연결 정보, CA인증서가 있다.
        - users에는 이름과 crt, key가 있다.
        - 이를 통해서 연결 가능
- Service Account
    - Kubernetes API Service
    - Pod ServiceAccount 에 토큰값을 통해서 사용자가 접근 가능

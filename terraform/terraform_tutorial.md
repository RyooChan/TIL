# Terraform 기본 개념

Infrastrupctupre as Code, 코드로써의 인프라

> 인프라를 이루는 서버, 미들웨어 그리고 서비스 등, 인프라 구성요소들을 코드를 통해 구축하는 것

IaC는 코드로써의 장점, 즉 작성용이성, 재사용성, 유지보수 등의 장점을 가진다.

## Terraform by Hashicorp

테라폼은 현재 정말 많이 쓰이는 도구이고, 현재는 거의 업계 표준으로 쓰인다.

테라폼은 인프라를 만들고, 변경하고, 기록하는 IaC를 위해 만들어진 도구이다.
문법이 쉬워 비교적 다루기 쉽고 사용자가 많아 레퍼런스를 찾기 쉽다.
`.tf`형식의 파일 확장자를 갖는다.

AWS, Azure, GCP같은 퍼블릭 클라우드뿐만이 아닌 다양한 서비스 역시 지원한다.

## 테라폼 구성요소

* provider
    * 테라폼으로 생성할 인프라의 종류를 의미한다.
* resource
    * 테라폼으로 실제 생성할 인프라 자원을 의미한다.
* state
    * 테라폼을 통해 생성한 자원의 상태를 의미한다.
    * 실제로 테라폼 코드를 실행한 결과물이 파일 형태로 남게된다.
* output
    * 테라폼으로 만든 자원을 변수 형태로 state에 저장하는 것을 의미한다.
* module
    * 공통적으로 활용할 수 있는 코드를 문자 그대로 모듈 형태로 정의하는 것을 의미한다.
    * 재사용에 큰 장점이 있다.
* remote
    * 다른 경로의 state를 참조하는 것을 말한다.
    * output 변수를 불러올 때 주로 사용한다.

### provider

```
# 보통 provider.tf 로 파일을 생성한다.
# AWS provider
provider "aws" {
    region = "ap-northeast-2"
    version = "~> 3.0"
}
```

실제 프로바이더들이 provider "aws" 장소에 위치하게 되고, 그 프로바이더들마다 각각의 인자값이 들어가게 된다.
보통 AWS라면, 이 AWS를 다루기 위한 내부 파일들을 다운로드하는 역할 -> SDK같은 애들을 다운로드하는 역할을 한다.


Provider 내에서 다양한 Arguments를 가진다.
AWS resource를 다루기 위한 파일들을 다운로드 하는 역할을 한다.

### resource

```
# main.tf, vpc.tf등 원하는 형태로 파일이름 사용
# Create a VPC
resource "aws_voc" "example" {
    cidr_block = "10.0.0.0/16"
    # cidr_block 이외에도 수많은 인자 존재
}
```

파일명은 알아서 원하는대로 하면 된다.
코드 형태는 resource가 가장 먼저 오게 되고, 그 리소스의 이름이 온다.
`aws_vpc` <- 어떤 resource를 만들지에 대한 명시이다.
이외로도 `aws_lb` 등등이 있다.

그리고 저 위의 `"example"` 부분이 실제 이름이 된다.

각각의 resource마다 수많은 인자값들이 존재한다.

이 코드는 테라폼으로 VPC를 생성하는 코드이다.
VPC역시 다양한 Argument와 다른 구성요소가 존재한다.

### state

```
# terraform.tfstate 라는 파일명을 가진다.
{
    "version" : 4,
    "terraform_version" : "0.12.24",
    "serial" : 3,
    "lineage" : "3c77XXXX-2de4-7736-1147-038974a3c187",
    "outputs": {},
    "resources": [
        {....},
        {....}
    ]
}
```

테라폼 state이다.

테라폼으로 작성한 코드를 실제로 실행하게 되면 생성되는 파일이다.
참고로 실제로는 코드가 굉장히 길어질 수 있다.

현재 인프라의 상태를 의미하는 것은 아니다.
state는 원격 저장소인 'backend'에도 저장될 수 있다. -> 대부분 현업에서는 'backend'에 저장한다.

### output

```
resource "aws_vpc" "default" {
    cidr_block = "10.0.0.0/16"
    # cidr_block 외에도 수많은 인자가 존재한다.
}

output "vpc_id" {
    value = aws_vpc.default.id
}

output "cidr_block" {
    value = aws_vpc.default.cidr_block
}
```

아웃풋은 이런 식으로 aws_vpc resource를 만들었다고 하면 vpc ID나 cidr값들이 생기는데, 그런 것들을 참조해서 vpc_id란 형태의 변수를 state파일로 저장한다.

이 값들은 remote를 사용해서 재사용할 수 있다.

VPC역시 다양한 Arguement와 다른 구성요소가 존재한다.

### module

```
module "vpc" {
    source = "../_modules/vpc"

    cidr_block = "10.0.0.0/16"
}
```

실제 모듈의 resource에 대한 코드는 `"../_modules/vpc"` 이런 느낌으로 경로로서 포함하게 되고, 이 하위 경로로 tf파일들 == 리소스 파일들이 위치하게 된다. 

이 안에 들어갈 인자값(variable)들을 `cidr_block = "10.0.0.0/16"` 이런 느낌으로 넣어주게 된다.

-> 이 모듈은 재사용성에 큰 강점이 있고, 모듈을 잘 쓰냐 아니냐에 따라 테라폼을 잘 쓰는지가 나타난다.

### remote

```
# remote는 원격 참조 개념으로 이해하면 좋다.
data "terraform_remote_state" "vpc" {
    backend = "remote"

    config = {
        bucket         = "terraform-s3-bucket"      # backend에서의 s3 bucket을 의미한다.
        region         = "ap-northeast-2"           # 특정 region값
        key            = "terraform/vpc/terraform.tfstate"  # 해당 region에 대한 key값
    }
}
```

리모트는 원격 참조 개념으로 이해하면 된다.
위처럼 bucket을 적어두고, 어떤 특정 region값과 그에 맞춘 key값을 명시하게 되면 여기 저장된 state값을 참조할 수 있게 된다.

결국 state파일에서 output으로 저장된 변수들을 가져오게 된다.

## 테라폼 기본 명령어

일단 기본 명령어들만.

* init
    * 테라폼 명령어 사용을 위해 이에 필요한 각종 설정을 진행한다.
    * 최초에 테라폼 명령어를 사용할 때에 해주어야 한다.
* plan
    * 테라폼으로 작성한 코드가 실제로 어떻게 만들어질지에 대한 예측 결과를 보여준다.
    * 실제로 가장 많이 쓰이는 명령어이다.
* apply
    * 테라폼 코드로 실제 인프라를 생성하는 명령어이다.
* import 
    * 이미 만들어진 자원을 테라폼 state파일로 옮겨주는 명령어이다.
    * 예를 들어 `route53`이라는 것을 만든 후에, 이거를 다시 코드로 만들고 싶을때 사용한다.
        * 즉 이미 만들어진 자원을 코드로 만들고 싶을때!!
* state
    * 테라폼 state를 다루는 명령어이다. 하위 명령어로 mv, push와 같은 명령어가 있다.
* destroy
    * 생성된 자원들 state파일 모두를 삭제하는 명령어이다.

### 테라폼 명령어의 Process

#### 1. init

맨 처음에 프로세스는 init을 진행한다.
init을 하면 provider설정에 따라 테라폼의 다른 명령어들을 위한 설정을 진행하게 된다.
내부적으로는 provider와 state, module설정 등이 있다.

#### 2. plan

실제로 작성한 테라폼 코드가 어떻게 만들어질지에 대한 예측 결과를 보여주는 명령어이다.
가장 많이 쓰이는 명령어이다.
`기본적으로 plan에 문제가 없어야 apply에 문제가 없을 확률이 높다.`
그리고 뭔가 변경점이 있거나 시행할 때에 plan을 계속 실행해보면서 시도해보는 것이 중요하다.

#### 3. Apply

실제로 작성한 코드로 명령어를 생성하는 명령어이다.
실제 서버에 Apply를 통해서 적용한다.
위의 plan을 자주 실행해야 하는 이유는, 이 Apply명령어를 적용하게 되면 인프라가 변경되기 때문이다.
이건 좀 주의깊게 실행합시다

---

이 밖에도 명령어가 굉장히 많기는 한데, 이거는 기본 명령어 습득 이후에 시작해도 괜찮다.

> 테라폼은 정말 많은 기능을 지원하는데, 기본 명령어가 익숙해진 다음에 시작하자. 왜냐면 기본 명령의 사용 빈도가 실제 90% 이상을 차지하니까, 이거부터 쓰면서 해워나가면 된다 한다!!!

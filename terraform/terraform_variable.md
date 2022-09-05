# Terraform

Infrastructure as Code

##  Variables

코드에서 변수를 사용할 수 없다면, 매번 다른 값이 필요할 때 마다 하드코딩된 코드 한벌씩을 새로 만들어야 할 것이다.

테라폼의 설정 파일을 작성하는 운영자와 개발자는 변수의 정의를 코드와 함께 작성하여 텍스트 편집기를 열지 않고도 인프라를 코드로 쉽게 정의할 수 있다.

* .tfvars형식의 직접적 변수 선언
* 일반 .tf파일에서의 변수 선언
* Inline변수 선언 또는 변수 파일 지정 (-var / -var-file)
* 환경 변수 선언

OSS

* Terraform 구성 또는 모듈의 입력 변수 관리

지원되는 변수의 종류는 이렇게 3가지 카테코리로 이루어져 있다.

```
Primitive Types
    string
    number
    bool

Collection Types
    list
    map
    set

Structural Types
    object
    tuple
```

```
variable "string" {
    type        = striung       // 해당 변수의 형태를 지정
    description = "var String"  // 어떤 의미를 갖고 있는지에 대한 설명을 기입할 수 있다. 이는 협업 등을 할 때에 도움이 된다. 
    default     = "myString"    // 변수의 기본 값을 여기서 정의해 줄 수 있다.
}

variable "number" {
    type        = number
    default     = "123"
}

variable "boolean" {
    default = true
}

variable "list" {     
    default = [
        "google" ,
        "vmware" ,
        "amazon"
    ]
}

output "list_index_0" {
    value = var.list.0    // list라는 var의 0번째 값 google을 가져온다.
}

output "list_all" {
    value = [
        for name in var.list :
            upper(name)         // 해당 list의 모든 값들을 가져온다.
    ]
}

variable "map" {        // key-value형태로 돌아간다.
    default = {         // 그리고 map은 key를 기준으로 sort된다.
        azure = "microsoft" ,
        gcp = "google" ,
        aws = "amazon"
    }
}

output "map_key" {
    value = var.map.aws      // 해당 `map`에 있는 aws를 가져온다. -> "amazon"을 가져오겠지.
}

output "map_all" {
    value = var.map     // 이렇게 하면 `map`에 있는 모든 값들을 가져온다.
}

variable "set" {
    type = set(string)
    default = [
        "google", 
        "vmware",
        "amazon",
        "microsoft"
    ]
}

output "set" {
    value = var.set
}

variable "object" {
    type = object({ name = string, age = number})
    default = {
        name = "abc"
        age = 12
    }
}

output "object" {
    value = var.object
}

variable "tuple" {      // 튜플을 사용해 복합적인 형태 작성 가능
     type = tuple([string, number, bool])
     default = ["abc", 123, true]
}

output "tuple" {
    value = var.tuple
}

```

---

추가로 validation을 통해 해당 validate를 진행할 수 있는데

```
variable "testVar"{
    type = string
    description = "for test"
    default = "testVar"

    validation {
        condition = can(regex("^test", var.testVar))
        error_message = "The testVar must be starting with 'test'"
    }
}

variable "testVar2"{
    type = string
    description = "for test"
    default = "testVar2"

    validation {
        condition = length(var.testVar2) > 4
        error_message = "The testVar length up to 4"
    }
}
```

이런 식으로 condition을 통해 validation체크와 정규식 사용 모두 가능하다.

---

그리고 변수들은 순서대로 마지막에 오는 값이 실제 해당 변수의 값이 된다.

```
Order
    ENV
    terraform.tfvars
    terraform.tfvars.json
    *.auto.tfvars or *.auto.tfvars.json
    CLI comment -var -var-file
```

요기서 처음에 환경 변수 `ENV`가 나오고 해당에서 처음 변수가 선언된 후에
terraform.tfvars 라는 테라폼 파일로 다시 변수가 엎어쳐지게 된다.
그리고 또 그 뒤에 terraform.tfvars.json 으로 바뀌고... 위의 순서에 맞춰서 자꾸자꾸 overwrite된다.

```

variable "order" {
    type = string
}

output "order" {
    value = var.order
}

```

1. 여기서 order은 처음에 default 값이 정의되어 있지 않아서, 처음 실행할 때에 유저에게 초기값을 적으라고 시킨다.
그리고 그 때 넣은 값으로 초기화가 된다.

2. 다음으로 `export TF_VAR_order="ryoochan"` 이런 식으로 실행할 때에 바로 값을 넣어줄 수 도 있다. 이렇게 하면 돌아갈 때 알아서 값이 세팅된다.

3. 근데 만약에 저 위에있는 `terraform.tfvars`라는 파일에서 미리 세팅을 한다면 어떨까? 그러면 이걸로 세팅되게 될 것이다!

4. 다음으로  `terraform.tfvars.json`이라는 파일을 만들어서 거기에
```
{
    "order" : "is handsome!!!!"
}
```
요런 식으로 세팅해주면 이걸로 이름이 다시 overwrite된다.

5. CLI 에서 `terraform apply -var order=cli` 이런 식으로 해주면 이것이 가장 높은 우선순위로 overwrite시킨다.

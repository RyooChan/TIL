# Vault 

## Token방식을 사용한 Vault서버 로그인 및 policy를 통한 권한 부여

### 1. Vault서버에 Root 계정으로 로그인하기

#### 입력

```
vault login hvs.RootToken~~~~~
```

#### 결과

```
Success! You are now authenticated. The token information displayed below
is already stored in the token helper. You do NOT need to run "vault login"
again. Future Vault requests will automatically use this token.

Key                  Value
---                  -----
token                hvs.RootToken~~~~~
token_accessor       mDI0u6IZs3DSAog006aliFlK
token_duration       ∞
token_renewable      false
token_policies       ["root"]
identity_policies    []
policies             ["root"]
```


> Root계정은 전체 path에 관한 모든 권한을 가지고 있다.

그렇기 때문에 최초에 로그인해서, 여기서 다른 계정들의 policy와 로그인 방식을 만들어주면 된다.

### 2. Vault 에서 사용할 path 활성화하기


#### 입력

```
vault secrets enable -path=aaa -description="triple a" kv-v2
```

```
vault secrets enable -path=bbb -description="triple b" kv-v2
```

#### 결과

```
Success! Enabled the kv-v2 secrets engine at: bbb/
```

```
Success! Enabled the kv-v2 secrets engine at: aaa/
```

* kv 버전2로 path활성화
    * kv버전2에서는 version1에 비해서 메타정보, 버전관리, 값 패치 등의 더 많은 기능을 제공한다.

> `aaa` `bbb` 두 개의 path를 활성화 해 주었다.

aaa에 권한을 부여할 것이고, bbb는 권한이 없을 때에 어떤 식으로 동작할지를 보이기 위해 일단 활성화 해 두었다.

#### 입력

```
vault kv put aaa/oracleDB id="admin" pw="changeme"
```

여기서 이제 `aaa` path의 하위에 `oracleDB`라는 디렉터리를 만들고, 거기에 kv로 {id:admin, pw:change} 두개를 설정해 주었다.

### 3. vault 정책설정파일 생성

> HCL(HashiCorp Configuration Language) 파일

#### 입력

```
vi aaa_oracle_read_policy.hcl
```

```
path "aaa/*" {
	capabilities = ["read"]
}
```

> `create`, `read`, `update`, `delete`, `list` 중에 원하는 기능을 선택한다.
> 어떤 path에 대해 허용할지에 관한 정책을 작성한다.

여기서는 aaa하위의 모든 path 에 관하여 권한을 부여해준다.

### 4. vault 정책 생성

#### 입력

```
vault policy write aaa_oracle_read ./aaa_oracle_read_policy.hcl
```

#### 결과

```
Success! Uploaded policy: aaa_oracle_read
```

> aaa_oracle_read 라는 이름의 정책을 생성하였다.

이를 확인해 보려면

#### 입력

```
vault policy read aaa_oracle_read
```

이렇게 해당 policy를 확인하면

#### 출력

```
path "aaa/*" {
        capabilities = ["read"]
}

```

이런 식으로 해당 policy의 정보가 출력된다.


### 5. 위에서 생성한 권한을 가진 Vault Token생성

#### 입력

```
vault token create -policy=aaa_oracle_read
```

#### 출력

```
Key                  Value
---                  -----
token                hvs.aaa_oracle_read_token~~~~~
token_accessor       5gxrJMFkHK6jtoVMsmBP6Ert
token_duration       768h
token_renewable      true
token_policies       ["aaa_oracle_read" "default"]
identity_policies    []
policies             ["aaa_oracle_read" "default"]
```

이렇게 aaa_oracle_read의 policy를 가진 Token이 생성되었다.
해당 토큰은 768시간동안 유효하며, 기본적으로 default policy를 가지고 있고, aaa_oracle_read policy도 갖게 된다.

위에서 보았듯, 해당 토큰은 aaa라는 path에 대해 read권한만을 갖고 있을 것이다.

### 6. 기존 토큰 unset

먼저 현재 로그인되어있는 토큰에서 logout해준다.
Token을 unset하면 된다.

#### 입력

``` 
unset VAULT_TOKEN
```

이제 위에서 만들어진 aaa관련 Token으로 로그인해준다.

#### 입력

```
vault login hvs.aaa_oracle_read_token~~~~~
```

#### 출력

```
Success! You are now authenticated. The token information displayed below
is already stored in the token helper. You do NOT need to run "vault login"
again. Future Vault requests will automatically use this token.

Key                  Value
---                  -----
token                hvs.aaa_oracle_read_token~~~~~
token_accessor       5gxrJMFkHK6jtoVMsmBP6Ert
token_duration       767h52m14s
token_renewable      true
token_policies       ["aaa_oracle_read" "default"]
identity_policies    []
policies             ["aaa_oracle_read" "default"]
```

위에서 본 그 Toekn으로 로그인되었다.
이제 해당 토큰이 가진 권한들을 확인하기 위해 테스트를 진행해 본다.

### 7. Vault 데이터 조회

#### 입력

```
vault kv get aaa/oracleDB
```

#### 출력

```
== Secret Path ==
aaa/data/oracleDB

======= Metadata =======
Key                Value
---                -----
created_time       2022-08-29T05:52:29.852911728Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            1

=== Data ===
Key    Value
---    -----
id     admin
pw     changeme
```

여기서 보면 aaa 아래에 있는 oracleDB의 데이터들을 확인할 수 있다.
이는 현재 로그인한 계정이 해당 path 에 대한 read 권한이 있기 때문이다.

### 8. vault kv 변경 시도

#### 입력

```
vault kv put aaa/oracleDB email="changeEmail.hello.world" pw="changeIt"
```

> email을 chageEmail.hello.world로
> pw를 changeIt
> 으로 변경해 보면

#### 결과

````
Error writing data to aaa/data/oracleDB: Error making API request.

URL: PUT http://172.21.6.190:8200/v1/aaa/data/oracleDB
Code: 403. Errors:

* 1 error occurred:
        * permission denied
````

해당 기능에 대한 권한이 없기 때문에 permission denied가 나온다!!

### 9. 다른 path에의 read시도

#### 입력

```
vault kv get bbb/oracleDB
```

bbb에 대해 한번 검색해보면

#### 결과

```
Error making API request.

URL: GET http://172.21.6.190:8200/v1/sys/internal/ui/mounts/bbb/oracleDB
Code: 403. Errors:

* preflight capability check returned 403, please ensure client's policies grant access to path "bbb/oracleDB/"
```

권한이 없다고 나온다.
그런 권한이 있다면?

### 10. Vault root 계정으로 확인

root계정은 해당 권한들을 모두 가지고 있기 때문에, 위의 실패한 작업들이 성공할 것이다.

#### 입력

```
unset VAULT_TOKEN
```

기존 Token을 unset해준다.

```
vault login hvs.RootToken~~~~~
```

root계정으로 로그인해준다.

```
vault kv put aaa/oracleDB email="changeEmail.hello.world" pw="changeIt"
```

이전에 만든 친구의 kv를 변경해주면

#### 결과

```
== Secret Path ==
aaa/data/oracleDB

======= Metadata =======
Key                Value
---                -----
created_time       2022-08-29T07:21:25.584710513Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            2
```

이런 식으로 잘 변경된다.
한번 kv를 확인해 보면

#### 입력

```
vault kv get aaa/oracleDB
```

#### 출력

```
== Secret Path ==
aaa/data/oracleDB

======= Metadata =======
Key                Value
---                -----
created_time       2022-08-29T07:21:25.584710513Z
custom_metadata    <nil>
deletion_time      n/a
destroyed          false
version            10

==== Data ====
Key      Value
---      -----
email    changeEmail.hello.world
pw       changeIt
```

잘 바뀐다.

> 이번에는 다른 path에 대해 찾아보겠다.

#### 입력

```
vault kv get bbb/oracleDB
```

#### 출력

```
No value found at bbb/data/oracleDB
```

이렇게 해당 value가 없다는 것이 출력된다.
즉 검색은 됐는데, 안에 값이 없어서 나는 문제일 뿐이며 권한이 있는 곳에서는 변경이나 검색이 잘 된다는 것을 알 수 있다.

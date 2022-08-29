# Vault 

## userpass방식을 사용한 Vault서버 로그인 및 policy를 통한 권한 부여

[이전 방식](https://hello-backend.tistory.com/177)에서는 Token을 통해 권한의 확인을 진행해 보았다.
이번에는 여기에 userpass를 붙여본다.

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

새로운 방식을 enable시키거나 계정을 만들기 위해서는 많은 권한이 필요하므로 Root계정으로 로그인한다.

### 2. userpass방식 enable 하기

#### 입력

```
vault auth enable userpass
```

userpass방식을 enable해준다.

### 3. user생성해주기

#### 입력

```
vault write auth/userpass/users/ryoochan \
password=handsome \
policies=aaa_oracle_read
```

#### 결과

```
Success! Data written to: auth/userpass/users/ryoochan
```

> Id : ryoochan, PW : handsome

이라는 user를 만들어준다.
policy의 경우 이전에 만들어두었던 aaa_oracle_read를 그대로 써줄 것이다.

### 4. 해당 user 로그인하기

#### 입력

```
vault login -method=userpass \
username=ryoochan \
password=handsome
```

#### 결과

```
Success! You are now authenticated. The token information displayed below
is already stored in the token helper. You do NOT need to run "vault login"
again. Future Vault requests will automatically use this token.

Key                    Value
---                    -----
token                  hvs.CAESIHlCX944gfGhf_ORUY7o_aFO70DClRfxKjYoWjtyJ2HXGiEKHGh2cy5KME9CWHl1ME53NjdpNXBKdTZFUGh3WWwQhQQ
token_accessor         ognkTlied1CzuVsvpBaKBZqI
token_duration         768h
token_renewable        true
token_policies         ["aaa_oracle_read" "default"]
identity_policies      []
policies               ["aaa_oracle_read" "default"]
token_meta_username    ryoochan
```

여기서 메세지를 확인해 보면 해당 user로 만들어진 token이 이미 저장되어 있기 때문에 이를 사용할 필요가 없다고 나온다.
즉 userpass를 사용해서 로그인이 완료된 것이다.

### 5. 테스트하기

이전과 마찬가지로 지금 user에 부여된 policy는 aaa에 대한 read권한만을 가지고 있을 것이다.
이게 잘 되는지 한번 테스트해본다.

#### 입력

```
vault kv get aaa/oracleDB
```

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

#### 입력

```
vault kv put aaa/oracleDB email="changeAgain" pw="good"
```

#### 출력

```
Error writing data to aaa/data/oracleDB: Error making API request.

URL: PUT http://172.21.6.190:8200/v1/aaa/data/oracleDB
Code: 403. Errors:

* 1 error occurred:
        * permission denied

```

#### 입력

```
vault kv get bbb/oracleDB
```

#### 결과

```
Error making API request.

URL: GET http://172.21.6.190:8200/v1/sys/internal/ui/mounts/bbb/oracleDB
Code: 403. Errors:

* preflight capability check returned 403, please ensure client's policies grant access to path "bbb/oracleDB/"
```

> `aaa`에 대한 검색은 가능하고, `aaa`에 대한 변경은 불가능, `bbb`에 대한 검색은 불가능한 것을 확인 가능하다.

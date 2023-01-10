# MySQL8 대소문자 구분하기

> MySQL은 기본적으로 대소문자를 구분한다.

그러면 이거를 대소문자를 구별하지 않도록 하려하면 어떻게 해야 할까??

## 대소문자 구별 여부 확인하기

`show variables like 'lower_case_table_names';`

다음 명령어를 입력하면 대소문자 구별여부를 알 수 있다.

* 0
    * 대소문자 구별 O (default)
* 1
    * 대소문자 구별 X
        * 모든 네이밍을 소문자로 변환하여 저장
* 2
    * 대소문자 구별 X
        * 모든 네이밍을 사용한 그대로 저장
        * 하지만 조회할 때에는 소문자로 변환하여 사용

## 대소문자 구별 여부 변경하기

저 lower_case_table_names를 바꾸면 알아서 바뀔 것이다.

그러면 그냥

`set lower_case_table_name=1;`

이렇게 하면 구별을 안하겠네??

근데 실제로 해보면

`Error Code: 1238. Variable 'lower_case_table_names' is a read only variable`

이런 메세지가 나오면서 설정이 안된다.
보면, 저 lower_case_table_names는 read only라는 것이다.
그럼 어떻게 할까?

`vi /etc/my.cnf`

를 사용해서 mysql 설정파일을 확인한다.

거기서 쭉 내려보면

```
[mysqld]
lower_case_table_name=0
```

요게 있는데, 이거를 원하는 값으로 바꿔주면 된다.


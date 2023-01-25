# 프로토파입 패턴

> 기존 인스턴스를 복제하여 새로운 인스턴스를 만드는 방법

복제 기능을 갖추고 있는 기존 인스턴스를 프로토타입으로 사용해 새 인스턴스를 만들 수 있다.

![](https://i.imgur.com/SCNEfiv.png)

시간이 오래 걸리는 작업(예를 들어 DB에서 데이터를 읽어와서 이걸 토대로 인스턴스를 만들어야 하는 경우, http를 통해 얻은 데이터를 통해 인스턴스를 만드는 경우 등..) 을 할 때에 매번 이를 만드는 것은 리소스와 시간 낭비가 크다.

그렇기 때문에 이렇게 만들어 둔 데이터를 복제해서 새로운 인스턴스를 만들고, 이곳에서 얻어온 정보를 토대로 변경 작업 등을 해주면 더 효율적이게 될 것이다.

## issue 상황

```
public class GithubIssue {

    private int id;

    private String title;

    private GithubRepository repository;

    public GithubIssue(GithubRepository repository) {
        this.repository = repository;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GithubRepository getRepository() {
        return repository;
    }

    public String getUrl() {
        return String.format("https://github.com/%s/%s/issues/%d",
                repository.getUser(),
                repository.getName(),
                this.getId());
    }
}
```

이와 같은 githubIssue class가 있다.

그리고 

```
public class GithubRepository {

    private String user;

    private String name;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```

githubRepository도 있다.

이제 이를

```
public class App {

    public static void main(String[] args) {
        GithubRepository repository = new GithubRepository();
        repository.setUser("whiteship");
        repository.setName("live-study");

        GithubIssue githubIssue = new GithubIssue(repository);
        githubIssue.setId(1);
        githubIssue.setTitle("1주차 과제: JVM은 무엇이며 자바 코드는 어떻게 실행하는 것인가.");

        String url = githubIssue.getUrl();
        System.out.println(url);
    }
}
```

이런 식으로 이슈 세팅해준다.
근데 이거를 하나하나 다 입력해서 만들게 아니라, 복제해서 새로운 인스턴스를 만들어 사용해줄 것이다.

## 프로토타입 패턴 사용

> 일단 java에서 이걸 구현하는 방식을 기본으로 제공해 주는데, 해당 방식을 사용해서 구현해 본다.

```
public class GithubIssue implements Cloneable {

    private int id;

    private String title;

    private GithubRepository repository;

    public GithubIssue(GithubRepository repository) {
        this.repository = repository;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GithubRepository getRepository() {
        return repository;
    }

    public String getUrl() {
        return String.format("https://github.com/%s/%s/issues/%d",
                repository.getUser(),
                repository.getName(),
                this.getId());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GithubIssue that = (GithubIssue) o;
        return id == that.id && Objects.equals(title, that.title) && Objects.equals(repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, repository);
    }
}
```

이와 같이 `Cloneable`을 상속받고, 내부의 clone을 사용한다.
이는 clone메서드가 Object내에 protected로 구현되어 있기 때문이고, clone의 사용을 위해 super를 사용하여 상위의 clone 구현을 그대로 가져와 준다.

```
public class App {

    public static void main(String[] args) throws CloneNotSupportedException {
        GithubRepository repository = new GithubRepository();
        repository.setUser("whiteship");
        repository.setName("live-study");

        GithubIssue githubIssue = new GithubIssue(repository);
        githubIssue.setId(1);
        githubIssue.setTitle("1주차 과제: JVM은 무엇이며 자바 코드는 어떻게 실행하는 것인가.");

        String url = githubIssue.getUrl();
        System.out.println(url);

        GithubIssue clone = (GithubIssue) githubIssue.clone();
        System.out.println(clone.getUrl());

        repository.setUser("Keesun");

        System.out.println(clone != githubIssue);
        System.out.println(clone.equals(githubIssue));
        System.out.println(clone.getClass() == githubIssue.getClass());

        System.out.println(clone.getUrl());
    }

}

```

한번 이렇게 가져와서 확인해 보면

![](https://i.imgur.com/HcxPz5b.png)

요렇게 얘들이 같은거를 확인할 수 있다.
근데 java가 기본적으로 제공해주는 clone의 내용을 보면, 그리고 [이 글](https://hello-backend.tistory.com/218)을 확인해 보면 기본적으로 자바에서 제공해주는 clone은 얕은 복사를 제공한다는 사실을 알 수 있다.

알다시피 얕은복사는 쪼오끔 문제가 생길수 있다.
뭐 내용을 바꾸는 경우라거나 이렇게 되면 A만 바꿨는데 B도 바뀌게 되는거다... 곤란함

```
public class GithubIssue implements Cloneable {

    private int id;

    private String title;

    private GithubRepository repository;

    public GithubIssue(GithubRepository repository) {
        this.repository = repository;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GithubRepository getRepository() {
        return repository;
    }

    public String getUrl() {
        return String.format("https://github.com/%s/%s/issues/%d",
                repository.getUser(),
                repository.getName(),
                this.getId());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        GithubRepository repository = new GithubRepository();
        repository.setUser(this.repository.getUser());
        repository.setName(this.repository.getName());

        GithubIssue githubIssue = new GithubIssue(repository);
        githubIssue.setId(this.id);
        githubIssue.setTitle(this.title);

        return githubIssue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GithubIssue that = (GithubIssue) o;
        return id == that.id && Objects.equals(title, that.title) && Objects.equals(repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, repository);
    }
}
```

이런 식으로 clone내에서 deep copy를 제공하기 위한 내부 로직을 구현해 준다.

```
public class App {

    public static void main(String[] args) throws CloneNotSupportedException {
        GithubRepository repository = new GithubRepository();
        repository.setUser("whiteship");
        repository.setName("live-study");

        GithubIssue githubIssue = new GithubIssue(repository);
        githubIssue.setId(1);
        githubIssue.setTitle("1주차 과제: JVM은 무엇이며 자바 코드는 어떻게 실행하는 것인가.");

        String url = githubIssue.getUrl();
        System.out.println(url);

        GithubIssue clone = (GithubIssue) githubIssue.clone();
        System.out.println(clone.getUrl());

        repository.setUser("Keesun");

        System.out.println(clone != githubIssue);
        System.out.println(clone.equals(githubIssue));
        System.out.println(clone.getClass() == githubIssue.getClass());
        System.out.println(clone.getRepository() == githubIssue.getRepository());

        System.out.println(clone.getUrl());
    }

}
```

이거를 한번 확인해보면

![](https://i.imgur.com/dlDxDpk.png)

이렇게 제대로 deep copy가 이루어짐을 확인할 수 있다.

## 장단점이 뭐죠?

* 장점
    * 복잡한 객체를 만드는 과정을 숨길 수 있다.
    * 기존 객체를 복제하는 과정이 새 인스턴스를 만드는 것보다 비용(시간이나 메모리)적인 면에서 효율적일 수 있다.
    * 추상 타입의 리턴이 가능하다.
        * 그러니까 return된 object의 타입이 반드시 해당 클래스와 동일한 필요는 없다.
        * 이를 통해 유연한 instance 생성이 가능!
* 단점
    * 복제한 객체를 만드는 과정 자체가 복잡할 수 있다.
        * 특히 순환 참조가 있는 경우!

## 실무에서의 사용

```
public class Student {

    String name;

    public Student(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                '}';
    }
}
```

요런 Student라는 class가 있을 때에

```
public class JavaCollectionExample {

    public static void main(String[] args) {
        Student keesun = new Student("keesun");
        Student whiteship = new Student("whiteship");
        List<Student> students = new ArrayList<>();
        students.add(keesun);
        students.add(whiteship);

        List<Student> clone = new ArrayList<>(students);
        System.out.println(clone);
    }
}
```

이런 식으로 바로 clone해서 사용할 수 있다.
저기서 List에서 모든 내부 애들을 다 복사해서 쓸 수 있는 것이다.

```
public class ModelMapperExample {

    public static void main(String[] args) {
        GithubRepository repository = new GithubRepository();
        repository.setUser("whiteship");
        repository.setName("live-study");

        GithubIssue githubIssue = new GithubIssue(repository);
        githubIssue.setId(1);
        githubIssue.setTitle("1주차 과제: JVM은 무엇이며 자바 코드는 어떻게 실행하는 것인가.");

        ModelMapper modelMapper = new ModelMapper();
        GithubIssueData githubIssueData = modelMapper.map(githubIssue, GithubIssueData.class);
        System.out.println(githubIssueData);
    }
}

```

혹은 modelMapper도 이런 식으로 동작한다.


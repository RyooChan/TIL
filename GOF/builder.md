# 빌더 패턴

> 어떤 인스턴스를 만들 때 다양한 구성으로 만들어 질 수 있는데, 인스턴스를 동일한 프로세스를 통해 만들 수 있게끔 해주는 것이다.

## 문제 상황

```
public class TourPlan {

    private String title;

    private int nights;

    private int days;

    private LocalDate startDate;

    private String whereToStay;

    private List<DetailPlan> plans;

    public TourPlan() {
    }

    public TourPlan(String title, int nights, int days, LocalDate startDate, String whereToStay, List<DetailPlan> plans) {
        this.title = title;
        this.nights = nights;
        this.days = days;
        this.startDate = startDate;
        this.whereToStay = whereToStay;
        this.plans = plans;
    }

    @Override
    public String toString() {
        return "TourPlan{" +
                "title='" + title + '\'' +
                ", nights=" + nights +
                ", days=" + days +
                ", startDate=" + startDate +
                ", whereToStay='" + whereToStay + '\'' +
                ", plans=" + plans +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        this.nights = nights;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getWhereToStay() {
        return whereToStay;
    }

    public void setWhereToStay(String whereToStay) {
        this.whereToStay = whereToStay;
    }

    public List<DetailPlan> getPlans() {
        return plans;
    }

    public void setPlans(List<DetailPlan> plans) {
        this.plans = plans;
    }

    public void addPlan(int day, String plan) {
        this.plans.add(new DetailPlan(day, plan));
    }
}
```

다음과 같은 TourPlan과 이를 만들 때에 필요한 객체들이 선언되어 있다.

```
public class App {

    public static void main(String[] args) {
        TourPlan shortTrip = new TourPlan();
        shortTrip.setTitle("오레곤 롱비치 여행");
        shortTrip.setStartDate(LocalDate.of(2021, 7, 15));


        TourPlan tourPlan = new TourPlan();
        tourPlan.setTitle("칸쿤 여행");
        tourPlan.setNights(2);
        tourPlan.setDays(3);
        tourPlan.setStartDate(LocalDate.of(2020, 12, 9));
        tourPlan.setWhereToStay("리조트");
        tourPlan.addPlan(0, "체크인 이후 짐풀기");
        tourPlan.addPlan(0, "저녁 식사");
        tourPlan.addPlan(1, "조식 부페에서 식사");
        tourPlan.addPlan(1, "해변가 산책");
        tourPlan.addPlan(1, "점심은 수영장 근처 음식점에서 먹기");
        tourPlan.addPlan(1, "리조트 수영장에서 놀기");
        tourPlan.addPlan(1, "저녁은 BBQ 식당에서 스테이크");
        tourPlan.addPlan(2, "조식 부페에서 식사");
        tourPlan.addPlan(2, "체크아웃");
    }
}
```

이를 이와 같이 client쪽에서 하나씩 설정하는 경우가 있다고 생각해 보자.

1. 딱봐도 알겠는데 이게 코드가 좀 더럽다.
2. 그리고 만약 Day - Nights 같은 느낌으로, 하나가 세팅되면 반드시 다른것도 세팅해주어야 하는 것들을 해줄수가 없다...
3. 또, 생성자들을 만드는 방법이 장황해질 수 있다(예를 들어, 한번에 모든 것들을 만들 때에 필요한 파라미터들을 하나씩 하는건 좀 그렇다) -> 사용하는 생성자가 좀 많아서 이게 뭐가 뭔지 헷갈려짐


## 그래서!!

* Builder 패턴
    * 동일한 프로세스를 거쳐 다양한 구성의 인스턴스를 만드는 방법

을 사용하면 된다.
이를 사용하면 복잡한 객체를 만드는 프로세스를 독립적으로 분리할 수 있다.

![](https://i.imgur.com/tTmRnTk.png)

## 실제로 적용해 보기

```
public interface TourPlanBuilder {

    TourPlanBuilder nightsAndDays(int nights, int days);

    TourPlanBuilder title(String title);

    TourPlanBuilder startDate(LocalDate localDate);

    TourPlanBuilder whereToStay(String whereToStay);

    TourPlanBuilder addPlan(int day, String plan);

    TourPlan getPlan();

}
```

이런 식으로, TourPlanBuilder인터페이스 내에서, TourPlanBuilder return type을 가지는 메서드들을 여렇 만들어 준다.

이 빌더를 통해 각각의 메서드 타입을 사용하는 경우 return type으로 해당 빌더가 다시 돌아오기 때문에 다른 메서드들을 사용할 수 있다.
그렇기 때문에 안에 있는 다른 메서드들을 사용할 수 있도록 동일한 return type의 TourPlanBuilder를 가지는 메서드들을 정의해 준다.

그리고 다 사용한 뒤에는 원하는 결과인 TourPlan을 return하는 `getPlan()` 메서드를 만들어서 쓰면 된 것이다.

```
public class DefaultTourBuilder implements TourPlanBuilder {

    private String title;

    private int nights;

    private int days;

    private LocalDate startDate;

    private String whereToStay;

    private List<DetailPlan> plans;

    @Override
    public TourPlanBuilder nightsAndDays(int nights, int days) {
        this.nights = nights;
        this.days = days;
        return this;
    }

    @Override
    public TourPlanBuilder title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public TourPlanBuilder startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    @Override
    public TourPlanBuilder whereToStay(String whereToStay) {
        this.whereToStay = whereToStay;
        return this;
    }

    @Override
    public TourPlanBuilder addPlan(int day, String plan) {
        if (this.plans == null) {
            this.plans = new ArrayList<>();
        }

        this.plans.add(new DetailPlan(day, plan));
        return this;
    }

    @Override
    public TourPlan getPlan() {
        return new TourPlan(title, nights, days, startDate, whereToStay, plans);
    }
}

```

내부 로직은 이런 식으로 구현해 준다.
이제 이거를 client쪽에서 한번 사용해 보자.

```
public class App {

    public static void main(String[] args) {
        TourPlanBuilder builder = new DefaultTourBuilder();
        TourPlan plan = builder.title("칸쿤 여행")
                    .nightsAndDays(2, 3)
                    .startDate(LocalDate.of(2020, 12, 9))
                    .whereToStay("리조트")
                    .addPlan(0, "체크인하고 짐 풀기")
                    .addPlan(0, "저녁식사")
                    .addPlan(0, "이런식으로 끼요오오옷")
                    .getPlan();
                    
        TourPlan logBeachTrip = builder.title("롱비치")
                    .startDate(LocalDate.of(2023, 7, 15))
                    .getPlan();
    }
}

```

이런 식으로 빌더를 사용해 주면, 위의 client와 동일한 기능을 하는 것을 구현 가능하다.
이는 위의 방식에 비해 훨씬 코드를 보기도 깔끔하고, 장황한 코드를 줄일 수 있다.

## Directer사용

> 위의 코드에서 자주 사용되는 set을 director에 넣어 두고 재사용 할수도 있다.

```
public class TourDirector {

    private TourPlanBuilder tourPlanBuilder;

    public TourDirector(TourPlanBuilder tourPlanBuilder) {
        this.tourPlanBuilder = tourPlanBuilder;
    }

    public TourPlan cancunTrip() {
        return tourPlanBuilder.title("칸쿤 여행")
                .nightsAndDays(2, 3)
                .startDate(LocalDate.of(2020, 12, 9))
                .whereToStay("리조트")
                .addPlan(0, "체크인하고 짐 풀기")
                .addPlan(0, "저녁 식사")
                .getPlan();
    }

    public TourPlan longBeachTrip() {
        return tourPlanBuilder.title("롱비치")
                .startDate(LocalDate.of(2021, 7, 15))
                .getPlan();
    }
}
```

이렇게 생성자 주입을 통해 쓸 수 있다.
요렇게 하면 `cancunTrip`, `longBeachTrip`을 미리 만들어 두고 갖다 쓰기만 하면 된다!!

이제 그럼 이걸 가져다가 쓰면

```
public class App {

    public static void main(String[] args) {
        TourDirector director = new TourDirector(new DefaultTourBuilder());
        TourPlan tourPlan = director.cancunTrip();
        TourPlan tourPlan1 = director.longBeachTrip();
    }
}
```

걍 요래 쓰면 된다.

이거 참고로 Builder에서 사용하는 메서드들을 무조건 순서대로 하게끔 강제할수도 있다.
이 또한 Builder가 생성자에 비해 가지는 장점이라 할 수 있다.

추가로 Director를 사용하면 이러한 생성 로직을 숨길 수도 있다.

또한 VIP용 plan 이런것도 다르게 받도록 해줄수도 있다.

## 이 빌더가 java랑 spring에서 어떻게 쓰일까??

```
public class StringBuilderExample {

    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        String result = stringBuilder.append("whiteship").append("keesun").toString();
        System.out.println(result);
    }
}
```

요런 식으로 [StringBuilder](https://hello-backend.tistory.com/111)는(이름부터 빌더임ㅋㅋ) Builder를 사용해서 문자들을 더하면서 할 수 있다.
이것도 보면 알겠지만 builder를 통해서 결국 원하는 내용을 완성시켜 나가는 것이다.

```
public class StreamExample {

    public static void main(String[] args) {
        Stream<String> names = Stream.<String>builder().add("keesun").add("whiteship").build();
        names.forEach(System.out::println);
    }
}

```

다음은 [Stream](https://hello-backend.tistory.com/229)쪽에서이다.
Stream을 만들 때에 들어갈 데이터를 add할 때에도 builder를 통해서 만들어나갈 수 있다.

참고로 저거 generic type을 써서 \<String>을 써줘야지 되는데, 이걸 안써주면 얘가 기본적으로 `Object`를 받기 때문이다. String으로 받아야 뒤의 값들을 성공적으로 받을 수 있다.

```
@Builder
public class LombokExample {

    private String title;

    private int nights;

    private int days;

    public static void main(String[] args) {
        LombokExample trip = LombokExample.builder()
                .title("여행")
                .nights(2)
                .days(3)
                .build();
    }

}
```

이렇게 @Builder 어노테이션을 적용해 주면, 클래스를 컴파일 할 때에 자동으로 내부에 builder api를 만들어 준다.
이를 그냥 사용하면 된다!

```
public class SpringExample {

    public static void main(String[] args) {
        UriComponents howToStudyJava = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("www.whiteship.me")
                .path("java playlist ep1")
                .build().encode();
        System.out.println(howToStudyJava);
    }
}
```

이런 UriComponents라는게 있는데 이걸 쓰면 Uri를 좀 안전하게 만들 수 있다.
저렇게 위의 방식으로 써주면 얘가 알아서 안전한 형태로 만들어 주는 것이다.



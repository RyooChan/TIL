# 직렬화(Serializable)

```
public interface Serializable{

}
```

직렬화용 Serializable 인터페이스 내부를 보면 메소드가 하나도 구현이 안되어있다.
그럼 얘가 뭔 용도가 있을까??

* 생성한 객체를 파일로 저장할 때
* 저장한 객체를 읽을 때
* 다른 서버에서 저장한 객체를 받아서 사용할 때

이런 식으로, 생성한 클래스를 파일에 읽거나 쓸 수 있도록 하거나 혹은 다른 서버로 보내거나 받을 수 있도록 하기 위해서는 Serializable 인터페이스를 반드시 필요로 한다.

즉

> Seriablizable 인터페이스를 통해 JVM은 특정 객체를 저장하고, 읽거나, 다른 서버와 주고받을 수 있게 된다.

## 이게 뭐길래??

* 직렬화
    * java내부의 객체나 데이터를 외부의 자바 시스템에서 사용할 수 있도록 하는 것.
    * 데이터를 바이트 형태로 변환한다.
* 역직렬화
    * 바이트 형태의 데이터를 다시 객체로 변경하는 것.

가끔 개발할 때 보면 `Serializable`인터페이스를 구현한 클래스들을 볼 수 있는데, 여기서 `seriaVersionUID`를 지정해주는 것을 알 수 있다.

예를들어

```
public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;
}
```

HashMap의 경우는 이런 식으로 설정되어 있다.
참고로 저거는 무조건 `private static final long` 의 `serialVersionUID`로 적어주어야 한다.

### 저게 뭐죠?

serialVersionUID는 **해당 객체의 버전 명시에 사용**된다.

예를 들어

1. A서버에서 B서버로 SerialDTO클래스의 객체를 전송하려 한다.
2. A서버에는 SerialDTO라는 클래스가 필요하고, B서버에도 SerialDTO라는 클래스가 있어야 한다.(그래야 해당 클래스의 객체임을 알고 데이터를 받을 수 있다.)
3. A서버에서 SerialDTO가 3개 있는 상황에서, B서버에는 4개의 SerialDTO가 있다면?

이렇게 하면 뭐가 뭔지 모르니까 제대로 처리할수가 없을 것이다.
그렇기 떄문에 이 객체들이 각각 같은지 다른지 알 수 있도록 `serialVersionUID`를 사용해 준다.
참고로 이거는 버전을 알면 되는거기 때문에 값이나 타입이 다르기만 하면 어떻게 지정해줘도 상관없다.

한번 예를 들어보자.

### 객체 저장

```
import java.io.Serializable;

public class SerialDTO implements Serializable {
    private String booName;
    private int bookOrder;
    private boolean bestSeller;
    private long soldPerDay;

    public SerialDTO(String booName, int bookOrder, boolean bestSeller, long soldPerDay) {
        this.booName = booName;
        this.bookOrder = bookOrder;
        this.bestSeller = bestSeller;
        this.soldPerDay = soldPerDay;
    }

    @Override
    public String toString() {
        return "SerialDTO{" +
                "booName='" + booName + '\'' +
                ", bookOrder=" + bookOrder +
                ", bestSeller=" + bestSeller +
                ", soldPerDay=" + soldPerDay +
                '}';
    }
}
```

```
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class ManageObject {
    public static void main(String[] args) {
        ManageObject manage = new ManageObject();
        String fullPath = "/Users/ryoochan/handsome/god.md";

        SerialDTO dto = new SerialDTO("God of Java", 1, true, 100);
        manage.saveObject(fullPath, dto);
    }

    public void saveObject(String fullPath, SerialDTO dto) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(fullPath);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(dto);
            System.out.println("Write Success");
        } catch (Exception e) { 
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

이런 식으로 객체를 저장해준다.

### 객체 읽기

```
import java.io.*;

public class ManageObject {
    public static void main(String[] args) {
        ManageObject manage = new ManageObject();
        String fullPath = "/Users/ryoochan/handsome/god.md";
        manage.loadObject(fullPath);
    }

    public void loadObject(String fullPath) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(fullPath);
            ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            SerialDTO dto = (SerialDTO)obj;
            System.out.println(dto);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (fis != null) {
            try {
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

`SerialDTO{booName='God of Java', bookOrder=1, bestSeller=true, soldPerDay=100}`

이런 식으로 파일의 객체를 쓰고 읽을 수 있다.

혹시 여기서 `SerialDTO`의 필드를 추가하게 되면

```
java.io.InvalidClassException: FileIO.SerialDTO; local class incompatible: stream classdesc serialVersionUID = -358710248991570103, local class serialVersionUID = 1424372278057927306
	at java.base/java.io.ObjectStreamClass.initNonProxy(ObjectStreamClass.java:689)
	at java.base/java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:1982)
	at java.base/java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1851)
	at java.base/java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2139)
	at java.base/java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1668)
	at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:482)
	at java.base/java.io.ObjectInputStream.readObject(ObjectInputStream.java:440)
	at FileIO.ManageObject.loadObject(ManageObject.java:49)
	at FileIO.ManageObject.main(ManageObject.java:12)
```

대충 이런 에러가 나는데, 이는 serialVersionUID의 값이 다르다는 것이다.
이런 식으로 객체의 형태가 변경되면 컴파일 시 serialVersionUID가 다시 생성되고 에러가 발생하는 것이다.

## Transient

> Serialize동안 과정에 제외하고 싶을 때에 사용.

해당 명령어는, 직렬화 하는 동안에 특정 정보를 제외할 때에 적용한다.
예를 들어, 비밀번호 등은 직렬화하지 않도록 하기 위해 사용한다.

예를 들어

```
class Member implements Serializable {
    private String name;
    private String email;
    private String password;

    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    @Override
    public String toString() {
        return String.format("Member{name='%s', email='%s', password='%s'}", name, email, password);
    }
}
```

```
  public static void main(String[] args) throws IOException, ClassNotFoundException {
        Member member = new Member("Ryoochan", "chanHandsome@tistory.com", "TooHandsome97");
        String serialData = serializeTest(member);
        deSerializeTest(serialData);
    }
```

이런 데이터에서

```
class Member implements Serializable {
    private String name;
    private String email;
    private transient String password;

    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    @Override
    public String toString() {
        return String.format("Member{name='%s', email='%s', password='%s'}", name, email, password);
    }
}
```

요렇게 비밀번호를 transient해주면

```
==Deserialize==
Member{name='Ryoochan', email='chanHandsome@tistory.com', password='null'}
```

이렇게 해당 필드는 있지만 내용물이 null로 보인다!!


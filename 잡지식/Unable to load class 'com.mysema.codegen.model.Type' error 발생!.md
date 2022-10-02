# Unable to load class 'com.mysema.codegen.model.Type' error 발생!

프로젝트 진행 중에 queryDSL을 통한 Q class 생성에서 위와 같은 에러가 발생하였다.

해결을 위해 찾아본 결과 spring boot 2.6이상에서는 querydsl5.0 이상 버전을 적용하고, 추가적인 내용들이 필요하다고 한다.

```
buildscript {
   ext {
      queryDslVersion = "5.0.0"
   }
}

plugins {
    id 'org.springframework.boot' version '2.7.2'
    id 'io.spring.dependency-management' version '1.0.12.RELEASE'
    id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
    id 'java'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

group = 'com.memwiki'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '11'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // QueryDSL
    implementation "com.querydsl:querydsl-jpa:5.0.0"
    annotationProcessor "com.querydsl:querydsl-apt:5.0.0"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"	// java.lang.NoClassDefFoundError(javax.annotation.Entity) 발생 대응
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"		// java.lang.NoClassDefFoundError (javax.annotation.Generated) 발생 대응

    implementation 'org.springframework.boot:spring-boot-starter-validation'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

}

tasks.named('test') {
    useJUnitPlatform()
}

def querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}
sourceSets {
    main.java.srcDir querydslDir
}
compileQuerydsl{
    options.annotationProcessorPath = configurations.querydsl
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
    querydsl.extendsFrom compileClasspath
}
```

---

나는 이렇게 

* querydsl의 buildscript를 5.0.0 으로 설정함
* 를 5.0.0 으로 설정함
* dependencies에서
    * querydsl-jpa의 버전 명시 (5.0.0)
    * querydsl-apt의 버전 명시 (5.0.0)
* compileQuerydsl의 옵션 추가

를 진행하였다.

그 결과 해당 에러를 해결할 수 있었다.
